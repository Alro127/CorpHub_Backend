package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBasicDto {
    private UUID id;
    private String fullName;
    private String email;
    private String avatar;
}

