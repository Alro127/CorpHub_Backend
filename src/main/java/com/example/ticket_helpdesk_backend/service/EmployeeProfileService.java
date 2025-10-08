package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.CreateEmployeeProfileRequest;
import com.example.ticket_helpdesk_backend.dto.CreateUserRequest;
import com.example.ticket_helpdesk_backend.dto.EmployeeProfileResponse;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.repository.DepartmentRepository;
import com.example.ticket_helpdesk_backend.repository.EmployeeProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeProfileService {
    @Autowired
    EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Transactional
    public boolean createEmployeeProfile(CreateEmployeeProfileRequest request) {

        EmployeeProfile employeeProfile = new EmployeeProfile();
        employeeProfile.setFullName(request.getFullName());
        employeeProfile.setDob(request.getDob());
        employeeProfile.setGender(request.getGender());
        employeeProfile.setPhone(request.getPhone());
        employeeProfile.setPersonalEmail(request.getPersonalEmail());
        employeeProfile.setAvatar(request.getAvatar());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employeeProfile.setDepartment(dept);
        }

        // ====== Map JobHistories ======
        if (request.getJobHistories() != null && !request.getJobHistories().isEmpty()) {
            List<EmployeeJobHistory> histories = request.getJobHistories().stream().map(j -> {
                EmployeeJobHistory job = new EmployeeJobHistory();
                job.setEmployeeProfile(employeeProfile);

                if (j.getDepartmentId() != null) {
                    Department jobDept = departmentRepository.findById(j.getDepartmentId())
                            .orElseThrow(() -> new RuntimeException("Department not found for job history"));
                    job.setDepartment(jobDept);
                }

                job.setPosition(j.getPosition());
                job.setContractType(j.getContractType());
                job.setStartDate(j.getStartDate());
                job.setEndDate(j.getEndDate());
                job.setEmploymentStatus(j.getEmploymentStatus());
                job.setNote(j.getNote());
                return job;
            }).toList();

            employeeProfile.setJobHistories(histories);
        }

        // ====== Map Competencies ======
        if (request.getCompetencies() != null && !request.getCompetencies().isEmpty()) {
            List<EmployeeCompetency> competencies = request.getCompetencies().stream().map(c -> {
                EmployeeCompetency competency = new EmployeeCompetency();
                competency.setEmployeeProfile(employeeProfile);
                competency.setType(c.getType());
                competency.setName(c.getName());
                competency.setLevel(c.getLevel());
                competency.setIssuedBy(c.getIssuedBy());
                competency.setIssuedDate(c.getIssuedDate());
                competency.setNote(c.getNote());
                return competency;
            }).toList();

            employeeProfile.setCompetencies(competencies);
        }

        // Lưu EmployeeProfile (cascading sẽ lưu cả jobHistories và competencies)
        employeeProfileRepository.save(employeeProfile);

        // ====== Thông báo cho IT ======
//        Ticket
//        ITNotification notif = new ITNotification();
//        notif.setEmployeeProfile(employeeProfile);
//        notif.setType("NEW_EMPLOYEE");
//        notif.setMessage("Nhân viên mới cần tạo tài khoản: " + employeeProfile.getFullName());
//        itNotificationRepository.save(notif);

        return true;
    }
    public List<EmployeeProfileResponse> getAllEmployeeProfiles() {
        return employeeProfileRepository.findAll().stream()
                .map(profile -> {
                    String avatarUrl = null;
                    if (profile.getAvatar() != null) {
                        avatarUrl = fileStorageService.getPresignedUrl("employee-avatars", profile.getAvatar());
                    }
                    return EmployeeProfileResponse.toResponse(profile, avatarUrl);
                })
                .toList();
    }

    public List<EmployeeProfile> getEmployeesByDepartment(UUID departmentId) {
        return employeeProfileRepository.findByDepartment_Id(departmentId);
    }

    public EmployeeProfile getEmployeeProfileById(UUID id) {
        return employeeProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

}
