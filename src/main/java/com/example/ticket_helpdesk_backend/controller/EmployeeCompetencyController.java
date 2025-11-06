package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyDto;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import com.example.ticket_helpdesk_backend.service.EmployeeCompetencyService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee/competency")
@RequiredArgsConstructor
public class EmployeeCompetencyController {

    private final EmployeeCompetencyService competencyService;

    @Autowired
    JwtUtil jwtUtil;

    // 🔹 Lấy danh sách competency của 1 nhân viên
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EmployeeCompetencyDto>>> getMyCompetencies(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);

        UUID employeeId = jwtUtil.getUserId(token);

        List<EmployeeCompetencyDto> competencyDtos = competencyService
                .getByEmployeeId(employeeId)
                .stream()
                .map(EmployeeCompetencyDto::fromEntity)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Load competencies successfully",
                        LocalDateTime.now(),
                        competencyDtos
                )
        );
    }

    // 🔹 Lấy danh sách competency của 1 nhân viên
    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<List<EmployeeCompetencyDto>>> getByEmployee(@PathVariable UUID employeeId) {
        List<EmployeeCompetencyDto> competencyDtos = competencyService
                .getByEmployeeId(employeeId)
                .stream()
                .map(EmployeeCompetencyDto::fromEntity)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Load competencies successfully",
                        LocalDateTime.now(),
                        competencyDtos
                )
        );
    }

    // 🔹 Thêm mới competency
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeCompetencyDto>> create(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody EmployeeCompetencyDto request
    ) {

        String token = authHeader.substring(7);

        EmployeeCompetency saved = competencyService.create(request, token);
        EmployeeCompetencyDto dto = EmployeeCompetencyDto.fromEntity(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Create competency successfully",
                        LocalDateTime.now(),
                        dto
                )
        );
    }

    // 🔹 Cập nhật competency
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeCompetencyDto>> update(
            @PathVariable UUID id,
            @RequestBody EmployeeCompetencyDto request
    ) {
        EmployeeCompetency updated = competencyService.update(id, request);
        EmployeeCompetencyDto dto = EmployeeCompetencyDto.fromEntity(updated);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Update competency successfully",
                        LocalDateTime.now(),
                        dto
                )
        );
    }

    // 🔹 Xóa competency
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        competencyService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete competency successfully",
                        LocalDateTime.now(),
                        null
                )
        );
    }
}
