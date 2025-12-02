package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionChangeApprovalRepository extends JpaRepository<PositionChangeApproval, UUID> {
    List<PositionChangeApproval> findByRequestIdOrderByStepOrder(UUID requestId);
}

