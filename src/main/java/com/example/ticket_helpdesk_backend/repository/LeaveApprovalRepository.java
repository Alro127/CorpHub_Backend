package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, UUID> {
}