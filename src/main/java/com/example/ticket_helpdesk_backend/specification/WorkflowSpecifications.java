package com.example.ticket_helpdesk_backend.specification;

import com.example.ticket_helpdesk_backend.consts.WorkflowStatus;
import com.example.ticket_helpdesk_backend.entity.WorkflowInstance;
import com.example.ticket_helpdesk_backend.entity.WorkflowStep;
import com.example.ticket_helpdesk_backend.entity.WorkflowStepAction;
import com.example.ticket_helpdesk_backend.entity.WorkflowTemplate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class WorkflowSpecifications {
    public static Specification<WorkflowTemplate> byTargetEntity(String targetEntity) {
        return (root, query, cb) -> cb.equal(
                root.get("targetEntity"),
                targetEntity
        );
    }

    public static Specification<WorkflowTemplate> byName(String name) {
        return (root, query, cb) -> cb.equal(
                root.get("name"),
                name
        );
    }

    public static Specification<WorkflowStep> byTemplate(WorkflowTemplate template) {
        return (root, query, cb) -> cb.equal(
                root.get("template"),
                template
        );
    }

    public static Specification<WorkflowStep> greaterOrder(Integer order) {
        return (root, query, cb) -> cb.greaterThan(
                root.get("stepOrder"),
                order
        );
    }

    public static Specification<WorkflowInstance> byEntityId(UUID entityId) {
        return (root, query, cb) -> cb.equal(
                root.get("entityId"),
                entityId
        );
    }

    public static Specification<WorkflowStepAction> byInstance(WorkflowInstance instance) {
        return (root, query, cb) -> cb.equal(
                root.get("instance"),
                instance
        );
    }

    public static Specification<WorkflowInstance> instanceByTargetEntity(String targetEntity) {
        return (root, query, cb) ->
                cb.equal(root.join("template").get("targetEntity"), targetEntity);
    }

    public static Specification<WorkflowInstance> byCurrentApprover(UUID approverId) {
        return (root, query, cb) ->
                cb.equal(root.get("currentApproverId"), approverId);
    }

    public static Specification<WorkflowInstance> byStatus(WorkflowStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<WorkflowInstance> userInvolved(UUID userId) {
        return (root, query, cb) -> {
            // Join với actions
            Join<Object, Object> actionJoin = root.join("actions", JoinType.LEFT);

            return cb.or(
                    cb.equal(root.get("currentApproverId"), userId),          // họ đang duyệt
                    cb.equal(actionJoin.get("actor").get("id"), userId)       // họ đã duyệt
            );
        };
    }


}
