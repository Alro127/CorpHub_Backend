package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.Department;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class RoomSpecifications {

    // 🔍 Tìm theo tên (LIKE)
    public static Specification<Room> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    // 🔍 Tìm theo loại
    public static Specification<Room> hasType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.trim().isEmpty()) return null;
            return cb.equal(cb.lower(root.get("type")), type.toLowerCase());
        };
    }

    // 🔍 Tìm theo trạng thái
    public static Specification<Room> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.trim().isEmpty()) return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase());
        };
    }

    // 🔍 Tìm theo sức chứa >= min
    public static Specification<Room> hasMinCapacity(Integer min) {
        return (root, query, cb) -> {
            if (min == null) return null;
            return cb.greaterThanOrEqualTo(root.get("capacity"), min);
        };
    }

    // 🔍 Tìm theo sức chứa <= max
    public static Specification<Room> hasMaxCapacity(Integer max) {
        return (root, query, cb) -> {
            if (max == null) return null;
            return cb.lessThanOrEqualTo(root.get("capacity"), max);
        };
    }

    // 🔍 Tìm theo diện tích tối thiểu
    public static Specification<Room> hasMinArea(BigDecimal minArea) {
        return (root, query, cb) -> {
            if (minArea == null) return null;
            return cb.greaterThanOrEqualTo(root.get("area"), minArea);
        };
    }

    // 🔍 Tìm theo diện tích tối đa
    public static Specification<Room> hasMaxArea(BigDecimal maxArea) {
        return (root, query, cb) -> {
            if (maxArea == null) return null;
            return cb.lessThanOrEqualTo(root.get("area"), maxArea);
        };
    }

    // 🔍 Tìm theo phòng ban (id)
    public static Specification<Room> belongsToDepartment(UUID departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            return cb.equal(root.join("department").get("id"), departmentId);
        };
    }
}
