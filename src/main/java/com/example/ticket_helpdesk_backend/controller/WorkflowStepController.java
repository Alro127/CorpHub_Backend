package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.WorkflowStepRequest;
import com.example.ticket_helpdesk_backend.dto.WorkflowStepResponse;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.service.WorkflowStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/steps")
@RequiredArgsConstructor
@PreAuthorize("@securityService.hasRole('ADMIN')")
public class WorkflowStepController {

    private final WorkflowStepService service;

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<List<WorkflowStepResponse>>> listSteps(
            @PathVariable UUID templateId
    ) {
        List<WorkflowStepResponse> data = service.getSteps(templateId);

        ApiResponse<List<WorkflowStepResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Retrieved workflow steps successfully",
                LocalDateTime.now(),
                data,
                Map.of("templateId", templateId, "count", data.size())
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> createStep(
            @RequestBody WorkflowStepRequest req
    ) {
        WorkflowStepResponse data = service.createStep(req);

        ApiResponse<WorkflowStepResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Workflow step created successfully",
                LocalDateTime.now(),
                data,
                Map.of("templateId", req.getTemplateId())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowStepResponse>> updateStep(
            @PathVariable UUID id,
            @RequestBody WorkflowStepRequest req
    ) {
        WorkflowStepResponse data = service.updateStep(id, req);

        ApiResponse<WorkflowStepResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Workflow step updated successfully",
                LocalDateTime.now(),
                data,
                Map.of("updatedId", id)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStep(@PathVariable UUID id) {

        service.deleteStep(id);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Workflow step deleted successfully",
                LocalDateTime.now(),
                "OK",
                Map.of("deletedId", id)
        );

        return ResponseEntity.ok(response);
    }
}

