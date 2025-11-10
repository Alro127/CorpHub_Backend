package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.AbsenceBalanceResponse;
import com.example.ticket_helpdesk_backend.service.AbsenceBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/absence/balance")
public class AbsenceBalanceController {

    private final AbsenceBalanceService absenceBalanceService;

    /**
     * ✅ Lấy danh sách AbsenceBalance có thể lọc theo userId, absenceTypeId, year
     * Ví dụ: /api/absence/balances?userId=...&year=2025
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AbsenceBalanceResponse>>> getAll(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID absenceTypeId,
            @RequestParam(required = false) Integer year
    ) {
        var data = absenceBalanceService.getAll(userId, absenceTypeId, year);
        var response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched absence balances successfully",
                LocalDateTime.now(),
                data
        );
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ HR hoặc Admin chủ động tạo absence balance cho 1 năm cụ thể
     * Ví dụ: POST /api/absence/balances/generate?year=2025
     */
    @PreAuthorize("@securityService.hasRole('ADMIN') or @securityService.hasRole('HR')")
    @PostMapping("/generate")
    public ResponseEntity<?> generateBalances(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = (year != null) ? year : LocalDateTime.now().getYear();
        absenceBalanceService.generateForYear(targetYear);

        var response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Generated absence balances for year " + targetYear,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
