package com.example.colo.bookbud.repository;

import com.example.colo.bookbud.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(String userId);
    
    Page<Notification> findByUserUserId(String userId, Pageable pageable);
    
    Optional<Notification> findByNotificationIdAndUserUserId(String notificationId, String userId);
    
    long countByUserUserIdAndIsReadFalse(String userId);
}
