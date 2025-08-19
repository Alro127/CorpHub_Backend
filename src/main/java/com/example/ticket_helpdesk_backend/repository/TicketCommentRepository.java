package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Integer> {
}