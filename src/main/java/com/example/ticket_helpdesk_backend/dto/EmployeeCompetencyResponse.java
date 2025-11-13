package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private UUID levelId;
    private String levelName;
    private String issuedBy;
    private LocalDateTime issuedDate;
    private LocalDateTime expireDate;
    private String note;

    // --- Tài liệu chứng minh ---
    private UUID documentId;
    private LocalDateTime uploadDate;
    private String fileName;
    private String fileType;

    // --- Thông tin xác thực ---
    private String certificateCode;
    private String verifyUrl;
    private VerificationStatus verificationStatus;
    private String verifiedBy;
    private LocalDateTime verifiedDate;

    // --- Người upload ---
    private UUID uploadedById;
    private String uploadedByName;

    // --- Người sở hữu ---
    private UUID employeeProfileId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;

    private UUID updatedBy;
    private String updatedByName;
    private LocalDateTime updatedDate;

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
        if (entity.getLevel() != null) {
            dto.setLevelId(entity.getLevel().getId());
            dto.setLevelName(entity.getLevel().getName());
        }
        dto.setIssuedBy(entity.getIssuedBy());
        dto.setIssuedDate(entity.getIssuedDate());
        dto.setExpireDate(entity.getExpireDate());
        dto.setNote(entity.getNote());

        if (entity.getDocument() != null) {
            dto.setDocumentId(entity.getDocument().getId());
            dto.setFileName(entity.getDocument().getFileName());
            dto.setFileType(entity.getDocument().getFileType());
            dto.setUploadDate(entity.getDocument().getUploadDate());
        }
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

        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedBy(entity.getUpdatedBy().getId());
            if (entity.getUpdatedBy().getEmployeeProfile() != null)
                dto.setUpdatedByName(entity.getUpdatedBy().getEmployeeProfile().getFullName());
        }
        dto.setUpdatedDate(entity.getUpdatedDate());

        dto.setEmployeeProfileId(entity.getEmployeeProfile().getId());
        dto.setEmployeeName(entity.getEmployeeProfile().getFullName());
        dto.setEmployeeCode(entity.getEmployeeProfile().getCode());
        dto.setDepartmentName(entity.getEmployeeProfile().getDepartment().getName());

        return dto;
    }
}
