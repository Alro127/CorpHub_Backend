package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecifications {

    // role.name = ?
    public static Specification<User> hasRoleName(String roleName) {
        return (root, query, cb) -> {
            Join<Object, Object> role = root.join("role", JoinType.INNER);
            return cb.equal(cb.lower(role.get("name")), roleName.toLowerCase());
        };
    }

    // active = ?
    public static Specification<User> isActive(boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    // employeeProfile.department.id IN (subquery: phòng ban của userId đầu vào)
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
}
