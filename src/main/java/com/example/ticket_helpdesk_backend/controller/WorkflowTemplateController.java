package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.WorkflowTemplateRequest;
import com.example.ticket_helpdesk_backend.dto.WorkflowTemplateResponse;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.service.WorkflowTemplateService;
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
@RequestMapping("/api/workflow/templates")
@RequiredArgsConstructor
@PreAuthorize("@securityService.hasRole('ADMIN')")
public class WorkflowTemplateController {

    private final WorkflowTemplateService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkflowTemplateResponse>>> listTemplates(
            @RequestParam(required = false) String keyword
    ) {

        List<WorkflowTemplateResponse> data = service.getAll(keyword);

        ApiResponse<List<WorkflowTemplateResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Retrieved workflow templates successfully",
                LocalDateTime.now(),
                data,
                Map.of("count", data.size())
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> getTemplate(@PathVariable UUID id) {

        WorkflowTemplateResponse data = service.getById(id);

        ApiResponse<WorkflowTemplateResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Retrieved workflow template successfully",
                LocalDateTime.now(),
                data,
                Map.of("templateId", id)
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> create(@RequestBody WorkflowTemplateRequest req) {

        WorkflowTemplateResponse data = service.create(req);

        ApiResponse<WorkflowTemplateResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Created workflow template successfully",
                LocalDateTime.now(),
                data,
                Map.of("createdBy", "ADMIN")
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkflowTemplateResponse>> update(
            @PathVariable UUID id,
            @RequestBody WorkflowTemplateRequest req
    ) {

        WorkflowTemplateResponse data = service.update(id, req);

        ApiResponse<WorkflowTemplateResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated workflow template successfully",
                LocalDateTime.now(),
                data,
                Map.of("updatedId", id)
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {

        service.delete(id);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Deleted workflow template successfully",
                LocalDateTime.now(),
                "OK",
                Map.of("deletedId", id)
        );

        return ResponseEntity.ok(response);
    }
}
