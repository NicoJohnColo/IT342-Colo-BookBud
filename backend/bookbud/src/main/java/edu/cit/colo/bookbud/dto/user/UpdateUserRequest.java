package edu.cit.colo.bookbud.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;
    private String facebookUrl;
    private String messenger;
    private String mobileNumber;
}
