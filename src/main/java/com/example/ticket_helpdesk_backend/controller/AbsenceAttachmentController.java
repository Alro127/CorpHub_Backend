package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.AttachmentUploadResponse;
import com.example.ticket_helpdesk_backend.service.AbsenceAttachmentService;
import com.example.ticket_helpdesk_backend.service.AbsenceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/absence/attachments")
@RequiredArgsConstructor
public class AbsenceAttachmentController {

    private final AbsenceAttachmentService absenceAttachmentService;
    private final AbsenceRequestService 

    /* ----------------------------------------------------
     * 1️⃣ Upload file tạm trước khi submit request
     * ---------------------------------------------------- */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadProof(@RequestParam("file") MultipartFile file) {

        String objectKey = absenceAttachmentService.uploadProofFile(file);
        String presignedUrl = absenceAttachmentService.generatePresignedUrl(objectKey);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Upload attachment successfully",
                        LocalDateTime.now(),
                        new AttachmentUploadResponse(objectKey, presignedUrl)
                )
        );
    }

    /* ----------------------------------------------------
     * 2️⃣ Xóa attachment tạm trước khi submit
     * ---------------------------------------------------- */
    @DeleteMapping
    public ResponseEntity<?> deleteTempAttachment(@RequestParam String objectKey) {

        absenceAttachmentService.deleteProofFile(objectKey);



        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Attachment deleted successfully",
                        LocalDateTime.now(),
                        null
                )
        );
    }

    /* ----------------------------------------------------
     * 3️⃣ Thay thế attachment (xóa file cũ, upload file mới)
     * ---------------------------------------------------- */
    @PutMapping
    public ResponseEntity<?> replaceAttachment(
            @RequestParam("file") MultipartFile newFile,
            @RequestParam("oldKey") String oldKey
    ) {
        // 1. Xóa file cũ
        absenceAttachmentService.deleteProofFile(oldKey);

        // 2. Upload file mới
        String newKey = absenceAttachmentService.uploadProofFile(newFile);
        String newUrl = absenceAttachmentService.generatePresignedUrl(newKey);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Attachment replaced successfully",
                        LocalDateTime.now(),
                        new AttachmentUploadResponse(newKey, newUrl)
                )
        );
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadAttachment(@RequestParam String objectKey) {
        InputStream fileStream = absenceAttachmentService.downloadFile(objectKey);
        String contentType = absenceAttachmentService.detectContentType(objectKey);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + Paths.get(objectKey).getFileName() + "\"")
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .body(new InputStreamResource(fileStream));
    }

}

