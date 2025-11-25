package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.CompetencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompetencyLevelRepository extends JpaRepository<CompetencyLevel, UUID> {
    List<CompetencyLevel> findByTypeId(UUID typeId);
}
