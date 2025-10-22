package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.NotificationDto;
import com.example.ticket_helpdesk_backend.entity.Notification;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationQueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ModelMapper modelMapper;

    /**
     * Gửi thông báo mới đến người nhận
     * - Nếu người nhận đang online (Redis có userId) → gửi qua WebSocket
     * - Nếu offline → lưu vào Redis queue
     */
    public NotificationDto sendNotification(NotificationDto dto) {
        // 🔹 Ánh xạ DTO → Entity
        Notification notif = modelMapper.map(dto, Notification.class);
        notif.setCreatedAt(LocalDateTime.now());
        notif.setIsRead(0);

        // 🔹 Lưu DB

        Notification saved = repository.save(notif);


        UUID receiverId = saved.getReceiver().getId();

        // 🔹 Kiểm tra user có online không
        boolean isOnline = Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember("online_users", receiverId.toString())
        );

        NotificationDto responseDto = modelMapper.map(saved, NotificationDto.class);

        if (isOnline) {
            // 🔔 Gửi realtime qua WebSocket
            System.out.println("✅ [WebSocket] Sending notification to /topic/users/" + receiverId);
            messagingTemplate.convertAndSend("/topic/users/" + receiverId, responseDto);
        } else {
            // 💾 Lưu queue Redis (để gửi lại khi user online)
            System.out.println("💾 [Redis] Queued notification for user " + receiverId);
            queueService.pushToQueue(receiverId, responseDto);
        }

        return responseDto;
    }

    public List<NotificationDto> getNotifications(UUID receiverId) {
        return repository.findByReceiverIdOrderByCreatedAtDesc(receiverId).stream().map((element) -> modelMapper.map(element, NotificationDto.class)).collect(Collectors.toList());
    }

    public void markAsRead(UUID id) {
        repository.findById(id).ifPresent(n -> {
            n.setReadAt(LocalDateTime.now());
            repository.save(n);
        });
    }
}
