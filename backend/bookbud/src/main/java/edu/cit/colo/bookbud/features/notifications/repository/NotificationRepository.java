package edu.cit.colo.bookbud.features.notifications.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.colo.bookbud.features.notifications.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(String userId);
    
    Page<Notification> findByUserUserId(String userId, Pageable pageable);
    
    Optional<Notification> findByNotificationIdAndUserUserId(String notificationId, String userId);
    
    long countByUserUserIdAndIsReadFalse(String userId);
}
