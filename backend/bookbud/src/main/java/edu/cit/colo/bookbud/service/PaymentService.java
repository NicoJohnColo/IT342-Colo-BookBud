package edu.cit.colo.bookbud.service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.dto.payment.CreatePaymentRequest;
import edu.cit.colo.bookbud.dto.payment.EarningsSummaryDTO;
import edu.cit.colo.bookbud.dto.payment.InitiatePaymentRequest;
import edu.cit.colo.bookbud.dto.payment.PaymentDTO;
import edu.cit.colo.bookbud.dto.payment.PaymentInitiateResponse;
import edu.cit.colo.bookbud.dto.payment.PaymentStatsDTO;
import edu.cit.colo.bookbud.entity.Payment;
import edu.cit.colo.bookbud.entity.Transaction;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.PaymentRepository;
import edu.cit.colo.bookbud.repository.TransactionRepository;
import edu.cit.colo.bookbud.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public PaymentDTO recordPayment(String userId, CreatePaymentRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        if (paymentRepository.existsByTransactionTransactionId(request.getTransactionId())) {
            throw new BusinessException("BUSINESS-005", "A payment record already exists for this transaction");
        }

        // Use amount from request, or fallback to transaction amount if not provided
        BigDecimal paymentAmount = request.getAmount();
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentAmount = transaction.getAmount() != null ? new BigDecimal(transaction.getAmount()) : BigDecimal.ZERO;
        }

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .transaction(transaction)
                .amount(paymentAmount)
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod().replace(" ", "_")))
                .paymentDate(request.getPaymentDate())
                .paymentStatus(Payment.PaymentStatus.Pending)
                .build();

        payment = paymentRepository.save(payment);

        // Notify owner
        notificationService.createNotification(transaction.getOwner().getUserId(), 
            "Payment recorded for transaction: " + transaction.getTransactionId());

        return mapToDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransaction(String transactionId, String userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        if (!transaction.getUser().getUserId().equals(userId) && 
            !transaction.getOwner().getUserId().equals(userId)) {
            boolean isAdmin = userRepository.findById(userId)
                    .map(u -> u.getRole() == User.Role.ADMIN)
                    .orElse(false);
            if (!isAdmin) {
                throw new BusinessException("AUTH-003", "Not a party to this transaction");
            }
        }

        Payment payment = paymentRepository.findByTransactionTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Payment not found"));

        return mapToDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(String paymentId, String userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Payment not found"));

        // Verify user has access to this payment
        String ownerId = payment.getTransaction().getOwner().getUserId();
        String renterId = payment.getTransaction().getUser().getUserId();
        
        if (!ownerId.equals(userId) && !renterId.equals(userId)) {
            boolean isAdmin = userRepository.findById(userId)
                    .map(u -> u.getRole() == User.Role.ADMIN)
                    .orElse(false);
            if (!isAdmin) {
                throw new BusinessException("AUTH-003", "Not authorized to view this payment");
            }
        }

        return mapToDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<PaymentDTO> getPaymentsReceivedByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentRepository.findPaymentsReceivedByUser(userId, pageable);
        
        return PaginatedResponse.<PaymentDTO>builder()
                .content(payments.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .page(payments.getNumber())
                .size(payments.getSize())
                .totalElements(payments.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<PaymentDTO> getPaymentsMadeByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentRepository.findPaymentsMadeByUser(userId, pageable);
        
        return PaginatedResponse.<PaymentDTO>builder()
                .content(payments.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .page(payments.getNumber())
                .size(payments.getSize())
                .totalElements(payments.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<PaymentDTO> getAllPaymentsForUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentRepository.findAllPaymentsForUser(userId, pageable);
        
        return PaginatedResponse.<PaymentDTO>builder()
                .content(payments.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .page(payments.getNumber())
                .size(payments.getSize())
                .totalElements(payments.getTotalElements())
                .build();
    }

    @Transactional
    public PaymentDTO updatePaymentStatus(String paymentId, String userId, String newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Payment not found"));

        // Only owner of transaction can update payment status
        if (!payment.getTransaction().getOwner().getUserId().equals(userId)) {
            boolean isAdmin = userRepository.findById(userId)
                    .map(u -> u.getRole() == User.Role.ADMIN)
                    .orElse(false);
            if (!isAdmin) {
                throw new BusinessException("AUTH-003", "Only transaction owner can update payment status");
            }
        }

        Payment.PaymentStatus status = Payment.PaymentStatus.valueOf(newStatus);
        payment.setPaymentStatus(status);
        payment = paymentRepository.save(payment);

        // Notify renter/buyer about payment status change
        notificationService.createNotification(payment.getTransaction().getUser().getUserId(),
                "Payment status updated to: " + status);

        return mapToDTO(payment);
    }

    @Transactional(readOnly = true)
    public EarningsSummaryDTO getEarningsSummary(String userId) {
        Double totalEarnings = paymentRepository.getTotalEarningsForUser(userId);
        if (totalEarnings == null) {
            totalEarnings = 0.0;
        }
        long pendingPayments = paymentRepository.getPendingPaymentCountForUser(userId);
        long successfulPayments = paymentRepository.getSuccessfulPaymentCountForUser(userId);
        long failedPayments = paymentRepository.getFailedPaymentCountForUser(userId);

        return EarningsSummaryDTO.builder()
                .totalEarnings(totalEarnings)
                .pendingPayments(pendingPayments)
                .successfulPayments(successfulPayments)
                .failedPayments(failedPayments)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentStatsDTO getPaymentStats(String userId) {
        EarningsSummaryDTO summary = getEarningsSummary(userId);
        
        return PaymentStatsDTO.builder()
                .totalEarnings(summary.getTotalEarnings())
                .pendingPayments(summary.getPendingPayments())
                .successfulPayments(summary.getSuccessfulPayments())
                .failedPayments(summary.getFailedPayments())
                .build();
    }

    @Transactional
    public PaymentInitiateResponse initiatePayment(String userId, InitiatePaymentRequest request) {
        // TODO: Implement PayMongo integration
        // For now, return a mock response
        return PaymentInitiateResponse.builder()
                .paymentId(UUID.randomUUID().toString())
                .checkoutUrl("https://paymongo.com/checkout/mock")
                .paymentStatus("Pending")
                .build();
    }

    private PaymentDTO mapToDTO(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransaction().getTransactionId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name().replace("_", " "))
                .paymentDate(payment.getPaymentDate())
                .paymentStatus(payment.getPaymentStatus().name())
                .build();
    }
}

