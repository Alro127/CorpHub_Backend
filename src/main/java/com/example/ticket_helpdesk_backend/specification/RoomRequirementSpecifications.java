package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class RoomRequirementSpecifications {

    // 🔍 Lọc theo Room ID
    public static Specification<RoomRequirement> hasRoomId(UUID roomId) {
        return (root, query, cb) -> {
            if (roomId == null) return null;
            return cb.equal(root.join("room").get("id"), roomId);
        };
    }

    // 🔍 Lọc theo Meeting ID
    public static Specification<RoomRequirement> hasMeetingId(UUID meetingId) {
        return (root, query, cb) -> {
            if (meetingId == null) return null;
            return cb.equal(root.join("meeting").get("id"), meetingId);
        };
    }

    // 🔍 Lọc theo khoảng thời gian trùng (startTime < end && endTime > start)
    public static Specification<RoomRequirement> overlapsWith(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null || end == null) return null;
            return cb.and(
                    cb.lessThan(root.get("startTime"), end),
                    cb.greaterThan(root.get("endTime"), start)
            );
        };
    }

    // 🔍 Phòng có trùng lịch trong khoảng thời gian
    public static Specification<RoomRequirement> roomHasConflict(UUID roomId, LocalDateTime start, LocalDateTime end) {
        return Specification
                .where(hasRoomId(roomId))
                .and(overlapsWith(start, end));
    }
}
