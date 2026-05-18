package edu.cit.colo.bookbud.features.payments.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.notifications.service.NotificationService;
import edu.cit.colo.bookbud.features.payments.entity.Payment;
import edu.cit.colo.bookbud.features.payments.repository.PaymentRepository;
import edu.cit.colo.bookbud.features.transactions.entity.Transaction;
import edu.cit.colo.bookbud.features.transactions.repository.TransactionRepository;
import edu.cit.colo.bookbud.shared.exception.BusinessException;
import edu.cit.colo.bookbud.shared.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // ← THIS WAS MISSING — caused the 500 error

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final TransactionRepository transactionRepository;
    private final PaymentRepository paymentRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    private RequestOptions getRequestOptions() {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            System.err.println("CRITICAL ERROR: stripe.api.key is MISSING in application.properties!");
            throw new BusinessException("SYSTEM-001", "Stripe API key not configured");
        }
        return RequestOptions.builder()
                .setApiKey(stripeApiKey)
                .build();
    }

    @Transactional
    public Map<String, String> createPaymentIntent(String transactionId) throws StripeException {
        log.info("Initiating Stripe PaymentIntent for transactionId: {}", transactionId);

        if (transactionId == null || transactionId.isBlank()) {
            log.error("Transaction ID is null or blank");
            throw new BusinessException("VALID-001", "Transaction ID is required");
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found for ID: {}", transactionId);
                    return new ResourceNotFoundException("DB-001", "Transaction not found");
                });

        // Allow Pending status — this is the expected state when Stripe is initiated
        if (transaction.getStatus() != Transaction.Status.Pending) {
            log.warn("Transaction {} has status {}, expected Pending", transactionId, transaction.getStatus());
            throw new BusinessException("BUSINESS-004", "Transaction must be in Pending status to initiate payment");
        }

        Double amount = transaction.getAmount();
        if (amount == null || amount <= 0) {
            log.error("Invalid transaction amount: {}", amount);
            throw new BusinessException("VALID-002", "Invalid transaction amount");
        }

        long amountInCents = (long) (amount * 100);
        log.debug("Amount in cents: {}", amountInCents);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("php")
                .putMetadata("transactionId", transactionId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        try {
            log.info("Creating PaymentIntent with Stripe API...");
            PaymentIntent intent = PaymentIntent.create(params, getRequestOptions());
            log.info("Successfully created PaymentIntent: {}", intent.getId());

            // Upsert payment record
            Optional<Payment> existingOpt = paymentRepository.findByTransactionTransactionId(transactionId);
            Payment payment;

            if (existingOpt.isPresent()) {
                payment = existingOpt.get();
                log.info("Updating existing payment record: {}", payment.getPaymentId());
            } else {
                payment = new Payment();
                payment.setTransaction(transaction);
                payment.setAmount(BigDecimal.valueOf(transaction.getAmount()));
                payment.setPaymentMethod(Payment.PaymentMethod.Stripe_Card);
                payment.setPaymentDate(LocalDate.now());
                log.info("Creating new payment record for transaction: {}", transactionId);
            }

            payment.setPaymentStatus(Payment.PaymentStatus.Pending);
            payment.setStripePaymentIntentId(intent.getId());
            payment.setStripeClientSecret(intent.getClientSecret());
            paymentRepository.save(payment);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentIntentId", intent.getId());
            return response;

        } catch (StripeException e) {
            System.err.println("STRIPE API ERROR: " + e.getMessage());
            e.printStackTrace();
            log.error("Stripe API error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            System.err.println("UNEXPECTED ERROR IN STRIPE SERVICE: " + e.getMessage());
            e.printStackTrace();
            log.error("Unexpected error during PaymentIntent creation: {}", e.getMessage(), e);
            throw new BusinessException("SYSTEM-001", "An error occurred while preparing payment: " + e.getMessage());
        }
    }

    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Stripe Webhook signature error: {}", e.getMessage());
            throw new BusinessException("STRIPE-001", "Invalid webhook signature");
        }

        log.info("Handling Stripe Webhook event: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent != null) fulfillPayment(intent);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (intent != null) failPayment(intent);
            }
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void fulfillPayment(PaymentIntent intent) {
        String transactionId = intent.getMetadata().get("transactionId");
        if (transactionId == null) {
            log.warn("No transactionId in PaymentIntent metadata: {}", intent.getId());
            return;
        }

        log.info("Fulfilling payment for transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            log.warn("Transaction not found during fulfillment: {}", transactionId);
            return;
        }

        Payment payment = paymentRepository.findByTransactionTransactionId(transactionId).orElse(null);
        if (payment == null) {
            log.warn("Payment record not found for transaction: {}", transactionId);
            return;
        }

        payment.setPaymentStatus(Payment.PaymentStatus.Paid);
        payment.setPaymentDate(LocalDate.now());
        paymentRepository.save(payment);

        transaction.setStatus(Transaction.Status.Completed);
        transactionRepository.save(transaction);

        Book book = transaction.getBook();
        if (book != null) {
            book.setStatus(Book.Status.Sold);
            bookRepository.save(book);

            notificationService.createNotification(
                    transaction.getOwner().getUserId(),
                    "Payment received via Stripe for: " + book.getTitle()
            );
            notificationService.createNotification(
                    transaction.getUser().getUserId(),
                    "Your payment for " + book.getTitle() + " has been confirmed."
            );
        }
    }

    private void failPayment(PaymentIntent intent) {
        String transactionId = intent.getMetadata().get("transactionId");
        if (transactionId == null) return;

        log.info("Failing payment for transaction: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionTransactionId(transactionId).orElse(null);
        if (payment != null) {
            payment.setPaymentStatus(Payment.PaymentStatus.Failed);
            paymentRepository.save(payment);
        }

        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction != null) {
            transaction.setStatus(Transaction.Status.Cancelled);
            transactionRepository.save(transaction);

            Book book = transaction.getBook();
            if (book != null) {
                book.setStatus(Book.Status.Available);
                bookRepository.save(book);
            }
        }
    }

    @Transactional
    public void confirmStripePayment(String transactionId) throws StripeException {
        log.info("Manually confirming Stripe payment for transaction: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionTransactionId(transactionId)
                .orElseThrow(() -> {
                    log.error("Payment record not found for transaction: {}", transactionId);
                    return new ResourceNotFoundException("DB-001", "Payment record not found");
                });
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.Paid) {
            log.info("Payment already marked as Paid for transaction: {}", transactionId);
            return;
        }
        
        String intentId = payment.getStripePaymentIntentId();
        if (intentId == null || intentId.isBlank()) {
            log.error("No Stripe PaymentIntent ID found for transaction: {}", transactionId);
            throw new BusinessException("VALID-001", "No Stripe PaymentIntent ID found");
        }
        
        log.info("Retrieving PaymentIntent {} from Stripe...", intentId);
        PaymentIntent intent = PaymentIntent.retrieve(intentId, getRequestOptions());
        
        if (!"succeeded".equals(intent.getStatus())) {
            log.info("PaymentIntent is in status {}, attempting manual confirmation using test credentials...", intent.getStatus());
            try {
                com.stripe.param.PaymentIntentConfirmParams confirmParams = 
                    com.stripe.param.PaymentIntentConfirmParams.builder()
                        .setPaymentMethod("pm_card_visa")
                        .build();
                intent = intent.confirm(confirmParams, getRequestOptions());
                log.info("Stripe PaymentIntent manual confirmation success! Status: {}", intent.getStatus());
            } catch (Exception e) {
                log.warn("Failed to manually confirm PaymentIntent on Stripe: {}", e.getMessage());
            }
        }

        if ("succeeded".equals(intent.getStatus())) {
            log.info("PaymentIntent succeeded! Fulfilling payment for transaction: {}", transactionId);
            fulfillPayment(intent);
        } else {
            log.warn("PaymentIntent {} is not succeeded. Current status: {}", intentId, intent.getStatus());
            throw new BusinessException("BUSINESS-005", "Payment is not succeeded. Status: " + intent.getStatus());
        }
    }
}