package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.DocumentType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class DocumentTypeDto {
    private UUID id;
    private String name;
    private String code;

    public static DocumentTypeDto fromEntity(DocumentType documentType) {
        DocumentTypeDto documentTypeDto = new DocumentTypeDto();
        documentTypeDto.setId(documentType.getId());
        documentTypeDto.setName(documentType.getName());
        documentTypeDto.setCode(documentType.getCode());

        return documentTypeDto;
    }
}
