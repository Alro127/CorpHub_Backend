package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.service.InternalWorkHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal-work-history")
@RequiredArgsConstructor
public class InternalWorkHistoryController {

    private final InternalWorkHistoryService service;

    /* -------------------------------------
     * CREATE INTERNAL WORK HISTORY
     * ------------------------------------- */
    @PostMapping
    public ApiResponse<?> create(@RequestBody InternalWorkHistory dto) {

        var created = service.createHistory(dto);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Create internal work history successfully",
                LocalDateTime.now(),
                created
        );
    }

    /* -------------------------------------
     * GET HISTORY BY EMPLOYEE
     * ------------------------------------- */
    @GetMapping("/employee/{employeeId}")
    public ApiResponse<?> getByEmployee(@PathVariable UUID employeeId) {

        var histories = service.getByEmployee(employeeId);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Get internal work history by employee successfully",
                LocalDateTime.now(),
                histories
        );
    }
}

