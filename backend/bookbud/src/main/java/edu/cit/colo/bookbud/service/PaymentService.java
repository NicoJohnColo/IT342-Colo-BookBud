package edu.cit.colo.bookbud.service;

import edu.cit.colo.bookbud.dto.payment.*;
import edu.cit.colo.bookbud.entity.Payment;
import edu.cit.colo.bookbud.entity.Transaction;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.PaymentRepository;
import edu.cit.colo.bookbud.repository.TransactionRepository;
import edu.cit.colo.bookbud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .transaction(transaction)
                .amount(request.getAmount())
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
