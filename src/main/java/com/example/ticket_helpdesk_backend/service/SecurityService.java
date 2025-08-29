package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.repository.AccountRepository;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("securityService")
public class SecurityService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final DepartmentRepository departmentRepository;
    @Autowired
    private final AccountRepository accountRepository;

    public SecurityService(UserRepository userRepository,
                           DepartmentRepository departmentRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.accountRepository = accountRepository;
    }

    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isManagerOfDepartment(UUID departmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return false;
        }
        Optional<Account> account = accountRepository.findById(user.get().getId());
        if (account.isEmpty()) {
            return false;
        }
        Optional<Department> department = departmentRepository.findById(departmentId);

        return department.isPresent() && account.get().getRole().getName().equals("ROLE_MANAGER");
    }
}

