package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.HolidayCalendarRequest;
import com.example.ticket_helpdesk_backend.dto.HolidayCalendarResponse;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.HolidayCalendarService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/holiday-calendar")
public class HolidayCalendarController {

    private final HolidayCalendarService holidayCalendarService;

    @GetMapping
    public ResponseEntity<?> getAllHoliday() {
        List<HolidayCalendarResponse> holidayCalendarResponseList = holidayCalendarService.getAll();
        ApiResponse<List<HolidayCalendarResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched all holiday calendar successfully.",
                LocalDateTime.now(),
                holidayCalendarResponseList
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHolidayById(@PathVariable UUID id) throws ResourceNotFoundException {
        HolidayCalendarResponse responseData = holidayCalendarService.getById(id);
        ApiResponse<HolidayCalendarResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched holiday calendar successfully.",
                LocalDateTime.now(),
                responseData
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createHoliday(@RequestBody HolidayCalendarRequest request) {
        HolidayCalendarResponse created = holidayCalendarService.create(request);
        ApiResponse<HolidayCalendarResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Created new holiday calendar successfully.",
                LocalDateTime.now(),
                created
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHoliday(@PathVariable UUID id, @RequestBody HolidayCalendarRequest request)
            throws ResourceNotFoundException {
        HolidayCalendarResponse updated = holidayCalendarService.update(id, request);
        ApiResponse<HolidayCalendarResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Updated holiday calendar successfully.",
                LocalDateTime.now(),
                updated
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable UUID id) throws ResourceNotFoundException {
        holidayCalendarService.delete(id);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.NO_CONTENT.value(),
                "Deleted holiday calendar successfully.",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }
}
