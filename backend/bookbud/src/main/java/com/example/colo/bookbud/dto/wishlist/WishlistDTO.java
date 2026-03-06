package com.example.colo.bookbud.dto.wishlist;

import com.example.colo.bookbud.dto.book.BookDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    private String wishlistId;
    private String userId;
    private String bookId;
    private BookDTO book;
    private String dateAdded;
}
