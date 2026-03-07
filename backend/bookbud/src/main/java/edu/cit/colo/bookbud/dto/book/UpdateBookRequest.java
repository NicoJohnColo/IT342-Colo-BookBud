package edu.cit.colo.bookbud.dto.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {
    private String title;
    private String author;
    private String genre;
    private String condition;
    private String transactionType;
    private BigDecimal priceRent;
    private BigDecimal priceSale;
}
