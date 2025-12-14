package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.WorkScheduleExportService;
import com.example.ticket_helpdesk_backend.service.WorkScheduleService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
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
    private final WorkScheduleExportService exportService;
    private final JwtUtil jwtUtil;

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

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getTodayShiftsForUser(
            @RequestHeader("Authorization") String authHeader
    ) throws ResourceNotFoundException {

        LocalDate today = LocalDate.now();

        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        List<WorkScheduleResponse> schedules =
                workScheduleService.getShiftsForUserOnDate(userId, today);

        ApiResponse<List<WorkScheduleResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched today's work schedules successfully",
                LocalDateTime.now(),
                schedules,
                Map.of(
                        "userId", userId,
                        "date", today
                )
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/auto-assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> autoAssign(
            @RequestBody AutoAssignRequest req
    ) throws ResourceNotFoundException {

        List<WorkScheduleResponse> results = workScheduleService.autoAssignShifts(req);

        ApiResponse<List<WorkScheduleResponse>> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Auto-assigned work schedules successfully",
                LocalDateTime.now(),
                results,
                Map.of(
                        "shiftId", req.getShiftId(),
                        "startDate", req.getStartDate(),
                        "endDate", req.getEndDate(),
                        "replaceExisting", req.getReplaceExisting(),
                        "respectAbsenceRequests", req.getRespectAbsenceRequests(),
                        "includeWeekend", req.getIncludeWeekend(),
                        "createdCount", results.size()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> update(@PathVariable UUID id, @RequestBody WorkScheduleRequest req)
            throws ResourceNotFoundException {
        WorkScheduleResponse updated = workScheduleService.update(id, req);
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

    @PostMapping("/export")
    public ResponseEntity<InputStreamResource> export(@RequestBody @Valid WorkScheduleExportRequest request) {

        ExportFileResult result = exportService.export(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"")
                .contentType(result.getMediaType())
                .body(new InputStreamResource(result.getStream()));
    }
}

