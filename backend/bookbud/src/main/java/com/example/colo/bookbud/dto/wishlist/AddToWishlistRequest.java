package com.example.colo.bookbud.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToWishlistRequest {
    @NotBlank(message = "Book ID is required")
    private String bookId;
}
