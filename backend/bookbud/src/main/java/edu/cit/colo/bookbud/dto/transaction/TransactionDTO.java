package edu.cit.colo.bookbud.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String transactionId;
    private String bookId;
    private String userId;
    private String ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String createdAt;
    private String userRole;
}
