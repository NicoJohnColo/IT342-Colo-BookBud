package edu.cit.colo.bookbud.dto.book;

import java.math.BigDecimal;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    private String title;
    private String author;
    private String genre;
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;
    private String condition;
    private String transactionType;
    private BigDecimal priceRent;
    private BigDecimal priceSale;
}
