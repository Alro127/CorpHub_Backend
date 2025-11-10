package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.WorkScheduleStatus;
import com.example.ticket_helpdesk_backend.dto.ShiftDto;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.dto.WorkScheduleRequest;
import com.example.ticket_helpdesk_backend.dto.WorkScheduleResponse;
import com.example.ticket_helpdesk_backend.entity.Shift;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.ShiftRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.repository.WorkScheduleRepository;
import com.example.ticket_helpdesk_backend.specification.WorkScheduleSpecifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper;

    public Page<WorkScheduleResponse> getAll(
            int page, int size, String keywords,
            UUID userId, UUID shiftId,
            WorkScheduleStatus status,
            LocalDate fromDate, LocalDate toDate,
            String sortBy, String direction
    ) {
        Sort sort = Sort.by((direction != null && direction.equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                (sortBy == null || sortBy.isBlank()) ? "workDate" : sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<WorkSchedule> spec = Specification
                .where(WorkScheduleSpecifications.hasKeywords(keywords))
                .and(WorkScheduleSpecifications.hasUserId(userId))
                .and(WorkScheduleSpecifications.hasShiftId(shiftId))
                .and(WorkScheduleSpecifications.hasStatus(status))
                .and(WorkScheduleSpecifications.workDateFrom(fromDate))
                .and(WorkScheduleSpecifications.workDateTo(toDate));

        return workScheduleRepository.findAll(spec, pageable).map(this::toResponse);
    }

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
}
