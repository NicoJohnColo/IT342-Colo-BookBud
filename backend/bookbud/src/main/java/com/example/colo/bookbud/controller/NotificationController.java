package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.notification.NotificationDTO;
import com.example.colo.bookbud.security.JwtUtil;
import com.example.colo.bookbud.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(userId)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(notificationId, userId)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader.substring(7));
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
