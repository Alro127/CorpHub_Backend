package com.example.ticket_helpdesk_backend.scheduler;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.entity.AttendanceRecord;
import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import com.example.ticket_helpdesk_backend.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkScheduleEventWorker {

    private static final String REDIS_KEY = "schedule_end_events";

    private final StringRedisTemplate redis;
    private final WorkScheduleRepository workScheduleRepository;

    /**
     * Worker chạy định kỳ (mỗi 10s) để:
     * - Lấy các event đến hạn (endTime)
     * - Xử lý MISSED (vắng mặt)
     * - Có thể mở rộng để xử lý thêm nhiều loại event khác
     */
    @Scheduled(fixedRate = 10_000)
    @Transactional
    public void processEvents() {

        long nowEpoch = Instant.now().getEpochSecond();

        // lấy tất cả scheduleId có score <= nowEpoch
        Set<String> dueIds =
                redis.opsForZSet().rangeByScore(REDIS_KEY, 0, nowEpoch);

        if (dueIds == null || dueIds.isEmpty()) {
            return;
        }

        for (String idStr : dueIds) {
            try {
                UUID wsId = UUID.fromString(idStr);

                WorkSchedule ws = workScheduleRepository.findById(wsId).orElse(null);
                if (ws == null) {
                    redis.opsForZSet().remove(REDIS_KEY, idStr);
                    continue;
                }

                AttendanceRecord ar = ws.getAttendanceRecord();

                boolean noAttendance =
                        (ar == null) ||
                                (ar.getCheckInTime() == null && ar.getCheckOutTime() == null);

                // ✔ MISSED
                if (noAttendance && ws.getStatus() == WorkScheduleStatus.SCHEDULED) {
                    ws.setStatus(WorkScheduleStatus.MISSED);
                    workScheduleRepository.save(ws);
                    log.info("[WorkScheduleEventWorker] Set MISSED for schedule {}", wsId);
                }

                // ❗ tương lai: có thể thêm xử lý khác tại đây
                // if(...) { handleLateCheckout(ws); }
                // if(...) { autoSendNotifications(ws); }

                // Xóa event để không xử lý lại
                redis.opsForZSet().remove(REDIS_KEY, idStr);

            } catch (Exception ex) {
                redis.opsForZSet().remove(REDIS_KEY, idStr);
                log.warn("[WorkScheduleEventWorker] Invalid schedule id in Redis: {}", idStr);
            }
        }
    }
}
