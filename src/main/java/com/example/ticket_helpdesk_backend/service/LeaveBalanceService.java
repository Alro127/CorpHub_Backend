package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.LeaveBalanceRequest;
import com.example.ticket_helpdesk_backend.dto.LeaveBalanceResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.LeaveBalance;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.LeaveBalanceRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class LeaveBalanceService {
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<LeaveBalanceResponse> getAll() {
        return leaveBalanceRepository.findAll()
                .stream()
                .map(entity -> {
                    LeaveBalanceResponse dto = modelMapper.map(entity, LeaveBalanceResponse.class);
                    dto.setUser(UserDto.toUserDto(entity.getUser()));
                    return dto;
                })
                .toList();
    }

    public LeaveBalanceResponse getById(UUID id) throws ResourceNotFoundException {
        LeaveBalance LeaveBalance = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));
        return modelMapper.map(LeaveBalance, LeaveBalanceResponse.class);
    }

    @Transactional
    public LeaveBalanceResponse create(LeaveBalanceRequest request) throws ResourceNotFoundException {

        User user = userService.getUserById(request.getUserId());

        LeaveBalance leaveBalance = modelMapper.map(request, LeaveBalance.class);
        leaveBalance.setLastUpdated(LocalDateTime.now());
        leaveBalance.setUser(user);

        LeaveBalance saved = leaveBalanceRepository.save(leaveBalance);
        LeaveBalanceResponse leaveBalanceResponse = modelMapper.map(saved, LeaveBalanceResponse.class);
        leaveBalanceResponse.setUser(UserDto.toUserDto(user));

        return leaveBalanceResponse;
    }

    @Transactional
    public LeaveBalanceResponse update(UUID id, LeaveBalanceRequest request) throws ResourceNotFoundException {
        LeaveBalance existing = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));

        modelMapper.map(request, existing);

        existing.setLastUpdated(LocalDateTime.now());
        LeaveBalance updated = leaveBalanceRepository.save(existing);

        LeaveBalanceResponse leaveBalanceResponse = modelMapper.map(updated, LeaveBalanceResponse.class);
        leaveBalanceResponse.setUser(UserDto.toUserDto(updated.getUser()));
        return leaveBalanceResponse;
    }

    @Transactional
    public void delete(UUID id) throws ResourceNotFoundException {
        if (!leaveBalanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leave type not found with id: " + id);
        }
        leaveBalanceRepository.deleteById(id);
    }
}
