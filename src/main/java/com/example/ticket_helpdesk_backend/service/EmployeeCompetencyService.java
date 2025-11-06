package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.EmployeeCompetencyDto;
import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.repository.EmployeeCompetencyRepository;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    JwtUtil jwtUtil;

    // 🔹 Lấy danh sách competency theo nhân viên
    @Transactional(readOnly = true)
    public List<EmployeeCompetency> getByEmployeeId(UUID employeeId) {
        return competencyRepository.findByEmployeeProfile_Id(employeeId);
    }

    // 🔹 Thêm mới
    public EmployeeCompetency create(EmployeeCompetencyDto competency, String token) {
        EmployeeCompetency employeeCompetency = EmployeeCompetencyDto.toEntity(competency);

        UUID userId = jwtUtil.getUserId(token);

        EmployeeProfile employeeProfile = employeeProfileRepository.findById(userId).orElseThrow();

        employeeCompetency.setEmployeeProfile(employeeProfile);

        return competencyRepository.save(employeeCompetency);
    }

    // 🔹 Cập nhật
    public EmployeeCompetency update(UUID id, EmployeeCompetencyDto updated) {
        EmployeeCompetency existing = competencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy năng lực: " + id));

        existing.setType(updated.getType());
        existing.setName(updated.getName());
        existing.setLevel(updated.getLevel());
        existing.setIssuedBy(updated.getIssuedBy());
        existing.setIssuedDate(updated.getIssuedDate());
        existing.setExpireDate(updated.getExpireDate());
        existing.setNote(updated.getNote());

        return competencyRepository.save(existing);
    }

    // 🔹 Xóa
    public void delete(UUID id) {
        if (!competencyRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy năng lực: " + id);
        }
        competencyRepository.deleteById(id);
    }
}
