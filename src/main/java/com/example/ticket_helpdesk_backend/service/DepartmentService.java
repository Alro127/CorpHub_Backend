package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public Department getDepartmentById(Integer id) {
        return departmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Department không tồn tại"));
    }

}
