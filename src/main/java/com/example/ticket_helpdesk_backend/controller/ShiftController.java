package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.ShiftDto;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftDto>>> getAllShifts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTo
    ) {
        Page<ShiftDto> pageData = shiftService.getAll(page, size, keywords, startFrom, endTo);

        ApiResponse<List<ShiftDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched all shifts successfully",
                LocalDateTime.now(),
                pageData.getContent(),
                Map.of(
                        "page", pageData.getNumber(),
                        "size", pageData.getSize(),
                        "totalElements", pageData.getTotalElements(),
                        "totalPages", pageData.getTotalPages(),
                        "last", pageData.isLast()
                )
        );
        return ResponseEntity.ok(response);
    }


    /**
     * Lấy chi tiết 1 ca làm
     */
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftDto>> getShift(@PathVariable UUID id)
            throws ResourceNotFoundException {
        ShiftDto shift = shiftService.getById(id);
        ApiResponse<ShiftDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched shift successfully",
                LocalDateTime.now(),
                shift
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Tạo mới ca làm
     */
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<ShiftDto>> createShift(@RequestBody ShiftDto shiftDto)
            throws ResourceNotFoundException {
        ShiftDto saved = shiftService.create(shiftDto);
        ApiResponse<ShiftDto> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Shift created successfully",
                LocalDateTime.now(),
                saved
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật ca làm
     */
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftDto>> updateShift(@PathVariable UUID id, @RequestBody ShiftDto shiftDto)
            throws ResourceNotFoundException {
        ShiftDto updated = shiftService.update(id, shiftDto);
        ApiResponse<ShiftDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Shift updated successfully",
                LocalDateTime.now(),
                updated
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa ca làm
     */
    @PreAuthorize("@securityService.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftDto>> deleteShift(@PathVariable UUID id)
            throws ResourceNotFoundException {
        ShiftDto deleted = shiftService.delete(id);
        ApiResponse<ShiftDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Shift deleted successfully",
                LocalDateTime.now(),
                deleted
        );
        return ResponseEntity.ok(response);
    }
}
