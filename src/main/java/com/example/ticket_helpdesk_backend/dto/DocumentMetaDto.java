package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentMetaDto {
    private UUID documentTypeId;
    private String title;
    private String description;
    private String objectName;
}
