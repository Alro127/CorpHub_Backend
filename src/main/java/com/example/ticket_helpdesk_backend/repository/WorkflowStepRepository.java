package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID>, JpaSpecificationExecutor<WorkflowStep> {
}