package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {
    DocumentType findByCode(String code);
}
