package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.AbsenceBalance;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class AbsenceBalanceSpecifications {

    public static Specification<AbsenceBalance> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null
                ? cb.conjunction()
                : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<AbsenceBalance> hasAbsenceTypeId(UUID absenceTypeId) {
        return (root, query, cb) -> absenceTypeId == null
                ? cb.conjunction()
                : cb.equal(root.get("absenceType").get("id"), absenceTypeId);
    }

    public static Specification<AbsenceBalance> hasYear(Integer year) {
        return (root, query, cb) -> year == null
                ? cb.conjunction()
                : cb.equal(root.get("year"), year);
    }
}
