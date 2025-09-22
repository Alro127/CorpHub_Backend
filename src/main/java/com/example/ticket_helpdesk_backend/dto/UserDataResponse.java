package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.Account;
import com.example.ticket_helpdesk_backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataResponse {
    private UUID id;
    private String fullname;
    private String gender;
    private LocalDate dob;
    private String email;
    private String phone;
    private String type;
    private LocalDate startDate;

    // Department
    private UUID departmentId;
    private String departmentName;
    private String departmentDescription;

    // Account
    private String roleName;
    private Boolean active;
    private LocalDateTime expired;

    // ✨ Hàm tiện ích để mapping từ entity sang DTO
    public static UserDataResponse fromEntity(User user, Account account) {
        if (user == null) {
            return null;
        }

        return new UserDataResponse(
                user.getId(),
                user.getFullname(),
                user.getGender(),
                user.getDob(),
                user.getEmail(),
                user.getPhone(),
                user.getType(),
                user.getStartDate(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getDepartment() != null ? user.getDepartment().getDescription() : null,
                account != null && account.getRole() != null ? account.getRole().getName() : null,
                account != null ? account.getActive() : null,
                account != null ? account.getExpired() : null
        );
    }

}
