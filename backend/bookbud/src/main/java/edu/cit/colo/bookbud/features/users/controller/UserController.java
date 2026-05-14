package edu.cit.colo.bookbud.features.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.colo.bookbud.features.auth.security.JwtUtil;
import edu.cit.colo.bookbud.features.users.dto.UpdateUserRequest;
import edu.cit.colo.bookbud.features.users.dto.UserProfileDTO;
import edu.cit.colo.bookbud.features.users.service.UserService;
import edu.cit.colo.bookbud.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
