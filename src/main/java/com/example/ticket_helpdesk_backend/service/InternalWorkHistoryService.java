package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.InternalWorkHistoryDto;
import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.repository.InternalWorkHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalWorkHistoryService {

    private final InternalWorkHistoryRepository repository;

    /* -----------------------------
     * CREATE NEW HISTORY
     * ----------------------------- */
    public InternalWorkHistoryDto createHistory(InternalWorkHistory history) {
        InternalWorkHistory saved = repository.save(history);
        return InternalWorkHistoryDto.mapToDto(saved);
    }

    /* -----------------------------
     * GET ALL BY EMPLOYEE
     * ----------------------------- */
    public List<InternalWorkHistoryDto> getByEmployee(UUID employeeId) {
        return repository.findByEmployeeProfileIdOrderByEffectiveDateDesc(employeeId)
                .stream()
                .map(InternalWorkHistoryDto::mapToDto)
                .toList();
    }

}

