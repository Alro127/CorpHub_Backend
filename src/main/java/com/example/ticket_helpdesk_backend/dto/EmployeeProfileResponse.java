package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileResponse {
    private UUID id;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String personalEmail;
    private String avatarUrl;      // URL từ MinIO (convert từ objectName)
    private String departmentName; // lấy từ Department

    private UserSummary user; // tóm tắt thông tin account
    private List<JobHistoryResponse> jobHistories;
    private List<CompetencyResponse> competencies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String username;
        private String roleName;
        private Boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobHistoryResponse {
        private UUID id;
        private String departmentName;
        private String position;
        private String contractType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String employmentStatus;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetencyResponse {
        private UUID id;
        private String type;   // SKILL, DEGREE, CERTIFICATION, LANGUAGE
        private String name;
        private String level;
        private String issuedBy;
        private LocalDate issuedDate;
        private String note;
    }

    public static EmployeeProfileResponse toResponse(EmployeeProfile profile, String avatarUrl) {
        if (profile == null) return null;

        EmployeeProfileResponse response = new EmployeeProfileResponse();
        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setDob(profile.getDob());
        response.setGender(profile.getGender());
        response.setPhone(profile.getPhone());
        response.setPersonalEmail(profile.getPersonalEmail());

        response.setAvatarUrl(avatarUrl); // convert từ MinIO objectName -> URL
        response.setDepartmentName(
                profile.getDepartment() != null ? profile.getDepartment().getName() : null
        );

        // User summary
        User user = profile.getUser();
        if (user != null) {
            response.setUser(new EmployeeProfileResponse.UserSummary(
                    user.getId(),
                    user.getUsername(),
                    user.getRole() != null ? user.getRole().getName() : null,
                    user.getActive()
            ));
        }

        // Job histories
        if (profile.getJobHistories() != null) {
            response.setJobHistories(profile.getJobHistories().stream()
                    .map(job -> new EmployeeProfileResponse.JobHistoryResponse(
                            job.getId(),
                            job.getDepartment() != null ? job.getDepartment().getName() : null,
                            job.getPosition(),
                            job.getContractType(),
                            job.getStartDate(),
                            job.getEndDate(),
                            job.getEmploymentStatus(),
                            job.getNote()
                    ))
                    .collect(Collectors.toList()));
        }

        // Competencies
        if (profile.getCompetencies() != null) {
            response.setCompetencies(profile.getCompetencies().stream()
                    .map(c -> new EmployeeProfileResponse.CompetencyResponse(
                            c.getId(),
                            c.getType(),
                            c.getName(),
                            c.getLevel(),
                            c.getIssuedBy(),
                            c.getIssuedDate(),
                            c.getNote()
                    ))
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
