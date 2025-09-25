package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.repository.AccountRepository;
import com.example.ticket_helpdesk_backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public Account getAccountById(UUID userId) {
        return accountRepository.findById(userId).orElse(null);
    }

    private String getRoleName(UUID userId) {
        Account account = getAccountById(userId);
        return account != null ? account.getRole().getName() : null;
    }

    public boolean isAdmin(UUID userId) {
        return "ROLE_ADMIN".equals(getRoleName(userId));
    }

    public boolean isManager(UUID userId) {
        return "ROLE_MANAGER".equals(getRoleName(userId));
    }

    public boolean isUser(UUID userId) {
        return "ROLE_USER".equals(getRoleName(userId));
    }
}
