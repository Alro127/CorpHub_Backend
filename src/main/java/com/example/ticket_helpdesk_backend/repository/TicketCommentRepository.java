package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.dto.TicketCommentResponse;
import com.example.ticket_helpdesk_backend.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {
  List<TicketComment> findByTicketId(UUID ticketId);

  List<TicketComment> findByTicketIdAndIsDeletedFalse(UUID ticketId);
}