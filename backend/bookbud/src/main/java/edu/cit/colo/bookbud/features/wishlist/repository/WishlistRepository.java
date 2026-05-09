package edu.cit.colo.bookbud.features.wishlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.wishlist.entity.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    
    List<Wishlist> findByUserUserId(String userId);
    
    boolean existsByUserUserIdAndBookBookId(String userId, String bookId);
    
    Optional<Wishlist> findByWishlistIdAndUserUserId(String wishlistId, String userId);
    
    void deleteByBookBookId(String bookId);
}
