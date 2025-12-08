package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionChangeApprovalRepository extends JpaRepository<PositionChangeApproval, UUID> {
    List<PositionChangeApproval> findByRequestIdOrderByStepOrder(UUID requestId);
    List<PositionChangeApproval> findByRequestIdOrderByStepOrderAsc(UUID requestId);

    // Lấy step đang pending (sequential)
    Optional<PositionChangeApproval> findFirstByRequestIdAndDecisionOrderByStepOrderAsc(
            UUID requestId,
            String decision
    );

    // Lấy các request mà user này đang phải duyệt
    List<PositionChangeApproval> findByApproverIdAndDecision(UUID approverId, String decision);
}

