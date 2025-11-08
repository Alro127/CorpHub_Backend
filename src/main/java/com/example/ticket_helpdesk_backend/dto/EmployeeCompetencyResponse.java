package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompetencyResponse {

    private UUID id;

    // --- Loại năng lực ---
    private UUID typeId;
    private String typeCode;
    private String typeName;

    // --- Thông tin năng lực ---
    private String name;
    private String level;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expireDate;
    private String note;

    // --- Tài liệu chứng minh ---
    private UUID documentId;

    // --- Thông tin xác thực ---
    private String certificateCode;
    private String verifyUrl;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDate verifiedDate;

    // --- Người upload ---
    private UUID uploadedById;
    private String uploadedByName;

    // =============================
    // Mapping từ Entity → Response
    // =============================
    public static EmployeeCompetencyResponse fromEntity(EmployeeCompetency entity) {
        if (entity == null) return null;

        EmployeeCompetencyResponse dto = new EmployeeCompetencyResponse();
        dto.setId(entity.getId());

        if (entity.getType() != null) {
            dto.setTypeId(entity.getType().getId());
            dto.setTypeCode(entity.getType().getCode());
            dto.setTypeName(entity.getType().getName());
        }

        dto.setName(entity.getName());
        dto.setLevel(entity.getLevel());
        dto.setIssuedBy(entity.getIssuedBy());
        dto.setIssuedDate(entity.getIssuedDate());
        dto.setExpireDate(entity.getExpireDate());
        dto.setNote(entity.getNote());

        if (entity.getDocument() != null)
            dto.setDocumentId(entity.getDocument().getId());

        dto.setCertificateCode(entity.getCertificateCode());
        dto.setVerifyUrl(entity.getVerifyUrl());
        dto.setVerificationStatus(entity.getVerificationStatus());
        dto.setVerifiedBy(entity.getVerifiedBy());
        dto.setVerifiedDate(entity.getVerifiedDate());

        if (entity.getUploadedBy() != null) {
            dto.setUploadedById(entity.getUploadedBy().getId());
            if (entity.getUploadedBy().getEmployeeProfile() != null)
                dto.setUploadedByName(entity.getUploadedBy().getEmployeeProfile().getFullName());
        }

        return dto;
    }
}
