package com.example.colo.bookbud.dto.book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private String condition;
    private BigDecimal priceRent;
    private BigDecimal priceSale;
    private String transactionType;
    private String status;
    private String ownerId;
    private String createdAt;
}
