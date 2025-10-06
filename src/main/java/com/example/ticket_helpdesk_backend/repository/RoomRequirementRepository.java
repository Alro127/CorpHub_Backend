package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRequirementRepository extends JpaRepository<RoomRequirement, UUID> {
    RoomRequirement findByMeetingId(UUID id);
}