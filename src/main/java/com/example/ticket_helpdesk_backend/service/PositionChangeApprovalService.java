package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.PositionChangeApprovalDto;
import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import com.example.ticket_helpdesk_backend.repository.PositionChangeApprovalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionChangeApprovalService{

    private final PositionChangeApprovalRepository approvalRepository;
    private final PositionChangeRequestService requestService;
    private final InternalWorkHistoryService historyService;


    @Transactional
    public PositionChangeApproval approve(UUID stepId, String comment) {
        PositionChangeApproval step = approvalRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Approval step not found"));

        if (!PositionChangeApproval.DECISION_PENDING.equals(step.getDecision())) {
            throw new RuntimeException("This step has already been processed");
        }

        step.setDecision(PositionChangeApproval.DECISION_APPROVED);
        step.setComment(comment);
        step.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(step);

        PositionChangeRequest request = step.getRequest();

        // Kiểm tra còn step pending nào không
        var nextPendingOpt = approvalRepository
                .findFirstByRequestIdAndDecisionOrderByStepOrderAsc(
                        request.getId(),
                        PositionChangeApproval.DECISION_PENDING
                );

        if (nextPendingOpt.isEmpty()) {
            // Không còn step pending -> finalize
            requestService.finalizeRequest(request.getId());
            createWorkHistory(request);
        } else {
            // Còn step pending -> có thể cập nhật status request sang IN_REVIEW (nếu muốn)
            if (PositionChangeRequest.STATUS_PENDING.equals(request.getStatus())) {
                requestService.updateStatus(request.getId(), PositionChangeRequest.STATUS_IN_REVIEW);
            }
        }

        return step;
    }

    @Transactional
    public PositionChangeApproval reject(UUID stepId, String comment) {
        PositionChangeApproval step = approvalRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Approval step not found"));

        if (!PositionChangeApproval.DECISION_PENDING.equals(step.getDecision())) {
            throw new RuntimeException("This step has already been processed");
        }

        step.setDecision(PositionChangeApproval.DECISION_REJECTED);
        step.setComment(comment);
        step.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(step);

        // Update trạng thái request
        requestService.updateStatus(step.getRequest().getId(), PositionChangeRequest.STATUS_REJECTED);

        return step;
    }

    public List<PositionChangeApprovalDto> getSteps(UUID requestId) {
        return approvalRepository.findByRequestIdOrderByStepOrderAsc(requestId)
                .stream()
                .map(PositionChangeApprovalDto::mapToDto)
                .toList();
    }


    private void createWorkHistory(PositionChangeRequest req) {
        InternalWorkHistory history = new InternalWorkHistory();
        history.setEmployeeProfile(req.getEmployee());
        history.setDepartment(req.getNewDepartment());
        history.setPosition(req.getNewPosition());
        history.setEffectiveDate(req.getEffectDate());
        history.setChangeType(req.getType());
        history.setReason(req.getReason());
        history.setRequest(req);
        historyService.createHistory(history);
    }
}
