package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Notification;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.Notification}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 200)
    String title;
    @NotNull
    String message;
    @Size(max = 500)
    String link;
    UUID senderId;
    UUID receiverId;
    @Size(max = 50)
    String type;
    LocalDateTime createdAt;
    LocalDateTime deliveredAt;
    LocalDateTime seenAt;
    LocalDateTime readAt;
    String metadata;
    Integer isRead;

    public static NotificationDto toDto(com.example.ticket_helpdesk_backend.entity.Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getLink(),
                n.getSender().getId(),
                n.getReceiver().getId(),
                n.getType(),
                n.getCreatedAt(),
                n.getDeliveredAt(),
                n.getSeenAt(),
                n.getReadAt(),
                n.getMetadata(),
                n.getReadAt() == null ? 0 : 1
        );
    }

}