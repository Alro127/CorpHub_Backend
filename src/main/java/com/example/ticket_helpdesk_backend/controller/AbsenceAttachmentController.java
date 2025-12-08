package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.AttachmentUploadResponse;
import com.example.ticket_helpdesk_backend.service.AbsenceAttachmentService;
import com.example.ticket_helpdesk_backend.service.AbsenceRequestService;
import com.example.ticket_helpdesk_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/absence/attachments")
@RequiredArgsConstructor
public class AbsenceAttachmentController {

    private final AbsenceAttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    /* -----------------------------------------
     * 1️⃣ Upload file tạm trước khi submit
     * ----------------------------------------- */
    @PostMapping("/upload-temp")
    public ResponseEntity<?> uploadTemp(@RequestParam("file") MultipartFile file) {

        String objectKey = attachmentService.uploadTemp(file);
        String url = fileStorageService.getPresignedUrl("absence", objectKey);

        String fileName = attachmentService.extractFileName(objectKey);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        "Temporary upload successful",
                        LocalDateTime.now(),
                        new AttachmentUploadResponse(objectKey, url, fileName)
                )
        );
    }

    @DeleteMapping("/temp")
    public ResponseEntity<?> deleteTemp(@RequestParam String objectKey) {

        attachmentService.deleteTemp(objectKey);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Temp file deleted", LocalDateTime.now(), null)
        );
    }

    /* -----------------------------------------
     * 2️⃣ Replace attachment sau submit
     * ----------------------------------------- */
    @PutMapping("/{requestId}")
    public ResponseEntity<?> replace(
            @PathVariable UUID requestId,
            @RequestParam("file") MultipartFile file
    ) {
        var result = attachmentService.replaceAttachment(requestId, file);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Attachment replaced", LocalDateTime.now(), result)
        );
    }

    /* -----------------------------------------
     * 3️⃣ Delete attachment sau submit
     * ----------------------------------------- */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> delete(@PathVariable UUID requestId) {

        attachmentService.deleteAttachment(requestId);

        return ResponseEntity.ok(
                new ApiResponse<>(200, "Attachment deleted", LocalDateTime.now(), null)
        );
    }

    /* -----------------------------------------
     * 4️⃣ Download attachment
     * ----------------------------------------- */
    @GetMapping("/{requestId}/download")
    public ResponseEntity<?> download(@PathVariable UUID requestId) {

        String objectKey = attachmentService.getObjectKey(requestId);
        String fileName = attachmentService.extractFileName(objectKey);
        String contentType = attachmentService.detectContentType(objectKey);
        InputStream stream = attachmentService.downloadAttachment(requestId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Content-Type", contentType)
                .body(new InputStreamResource(stream));
    }

}


