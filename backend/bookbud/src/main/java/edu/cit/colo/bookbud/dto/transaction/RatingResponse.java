package edu.cit.colo.bookbud.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
