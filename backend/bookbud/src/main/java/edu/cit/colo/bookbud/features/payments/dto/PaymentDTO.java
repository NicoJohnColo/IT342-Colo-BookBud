package edu.cit.colo.bookbud.features.payments.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private String paymentId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String paymentStatus;
}
