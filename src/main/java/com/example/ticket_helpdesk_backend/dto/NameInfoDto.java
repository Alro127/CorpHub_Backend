package com.example.ticket_helpdesk_backend.dto;

import com.example.ticket_helpdesk_backend.entity.UserDb;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameInfoDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 100)
    String fullName;
}