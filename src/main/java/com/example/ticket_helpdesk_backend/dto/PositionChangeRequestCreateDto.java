package com.example.ticket_helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PositionChangeRequestCreateDto {

    private String employeeId;

    private String oldPositionId;     // optional
    private String oldDepartmentId;   // optional

    private String newPositionId;
    private String newDepartmentId;

    private String type;              // PROMOTION / TRANSFER / ...

    private LocalDate effectDate;

    private String reason;

    private String createdById;       // ai tạo: nhân viên / manager / HR / admin

    private List<PositionChangeAttachmentDto> attachments;
}