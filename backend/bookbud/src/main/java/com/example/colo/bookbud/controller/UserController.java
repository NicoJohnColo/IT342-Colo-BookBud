package com.example.colo.bookbud.controller;

import com.example.colo.bookbud.dto.ApiResponse;
import com.example.colo.bookbud.dto.user.UpdateUserRequest;
import com.example.colo.bookbud.dto.user.UserProfileDTO;
import com.example.colo.bookbud.security.JwtUtil;
import com.example.colo.bookbud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        String requestingUserId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId, requestingUserId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String requestingUserId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserProfile(userId, requestingUserId, request)));
    }
}
