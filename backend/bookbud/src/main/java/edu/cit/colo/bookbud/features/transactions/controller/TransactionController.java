package edu.cit.colo.bookbud.features.transactions.controller;

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

import edu.cit.colo.bookbud.dto.ApiResponse;
import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.features.transactions.dto.CreateTransactionRequest;
import edu.cit.colo.bookbud.features.transactions.dto.RatingResponse;
import edu.cit.colo.bookbud.features.transactions.dto.SubmitRatingRequest;
import edu.cit.colo.bookbud.features.transactions.dto.TransactionDTO;
import edu.cit.colo.bookbud.features.transactions.dto.UpdateTransactionStatusRequest;
import edu.cit.colo.bookbud.features.users.security.JwtUtil;
import edu.cit.colo.bookbud.features.transactions.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
