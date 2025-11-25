package com.example.ticket_helpdesk_backend.context;

import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AbsenceRequestRepository;
import com.example.ticket_helpdesk_backend.service.AbsenceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AbsenceWorkflowContextProvider implements WorkflowContextProvider {

    private final AbsenceRequestRepository absenceRequestRepository;

    @Override
    public String getTargetEntity() {
        return "ABSENCE_REQUEST";
    }

    @Override
    public Map<String, Object> buildContext(UUID entityId) {
        AbsenceRequest absenceRequest = absenceRequestRepository.findById(entityId).orElseThrow(() ->
                new RuntimeException("AbsenceRequest")
        );

        User user = absenceRequest.getUser();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("absenceType", absenceRequest.getAbsenceType().getCode());
        ctx.put("durationDays", absenceRequest.getDurationDays());

        ctx.put("managerId", user.getEmployeeProfile().getManager().getId());
        return ctx;
    }
}