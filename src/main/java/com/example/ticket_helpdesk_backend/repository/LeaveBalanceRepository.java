package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
}