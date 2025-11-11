package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.CompetencyTypeDto;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.CompetencyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parameters/competency-types")
@RequiredArgsConstructor
public class CompetencyTypeController {

    private final CompetencyTypeService competencyTypeService;

    @GetMapping
    public ResponseEntity<List<CompetencyTypeDto>> getAll() {
        return ResponseEntity.ok(competencyTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetencyTypeDto> getById(@PathVariable UUID id) throws ResourceNotFoundException {
        return ResponseEntity.ok(competencyTypeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CompetencyTypeDto dto) {
        ApiResponse<CompetencyTypeDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Created successfully",
                LocalDateTime.now(),
                competencyTypeService.create(dto)
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody CompetencyTypeDto dto) throws ResourceNotFoundException {
        ApiResponse<CompetencyTypeDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated successfully",
                LocalDateTime.now(),
                competencyTypeService.update(id, dto)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) throws ResourceNotFoundException {
        competencyTypeService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Deleted successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
}
