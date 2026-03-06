package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.PaginatedResponse;
import com.example.colo.bookbud.dto.book.BookDTO;
import com.example.colo.bookbud.dto.notification.NotificationDTO;
import com.example.colo.bookbud.dto.transaction.TransactionDTO;
import com.example.colo.bookbud.dto.user.UserProfileDTO;
import com.example.colo.bookbud.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/books")
    public ResponseEntity<ApiResponse<PaginatedResponse<BookDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllBooks(page, size)));
    }

    @PutMapping("/books/{bookId}/status")
    public ResponseEntity<ApiResponse<BookDTO>> updateBookStatus(
            @PathVariable String bookId,
            @RequestBody String status) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateBookStatus(bookId, status)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserProfileDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(page, size)));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody String status) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserStatus(userId, status)));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionDTO>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllTransactions(page, size)));
    }

    @PutMapping("/transactions/{transactionId}/cancel")
    public ResponseEntity<ApiResponse<TransactionDTO>> cancelTransaction(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.cancelTransaction(transactionId)));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationDTO>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllNotifications(page, size)));
    }
}
