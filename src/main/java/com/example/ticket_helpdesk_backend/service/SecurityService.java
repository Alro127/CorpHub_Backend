package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.UserDb;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.UserDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("securityService")
public class SecurityService {

    @Autowired
    private final UserDbRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public SecurityService(UserDbRepository userRepository,
                           DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isManagerOfDepartment(Integer departmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserDb> user = userRepository.findUserDbByEmail(email);
        Optional<Department> department = departmentRepository.findById(departmentId);

        return user.isPresent() && department.isPresent() &&
                user.get().getRole().equals("ROLE_MANAGER") &&
                department.get().getManager().getId().equals(user.get().getId());
    }
}

