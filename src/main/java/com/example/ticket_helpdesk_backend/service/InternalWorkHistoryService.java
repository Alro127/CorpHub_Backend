package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.repository.InternalWorkHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalWorkHistoryService{

    private final InternalWorkHistoryRepository repository;

    public InternalWorkHistory createHistory(InternalWorkHistory history) {
        return repository.save(history);
    }

    public List<InternalWorkHistory> getByEmployee(UUID employeeId) {
        return repository.findByEmployeeProfileIdOrderByEffectiveDateDesc(employeeId);
    }
}
