package com.example.ticket_helpdesk_backend.service;

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

        // Lưu vào DB
        return competencyRepository.save(entity);
    }

    // 🔹 Xóa competency
    public void delete(UUID id) {
        if (!competencyRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy năng lực: " + id);
        }
        competencyRepository.deleteById(id);
    }
}
