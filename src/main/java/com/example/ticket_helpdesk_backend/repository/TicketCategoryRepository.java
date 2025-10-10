package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {
    Optional<TicketCategory> findByName(String name);
}