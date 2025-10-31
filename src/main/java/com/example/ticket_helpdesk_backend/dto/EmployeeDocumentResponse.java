package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentResponse {
    private UUID id;
    private String documentTypeName; // lấy từ DocumentType
    private String title;
    private String description;
    private LocalDateTime uploadDate;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Boolean active;

    public static EmployeeDocumentResponse fromEntity(EmployeeDocument doc) {
        if (doc == null) return null;

        return new EmployeeDocumentResponse(
                doc.getId(),
                doc.getDocumentType() != null ? doc.getDocumentType().getName() : null,
                doc.getTitle(),
                doc.getDescription(),
                doc.getUploadDate(),
                doc.getFileUrl(),
                doc.getFileName(),
                doc.getFileType(),
                doc.getActive()
        );
    }
}
