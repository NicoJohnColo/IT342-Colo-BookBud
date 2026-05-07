package edu.cit.colo.bookbud.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    Optional<Payment> findByTransactionTransactionId(String transactionId);
    
    boolean existsByTransactionTransactionId(String transactionId);
    
    // Get payments received by user (as owner of transaction)
    @Query("SELECT p FROM Payment p WHERE p.transaction.owner.userId = :userId ORDER BY p.paymentDate DESC")
    Page<Payment> findPaymentsReceivedByUser(@Param("userId") String userId, Pageable pageable);
    
    // Get payments made by user (as renter/buyer in transaction)
    @Query("SELECT p FROM Payment p WHERE p.transaction.user.userId = :userId ORDER BY p.paymentDate DESC")
    Page<Payment> findPaymentsMadeByUser(@Param("userId") String userId, Pageable pageable);
    
    // Get all payments for a user (both received and made)
    @Query("SELECT p FROM Payment p WHERE p.transaction.owner.userId = :userId OR p.transaction.user.userId = :userId ORDER BY p.paymentDate DESC")
    Page<Payment> findAllPaymentsForUser(@Param("userId") String userId, Pageable pageable);
    
    // Get payment statistics for user (earnings)
    @Query("SELECT COALESCE(SUM(CAST(p.amount AS double)), 0.0) FROM Payment p WHERE p.transaction.owner.userId = :userId AND p.paymentStatus = 'Paid'")
    Double getTotalEarningsForUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.transaction.owner.userId = :userId AND p.paymentStatus = 'Pending'")
    long getPendingPaymentCountForUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.transaction.owner.userId = :userId AND p.paymentStatus = 'Paid'")
    long getSuccessfulPaymentCountForUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.transaction.owner.userId = :userId AND p.paymentStatus = 'Failed'")
    long getFailedPaymentCountForUser(@Param("userId") String userId);
}
