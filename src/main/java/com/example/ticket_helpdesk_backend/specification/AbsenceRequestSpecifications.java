package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AbsenceRequestSpecifications {

    /** 🔹 Lọc theo userId (người nộp đơn) */
    public static Specification<AbsenceRequest> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction() :
                        cb.equal(root.get("user").get("id"), userId);
    }

    /** 🔹 Lọc theo loại nghỉ phép */
    public static Specification<AbsenceRequest> hasAbsenceType(UUID absenceTypeId) {
        return (root, query, cb) ->
                absenceTypeId == null ? cb.conjunction() :
                        cb.equal(root.get("absenceType").get("id"), absenceTypeId);
    }

    /** 🔹 Lọc theo trạng thái (PENDING, APPROVED, REJECTED) */
    public static Specification<AbsenceRequest> hasStatus(String status) {
        return (root, query, cb) ->
                (status == null || status.isBlank()) ? cb.conjunction() :
                        cb.equal(cb.lower(root.get("status")), status.toLowerCase());
    }

    /** 🔹 Lọc theo người duyệt */
    public static Specification<AbsenceRequest> hasApproverId(UUID approverId) {
        return (root, query, cb) ->
                approverId == null ? cb.conjunction() :
                        cb.equal(root.get("approver").get("id"), approverId);
    }

    /** 🔹 Lọc các đơn có ngày bắt đầu >= fromDate */
    public static Specification<AbsenceRequest> startDateAfter(LocalDate fromDate) {
        return (root, query, cb) ->
                fromDate == null ? cb.conjunction() :
                        cb.greaterThanOrEqualTo(root.get("startDate"), fromDate);
    }

    /** 🔹 Lọc các đơn có ngày kết thúc <= toDate */
    public static Specification<AbsenceRequest> endDateBefore(LocalDate toDate) {
        return (root, query, cb) ->
                toDate == null ? cb.conjunction() :
                        cb.lessThanOrEqualTo(root.get("endDate"), toDate);
    }

    /** 🔹 Tìm theo keyword (áp dụng cho lý do nghỉ, tên loại nghỉ, tên người dùng) */
    public static Specification<AbsenceRequest> containsKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("reason")), likePattern),
                    cb.like(cb.lower(root.get("absenceType").get("name")), likePattern),
                    cb.like(cb.lower(root.get("user").get("fullName")), likePattern)
            );
        };
    }

    /** 🔹 Gộp điều kiện (builder tiện lợi) */
    public static Specification<AbsenceRequest> build(
            UUID userId,
            UUID absenceTypeId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String keyword
    ) {
        return Specification.where(hasUserId(userId))
                .and(hasAbsenceType(absenceTypeId))
                .and(hasStatus(status))
                .and(startDateAfter(fromDate))
                .and(endDateBefore(toDate))
                .and(containsKeyword(keyword));
    }
}
