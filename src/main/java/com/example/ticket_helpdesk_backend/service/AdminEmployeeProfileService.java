package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyResponse;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.EmployeeCompetencyRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminEmployeeProfileService {

    private final EmployeeCompetencyRepository employeeCompetencyRepository;
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

}
