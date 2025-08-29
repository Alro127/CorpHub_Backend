package com.example.ticket_helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.example.ticket_helpdesk_backend.entity.User}
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
    @Size(max = 50)
    String type;
    LocalDate startDate;
    UUID departmentId;
}