package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeBasicInfoResponse {

    private UUID id;
    private String code;

    private String fullName;
    private LocalDate dob;
    private String gender;

    private String positionName;
    private UUID positionId;

    private String departmentName;
    private UUID departmentId;

    private String managerName;
    private UUID managerId;

    private LocalDate joinDate;

    public static HrEmployeeBasicInfoResponse toBasicInfo(EmployeeProfile profile) {
        return new HrEmployeeBasicInfoResponse(
                profile.getId(),
                profile.getCode(),
                profile.getFullName(),
                profile.getDob(),
                profile.getGender(),
                profile.getPosition() != null ? profile.getPosition().getName() : null,
                profile.getPosition() != null ? profile.getPosition().getId() : null,
                profile.getDepartment() != null ? profile.getDepartment().getName() : null,
                profile.getDepartment() != null ? profile.getDepartment().getId() : null,
                profile.getManager() != null ? profile.getManager().getFullName() : null,
                profile.getManager() != null ? profile.getManager().getId() : null,
                profile.getJoinDate()
        );
    }

}
