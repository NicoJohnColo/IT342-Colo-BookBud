package edu.cit.colo.bookbud.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String notificationId;
    private String userId;
    private String message;
    private Boolean isRead;
    private String createdAt;
}
