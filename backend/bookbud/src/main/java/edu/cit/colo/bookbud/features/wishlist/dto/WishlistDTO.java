package edu.cit.colo.bookbud.features.wishlist.dto;

import edu.cit.colo.bookbud.features.books.dto.BookDTO;
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
