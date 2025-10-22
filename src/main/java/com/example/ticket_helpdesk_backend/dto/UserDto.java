package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto implements Serializable {
    UUID id;
    @NotNull
    @Size(max = 100)
    String fullName;
    @Size(max = 10)
    String gender;
    LocalDate dob;
    @Size(max = 255)
    String email;
    @Size(max = 20)
    String phone;
    String avatar;
    Boolean active;
    LocalDate startDate;
    DepartmentDto department;

    static public UserDto toUserDto(User user) {
        if (user == null) return null;

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());

        if (user.getEmployeeProfile() != null) {
            userDto.setFullName(user.getEmployeeProfile().getFullName());
            userDto.setDob(user.getEmployeeProfile().getDob());
            userDto.setGender(user.getEmployeeProfile().getGender());
            userDto.setPhone(user.getEmployeeProfile().getPhone());
            userDto.setActive(user.getActive());
            userDto.setAvatar(user.getEmployeeProfile().getAvatar());
            // username là workemail
            userDto.setEmail(user.getUsername());
            userDto.setDepartment(
                    user.getEmployeeProfile().getDepartment() != null
                            ? new DepartmentDto(
                            user.getEmployeeProfile().getDepartment().getId(),
                            user.getEmployeeProfile().getDepartment().getName(),
                            user.getEmployeeProfile().getDepartment().getDescription()
                    )
                            : null
            );
        }

        return userDto;
    }

}