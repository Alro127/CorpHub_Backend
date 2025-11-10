package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.AbsenceTypeRequest;
import com.example.ticket_helpdesk_backend.dto.AbsenceTypeResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AbsenceTypeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/absence/type")
@AllArgsConstructor
public class AbsenceTypeController {

    private final AbsenceTypeService absenceTypeService;

    @GetMapping
    public ResponseEntity<?> getAllAbsenceTypes() {
        ApiResponse<List<AbsenceTypeResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched absence types successfully",
                LocalDateTime.now(),
                absenceTypeService.getAll()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) throws ResourceNotFoundException {
        AbsenceTypeResponse absenceType = absenceTypeService.getById(id);
        ApiResponse<AbsenceTypeResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched absence type successfully",
                LocalDateTime.now(),
                absenceType
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createAbsenceType(@RequestBody AbsenceTypeRequest request) {
        AbsenceTypeResponse created = absenceTypeService.create(request);
        ApiResponse<AbsenceTypeResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Created absence type successfully",
                LocalDateTime.now(),
                created
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAbsenceType(@PathVariable UUID id,
                                             @RequestBody AbsenceTypeRequest request) throws ResourceNotFoundException {
        AbsenceTypeResponse updated = absenceTypeService.update(id, request);
        ApiResponse<AbsenceTypeResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated absence type successfully",
                LocalDateTime.now(),
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbsenceType(@PathVariable UUID id) throws ResourceNotFoundException {
        absenceTypeService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Deleted absence type successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
