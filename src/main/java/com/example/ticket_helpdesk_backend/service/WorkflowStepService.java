package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.WorkflowStepRequest;
import com.example.ticket_helpdesk_backend.dto.WorkflowStepResponse;
import com.example.ticket_helpdesk_backend.entity.WorkflowStep;
import com.example.ticket_helpdesk_backend.entity.WorkflowTemplate;
import com.example.ticket_helpdesk_backend.repository.WorkflowStepRepository;
import com.example.ticket_helpdesk_backend.repository.WorkflowTemplateRepository;
import com.example.ticket_helpdesk_backend.specification.WorkflowSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowStepService {

    private final WorkflowStepRepository stepRepo;
    private final WorkflowTemplateRepository templateRepo;
    private final ModelMapper modelMapper;

    public List<WorkflowStepResponse> getSteps(UUID templateId) {
        WorkflowTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        return stepRepo.findAll(
                Specification.where(WorkflowSpecifications.byTemplate(template)),
                Sort.by("stepOrder").ascending()
        ).stream().map((element) -> modelMapper.map(element, WorkflowStepResponse.class)).collect(Collectors.toList());
    }

    @Transactional
    public WorkflowStepResponse createStep(WorkflowStepRequest req) {
        WorkflowTemplate template = templateRepo.findById(req.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        WorkflowStep step = WorkflowStep.builder()
                .id(UUID.randomUUID())
                .template(template)
                .name(req.getName())
                .stepOrder(req.getStepOrder())
                .stepType(req.getStepType())
                .assignedRole(req.getAssignedRole())
                .conditionExpr(req.getConditionExpr())
                .createdAt(LocalDateTime.now())
                .build();

        stepRepo.save(step);
        return modelMapper.map(step, WorkflowStepResponse.class);
    }

    @Transactional
    public WorkflowStepResponse updateStep(UUID stepId, WorkflowStepRequest req) {

        WorkflowStep step = stepRepo.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found"));

        if (req.getName() != null)
            step.setName(req.getName());

        if (req.getStepType() != null)
            step.setStepType(req.getStepType());

        if (req.getAssignedRole() != null)
            step.setAssignedRole(req.getAssignedRole());

        if (req.getConditionExpr() != null)
            step.setConditionExpr(req.getConditionExpr());

        if (req.getStepOrder() != null && req.getStepOrder() > 0) {
            step.setStepOrder(req.getStepOrder());
        }

        stepRepo.save(step);

        return modelMapper.map(step, WorkflowStepResponse.class);
    }

    @Transactional
    public void deleteStep(UUID stepId) {
        if (!stepRepo.existsById(stepId)) {
            throw new RuntimeException("Step not found");
        }
        stepRepo.deleteById(stepId);
    }
}

