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
    private String code;
    private String fullName;
    private String avatarUrl;
    private String position;
    private String gender;
    private String phone;
    private String personalEmail;
    private String address;
    private LocalDate dob;
    private LocalDate joinDate;
    private Boolean active;
    private String departmentName;
    private String managerName;
    private String about;

    private UserSummary user; // Thông tin tài khoản
    private List<EmployeeJobHistoryResponse> jobHistories;
    private List<EmployeeCompetencyDto> competencies;
    private List<EmployeeDocumentResponse> documents;
    private List<ActivityTimelineResponse> timeline; // Nhật ký hoạt động

    // ---- Inner class ----
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String username;
        private String roleName;
        private Boolean active;
    }

    // ---- Mapping ----
    public static EmployeeProfileResponse fromEntity(EmployeeProfile profile, String avatarUrl) {
        if (profile == null) return null;

        EmployeeProfileResponse res = new EmployeeProfileResponse();
        res.setId(profile.getId());
        res.setFullName(profile.getFullName());
        res.setGender(profile.getGender());
        res.setDob(profile.getDob());
        res.setPhone(profile.getPhone());
        res.setPersonalEmail(profile.getPersonalEmail());
        res.setAddress(profile.getAddress());
        res.setJoinDate(profile.getJoinDate());
        res.setAvatarUrl(avatarUrl);
        res.setActive(profile.getUser() != null ? profile.getUser().getActive() : null);
        res.setDepartmentName(profile.getDepartment() != null ? profile.getDepartment().getName() : null);

        // user summary
        User u = profile.getUser();
        if (u != null) {
            res.setUser(new UserSummary(
                    u.getId(),
                    u.getUsername(),
                    u.getRole() != null ? u.getRole().getName() : null,
                    u.getActive()
            ));
        }

        // convert các list con nếu có (jobHistories, competencies, documents)
        if (profile.getJobHistories() != null) {
            res.setJobHistories(profile.getJobHistories().stream()
                    .map(EmployeeJobHistoryResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (profile.getCompetencies() != null) {
            res.setCompetencies(profile.getCompetencies().stream()
                    .map(EmployeeCompetencyDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (profile.getDocuments() != null) {
            res.setDocuments(profile.getDocuments().stream()
                    .map(EmployeeDocumentResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return res;
    }
}
