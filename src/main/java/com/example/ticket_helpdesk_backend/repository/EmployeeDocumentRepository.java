package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.dto.EmployeeDocumentResponse;
import com.example.ticket_helpdesk_backend.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {
    List<EmployeeDocument> findByEmployeeProfile_Id(UUID employeeId);
}
