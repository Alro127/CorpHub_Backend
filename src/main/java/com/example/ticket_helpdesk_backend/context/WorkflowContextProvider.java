package com.example.ticket_helpdesk_backend.context;

import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.UUID;

public interface WorkflowContextProvider {
    String getTargetEntity();
    Map<String, Object> buildContext(UUID entityId);
}
