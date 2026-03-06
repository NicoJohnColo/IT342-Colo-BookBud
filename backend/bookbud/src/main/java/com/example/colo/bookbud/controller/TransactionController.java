package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.PaginatedResponse;
import com.example.colo.bookbud.dto.transaction.*;
import com.example.colo.bookbud.security.JwtUtil;
import com.example.colo.bookbud.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(transactionService.createTransaction(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionDTO>>> getMyTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(transactionService.getMyTransactions(userId, status, page, size)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(
            @PathVariable String transactionId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(transactionService.getTransaction(transactionId, userId)));
    }

    @PutMapping("/{transactionId}/status")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransactionStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.updateTransactionStatus(transactionId, userId, request.getStatus())));
    }

    @PostMapping("/{transactionId}/rating")
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @PathVariable String transactionId,
            @Valid @RequestBody SubmitRatingRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.submitRating(transactionId, userId, request.getRating())));
    }
}
