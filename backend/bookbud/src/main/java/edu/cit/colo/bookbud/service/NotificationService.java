package edu.cit.colo.bookbud.service;

import edu.cit.colo.bookbud.dto.notification.NotificationDTO;
import edu.cit.colo.bookbud.entity.Notification;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.NotificationRepository;
import edu.cit.colo.bookbud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(String userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .user(user)
                .message(message)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications(String userId) {
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDTO markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findByNotificationIdAndUserUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException("AUTH-003", "Not own notification"));

        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return mapToDTO(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findByNotificationIdAndUserUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException("AUTH-003", "Not own notification"));

        notificationRepository.delete(notification);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt().toString())
                .build();
    }
}
