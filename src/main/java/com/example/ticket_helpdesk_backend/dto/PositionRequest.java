package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Position;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Setter
@Getter
public class PositionRequest {
    private UUID positionId; // null
    private String name;
    private String code;
    private String description;
    private Integer levelOrder;

}
