package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.DepartmentDto;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
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

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

    public List<DepartmentDto> getDepartmentDtoList() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentDto.class))
                .toList();
    }

//    @Cacheable(value = "usersByDepartment", key = "#departmentId")
//    public List<UserDto> getUsersByDepartment(UUID departmentId) {
//        return userRepository.findByDepartmentId(departmentId)
//                .stream()
//                .map(user -> modelMapper.map(user, UserDto.class))
//                .toList();
//    }

}
