package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.PositionChangeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionChangeAttachmentRepository extends JpaRepository<PositionChangeAttachment, UUID> {
}
