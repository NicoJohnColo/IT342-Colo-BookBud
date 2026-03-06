package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.payment.*;
import com.example.colo.bookbud.security.JwtUtil;
import com.example.colo.bookbud.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> recordPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.recordPayment(userId, request)));
    }

    @GetMapping("/transactions/{transactionId}/payment")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByTransaction(
            @PathVariable String transactionId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByTransaction(transactionId, userId)));
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
}
