package edu.cit.colo.bookbud.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String genre;

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    private BigDecimal priceRent;
    private BigDecimal priceSale;
}
