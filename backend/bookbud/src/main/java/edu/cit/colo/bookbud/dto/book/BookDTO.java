package edu.cit.colo.bookbud.dto.book;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private String description;
    private String imageUrl;
    private String condition;
    private BigDecimal priceRent;
    private BigDecimal priceSale;
    private String transactionType;
    private String status;
    private String ownerId;
    private String createdAt;
}
