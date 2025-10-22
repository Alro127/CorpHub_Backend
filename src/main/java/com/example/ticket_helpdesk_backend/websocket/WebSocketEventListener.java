package com.example.ticket_helpdesk_backend.websocket;

import com.example.ticket_helpdesk_backend.dto.NotificationDto;
import com.example.ticket_helpdesk_backend.service.NotificationQueueService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final NotificationQueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    private UUID getUserIdFromSession(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String authHeader = accessor.getFirstNativeHeader("Authorization"); // ✅ Lấy từ STOMP header

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Missing Authorization header in WebSocket connection");
        }

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.getUserId(token);

        if (userId == null) {
            throw new IllegalStateException("Invalid JWT: cannot extract userId");
        }

        return userId;
    }

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        UUID userId = getUserIdFromSession(event);

        redisTemplate.opsForSet().add("online_users", userId.toString());
        System.out.println("✅ WebSocket connected: " + userId);

        // Gửi lại các thông báo pending
        List<NotificationDto> pending = queueService.popAllFromQueue(userId);
        for (NotificationDto n : pending) {
            messagingTemplate.convertAndSend("/topic/users/" + userId, n);
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        System.out.println("❌ WebSocket disconnected: " + event.getSessionId());
    }
}
