package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.WorkflowTemplateRequest;
import com.example.ticket_helpdesk_backend.dto.WorkflowTemplateResponse;
import com.example.ticket_helpdesk_backend.entity.WorkflowTemplate;
import com.example.ticket_helpdesk_backend.repository.WorkflowTemplateRepository;
import com.example.ticket_helpdesk_backend.specification.WorkflowSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class WorkflowTemplateService {

    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final ModelMapper modelMapper;

    public List<WorkflowTemplateResponse> getAll(String keyword) {
        Specification<WorkflowTemplate> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(WorkflowSpecifications.byName(keyword)
                    .or(WorkflowSpecifications.byTargetEntity(keyword)));
        }

        return workflowTemplateRepository.findAll(spec, Sort.by("createdAt").descending())
                .stream().map((element) -> modelMapper.map(element, WorkflowTemplateResponse.class)).collect(Collectors.toList());
    }

    public WorkflowTemplateResponse getById(UUID id) {
        return workflowTemplateRepository.findById(id).map((element) -> modelMapper.map(element, WorkflowTemplateResponse.class)).orElse(null);
    }

    public WorkflowTemplateResponse create(WorkflowTemplateRequest req) {
        WorkflowTemplate entity = WorkflowTemplate.builder()
                .name(req.getName())
                .targetEntity(req.getTargetEntity())
                .build();
        workflowTemplateRepository.save(entity);
        return modelMapper.map(entity, WorkflowTemplateResponse.class);
    }

    public WorkflowTemplateResponse update(UUID id, WorkflowTemplateRequest req) {
        WorkflowTemplate entity = workflowTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow Template not found"));

        modelMapper.map(req, entity);
        workflowTemplateRepository.save(entity);

        return modelMapper.map(entity, WorkflowTemplateResponse.class);
    }

    public void delete(UUID id) {
        if (!workflowTemplateRepository.existsById(id)) {
            throw new RuntimeException("Workflow Template not found");
        }
        workflowTemplateRepository.deleteById(id);
    }
}
