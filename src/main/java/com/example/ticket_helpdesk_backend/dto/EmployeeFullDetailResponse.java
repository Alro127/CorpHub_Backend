package com.example.ticket_helpdesk_backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFullDetailResponse {

    // 1) Personal Info
    private UUID id;
    private String code;
    private String fullName;
    private String avatarUrl;
    private String gender;
    private LocalDate dob;
    private String phone;
    private String personalEmail;
    private String address;
    private String about;
    private LocalDate joinDate;

    // 2) Job Info
    private String positionName;
    private UUID positionId;
    private String departmentName;
    private UUID departmentId;
    private String managerName;
    private UUID managerId;
    private Boolean active;

    // 3) Administrative Info
    private EmployeeAdministrativeInfoDto administrativeInfo;

    // 4) Competencies
    private List<EmployeeCompetencyResponse> competencies;

    // 5) Documents
    private List<EmployeeDocumentResponse> documents;

    // 6) Internal Work History
    private List<InternalWorkHistoryDto> internalHistories;

    // 7) Position Change Requests
    private List<PositionChangeRequestDetailDto> positionChangeRequests;
}
