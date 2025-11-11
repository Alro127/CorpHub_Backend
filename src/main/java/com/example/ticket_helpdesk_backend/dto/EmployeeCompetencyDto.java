package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompetencyDto {
    private UUID id;

    // --- Thông tin loại năng lực ---
    private UUID typeId;
    private String typeName;

    // --- Thông tin năng lực ---
    private String name;
    private UUID levelId;
    private String levelName;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expireDate;
    private String note;

    // --- Liên kết tài liệu ---
    private UUID documentId;

    // --- Xác thực ---
    private String certificateCode;
    private String verifyUrl;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDate verifiedDate;

    // --- Người upload ---
    private UUID uploadedById;
    private String uploadedByName;

    // =============================
    // Mapping từ Entity → DTO
    // =============================
    public static EmployeeCompetencyDto fromEntity(EmployeeCompetency entity) {
        if (entity == null) return null;

        EmployeeCompetencyDto dto = new EmployeeCompetencyDto();
        dto.setId(entity.getId());

        // Type
        if (entity.getType() != null) {
            dto.setTypeId(entity.getType().getId());
            dto.setTypeName(entity.getType().getName());
        }

        dto.setName(entity.getName());
        if (entity.getLevel() != null) {
            dto.setLevelId(entity.getLevel().getId());
            dto.setLevelName(entity.getLevel().getName());
        }
        dto.setIssuedBy(entity.getIssuedBy());
        dto.setIssuedDate(entity.getIssuedDate());
        dto.setExpireDate(entity.getExpireDate());
        dto.setNote(entity.getNote());

        // Document
        if (entity.getDocument() != null)
            dto.setDocumentId(entity.getDocument().getId());

        // Verification
        dto.setCertificateCode(entity.getCertificateCode());
        dto.setVerifyUrl(entity.getVerifyUrl());
        dto.setVerificationStatus(entity.getVerificationStatus());
        dto.setVerifiedBy(entity.getVerifiedBy());
        dto.setVerifiedDate(entity.getVerifiedDate());

        // Uploader
        if (entity.getUploadedBy() != null) {
            dto.setUploadedById(entity.getUploadedBy().getId());
            dto.setUploadedByName(entity.getUploadedBy().getEmployeeProfile().getFullName()); // giả sử có field fullName
        }

        return dto;
    }

    // =============================
    // Mapping từ DTO → Entity
    // =============================
    public static EmployeeCompetency toEntity(EmployeeCompetencyDto dto,
                                              EmployeeProfile employee,
                                              CompetencyType type,
                                              CompetencyLevel level,
                                              EmployeeDocument document,
                                              User uploader) {
        if (dto == null) return null;

        EmployeeCompetency entity = new EmployeeCompetency();
        entity.setId(dto.getId());
        entity.setEmployeeProfile(employee);
        entity.setType(type);
        entity.setName(dto.getName());
        entity.setLevel(level);
        entity.setIssuedBy(dto.getIssuedBy());
        entity.setIssuedDate(dto.getIssuedDate());
        entity.setExpireDate(dto.getExpireDate());
        entity.setNote(dto.getNote());

        entity.setDocument(document);
        entity.setCertificateCode(dto.getCertificateCode());
        entity.setVerifyUrl(dto.getVerifyUrl());
        entity.setVerificationStatus(dto.getVerificationStatus() != null ? dto.getVerificationStatus() : VerificationStatus.PENDING);
        entity.setVerifiedBy(dto.getVerifiedBy());
        entity.setVerifiedDate(dto.getVerifiedDate());
        entity.setUploadedBy(uploader);

        return entity;
    }
}
