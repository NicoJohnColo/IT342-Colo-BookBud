package com.example.colo.bookbud.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
