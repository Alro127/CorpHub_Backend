package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.entity.Meeting;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingSpecifications {

    public static Specification<Meeting> organizedBy(String email) {
        return (root, query, cb) ->
                (email == null) ? null : cb.equal(root.get("organizerEmail"), email);
    }

    public static Specification<Meeting> joinedBy(String email) {
        return (root, query, cb) -> {
            if (email == null) return null;
            var attendeesJoin = root.join("attendees", JoinType.LEFT);
            query.distinct(true);
            return cb.equal(attendeesJoin.get("email"), email);
        };
    }

    public static Specification<Meeting> organizedOrJoinedBy(List<String> emails) {
        return (root, query, cb) -> {
            if (emails == null || emails.isEmpty()) return null;

            query.distinct(true); // tránh trùng

            var attendeesJoin = root.join("attendees", JoinType.LEFT);

            return cb.or(
                    root.get("organizerEmail").in(emails),
                    attendeesJoin.get("email").in(emails)
            );
        };
    }


    public static Specification<Meeting> startAfter(LocalDateTime startTime) {
        return (root, query, cb) ->
                (startTime == null) ? null : cb.greaterThanOrEqualTo(root.get("startTime"), startTime);
    }

    public static Specification<Meeting> endBefore(LocalDateTime endTime) {
        return (root, query, cb) ->
                (endTime == null) ? null : cb.lessThanOrEqualTo(root.get("endTime"), endTime);
    }

    public static Specification<Meeting> hasEmails(List<String> emails) {
        return (root, query, cb) -> {
            if (emails == null || emails.isEmpty()) return null;
            var attendeesJoin = root.join("attendees", JoinType.LEFT);
            query.distinct(true);
            return attendeesJoin.get("email").in(emails);
        };
    }
}
