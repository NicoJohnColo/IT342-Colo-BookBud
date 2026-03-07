package edu.cit.colo.bookbud.repository;

import edu.cit.colo.bookbud.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    
    List<Book> findByOwnerUserId(String ownerId);
    
    Page<Book> findByStatus(Book.Status status, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE " +
           "(:q IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:genre IS NULL OR b.genre = :genre) AND " +
           "(:condition IS NULL OR b.condition = :condition) AND " +
           "(:type IS NULL OR b.transactionType = :type) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:minPrice IS NULL OR b.priceRent >= :minPrice OR b.priceSale >= :minPrice) AND " +
           "(:maxPrice IS NULL OR b.priceRent <= :maxPrice OR b.priceSale <= :maxPrice)")
    Page<Book> findByFilters(@Param("q") String q,
                             @Param("genre") String genre,
                             @Param("condition") Book.Condition condition,
                             @Param("type") Book.TransactionType type,
                             @Param("status") Book.Status status,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);
}
