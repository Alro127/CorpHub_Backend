package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
}