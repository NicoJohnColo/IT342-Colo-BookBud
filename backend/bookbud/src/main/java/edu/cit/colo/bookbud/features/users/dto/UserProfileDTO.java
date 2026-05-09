package edu.cit.colo.bookbud.features.users.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String userId;
    private String username;
    private BigDecimal rating;
    private String createdAt;
    private String facebookUrl;
    private String messenger;
    private String mobileNumber;
}
