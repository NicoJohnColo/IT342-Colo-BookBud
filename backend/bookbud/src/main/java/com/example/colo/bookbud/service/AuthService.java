package com.example.colo.bookbud.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.colo.bookbud.dto.auth.AuthResponse;
import com.example.colo.bookbud.dto.auth.LoginRequest;
import com.example.colo.bookbud.dto.auth.RefreshTokenRequest;
import com.example.colo.bookbud.dto.auth.RegisterRequest;
import com.example.colo.bookbud.entity.RefreshToken;
import com.example.colo.bookbud.entity.User;
import com.example.colo.bookbud.exception.AuthenticationException;
import com.example.colo.bookbud.exception.BusinessException;
import com.example.colo.bookbud.repository.RefreshTokenRepository;
import com.example.colo.bookbud.repository.UserRepository;
import com.example.colo.bookbud.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("VALID-001", "Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("DB-002", "Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("DB-002", "Username already exists");
        }

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .accountStatus("Active")
                .build();

        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("AUTH-001", "Invalid credentials"));

        if (!"Active".equals(user.getAccountStatus())) {
            throw new AuthenticationException("AUTH-004", "Account is suspended or banned");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("AUTH-001", "Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String token) {
        String userId = jwtUtil.extractUserId(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("AUTH-002", "User not found"));
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("AUTH-005", "Invalid refresh token"));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("AUTH-002", "Token expired");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = createRefreshToken(user);

        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user, accessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    private String createRefreshToken(User user) {
        String token = jwtUtil.generateRefreshToken(user.getUserId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(UUID.randomUUID().toString())
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .user(AuthResponse.UserDTO.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .rating(user.getRating() != null ? user.getRating().toString() : null)
                        .createdAt(user.getCreatedAt().toString())
                        .build())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
