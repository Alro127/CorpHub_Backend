package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.consts.AssetStatus;
import com.example.ticket_helpdesk_backend.entity.Asset;
import com.example.ticket_helpdesk_backend.entity.Room;
import com.example.ticket_helpdesk_backend.entity.AssetCategory;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class AssetSpecifications {

    public static Specification<Asset> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("code")), likePattern)
            );
        };
    }

    public static Specification<Asset> hasCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Asset> hasStatus(AssetStatus status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Asset> hasRoom(UUID roomId) {
        return (root, query, cb) -> {
            if (roomId == null) return null;
            return cb.equal(root.get("room").get("id"), roomId);
        };
    }

    public static Specification<Asset> hasMinValue(BigDecimal minValue) {
        return (root, query, cb) -> {
            if (minValue == null) return null;
            return cb.greaterThanOrEqualTo(root.get("value"), minValue);
        };
    }

    public static Specification<Asset> hasMaxValue(BigDecimal maxValue) {
        return (root, query, cb) -> {
            if (maxValue == null) return null;
            return cb.lessThanOrEqualTo(root.get("value"), maxValue);
        };
    }

    public static Specification<Asset> hasDepartment(UUID departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return null;
            // join room -> department
            return cb.equal(root.join("room").join("department").get("id"), departmentId);
        };
    }

    public static Specification<Asset> isAvailableOnly(Boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (onlyAvailable == null || !onlyAvailable) return null;
            // Giả định rằng chỉ những asset chưa có room mới là "available"
            return cb.isNull(root.get("room"));
        };
    }

}
