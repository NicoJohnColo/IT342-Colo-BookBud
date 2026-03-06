package com.example.colo.bookbud.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
