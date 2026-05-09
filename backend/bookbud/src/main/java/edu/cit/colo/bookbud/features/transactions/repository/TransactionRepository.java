package edu.cit.colo.bookbud.features.transactions.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.transactions.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findByUserUserId(String userId);
    
    List<Transaction> findByOwnerUserId(String ownerId);
    
    Page<Transaction> findByUserUserIdOrOwnerUserId(String userId, String ownerId, Pageable pageable);
    
    Page<Transaction> findByUserUserIdAndStatus(String userId, Transaction.Status status, Pageable pageable);
    
    Page<Transaction> findByOwnerUserIdAndStatus(String ownerId, Transaction.Status status, Pageable pageable);
    
    Optional<Transaction> findByTransactionIdAndUserUserId(String transactionId, String userId);
    
    Optional<Transaction> findByTransactionIdAndOwnerUserId(String transactionId, String ownerId);
    
    List<Transaction> findByBookBookId(String bookId);
    
    // Eagerly load the book relationship to avoid LazyInitializationException
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.book WHERE t.transactionId = :transactionId")
    Optional<Transaction> findByIdWithBook(@Param("transactionId") String transactionId);
}
