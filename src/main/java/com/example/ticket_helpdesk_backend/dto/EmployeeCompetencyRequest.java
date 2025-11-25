package com.example.ticket_helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompetencyRequest {

    private UUID id; // null khi tạo mới, có giá trị khi update

    // --- Thông tin loại năng lực ---
    private UUID typeId;  // ID của CompetencyType

    // --- Thông tin năng lực ---
    private String name;
    private UUID level;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expireDate;
    private String note;

    // --- Liên kết tài liệu ---
    private UUID documentId;        // optional - file chứng minh

    // --- Xác thực cơ bản (do nhân viên khai báo) ---
    private String certificateCode; // optional
    private String verifyUrl;       // optional
}

