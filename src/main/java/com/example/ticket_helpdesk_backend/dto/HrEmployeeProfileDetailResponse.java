package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeProfileDetailResponse {

    private HrEmployeeBasicInfoResponse basicInfo;
    private HrEmployeeContactInfoResponse contactInfo;
    private HrEmployeeAdministrativeInfoResponse administrativeInfo;

    public HrEmployeeProfileDetailResponse toDetail(EmployeeProfile profile) {
        return new HrEmployeeProfileDetailResponse(
                HrEmployeeBasicInfoResponse.toBasicInfo(profile),
                HrEmployeeContactInfoResponse.toContactInfo(profile),
                HrEmployeeAdministrativeInfoResponse.toAdministrativeInfo(profile.getAdministrativeInfo())
        );
    }
}