package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.consts.WorkflowActionType;
import com.example.ticket_helpdesk_backend.consts.WorkflowStatus;
import com.example.ticket_helpdesk_backend.context.WorkflowContextProvider;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.service.helper.ConditionEvaluator;
import com.example.ticket_helpdesk_backend.specification.WorkflowSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final WorkflowTemplateRepository templateRepo;
    private final WorkflowStepRepository stepRepo;
    private final WorkflowInstanceRepository instanceRepo;
    private final WorkflowStepActionRepository actionRepo;

    private final List<WorkflowContextProvider> contextProviders;
    private final UserRepository userRepository;

    private WorkflowContextProvider getProvider(String targetEntity) {
        return contextProviders.stream()
                .filter(p -> p.getTargetEntity().equalsIgnoreCase(targetEntity))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No WorkflowContextProvider for " + targetEntity));
    }

    private UUID resolveApprover(WorkflowStep step, Map<String, Object> ctx) {

        switch (step.getAssignedRole()) {

            case UserRole.ROLE_MANAGER:
                return (UUID) ctx.get("managerId");

            default:
                return null; // 🔥 skip
        }

    }

    /* ========= 1. START WORKFLOW ========= */
    @Transactional
    public WorkflowInstance startWorkflow(String targetEntity,
                                          String templateName,
                                          UUID entityId,
                                          UUID createdBy) {

        // Tìm template tương ứng với nghiệp vụ thông qua template name và target entity
        WorkflowTemplate template = templateRepo.findOne(
                Specification.where(WorkflowSpecifications.byTargetEntity(targetEntity))
                        .and(WorkflowSpecifications.byName(templateName))
        ).orElseThrow(() -> new RuntimeException("Template not found"));

        // Lấy context tương ứng với template
        Map<String, Object> ctx =
                getProvider(template.getTargetEntity()).buildContext(entityId);

        // Lấy các bước cần thực hiện
        List<WorkflowStep> steps = stepRepo.findAll(
                Specification.where(WorkflowSpecifications.byTemplate(template)),
                Sort.by("stepOrder").ascending()
        );

        // Lấy bước đầu tiên
        WorkflowStep firstStep = findFirstMatchStep(steps, ctx);

        if (firstStep == null) {
            throw new RuntimeException("No workflow steps matched condition");
        }

        UUID approverId = resolveApprover(firstStep, ctx);

        // Tạo instance
        WorkflowInstance instance = WorkflowInstance.builder()
                .template(template)
                .entityId(entityId)
                .createdBy(createdBy)
                .status(WorkflowStatus.IN_PROGRESS)
                .currentStepOrder(firstStep.getStepOrder())
                .currentApproverId(approverId)
                .build();

        if (approverId == null) {
            moveToNextStep(instance, steps, ctx);
            return instanceRepo.save(instance);
        }

        instanceRepo.save(instance);

        return instance;
    }

    private WorkflowStep findFirstMatchStep(List<WorkflowStep> steps,
                                            Map<String, Object> ctx) {
        return steps.stream()
                .filter(step -> ConditionEvaluator.evaluate(step.getConditionExpr(), ctx))
                .min(Comparator.comparing(WorkflowStep::getStepOrder))
                .orElse(null);
    }

    /* ========= 2. HANDLE ACTION (APPROVE / REJECT / COMMENT) ========= */
    @Transactional
    public void handleAction(UUID instanceId,
                             UUID actorId,
                             WorkflowActionType actionType,
                             String comment) {

        WorkflowInstance instance = instanceRepo.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Workflow instance not found"));

        WorkflowTemplate template = instance.getTemplate();

        Map<String, Object> ctx =
                getProvider(template.getTargetEntity()).buildContext(instance.getEntityId());

        if (instance.getCurrentStepOrder() == null) {
            throw new RuntimeException("No current step to act on");
        }

        List<WorkflowStep> steps = stepRepo.findAll(
                Specification.where(WorkflowSpecifications.byTemplate(template)),
                Sort.by("stepOrder").ascending()
        );

        WorkflowStep currentStep = steps.stream()
                .filter(s -> Objects.equals(s.getStepOrder(), instance.getCurrentStepOrder()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current step not found"));

        if (!actorId.equals(instance.getCurrentApproverId())) {
            throw new RuntimeException("Bạn không có quyền duyệt bước này");
        }

        User actor = userRepository.findById(actorId).orElseThrow(() -> new RuntimeException("Actor not found"));


        WorkflowStepAction action = WorkflowStepAction.builder()
                .instance(instance)
                .step(currentStep)
                .actor(actor)
                .action(actionType)
                .comment(comment)
                .build();

        actionRepo.save(action);

        if (actionType == WorkflowActionType.REJECT) {
            instance.setStatus(WorkflowStatus.REJECTED);
            instance.setCurrentStepOrder(null);
            instance.setCurrentApproverId(null);
        }
        else if (actionType == WorkflowActionType.APPROVE) {
            moveToNextStep(instance, steps, ctx);
        }

        instance.setUpdatedAt(LocalDateTime.now());
        instanceRepo.save(instance);
    }

    private void moveToNextStep(WorkflowInstance instance,
                                List<WorkflowStep> steps,
                                Map<String, Object> ctx) {

        Integer currentOrder = instance.getCurrentStepOrder();

        WorkflowStep nextStep = steps.stream()
                .filter(s -> s.getStepOrder() > currentOrder)
                .filter(s -> ConditionEvaluator.evaluate(s.getConditionExpr(), ctx))
                .min(Comparator.comparing(WorkflowStep::getStepOrder))
                .orElse(null);

        if (nextStep == null) {
            instance.setStatus(WorkflowStatus.APPROVED);
            instance.setCurrentStepOrder(null);
            instance.setCurrentApproverId(null);
        }
        else {
            instance.setCurrentStepOrder(nextStep.getStepOrder());

            UUID nextApprover = resolveApprover(nextStep, ctx);

            if (nextApprover == null) {
                moveToNextStep(instance, steps, ctx);
                return;
            }

            instance.setCurrentApproverId(nextApprover);
        }

    }
}
