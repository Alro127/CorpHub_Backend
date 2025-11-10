package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, UUID>, JpaSpecificationExecutor<AbsenceRequest> {
}