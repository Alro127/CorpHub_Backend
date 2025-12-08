package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.PositionChangeAttachmentDto;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/position-change/attachments")
@RequiredArgsConstructor
public class PositionChangeAttachmentController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedById") String uploadedById
    ) {
        String bucket = "position-change";
        String prefix = "attachments";

        // 1. Upload file lên MinIO
        String fileKey = fileStorageService.uploadFile(bucket, file, prefix);

        // 2. Generate URL cho UI hiển thị (presigned)
        String fileUrl = fileStorageService.getPresignedUrl(bucket, fileKey);

        PositionChangeAttachmentDto dto = new PositionChangeAttachmentDto();
        dto.setFileName(file.getOriginalFilename());
        dto.setFileUrl(fileUrl);
        dto.setUploadedById(uploadedById);

        ApiResponse<PositionChangeAttachmentDto> res = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Uploaded successfully",
                LocalDateTime.now(),
                dto
        );

        return ResponseEntity.ok(res);
    }
}

