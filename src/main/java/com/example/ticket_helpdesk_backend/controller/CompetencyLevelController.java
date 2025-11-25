package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.CompetencyLevelDto;
import com.example.ticket_helpdesk_backend.dto.CompetencyTypeDto;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.CompetencyLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parameters/competency-levels")
@RequiredArgsConstructor
public class CompetencyLevelController {

    private final CompetencyLevelService competencyLevelService;

    // Lấy toàn bộ level (nếu cần)
    @GetMapping
    public ResponseEntity<List<CompetencyLevelDto>> getAll() {
        return ResponseEntity.ok(competencyLevelService.getAll());
    }

    // Lấy các level thuộc 1 type
    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<CompetencyLevelDto>> getByType(@PathVariable UUID typeId) {
        return ResponseEntity.ok(competencyLevelService.getByType(typeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetencyLevelDto> getById(@PathVariable UUID id) throws ResourceNotFoundException {
        return ResponseEntity.ok(competencyLevelService.getById(id));
    }

    @PostMapping("/type/{typeId}")
    public ResponseEntity<?> create(@PathVariable UUID typeId, @RequestBody CompetencyLevelDto dto) throws ResourceNotFoundException {
        ApiResponse<CompetencyLevelDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Created level successfully",
                LocalDateTime.now(),
                competencyLevelService.create(typeId, dto)
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CompetencyLevelDto dto) throws ResourceNotFoundException {
        ApiResponse<CompetencyLevelDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated level successfully",
                LocalDateTime.now(),
                competencyLevelService.update(id, dto)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) throws ResourceNotFoundException {
        competencyLevelService.delete(id);
        ApiResponse<CompetencyLevelDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Deleted level successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
}

