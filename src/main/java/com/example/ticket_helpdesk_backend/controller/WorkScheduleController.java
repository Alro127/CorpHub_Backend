package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.WorkScheduleRequest;
import com.example.ticket_helpdesk_backend.dto.WorkScheduleResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.WorkScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID shiftId,
            @RequestParam(required = false) WorkScheduleStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "workDate") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction
    ) {
        Page<WorkScheduleResponse> pageData = workScheduleService.getAll(
                page, size, keywords, userId, shiftId, status, fromDate, toDate, sortBy, direction
        );

        ApiResponse<List<WorkScheduleResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched work schedules successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> getOne(@PathVariable UUID id)
            throws ResourceNotFoundException {
        WorkScheduleResponse data = workScheduleService.getById(id);
        ApiResponse<WorkScheduleResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Fetched work schedule successfully", LocalDateTime.now(), data
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> create(@RequestBody WorkScheduleRequest req)
            throws ResourceNotFoundException {
        WorkScheduleResponse saved = workScheduleService.create(req);
        ApiResponse<WorkScheduleResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(), "Work schedule created successfully", LocalDateTime.now(), saved
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> update(@RequestBody WorkScheduleRequest req)
            throws ResourceNotFoundException {
        WorkScheduleResponse updated = workScheduleService.update(req);
        ApiResponse<WorkScheduleResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Work schedule updated successfully", LocalDateTime.now(), updated
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> delete(@PathVariable UUID id)
            throws ResourceNotFoundException {
        WorkScheduleResponse deleted = workScheduleService.delete(id);
        ApiResponse<WorkScheduleResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(), "Work schedule deleted successfully", LocalDateTime.now(), deleted
        );
        return ResponseEntity.ok(response);
    }
}
