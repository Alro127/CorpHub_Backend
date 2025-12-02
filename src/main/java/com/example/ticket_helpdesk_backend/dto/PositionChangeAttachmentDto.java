package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionChangeAttachmentDto {
    private String fileName;
    private String fileUrl;
    private String uploadedById; // UUID dạng String
}
