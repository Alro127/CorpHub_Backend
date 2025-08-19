package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Integer> {
}