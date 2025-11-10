package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class WorkScheduleSpecifications {

    public static Specification<WorkSchedule> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<WorkSchedule> hasShiftId(UUID shiftId) {
        return (root, query, cb) -> {
            if (shiftId == null) return null;
            return cb.equal(root.get("shift").get("id"), shiftId);
        };
    }

    public static Specification<WorkSchedule> hasStatus(WorkScheduleStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<WorkSchedule> workDateFrom(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("workDate"), from);
        };
    }

    public static Specification<WorkSchedule> workDateTo(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("workDate"), to);
        };
    }

    /** Tìm theo tên user hoặc tên shift (keywords) */
    public static Specification<WorkSchedule> hasKeywords(String keywords) {
        return (root, query, cb) -> {
            if (keywords == null || keywords.isBlank()) return null;
            String like = "%" + keywords.trim().toLowerCase() + "%";
            // join optional: user.fullName, shift.name
            var userJoin = root.join("user");
            var shiftJoin = root.join("shift");
            return cb.or(
                    cb.like(cb.lower(userJoin.get("fullName")), like),
                    cb.like(cb.lower(shiftJoin.get("name")), like)
            );
        };
    }
}
