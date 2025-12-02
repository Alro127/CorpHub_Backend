package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.entity.InternalWorkHistory;
import com.example.ticket_helpdesk_backend.entity.PositionChangeApproval;
import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import com.example.ticket_helpdesk_backend.repository.PositionChangeApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionChangeApprovalService{

    private final PositionChangeApprovalRepository approvalRepo;
    private final PositionChangeRequestService requestService;
    private final InternalWorkHistoryService historyService;

    public PositionChangeApproval approve(UUID approvalId, String comment) {

        PositionChangeApproval approval = approvalRepo.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval step not found"));

        approval.setDecision("approved");
        approval.setComment(comment);
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepo.save(approval);

        // Kiểm tra xem tất cả các bước đã duyệt chưa
        List<PositionChangeApproval> steps = approvalRepo.findByRequestIdOrderByStepOrder(
                approval.getRequest().getId()
        );

        boolean allApproved = steps.stream()
                .allMatch(s -> "approved".equals(s.getDecision()));

        if (allApproved) {
            requestService.updateStatus(approval.getRequest().getId(), "approved");

            // Tạo lịch sử InternalWorkHistory
            createWorkHistory(approval.getRequest());
        }

        return approval;
    }

    public PositionChangeApproval reject(UUID approvalId, String comment) {
        PositionChangeApproval approval = approvalRepo.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval step not found"));

        approval.setDecision("rejected");
        approval.setComment(comment);
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepo.save(approval);

        // Update trạng thái request
        requestService.updateStatus(approval.getRequest().getId(), "rejected");

        return approval;
    }

    public List<PositionChangeApproval> getSteps(UUID requestId) {
        return approvalRepo.findByRequestIdOrderByStepOrder(requestId);
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
