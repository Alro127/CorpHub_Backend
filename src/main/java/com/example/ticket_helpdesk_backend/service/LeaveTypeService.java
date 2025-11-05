package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LeaveTypeRequest;
import com.example.ticket_helpdesk_backend.dto.LeaveTypeResponse;
import com.example.ticket_helpdesk_backend.entity.LeaveType;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.LeaveTypeRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final ModelMapper modelMapper;

    public List<LeaveTypeResponse> getAll() {
        return leaveTypeRepository.findAll()
                .stream()
                .map(entity -> modelMapper.map(entity, LeaveTypeResponse.class))
                .collect(Collectors.toList());
    }

    public LeaveTypeResponse getById(UUID id) throws ResourceNotFoundException {
        LeaveType leaveType = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));
        return modelMapper.map(leaveType, LeaveTypeResponse.class);
    }

    @Transactional
    public LeaveTypeResponse create(LeaveTypeRequest request) {
        LeaveType leaveType = modelMapper.map(request, LeaveType.class);
        leaveType.setCreatedAt(LocalDateTime.now());
        leaveType.setUpdatedAt(LocalDateTime.now());

        LeaveType saved = leaveTypeRepository.save(leaveType);
        return modelMapper.map(saved, LeaveTypeResponse.class);
    }

    @Transactional
    public LeaveTypeResponse update(UUID id, LeaveTypeRequest request) throws ResourceNotFoundException {
        LeaveType existing = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));

        modelMapper.map(request, existing);

        existing.setUpdatedAt(LocalDateTime.now());
        LeaveType updated = leaveTypeRepository.save(existing);

        return modelMapper.map(updated, LeaveTypeResponse.class);
    }

    @Transactional
    public void delete(UUID id) throws ResourceNotFoundException {
        if (!leaveTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leave type not found with id: " + id);
        }
        leaveTypeRepository.deleteById(id);
    }
}
