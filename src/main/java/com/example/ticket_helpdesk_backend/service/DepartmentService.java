package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.DepartmentBasicInfoDto;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ModelMapper modelMapper;

    public Department getDepartmentById(Integer id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

    public List<DepartmentBasicInfoDto> getDepartmentBasicInfoDtoList() {
        return departmentRepository.findAll().stream()
                .map(department -> modelMapper.map(department, DepartmentBasicInfoDto.class))
                .toList();
    }

}
