package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.entity.Shift;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.ShiftRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.repository.WorkScheduleRepository;
import com.example.ticket_helpdesk_backend.specification.UserSpecifications;
import com.example.ticket_helpdesk_backend.specification.WorkScheduleSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper;

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
        return toResponse(saved);
    }

    @Transactional
    public WorkScheduleResponse update(WorkScheduleRequest req) throws ResourceNotFoundException {
        if (req.getId() == null) throw new ResourceNotFoundException("WorkSchedule id is required");

        WorkSchedule existing = workScheduleRepository.findById(req.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule not found: " + req.getId()));

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
    public WorkScheduleResponse delete(UUID id) throws ResourceNotFoundException {
        WorkSchedule entity = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule not found: " + id));
        workScheduleRepository.delete(entity);
        return toResponse(entity);
    }

    private WorkScheduleResponse toResponse(WorkSchedule entity) {
        WorkScheduleResponse resp = new WorkScheduleResponse();
        resp.setId(entity.getId());
        resp.setWorkDate(entity.getWorkDate());
        resp.setStatus(entity.getStatus());

        // map nested
        UserDto userDto = UserDto.toUserDto(entity.getUser());
        ShiftDto shiftDto = modelMapper.map(entity.getShift(), ShiftDto.class);
        resp.setUser(userDto);
        resp.setShift(shiftDto);

        return resp;
    }

    public Page<EmployeeScheduleDto> getEmployeeSchedules(int page, int size, String keywords, UUID departmentId, LocalDate from, LocalDate to) {

        Sort sort = Sort.by(
                Sort.Order.asc("employeeProfile.fullName").ignoreCase()
        );

        Pageable pageable = PageRequest.of(page, size, sort);

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

        List<UUID> userIds = users.stream()
                .map(User::getId)
                .toList();

        List<WorkSchedule> schedules = workScheduleRepository.findAll(
                Specification
                        .where(WorkScheduleSpecifications.hasUserIdIn(userIds))
                        .and(WorkScheduleSpecifications.workDateFrom(from))
                        .and(WorkScheduleSpecifications.workDateTo(to))
        );

        Map<UUID, List<WorkSchedule>> grouped = schedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getUser().getId()));

        List<EmployeeScheduleDto> results = new ArrayList<>();
        for (User u : users) {
            List<WorkSchedule> wsList = grouped.getOrDefault(u.getId(), Collections.emptyList());

            List<EmployeeShiftDto> shiftDtos = wsList.stream()
                    .map(ws -> {
                        Shift s = ws.getShift();
                        return new EmployeeShiftDto(
                                ws.getId(),
                                ws.getWorkDate(),
                                s.getId(),
                                s.getName(),
                                s.getStartTime().toString(),
                                s.getEndTime().toString(),
                                null,                        // notes chưa có
                                ws.getStatus()
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
}
