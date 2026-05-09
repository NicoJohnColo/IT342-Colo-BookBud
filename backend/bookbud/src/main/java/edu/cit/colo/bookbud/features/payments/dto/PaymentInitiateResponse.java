package edu.cit.colo.bookbud.features.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {
    private String paymentId;
    private String checkoutUrl;
    private String paymentStatus;
}
