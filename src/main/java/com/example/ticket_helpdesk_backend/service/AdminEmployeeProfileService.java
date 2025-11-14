package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyResponse;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminEmployeeProfileService {

    private final EmployeeCompetencyRepository employeeCompetencyRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public EmployeeCompetency approve(UUID id, String token) throws ResourceNotFoundException {
        EmployeeCompetency competency = employeeCompetencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competency not found"));

        UUID adminId = jwtUtil.getUserId(token);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        competency.setVerificationStatus(VerificationStatus.VERIFIED);
        competency.setVerifiedBy(admin.getEmployeeProfile().getFullName());
        competency.setVerifiedDate(LocalDateTime.now());

        return employeeCompetencyRepository.save(competency);
    }

    public EmployeeCompetency reject(UUID id, String reason, String token) throws ResourceNotFoundException {
        EmployeeCompetency competency = employeeCompetencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competency not found"));

        UUID adminId = jwtUtil.getUserId(token);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        competency.setVerificationStatus(VerificationStatus.REJECTED);
        competency.setVerifiedBy(admin.getEmployeeProfile().getFullName());
        competency.setVerifiedDate(LocalDateTime.now());
        //Bổ sung Reason

        return employeeCompetencyRepository.save(competency);
    }

    public List<EmployeeCompetencyResponse> getPendingCompetencies() {
        List<EmployeeCompetency> list = employeeCompetencyRepository.findByVerificationStatus(VerificationStatus.PENDING);


        return list.stream()
                .map(EmployeeCompetencyResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void changeDepartmentAndPosition(UUID employeeId, UUID departmentId, UUID positionId) throws ResourceNotFoundException {

        EmployeeProfile employeeProfile = employeeProfileRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Department newDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Position newPosition = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));

        // ❗ Check position có thuộc department không
        if (!newPosition.getDepartment().getId().equals(newDepartment.getId())) {
            throw new RuntimeException("Position does not belong to the selected department");
        }

        boolean changed = false;

        // 🔹 Update department nếu khác
        if (employeeProfile.getDepartment() == null ||
                !employeeProfile.getDepartment().getId().equals(newDepartment.getId())) {
            employeeProfile.setDepartment(newDepartment);
            changed = true;
        }

        // 🔹 Update position nếu khác
        if (employeeProfile.getPosition() == null ||
                !employeeProfile.getPosition().getId().equals(newPosition.getId())) {
            employeeProfile.setPosition(newPosition);
            changed = true;
        }

        if (changed) {
            employeeProfileRepository.save(employeeProfile);
            // TODO: Lưu lại lịch sử làm việc nếu bạn có bảng WorkHistory
        }
    }



}
