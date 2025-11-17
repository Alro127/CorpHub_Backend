package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.AttendanceRecord;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.UUID;

public class AttendanceRecordSpecifications {

    // attendance_record.workSchedule.id = wsId
    public static Specification<AttendanceRecord> hasWorkScheduleId(UUID wsId) {
        return (root, query, cb) ->
                wsId == null ? cb.conjunction()
                        : cb.equal(root.get("workSchedule").get("id"), wsId);
    }

    // attendance_record.workSchedule.id IN (...)
    public static Specification<AttendanceRecord> workScheduleIdIn(List<UUID> wsIds) {
        return (root, query, cb) ->
                root.get("workSchedule").get("id").in(wsIds);
    }

    // attendance_record.user.id = userId
    public static Specification<AttendanceRecord> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction()
                        : cb.equal(root.get("user").get("id"), userId);
    }
}
