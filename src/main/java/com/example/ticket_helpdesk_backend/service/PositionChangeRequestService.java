package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.PositionChangeAttachmentDto;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestCreateDto;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestDetailDto;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import com.example.ticket_helpdesk_backend.repository.PositionChangeRequestRepository;
import com.example.ticket_helpdesk_backend.repository.PositionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionChangeRequestService {


    private final PositionChangeRequestRepository requestRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeProfileRepository employeeRepository;


    @Transactional
    public PositionChangeRequestDetailDto createRequest(PositionChangeRequestCreateDto dto) {

        // 1. Load entity chính
        EmployeeProfile employee = employeeProfileRepository.findById(UUID.fromString(dto.getEmployeeId()))
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        EmployeeProfile createdBy = employeeRepository.findById(UUID.fromString(dto.getCreatedById()))
                .orElseThrow(() -> new RuntimeException("CreatedBy employee not found"));

        Position newPosition = positionRepository.findById(UUID.fromString(dto.getNewPositionId()))
                .orElseThrow(() -> new RuntimeException("New position not found"));

        Department newDepartment = departmentRepository.findById(UUID.fromString(dto.getNewDepartmentId()))
                .orElseThrow(() -> new RuntimeException("New department not found"));

        Position oldPosition = null;
        if (dto.getOldPositionId() != null) {
            oldPosition = positionRepository.findById(UUID.fromString(dto.getOldPositionId()))
                    .orElseThrow(() -> new RuntimeException("Old position not found"));
        }

        Department oldDepartment = null;
        if (dto.getOldDepartmentId() != null) {
            oldDepartment = departmentRepository.findById(UUID.fromString(dto.getOldDepartmentId()))
                    .orElseThrow(() -> new RuntimeException("Old department not found"));
        }

        // 2. Mapping DTO -> Entity PositionChangeRequest
        PositionChangeRequest request = new PositionChangeRequest();
        request.setEmployee(employee);
        request.setOldPosition(oldPosition);
        request.setOldDepartment(oldDepartment);
        request.setNewPosition(newPosition);
        request.setNewDepartment(newDepartment);
        request.setType(dto.getType());
        request.setEffectDate(dto.getEffectDate());
        request.setReason(dto.getReason());
        request.setCreatedBy(createdBy);
        request.setCreatedAt(LocalDateTime.now());
        request.setStatus("pending"); // mặc định khi tạo xong chờ phê duyệt

        // 3. Ánh xạ attachments (nếu có)
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            List<PositionChangeAttachment> attachmentEntities = dto.getAttachments().stream()
                    .map(a -> mapAttachmentDtoToEntity(a, request))
                    .collect(Collectors.toList());
            request.setAttachments(attachmentEntities);
        }

        // 4. Lưu DB (cascade attachments)
        PositionChangeRequest saved = requestRepository.save(request);

        // TODO: Sau này ở đây sẽ thêm logic:
        // - tạo các bước phê duyệt PositionChangeApproval theo workflow & rule đặc biệt
        // Ứng với từng role sẽ có cách phát triển workflow khác nhau

        // 5. Mapping Entity -> Detail DTO
        return PositionChangeRequestDetailDto.mapEntityToDetailDto(saved);
    }

    private PositionChangeAttachment mapAttachmentDtoToEntity(
            PositionChangeAttachmentDto dto,
            PositionChangeRequest request
    ) {
        PositionChangeAttachment entity = new PositionChangeAttachment();
        entity.setRequest(request);
        entity.setFileName(dto.getFileName());
        entity.setFileUrl(dto.getFileUrl());

        EmployeeProfile uploader = employeeRepository.findById(UUID.fromString(dto.getUploadedById()))
                .orElseThrow(() -> new RuntimeException("Uploader not found"));

        entity.setUploadedBy(uploader);
        entity.setUploadedAt(LocalDateTime.now());
        return entity;
    }

    @Transactional
    public PositionChangeRequestDetailDto getRequest(UUID id) {
        PositionChangeRequest positionChangeRequest = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return PositionChangeRequestDetailDto.mapEntityToDetailDto(positionChangeRequest);
    }

    public List<PositionChangeRequestDetailDto> getRequestsByEmployee(UUID employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(PositionChangeRequestDetailDto::mapEntityToDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(UUID requestId, String status) {
        PositionChangeRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
    }
}
