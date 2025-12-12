package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Role;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class  RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
}
