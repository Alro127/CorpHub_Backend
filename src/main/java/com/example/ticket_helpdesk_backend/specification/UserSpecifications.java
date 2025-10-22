package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.entity.Role;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;
import java.time.LocalDateTime;
public class UserSpecifications {

    /** 🟩 Lọc theo ID người dùng (bỏ qua nếu null) */
    public static Specification<User> hasId(UUID id) {
        return (root, query, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("id"), id);
    }

    public static Specification<User> hasRoleName(String roleName) {
        return (root, query, cb) -> {
            Join<Object, Object> role = root.join("role", JoinType.INNER);
            return cb.equal(cb.lower(role.get("name")), roleName.toLowerCase());
        };
    }

    /** 🟦 Lọc theo username (chính là workEmail) */
    public static Specification<User> hasUsername(String username) {
        return (root, query, cb) -> {
            if (username == null || username.isBlank()) return cb.conjunction();
            String pattern = "%" + username.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("username")), pattern);
        };
    }
	
	public static Specification<User> inSameDepartmentAsUser(UUID userId) {
        return (root, query, cb) -> {
            // Subquery: lấy departmentId của user đang xét (userId)
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<User> u2 = sub.from(User.class);
            Join<Object, Object> ep2 = u2.join("employeeProfile", JoinType.LEFT);
            Join<Object, Object> dep2 = ep2.join("department", JoinType.LEFT);
            sub.select(dep2.get("id"))
                    .where(cb.equal(u2.get("id"), userId));

            // Join department của root
            Join<Object, Object> ep = root.join("employeeProfile", JoinType.LEFT);
            Join<Object, Object> dep = ep.join("department", JoinType.LEFT);

            return dep.get("id").in(sub);
        };
    }

    /** 🟧 Lọc theo trạng thái hoạt động (active) */
    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    /** 🟨 Lọc theo vai trò */
    public static Specification<User> hasRole(UUID roleId) {
        return (root, query, cb) -> {
            if (roleId == null) return cb.conjunction();
            Join<User, Role> roleJoin = root.join("role", JoinType.LEFT);
            return cb.equal(roleJoin.get("id"), roleId);
        };
    }

    /** 🟫 Lọc theo ngày hết hạn tài khoản (expired) */
    public static Specification<User> expiredBefore(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.lessThan(root.get("expired"), dateTime);
        };
    }

    public static Specification<User> expiredAfter(LocalDateTime dateTime) {
        return (root, query, cb) -> {
            if (dateTime == null) return cb.conjunction();
            return cb.greaterThan(root.get("expired"), dateTime);
        };
    }

    /** 🟪 Lọc theo phòng ban của nhân viên (thông qua employeeProfile.department) */
    public static Specification<User> belongsToDepartment(UUID departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            Join<Object, Object> emp = root.join("employeeProfile", JoinType.LEFT);
            return cb.equal(emp.get("department").get("id"), departmentId);
        };
    }

    /** 🔍 Tìm kiếm toàn văn theo tên nhân viên, email công ty hoặc số điện thoại */
    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            Join<Object, Object> emp = root.join("employeeProfile", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(emp.get("fullName")), pattern),
                    cb.like(cb.lower(emp.get("phone")), pattern),
                    cb.like(cb.lower(emp.get("personalEmail")), pattern)
            );
        };
    }

    /** 🧩 Kết hợp động nhiều điều kiện */
    public static Specification<User> buildFilter(
            UUID roleId,
            Boolean active,
            UUID departmentId,
            String keyword
    ) {
        return Specification
                .where(hasRole(roleId))
                .and(isActive(active))
                .and(belongsToDepartment(departmentId))
                .and(search(keyword));
    }
}
