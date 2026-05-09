package edu.cit.colo.bookbud.features.transactions.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private String transactionId;
    private String ratedUserId;
    private BigDecimal rating;
    private BigDecimal newAggregateRating;
}
