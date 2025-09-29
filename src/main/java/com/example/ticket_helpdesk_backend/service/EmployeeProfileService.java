package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.CreateEmployeeProfileRequest;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeProfileService {
    @Autowired
    EmployeeProfileRepository employeeProfileRepository;

    @Transactional
    public boolean CreateEmployeeProfile (CreateEmployeeProfileRequest request) {
//        if (userRepository.findByUsername(registerRequest.getUserName()).isPresent()) {
//            throw new RuntimeException("UserName đã tồn tại");
//        }
//        User user = new User();
//        user.setUsername(registerRequest.getUserName());
//        user.setActive(true);
//        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
//        user.setRole(roleRepository.findById(registerRequest.getRoleId()).orElseThrow(() -> new RuntimeException("Role không tồn tại")));
//
//        User savedUser = userRepository.save(user);
//
        return true;
    }
}
