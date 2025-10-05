package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.RoomRequirement;
import com.example.ticket_helpdesk_backend.entity.RoomRequirementAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRequirementAssetRepository extends JpaRepository<RoomRequirementAsset, UUID> {
    boolean existsByRoomRequirement(RoomRequirement roomRequirement);

    void deleteByRoomRequirementId(UUID id);
}