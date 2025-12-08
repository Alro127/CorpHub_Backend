package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.specification.AbsenceRequestSpecifications;
import com.example.ticket_helpdesk_backend.specification.AttendanceRecordSpecifications;
import com.example.ticket_helpdesk_backend.specification.UserSpecifications;
import com.example.ticket_helpdesk_backend.specification.WorkScheduleSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StringRedisTemplate redis;

    public WorkScheduleResponse getById(UUID id) throws ResourceNotFoundException {
        WorkSchedule entity = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule not found: " + id));
        return toResponse(entity);
    }

    @Transactional
    public WorkScheduleResponse create(WorkScheduleRequest req) throws ResourceNotFoundException {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));
        Shift shift = shiftRepository.findById(req.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + req.getShiftId()));

        WorkSchedule entity = new WorkSchedule();
        entity.setUser(user);
        entity.setShift(shift);
        entity.setWorkDate(req.getWorkDate());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : WorkScheduleStatus.SCHEDULED);

        WorkSchedule saved = workScheduleRepository.save(entity);

        scheduleEndEvent(saved);
        return toResponse(saved);
    }

    @Transactional
    public WorkScheduleResponse update(UUID id, WorkScheduleRequest req) throws ResourceNotFoundException {
        if (id == null) throw new ResourceNotFoundException("WorkSchedule id is required");

        WorkSchedule existing = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule not found: " + id));

        if (req.getUserId() != null && (existing.getUser() == null || !existing.getUser().getId().equals(req.getUserId()))) {
            User user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));
            existing.setUser(user);
        }
        if (req.getShiftId() != null && (existing.getShift() == null || !existing.getShift().getId().equals(req.getShiftId()))) {
            Shift shift = shiftRepository.findById(req.getShiftId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + req.getShiftId()));
            existing.setShift(shift);
        }
        if (req.getWorkDate() != null) existing.setWorkDate(req.getWorkDate());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());

        WorkSchedule updated = workScheduleRepository.save(existing);
        return toResponse(updated);
    }

    @Transactional
    public void updateWorkSchedulesFromAbsenceRequest(AbsenceRequest absenceRequest) {
        Specification<WorkSchedule> spec = Specification.where(WorkScheduleSpecifications.hasUserId(absenceRequest.getUser().getId()))
                .and(WorkScheduleSpecifications.workDateFrom(absenceRequest.getStartDate()))
                .and(WorkScheduleSpecifications.workDateTo(absenceRequest.getEndDate()))
                .and(WorkScheduleSpecifications.hasStatus(WorkScheduleStatus.SCHEDULED));

        List<WorkSchedule> workSchedules = workScheduleRepository.findAll(spec);
        for (WorkSchedule workSchedule : workSchedules) {
            workSchedule.setStatus(WorkScheduleStatus.ABSENCE);
            workScheduleRepository.save(workSchedule);
        }
    }

    @Transactional
    public WorkScheduleResponse delete(UUID id) throws ResourceNotFoundException {
        WorkSchedule entity = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule not found: " + id));
        workScheduleRepository.delete(entity);
        return toResponse(entity);
    }

    public WorkScheduleResponse toResponse(WorkSchedule entity) {
        WorkScheduleResponse resp = new WorkScheduleResponse();
        resp.setId(entity.getId());
        resp.setWorkDate(entity.getWorkDate());
        resp.setStatus(entity.getStatus());

        // map nested
        resp.setUser(UserDto.toUserDto(entity.getUser()));
        resp.setShift(modelMapper.map(entity.getShift(), ShiftDto.class));

        AttendanceRecord ar = entity.getAttendanceRecord();
        if (ar != null) {
            resp.setCheckInTime(ar.getCheckInTime());
            resp.setCheckOutTime(ar.getCheckOutTime());
        } else {
            resp.setCheckInTime(null);
            resp.setCheckOutTime(null);
        }

        return resp;
    }


    public Page<EmployeeScheduleDto> getEmployeeSchedules(
            int page,
            int size,
            String keywords,
            UUID departmentId,
            LocalDate from,
            LocalDate to
    ) {
        Sort sort = Sort.by(
                Sort.Order.asc("employeeProfile.fullName").ignoreCase()
        );

        Pageable pageable = PageRequest.of(page, size, sort);

        // 1️⃣ Lấy danh sách User theo page
        Page<User> userPage = userRepository.findAll(
                Specification
                        .where(UserSpecifications.withEmployeeJoins())
                        .and(UserSpecifications.search(keywords))
                        .and(UserSpecifications.belongsToDepartment(departmentId)),
                pageable
        );

        List<User> users = userPage.getContent();

        if (users.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, userPage.getTotalElements());
        }

        // 2️⃣ Lấy tất cả ID user
        List<UUID> userIds = users.stream()
                .map(User::getId)
                .toList();

        // 3️⃣ Lấy tất cả WorkSchedule theo user + thời gian
        List<WorkSchedule> schedules = workScheduleRepository.findAll(
                Specification
                        .where(WorkScheduleSpecifications.hasUserIdIn(userIds))
                        .and(WorkScheduleSpecifications.workDateFrom(from))
                        .and(WorkScheduleSpecifications.workDateTo(to))
        );

        // 4️⃣ Gom nhóm WS theo user
        Map<UUID, List<WorkSchedule>> groupedSchedules = schedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getUser().getId()));

        // 5️⃣ Lấy tất cả WorkScheduleIds → để query AttendanceRecord 1 lần
        List<UUID> wsIds = schedules.stream().map(WorkSchedule::getId).toList();

        // 6️⃣ Lấy AttendanceRecord theo tất cả WS
        List<AttendanceRecord> attendanceList =
                wsIds.isEmpty()
                        ? Collections.emptyList()
                        : attendanceRecordRepository.findAll(
                        Specification.where(AttendanceRecordSpecifications.workScheduleIdIn(wsIds))
                );

        // 7️⃣ Map WorkScheduleId → AttendanceRecord
        Map<UUID, AttendanceRecord> attendanceMap = attendanceList.stream()
                .collect(Collectors.toMap(ar -> ar.getWorkSchedule().getId(), ar -> ar));

        // 8️⃣ Tạo DTO trả về
        List<EmployeeScheduleDto> results = new ArrayList<>();

        for (User u : users) {

            List<WorkSchedule> wsList = groupedSchedules.getOrDefault(u.getId(), Collections.emptyList());

            List<EmployeeShiftDto> shiftDtos = wsList.stream()
                    .map(ws -> {
                        Shift s = ws.getShift();
                        AttendanceRecord ar = attendanceMap.get(ws.getId());

                        return new EmployeeShiftDto(
                                ws.getId(),
                                ws.getWorkDate(),
                                s.getId(),
                                s.getName(),
                                s.getStartTime(),
                                s.getEndTime(),
                                null,                 // notes nếu có
                                ws.getStatus(),
                                ar != null ? ar.getCheckInTime() : null,
                                ar != null ? ar.getCheckOutTime() : null
                        );
                    })
                    .sorted(Comparator.comparing(EmployeeShiftDto::getWorkDate))
                    .toList();

            results.add(
                    new EmployeeScheduleDto(
                            u.getId(),
                            u.getEmployeeProfile().getFullName(),
                            u.getEmployeeProfile().getDepartment().getName(),
                            shiftDtos
                    )
            );
        }

        return new PageImpl<>(results, pageable, userPage.getTotalElements());
    }


    @Transactional
    public List<WorkScheduleResponse> autoAssignShifts(AutoAssignRequest req) throws ResourceNotFoundException {
        Shift shift = shiftRepository.findById(req.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + req.getShiftId()));

        List<User> users = new ArrayList<>();

        if (req.getUserIds() != null && !req.getUserIds().isEmpty()) {
            users.addAll(userRepository.findAllById(req.getUserIds()));
        }

        if (req.getDepartmentIds() != null && !req.getDepartmentIds().isEmpty()) {
            List<User> deptUsers = userRepository.findAll(
                    Specification.where(UserSpecifications.belongsToDepartments(req.getDepartmentIds()))
            );
            users.addAll(deptUsers);
        }

        // Loại trùng và bỏ user không hoạt động (nếu có trạng thái active)
        users = users.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found for the given criteria");
        }

        List<WorkScheduleResponse> createdSchedules = new ArrayList<>();

        // Lặp qua từng ngày trong khoảng thời gian
        LocalDate current = req.getStartDate();
        while (!current.isAfter(req.getEndDate())) {

            // Bỏ qua T7, CN nếu includeWeekend = false
            if (!Boolean.TRUE.equals(req.getIncludeWeekend())
                    && (current.getDayOfWeek().getValue() == 6 || current.getDayOfWeek().getValue() == 7)) {
                current = current.plusDays(1);
                continue;
            }

            // Với mỗi nhân viên, kiểm tra điều kiện
            for (User user : users) {

                // Nếu respectAbsenceRequests = true → kiểm tra vắng mặt được duyệt
                boolean hasApprovedAbsence = false;
                if (Boolean.TRUE.equals(req.getRespectAbsenceRequests())) {
                    hasApprovedAbsence = absenceRequestRepository.exists(
                            AbsenceRequestSpecifications.isApprovedForUserOnDate(user.getId(), current));
                }

                LocalTime newStart = shift.getStartTime();
                LocalTime newEnd = shift.getEndTime();

                // Nếu replaceExisting = true → xóa lịch cũ
                if (Boolean.TRUE.equals(req.getReplaceExisting())) {
                    List<WorkSchedule> overlappingSchedules = workScheduleRepository.findAll(
                            Specification.where(WorkScheduleSpecifications.hasUserId(user.getId()))
                                    .and(WorkScheduleSpecifications.workDateEquals(current))
                                    .and(WorkScheduleSpecifications.timeOverlaps(newStart, newEnd))
                    );
                    if (!overlappingSchedules.isEmpty()) {
                        workScheduleRepository.deleteAll(overlappingSchedules);
                    }
                } else {
                    boolean hasOverlap = workScheduleRepository.exists(
                            Specification.where(WorkScheduleSpecifications.hasUserId(user.getId()))
                                    .and(WorkScheduleSpecifications.workDateEquals(current))
                                    .and(WorkScheduleSpecifications.timeOverlaps(newStart, newEnd))
                    );
                    if (hasOverlap) continue;
                }

                // 6️⃣ Tạo mới WorkSchedule
                WorkSchedule schedule = new WorkSchedule();
                schedule.setUser(user);
                schedule.setShift(shift);
                schedule.setWorkDate(current);
                schedule.setStatus(
                        hasApprovedAbsence ? WorkScheduleStatus.ABSENCE : WorkScheduleStatus.SCHEDULED
                );

                WorkSchedule saved = workScheduleRepository.save(schedule);
                scheduleEndEvent(saved);

                createdSchedules.add(toResponse(saved));
            }

            current = current.plusDays(1);
        }

        return createdSchedules;
    }

    public List<WorkScheduleResponse> getShiftsForUserOnDate(UUID userId, LocalDate date) {
        List<WorkSchedule> list = workScheduleRepository.findAll(
                Specification.where(WorkScheduleSpecifications.hasUserId(userId))
                        .and(WorkScheduleSpecifications.workDateEquals(date))
        );

        return list.stream()
                .sorted(Comparator.comparing(ws -> ws.getShift().getStartTime()))
                .map(this::toResponse)
                .toList();
    }

    private void scheduleEndEvent(WorkSchedule ws) {
        // endTime = LocalTime → phải ghép vào ngày
        long epoch = ws.getShift().getEndTime()
                .atDate(ws.getWorkDate())
                .atZone(java.time.ZoneId.systemDefault())
                .toEpochSecond();

        redis.opsForZSet().add("schedule_end_events", ws.getId().toString(), epoch);
    }

}
