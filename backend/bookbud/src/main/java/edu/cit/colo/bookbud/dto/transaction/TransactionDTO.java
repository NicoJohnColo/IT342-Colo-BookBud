package edu.cit.colo.bookbud.dto.transaction;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String transactionId;
    private String bookId;
    private String bookTitle;
    private String userId;
    private String renterUsername;
    private String ownerId;
    private String ownerUsername;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String createdAt;
    private String userRole;
}
