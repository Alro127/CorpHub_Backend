package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.CompetencyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompetencyTypeRepository extends JpaRepository<CompetencyType, UUID> {
    boolean existsByCode(String code);
}
