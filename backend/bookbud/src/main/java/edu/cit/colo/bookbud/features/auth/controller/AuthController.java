package edu.cit.colo.bookbud.features.auth.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.colo.bookbud.shared.dto.ApiResponse;
import edu.cit.colo.bookbud.features.auth.dto.AuthResponse;
import edu.cit.colo.bookbud.features.auth.dto.ForgotPasswordRequest;
import edu.cit.colo.bookbud.features.auth.dto.GoogleAuthRequest;
import edu.cit.colo.bookbud.features.auth.dto.LoginRequest;
import edu.cit.colo.bookbud.features.auth.dto.RefreshTokenRequest;
import edu.cit.colo.bookbud.features.auth.dto.RegisterRequest;
import edu.cit.colo.bookbud.features.auth.dto.ResetPasswordRequest;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.auth.service.AuthService;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.googleAuth(request)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader,
                                                   @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        User user = authService.getCurrentUser(token);
        return ResponseEntity.ok(ApiResponse.success(AuthResponse.UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt().toString())
                .build()));
    }

    @PostMapping("/setup-admin")
    public ResponseEntity<ApiResponse<String>> setupAdmin() {
        String password = "admin123";
        String hashedPassword = passwordEncoder.encode(password);
        
        User admin = userRepository.findByEmail("admin@bookbud.com")
                .orElse(User.builder()
                        .username("admin")
                        .email("admin@bookbud.com")
                        .passwordHash(hashedPassword)
                        .role(User.Role.ADMIN)
                        .rating(BigDecimal.valueOf(5.00))
                        .accountStatus("Active")
                        .build());
        
        if (admin.getUserId() != null) {
            admin.setPasswordHash(hashedPassword);
            admin.setAccountStatus("Active");
            admin.setRole(User.Role.ADMIN);
        }
        
        userRepository.save(admin);
        
        return ResponseEntity.ok(ApiResponse.success("Admin user created/updated successfully. Email: admin@bookbud.com, Password: admin123"));
    }
}
