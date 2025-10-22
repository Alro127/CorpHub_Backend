package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationQueueService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    /**
     * Sinh tên key trong Redis cho hàng đợi của từng người dùng
     * Ví dụ: notifications:6a80b2e2-1331-4ad9-a002-335d0b05fd2c
     */
    private String queueKey(UUID userId) {
        return "notifications:" + userId;
    }

    /**
     * Thêm một thông báo vào hàng đợi Redis của người nhận
     */
    public void pushToQueue(UUID receiverId, NotificationDto notif) {
        try {
            String key = queueKey(receiverId);
            String json = mapper.writeValueAsString(notif);

            redisTemplate.opsForList().rightPush(key, json);
            System.out.println("[Redis] Pushed notification to queue: " + key);
        } catch (Exception e) {
            System.err.println("[Redis] Failed to push notification: " + e.getMessage());
            throw new RuntimeException("Failed to push notification to Redis queue", e);
        }
    }

    /**
     * Lấy toàn bộ thông báo trong hàng đợi của user (và xóa khỏi Redis)
     */
    public List<NotificationDto> popAllFromQueue(UUID userId) {
        try {
            String key = queueKey(userId);
            List<String> rawList = redisTemplate.opsForList().range(key, 0, -1);
            redisTemplate.delete(key);

            if (rawList == null || rawList.isEmpty()) {
                System.out.println("ℹ️ [Redis] No pending notifications for user: " + userId);
                return Collections.emptyList();
            }

            List<NotificationDto> result = new ArrayList<>();
            for (String json : rawList) {
                result.add(mapper.readValue(json, NotificationDto.class));
            }

            System.out.println("📤 [Redis] Retrieved " + result.size() +
                    " pending notifications for user: " + userId);
            return result;

        } catch (Exception e) {
            System.err.println("❌ [Redis] Failed to read notifications: " + e.getMessage());
            throw new RuntimeException("Failed to read notifications from Redis queue", e);
        }
    }
}
