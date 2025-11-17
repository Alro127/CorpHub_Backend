package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.AttendanceRecordRequest;
import com.example.ticket_helpdesk_backend.dto.AttendanceRecordResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.AttendanceRecord;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import com.example.ticket_helpdesk_backend.exception.AuthException;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AttendanceRecordRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.repository.WorkScheduleRepository;
import com.example.ticket_helpdesk_backend.specification.AttendanceRecordSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.example.ticket_helpdesk_backend.specification.AttendanceRecordSpecifications.hasWorkScheduleId;

@Service
@RequiredArgsConstructor
public class AttendanceRecordService {
    private final UserRepository userRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final WorkScheduleService workScheduleService;

    public AttendanceRecordResponse toResponse(AttendanceRecord r) {
        AttendanceRecordResponse dto = new AttendanceRecordResponse();

        dto.setId(r.getId());
        dto.setWorkSchedule(workScheduleService.toResponse(r.getWorkSchedule()));

        dto.setCheckInTime(r.getCheckInTime());
        dto.setCheckOutTime(r.getCheckOutTime());
        dto.setCheckInLat(r.getCheckInLat());
        dto.setCheckInLng(r.getCheckInLng());
        dto.setCheckOutLat(r.getCheckOutLat());
        dto.setCheckOutLng(r.getCheckOutLng());
        dto.setCheckInIp(r.getCheckInIp());
        dto.setCheckOutIp(r.getCheckOutIp());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        return dto;
    }

    @Transactional
    public AttendanceRecordResponse doAttendance(UUID userId, UUID workScheduleId, AttendanceRecordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new RuntimeException("Work schedule not found"));

        if (schedule.getUser().getId() != user.getId()) {
            throw new AuthException("Work schedule does not belong to user");
        }

        Specification<AttendanceRecord> spec = Specification.where(hasWorkScheduleId(workScheduleId));

        AttendanceRecord record = attendanceRecordRepository
                .findOne(spec).orElse(null);

        if (record == null) {
            // -----------------------------
            // CASE 1: CHƯA CHECK-IN → TẠO MỚI
            // -----------------------------
            record = new AttendanceRecord();
            record.setUser(user);
            record.setWorkSchedule(schedule);

            record.setCheckInTime(now);
            record.setCheckInLat(request.getLat());
            record.setCheckInLng(request.getLng());
            record.setCheckInIp(request.getIp());

            record.setCreatedAt(now);
            record.setUpdatedAt(now);

            schedule.setStatus(WorkScheduleStatus.IN_PROGRESS);
            workScheduleRepository.save(schedule);

            record = attendanceRecordRepository.save(record);
            return toResponse(record);
        }
        // -----------------------------
        // CASE 2: ĐÃ CHECK-IN → CHECK-OUT
        // -----------------------------
        if (record.getCheckOutTime() != null) {
            throw new RuntimeException("User already checked out for this shift");
        }

        record.setCheckOutTime(now);
        record.setCheckOutLat(request.getLat());
        record.setCheckOutLng(request.getLng());
        record.setCheckOutIp(request.getIp());
        record.setUpdatedAt(now);

        schedule.setStatus(WorkScheduleStatus.COMPLETED);
        workScheduleRepository.save(schedule);

        record = attendanceRecordRepository.save(record);
        return toResponse(record);
    }

    public AttendanceRecordResponse getById(UUID id) throws ResourceNotFoundException {
        AttendanceRecord record = attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
        return toResponse(record);
    }

    public List<AttendanceRecordResponse> getAttendanceByUser(UUID userId) {
        List<AttendanceRecord> records =
                attendanceRecordRepository.findAll(
                        Specification.where(AttendanceRecordSpecifications.hasUserId(userId))
                );

        return records.stream()
                .map(this::toResponse)
                .toList();
    }

}
