package com.example.ticket_helpdesk_backend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class AttachmentUploadResponse {
    private String fileKey;     // object key, dùng để lưu DB
    private String fileUrl;     // presigned url để frontend hiển thị ngay
    private String fileName;
}
