package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AbsenceTypeRepository extends JpaRepository<AbsenceType, UUID> {
}