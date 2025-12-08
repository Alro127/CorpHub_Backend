package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class PositionChangeRequestDetailDto {

    private UUID id;

    private UUID employeeId;
    private String employeeName;

    private UUID oldPositionId;
    private String oldPositionName;

    private UUID newPositionId;
    private String newPositionName;

    private UUID oldDepartmentId;
    private String oldDepartmentName;

    private UUID newDepartmentId;
    private String newDepartmentName;

    private String type;
    private LocalDate effectDate;
    private String reason;
    private String status;

    private UUID createdById;
    private String createdByName;
    private LocalDateTime createdAt;

    private List<PositionChangeAttachmentDto> attachments;

    public static PositionChangeRequestDetailDto mapEntityToDetailDto(PositionChangeRequest entity) {
        PositionChangeRequestDetailDto dto = new PositionChangeRequestDetailDto();

        dto.setId(entity.getId());

        dto.setEmployeeId(entity.getEmployee().getId());
        dto.setEmployeeName(entity.getEmployee().getFullName()); // assume có field này

        if (entity.getOldPosition() != null) {
            dto.setOldPositionId(entity.getOldPosition().getId());
            dto.setOldPositionName(entity.getOldPosition().getName());
        }

        if (entity.getOldDepartment() != null) {
            dto.setOldDepartmentId(entity.getOldDepartment().getId());
            dto.setOldDepartmentName(entity.getOldDepartment().getName());
        }

        dto.setNewPositionId(entity.getNewPosition().getId());
        dto.setNewPositionName(entity.getNewPosition().getName());

        dto.setNewDepartmentId(entity.getNewDepartment().getId());
        dto.setNewDepartmentName(entity.getNewDepartment().getName());

        dto.setType(entity.getType());
        dto.setEffectDate(entity.getEffectDate());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());

        dto.setCreatedById(entity.getCreatedBy().getId());
        dto.setCreatedByName(entity.getCreatedBy().getFullName());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getAttachments() != null) {
            List<PositionChangeAttachmentDto> attachDtos = entity.getAttachments().stream()
                    .map(a -> {
                        PositionChangeAttachmentDto adto = new PositionChangeAttachmentDto();
                        adto.setFileName(a.getFileName());
                        adto.setFileUrl(a.getFileUrl());
                        adto.setUploadedById(a.getUploadedBy().getId().toString());
                        return adto;
                    })
                    .collect(Collectors.toList());
            dto.setAttachments(attachDtos);
        }

        return dto;
    }
}
