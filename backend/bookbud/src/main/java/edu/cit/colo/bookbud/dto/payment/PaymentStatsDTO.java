package edu.cit.colo.bookbud.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDTO {
    private Double totalEarnings;
    private Long pendingPayments;
    private Long successfulPayments;
    private Long failedPayments;
}
