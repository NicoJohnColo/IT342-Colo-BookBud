package edu.cit.colo.bookbud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.colo.bookbud.shared.dto.ApiResponse;
import edu.cit.colo.bookbud.features.payments.dto.EarningsSummaryDTO;
import edu.cit.colo.bookbud.features.payments.service.PaymentService;
import edu.cit.colo.bookbud.features.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/earnings")
@RequiredArgsConstructor
public class EarningsController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<EarningsSummaryDTO>> getEarningsSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("AUTH-001", "Missing or invalid authorization header", null));
        }
        try {
            String userId = jwtUtil.extractUserId(authHeader.substring(7));
            EarningsSummaryDTO summary = paymentService.getEarningsSummary(userId);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            // Return empty earnings summary instead of 500 error
            return ResponseEntity.ok(ApiResponse.success(EarningsSummaryDTO.builder()
                    .totalEarnings(0.0)
                    .pendingPayments(0L)
                    .successfulPayments(0L)
                    .failedPayments(0L)
                    .build()));
        }
    }
}
