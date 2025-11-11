package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.EmployeeScheduleDto;
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

    /**
     * ✅ API cho UI Timesheet
     * Trả danh sách nhân viên + danh sách ca làm theo từng ngày
     */
    @GetMapping("/employee-view")
    public ResponseEntity<ApiResponse<List<EmployeeScheduleDto>>> getEmployeeScheduleView(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,

            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) UUID departmentId,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        System.out.println("employee-view");
        Page<EmployeeScheduleDto> pageData =
                workScheduleService.getEmployeeSchedules(page, size, keywords, departmentId, from, to);

        ApiResponse<List<EmployeeScheduleDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched employee schedules successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast(),
                        "from", from,
                        "to", to
                )
        );

        return ResponseEntity.ok(response);
    }

    // ==============================
    // CRUD cũ vẫn giữ nguyên
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> getOne(@PathVariable UUID id)
            throws ResourceNotFoundException {
        WorkScheduleResponse data = workScheduleService.getById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Fetched work schedule successfully",
                        LocalDateTime.now(), data)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> create(@RequestBody WorkScheduleRequest req)
            throws ResourceNotFoundException {
        WorkScheduleResponse saved = workScheduleService.create(req);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.CREATED.value(), "Work schedule created successfully",
                        LocalDateTime.now(), saved)
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> update(@RequestBody WorkScheduleRequest req)
            throws ResourceNotFoundException {
        WorkScheduleResponse updated = workScheduleService.update(req);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Work schedule updated successfully",
                        LocalDateTime.now(), updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> delete(@PathVariable UUID id)
            throws ResourceNotFoundException {
        WorkScheduleResponse deleted = workScheduleService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Work schedule deleted successfully",
                        LocalDateTime.now(), deleted)
        );
    }
}

