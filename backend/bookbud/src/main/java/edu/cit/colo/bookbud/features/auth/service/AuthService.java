package edu.cit.colo.bookbud.features.auth.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import edu.cit.colo.bookbud.features.auth.dto.AuthResponse;
import edu.cit.colo.bookbud.features.auth.dto.ForgotPasswordRequest;
import edu.cit.colo.bookbud.features.auth.dto.GoogleAuthRequest;
import edu.cit.colo.bookbud.features.auth.dto.LoginRequest;
import edu.cit.colo.bookbud.features.auth.dto.RefreshTokenRequest;
import edu.cit.colo.bookbud.features.auth.dto.RegisterRequest;
import edu.cit.colo.bookbud.features.auth.dto.ResetPasswordRequest;
import edu.cit.colo.bookbud.shared.exception.AuthenticationException;
import edu.cit.colo.bookbud.shared.exception.BusinessException;
import edu.cit.colo.bookbud.features.auth.entity.PasswordResetToken;
import edu.cit.colo.bookbud.features.auth.entity.RefreshToken;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.auth.repository.PasswordResetTokenRepository;
import edu.cit.colo.bookbud.features.auth.repository.RefreshTokenRepository;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.features.auth.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       @Value("${app.google.oauth.client-id}") String googleClientId) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

        try {
            this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(java.util.List.of(googleClientId))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Unable to initialize Google ID token verifier", e);
        }
    }

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
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .accountStatus("Active")
                .build();

        user = userRepository.saveAndFlush(user);

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

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        GoogleIdToken idToken = verifyGoogleToken(request.getIdToken());
        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new AuthenticationException("AUTH-006", "Google account email is missing");
        }

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    if (!"Active".equals(existing.getAccountStatus())) {
                        throw new AuthenticationException("AUTH-004", "Account is suspended or banned");
                    }
                    return existing;
                })
                .orElseGet(() -> userRepository.saveAndFlush(User.builder()
                        .username(resolveUniqueUsername(payload))
                        .email(email)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(User.Role.USER)
                        .accountStatus("Active")
                        .build()));

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

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Check if user with this email exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("AUTH-007", "User not found"));

        // Check if user account is active
        if (!"Active".equals(user.getAccountStatus())) {
            throw new AuthenticationException("AUTH-004", "Account is suspended or banned");
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        
        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        
        passwordResetTokenRepository.save(token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("VALID-001", "Passwords do not match");
        }

        // Find the reset token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedAtIsNull(request.getToken())
                .orElseThrow(() -> new AuthenticationException("AUTH-008", "Invalid or expired reset token"));

        // Check if token has expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException("AUTH-008", "Reset token has expired");
        }

        // Update user password
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    private String createRefreshToken(User user) {
        String token = jwtUtil.generateRefreshToken(user.getUserId());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdToken token = googleIdTokenVerifier.verify(idTokenString);
            if (token == null) {
                throw new AuthenticationException("AUTH-006", "Invalid Google token");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            Object emailVerified = payload.get("email_verified");
            if (emailVerified instanceof Boolean && !((Boolean) emailVerified)) {
                throw new AuthenticationException("AUTH-006", "Google email is not verified");
            }

            return token;
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthenticationException("AUTH-006", "Unable to verify Google sign-in");
        }
    }

    private String resolveUniqueUsername(GoogleIdToken.Payload payload) {
        String baseUsername = sanitizeUsername(resolveDisplayName(payload));
        if (baseUsername.isBlank()) {
            baseUsername = sanitizeUsername(payload.getEmail().split("@")[0]);
        }

        String candidate = baseUsername;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = baseUsername + suffix;
            suffix++;
        }
        return candidate;
    }

    private String resolveDisplayName(GoogleIdToken.Payload payload) {
        Object name = payload.get("name");
        if (name instanceof String && !((String) name).isBlank()) {
            return (String) name;
        }

        Object givenName = payload.get("given_name");
        if (givenName instanceof String && !((String) givenName).isBlank()) {
            return (String) givenName;
        }

        return payload.getEmail() != null ? payload.getEmail().split("@")[0] : "bookbud-user";
    }

    private String sanitizeUsername(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]+", "");
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .user(AuthResponse.UserDTO.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .rating(user.getRating() != null ? user.getRating().toString() : null)
                        .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                        .build())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
