package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.Shift;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;

public class ShiftSpecifications {

    /**
     * Tìm kiếm theo từ khóa (tên ca)
     */
    public static Specification<Shift> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    /**
     * Lọc theo trạng thái ca đêm
     */
    public static Specification<Shift> isNightShift(Boolean isNightShift) {
        return (root, query, cb) -> {
            if (isNightShift == null) return null;
            return cb.equal(root.get("isNightShift"), isNightShift);
        };
    }

    /**
     * Lọc theo khoảng thời gian bắt đầu
     */
    public static Specification<Shift> startAfter(LocalTime start) {
        return (root, query, cb) -> {
            if (start == null) return null;
            return cb.greaterThanOrEqualTo(root.get("startTime"), start);
        };
    }

    /**
     * Lọc theo khoảng thời gian kết thúc
     */
    public static Specification<Shift> endBefore(LocalTime end) {
        return (root, query, cb) -> {
            if (end == null) return null;
            return cb.lessThanOrEqualTo(root.get("endTime"), end);
        };
    }
}
