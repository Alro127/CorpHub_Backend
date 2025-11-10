package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.AbsenceApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AbsenceApprovalRepository extends JpaRepository<AbsenceApproval, UUID> {
}