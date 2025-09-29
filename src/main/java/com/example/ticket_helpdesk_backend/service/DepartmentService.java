package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.DepartmentDto;
import com.example.ticket_helpdesk_backend.dto.TicketResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    JwtUtil jwtUtil;

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

    public List<DepartmentDto> getDepartmentDtoList() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentDto.class))
                .toList();
    }

    @Cacheable(value = "usersByDepartment", key = "#departmentId")
    public List<UserDto> getUsersByDepartment(UUID userId) throws ResourceNotFoundException {
        if (userId == null) {
            throw new RuntimeException("Invalid token, user id is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        if (user.getEmployeeProfile().getDepartment().getName() == null) {
            throw new ResourceNotFoundException("User with id " + userId + " has no department assigned");
        }

        UUID departmentId = user.getEmployeeProfile().getDepartment().getId();

        List<UserDto> usersDepartmentList = userRepository.findByEmployeeProfile_Department_Id(departmentId)
                .stream()
                .map(users -> modelMapper.map(users, UserDto.class))
                .toList();

        if (usersDepartmentList.isEmpty()) {
            throw new ResourceNotFoundException("No users found for department " + departmentId);
        }
        return usersDepartmentList;
    }

}
