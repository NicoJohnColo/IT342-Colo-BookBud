package com.example.colo.bookbud.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}
