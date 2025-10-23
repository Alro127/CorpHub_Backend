package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;


public interface RoomRequirementRepository extends
        JpaRepository<RoomRequirement, UUID>,
        JpaSpecificationExecutor<RoomRequirement> {
    RoomRequirement findByMeetingId(UUID id);
}