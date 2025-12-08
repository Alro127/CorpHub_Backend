package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.service.InternalWorkHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal-work-history")
@RequiredArgsConstructor
public class InternalWorkHistoryController {

    private final InternalWorkHistoryService service;

    @PostMapping
    public ResponseEntity<InternalWorkHistory> create(@RequestBody InternalWorkHistory dto) {
        return ResponseEntity.ok(service.createHistory(dto));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<InternalWorkHistory>> getByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.getByEmployee(employeeId));
    }
}
