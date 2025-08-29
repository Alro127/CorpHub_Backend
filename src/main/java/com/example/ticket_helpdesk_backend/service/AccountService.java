package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public Account getAccountById(UUID id) {
        return accountRepository.findById(id).orElse(null);
    }
}
