package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeProfileRequest {
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String personalEmail;
    private String avatar;
    private UUID departmentId;

    private List<JobHistoryRequest> jobHistories;
    private List<CompetencyRequest> competencies;

    @Data
    public static class JobHistoryRequest {
        private UUID departmentId;
        private String position;
        private String contractType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String employmentStatus;
        private String note;
    }

    @Data
    public static class CompetencyRequest {
        private String type;   // SKILL, DEGREE, CERTIFICATION, LANGUAGE
        private String name;
        private String level;
        private String issuedBy;
        private LocalDate issuedDate;
        private String note;
    }
}
