package com.example.ticket_helpdesk_backend.model;

import com.example.ticket_helpdesk_backend.consts.ApproverType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproverDefinition {

    private ApproverType type;  // USER, POSITION, POSITION_LEVEL, ROLE, DEPARTMENT

    private Map<String, Object> params;
}