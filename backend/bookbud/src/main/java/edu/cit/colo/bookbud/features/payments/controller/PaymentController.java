package edu.cit.colo.bookbud.features.payments.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.colo.bookbud.features.auth.security.JwtUtil;
import edu.cit.colo.bookbud.features.payments.dto.CreatePaymentRequest;
import edu.cit.colo.bookbud.features.payments.dto.InitiatePaymentRequest;
import edu.cit.colo.bookbud.features.payments.dto.PaymentDTO;
import edu.cit.colo.bookbud.features.payments.dto.PaymentInitiateResponse;
import edu.cit.colo.bookbud.features.payments.dto.PaymentStatsDTO;
import edu.cit.colo.bookbud.features.payments.service.PaymentService;
import edu.cit.colo.bookbud.features.payments.service.StripeService;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.shared.dto.ApiResponse;
import edu.cit.colo.bookbud.shared.dto.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.recordPayment(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentDTO>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPaymentsForUser(userId, page, size)));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(
            @PathVariable String paymentId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(paymentId, userId)));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentDTO>>> getPaymentsReceived(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("AUTH-001", "Missing or invalid authorization header", null));
        }
        try {
            String userId = jwtUtil.extractUserId(authHeader.substring(7));
            return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsReceivedByUser(userId, page, size)));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("AUTH-002", "Invalid or expired token", null));
        }
    }

    @GetMapping("/made")
    public ResponseEntity<ApiResponse<PaginatedResponse<PaymentDTO>>> getPaymentsMade(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsMadeByUser(userId, page, size)));
    }

    @GetMapping("/transactions/{transactionId}/payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByTransaction(
            @PathVariable String transactionId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByTransaction(transactionId, userId)));
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentDTO>> updatePaymentStatus(
            @PathVariable String paymentId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        String newStatus = request.get("status");
        return ResponseEntity.ok(ApiResponse.success(paymentService.updatePaymentStatus(paymentId, userId, newStatus)));
    }

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.initiatePayment(userId, request)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody String payload) {
        // TODO: Implement PayMongo webhook handling
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<PaymentStatsDTO>> getPaymentStats(
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentStats(userId)));
    }

    @PostMapping("/sync-completed-transactions")
    public ResponseEntity<ApiResponse<String>> syncCompletedTransactions(
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        // Check if user is admin
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getRole() != User.Role.ADMIN) {
                throw new RuntimeException("Admin access required");
            }
        });
        paymentService.updatePaymentsForCompletedTransactions();
        return ResponseEntity.ok(ApiResponse.success("Payment statuses updated for completed transactions"));
    }

    @PostMapping("/initiate-stripe")
    public ResponseEntity<ApiResponse<Map<String, String>>> initiateStripePayment(
            @RequestBody Map<String, String> request) throws com.stripe.exception.StripeException {
        String transactionId = request.get("transactionId");
        return ResponseEntity.ok(ApiResponse.success(stripeService.createPaymentIntent(transactionId)));
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-stripe")
    public ResponseEntity<ApiResponse<Void>> confirmStripePayment(
            @RequestBody Map<String, String> request) throws com.stripe.exception.StripeException {
        String transactionId = request.get("transactionId");
        stripeService.confirmStripePayment(transactionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

