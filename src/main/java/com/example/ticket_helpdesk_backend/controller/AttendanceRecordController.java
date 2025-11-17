package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.AttendanceRecordService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;
    private final JwtUtil jwtUtil;

    @PostMapping("/{workScheduleId}")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> doAttendance(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID workScheduleId,
            @RequestBody AttendanceRecordRequest req
    ) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);

        AttendanceRecordResponse data =
                attendanceRecordService.doAttendance(userId, workScheduleId, req);

        ApiResponse<AttendanceRecordResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Attendance recorded successfully",
                LocalDateTime.now(),
                data,
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> getOne(
            @PathVariable UUID id
    ) throws ResourceNotFoundException {

        AttendanceRecordResponse data = attendanceRecordService.getById(id);

        ApiResponse<AttendanceRecordResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched attendance record successfully",
                LocalDateTime.now(),
                data
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getAttendanceByUser(
            @PathVariable UUID userId
    ) {
        var list = attendanceRecordService.getAttendanceByUser(userId);

        ApiResponse<?> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Fetched attendance history successfully",
                LocalDateTime.now(),
                list,
                Map.of("count", list.size())
        );

        return ResponseEntity.ok(response);
    }
}
