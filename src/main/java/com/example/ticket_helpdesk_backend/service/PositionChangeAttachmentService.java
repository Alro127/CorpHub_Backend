package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.PositionChangeAttachmentDto;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.entity.PositionChangeAttachment;
import com.example.ticket_helpdesk_backend.entity.PositionChangeRequest;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import com.example.ticket_helpdesk_backend.repository.PositionChangeAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionChangeAttachmentService {

    private final PositionChangeAttachmentRepository attachmentRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public PositionChangeAttachment saveAttachment(
            PositionChangeAttachmentDto dto,
            PositionChangeRequest request
    ) {

        EmployeeProfile uploadedBy = employeeProfileRepository
                .findById(UUID.fromString(dto.getUploadedById()))
                .orElseThrow(() -> new RuntimeException("UploadedBy user not found"));

        PositionChangeAttachment attachment = new PositionChangeAttachment();
        attachment.setRequest(request);
        attachment.setFileName(dto.getFileName());
        attachment.setFileUrl(dto.getFileUrl());
        attachment.setUploadedBy(uploadedBy);
        attachment.setUploadedAt(LocalDateTime.now());

        return attachmentRepository.save(attachment);
    }
}
