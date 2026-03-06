package com.example.colo.bookbud.repository;

import com.example.colo.bookbud.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    Optional<Payment> findByTransactionTransactionId(String transactionId);
    
    boolean existsByTransactionTransactionId(String transactionId);
}
