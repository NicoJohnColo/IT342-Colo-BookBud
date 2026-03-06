package com.example.colo.bookbud.repository;

import com.example.colo.bookbud.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}
