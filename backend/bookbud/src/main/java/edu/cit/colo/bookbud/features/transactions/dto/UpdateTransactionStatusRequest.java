package edu.cit.colo.bookbud.features.transactions.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}
