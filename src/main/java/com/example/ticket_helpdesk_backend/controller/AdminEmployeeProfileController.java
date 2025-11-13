package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyResponse;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AdminEmployeeProfileService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/admin/employee")
@RequiredArgsConstructor
public class AdminEmployeeProfileController {

    private final AdminEmployeeProfileService adminEmployeeProfileService;
    private final JwtUtil jwtUtil;

    // 🔹 Duyệt competency trong hồ sơ nhân viên
    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('HR')")
    @PutMapping("/competencies/approve/{competencyId}")
    public ResponseEntity<ApiResponse<EmployeeCompetencyResponse>> approveCompetency(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID competencyId
    ) throws ResourceNotFoundException {
        String token = authHeader.substring(7);

        EmployeeCompetency updated = adminEmployeeProfileService.approve(competencyId, token);
        EmployeeCompetencyResponse dto = EmployeeCompetencyResponse.fromEntity(updated);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Competency approved successfully",
                        LocalDateTime.now(),
                        dto
                )
        );
    }

    // 🔹 Từ chối competency trong hồ sơ nhân viên
    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('HR')")
    @PutMapping("/competencies/reject/{competencyId}")
    public ResponseEntity<ApiResponse<EmployeeCompetencyResponse>> rejectCompetency(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID competencyId,
            @RequestParam(required = false) String reason
    ) throws ResourceNotFoundException {
        String token = authHeader.substring(7);

        EmployeeCompetency updated = adminEmployeeProfileService.reject(competencyId, reason, token);
        EmployeeCompetencyResponse dto = EmployeeCompetencyResponse.fromEntity(updated);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Competency rejected successfully",
                        LocalDateTime.now(),
                        dto
                )
        );
    }

    // 🔹 Lấy tất cả competency đang chờ duyệt
    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('HR')")
    @GetMapping("/competencies/pending")
    public ResponseEntity<?> getPendingCompetencies() {

        List<EmployeeCompetencyResponse> dto = adminEmployeeProfileService.getPendingCompetencies();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Pending competencies fetched successfully",
                        LocalDateTime.now(),
                        dto
                )
        );
    }

}

