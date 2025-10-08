package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TicketSpecifications {

    /** Lọc ticket do user là register (người gửi) */
    public static Specification<Ticket> byRequester(UUID userId) {
        return (root, query, cb) ->
                userId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("requester").get("id"), userId);
    }

    /** 🔵 Lọc ticket do user là assignee (người được giao) */
    public static Specification<Ticket> byAssignee(UUID userId) {
        return (root, query, cb) ->
                userId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("assignee").get("id"), userId);
    }

    public static Specification<Ticket> byUser(UUID userId) {
        return Specification
                .where(byRequester(userId))
                .or(byAssignee(userId));
    }

    public static Specification<Ticket> buildUserRoleSpec(UUID userId, Boolean isRequester) {
        if (isRequester == null) {
            return TicketSpecifications.byUser(userId);
        }
        if (isRequester) {
            // Chỉ lấy ticket do user tạo
            return TicketSpecifications.byRequester(userId);
        } else {
            // Chỉ lấy ticket user được giao
            return TicketSpecifications.byAssignee(userId);
        }
    }


    /** 🟣 Ticket gửi từ phòng ban */
    public static Specification<Ticket> sentByDepartment(UUID departmentId) {
        return (root, query, cb) -> {
            Join<Ticket, User> requester = root.join("requester", JoinType.LEFT);
            Join<Object, Object> employeeProfile = requester.join("employeeProfile", JoinType.LEFT);
            return cb.equal(employeeProfile.get("department").get("id"), departmentId);
        };
    }

    /** 🔵 Ticket nhận bởi phòng ban */
    public static Specification<Ticket> receivedByDepartment(UUID departmentId) {
        return (root, query, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    /** 🟩 Lọc theo trạng thái (bỏ qua nếu null hoặc rỗng) */
    public static Specification<Ticket> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction(); // không thêm điều kiện
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /** 🟧 Lọc theo độ ưu tiên (bỏ qua nếu null hoặc rỗng) */
    public static Specification<Ticket> hasPriority(String priority) {
        return (root, query, cb) -> {
            if (priority == null || priority.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("priority"), priority);
        };
    }

    /** 🟨 Lọc theo Category (bỏ qua nếu null) */
    public static Specification<Ticket> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /** 🟦 Lọc theo khoảng thời gian createdAt */
    public static Specification<Ticket> createdBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from != null && to != null)
                return cb.between(root.get("createdAt"), from, to);
            else if (from != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            else if (to != null)
                return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            else
                return cb.conjunction();
        };
    }

    /** 🔍 Tìm kiếm toàn văn */
    public static Specification<Ticket> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            Join<Ticket, User> requester = root.join("requester", JoinType.LEFT);
            Join<Ticket, User> assignee = root.join("assignee", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(root.get("description"), "%" + keyword + "%"),
                    cb.like(cb.lower(requester.get("employeeProfile").get("fullName")), pattern),
                    cb.like(cb.lower(assignee.get("employeeProfile").get("fullName")), pattern),
                    cb.like(cb.lower(root.get("category").get("name")), pattern)
            );
        };
    }
}
