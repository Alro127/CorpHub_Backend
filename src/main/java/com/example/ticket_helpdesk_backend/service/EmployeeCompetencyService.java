package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.BucketName;
import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyDto;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyResponse;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeCompetencyService {

    private final EmployeeCompetencyRepository competencyRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CompetencyTypeRepository competencyTypeRepository;
    private final CompetencyLevelRepository competencyLevelRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    // 🔹 Lấy danh sách competency theo nhân viên
    @Transactional(readOnly = true)
    public List<EmployeeCompetencyResponse> getByEmployeeId(UUID employeeId) {
        List<EmployeeCompetencyResponse> competencyDtos = competencyRepository.findByEmployeeProfile_Id(employeeId)
                .stream()
                .map(EmployeeCompetencyResponse::fromEntity)
                .toList();
        return competencyDtos;
    }

    // 🔹 Thêm mới competency bởi nhân viên đang đăng nhập
    public EmployeeCompetency create(EmployeeCompetencyDto dto, String token) throws ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);

        // Lấy user và employee profile tương ứng
        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        EmployeeProfile employeeProfile = uploader.getEmployeeProfile();

        // Lấy loại năng lực
        CompetencyType type = competencyTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Competency type not found"));

        // Lấy cấp độ năng lực (nếu có)
        CompetencyLevel level = null;
        if (dto.getLevelId() != null) {
            level = competencyLevelRepository.findById(dto.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Competency level not found"));
        }

        // Lấy tài liệu (nếu có)
        EmployeeDocument document = null;
        if (dto.getDocumentId() != null) {
            document = employeeDocumentRepository.findById(dto.getDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee document not found"));
        }

        // Map DTO → Entity
        EmployeeCompetency entity = EmployeeCompetencyDto.toEntity(
                dto,
                employeeProfile,
                type,
                level,
                document,
                uploader
        );

        entity.setUpdatedBy(uploader);
        entity.setUpdatedDate(LocalDateTime.now());

        // Lưu vào DB
        return competencyRepository.save(entity);
    }

    public EmployeeCompetency update(EmployeeCompetencyDto dto, String token) throws ResourceNotFoundException {
        // 1️⃣ Tìm entity gốc
        EmployeeCompetency entity = competencyRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Competency not found"));

        UUID userId = jwtUtil.getUserId(token);
        User updater = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2️⃣ Lấy quan hệ phụ (nếu có)
        CompetencyType type = competencyTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Competency type not found"));

        CompetencyLevel level = null;
        if (dto.getLevelId() != null) {
            level = competencyLevelRepository.findById(dto.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Competency level not found"));
        }

        EmployeeDocument document = null;
        if (dto.getDocumentId() != null) {
            document = employeeDocumentRepository.findById(dto.getDocumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee document not found"));
        }

        // 3️⃣ Cập nhật các trường được phép sửa
        entity.setType(type);
        entity.setLevel(level);
        entity.setDocument(document);

        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getIssuedBy() != null) entity.setIssuedBy(dto.getIssuedBy());
        if (dto.getIssuedDate() != null) entity.setIssuedDate(dto.getIssuedDate());
        if (dto.getExpireDate() != null) entity.setExpireDate(dto.getExpireDate());
        if (dto.getNote() != null) entity.setNote(dto.getNote());
        if (dto.getCertificateCode() != null) entity.setCertificateCode(dto.getCertificateCode());
        if (dto.getVerifyUrl() != null) entity.setVerifyUrl(dto.getVerifyUrl());


        if (updater.getRole().getName().equals("ROLE_ADMIN") || updater.getRole().getName().equals("ROLE_HR")) {
            entity.setVerificationStatus(VerificationStatus.VERIFIED);
            entity.setVerifiedDate(LocalDateTime.now());
            entity.setVerifiedBy(updater.getEmployeeProfile().getFullName());
        } else {
            entity.setVerificationStatus(VerificationStatus.PENDING);
            entity.setVerifiedBy(null);
            entity.setVerifiedDate(null);
        }

        entity.setUpdatedBy(updater);
        entity.setUpdatedDate(LocalDateTime.now());


        // 4️⃣ Lưu lại
        return competencyRepository.save(entity);
    }

    // 🔹 Xóa competency
    @Transactional
    public void delete(UUID id, Boolean isDeletedFile) throws ResourceNotFoundException {
        if (!competencyRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy năng lực: " + id);
        }


        // Nếu được yêu cầu thì xóa file đính kèm luôn
        if (isDeletedFile) {
            EmployeeCompetency employeeCompetency = competencyRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Competency not found"));
            if(employeeCompetency.getDocument() != null) {
                fileStorageService.deleteFile(BucketName.EMPLOYEE_DOCUMENT.getBucketName(), employeeCompetency.getDocument().getFileUrl());
            }
        }

        competencyRepository.deleteById(id);
    }
}
