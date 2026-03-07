package edu.cit.colo.bookbud.repository;

import edu.cit.colo.bookbud.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    
    List<Wishlist> findByUserUserId(String userId);
    
    boolean existsByUserUserIdAndBookBookId(String userId, String bookId);
    
    Optional<Wishlist> findByWishlistIdAndUserUserId(String wishlistId, String userId);
    
    void deleteByBookBookId(String bookId);
}
