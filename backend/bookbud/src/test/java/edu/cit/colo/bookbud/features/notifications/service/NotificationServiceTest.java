package edu.cit.colo.bookbud.features.notifications.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.colo.bookbud.features.notifications.entity.Notification;
import edu.cit.colo.bookbud.features.notifications.repository.NotificationRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.shared.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        User testUser = User.builder().userId("test-user-id").build();
        testNotification = Notification.builder()
                .notificationId("test-notification-id")
                .message("Test message")
                .isRead(false)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetNotificationsByUserId_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserUserIdOrderByCreatedAtDesc("test-user-id")).thenReturn(notifications);

        // When
        var result = notificationService.getMyNotifications("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserUserIdOrderByCreatedAtDesc("test-user-id");
    }

    @Test
    void testCreateNotification_Success() {
        // Given
        User testUser = User.builder().userId("test-user-id").build();
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        Notification result = notificationService.createNotification("test-user-id", "Test message");

        // Then
        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_Success() {
        // Given
        when(notificationRepository.findByNotificationIdAndUserUserId("test-notification-id", "test-user-id"))
                .thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        notificationService.markAsRead("test-notification-id", "test-user-id");

        // Then
        verify(notificationRepository, times(1)).findByNotificationIdAndUserUserId("test-notification-id", "test-user-id");
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testMarkAsRead_NotFound() {
        // Given
        when(notificationRepository.findByNotificationIdAndUserUserId("non-existent-id", "test-user-id"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            notificationService.markAsRead("non-existent-id", "test-user-id");
        });
        verify(notificationRepository, times(1)).findByNotificationIdAndUserUserId("non-existent-id", "test-user-id");
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}

