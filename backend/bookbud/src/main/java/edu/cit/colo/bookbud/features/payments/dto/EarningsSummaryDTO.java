package edu.cit.colo.bookbud.features.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsSummaryDTO {
    private Double totalEarnings;
    private Long pendingPayments;
    private Long successfulPayments;
    private Long failedPayments;
}
