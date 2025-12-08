package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.AttachmentUploadResponse;
import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import com.example.ticket_helpdesk_backend.repository.AbsenceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AbsenceAttachmentService {

    private final FileStorageService fileStorageService;
    private final AbsenceRequestRepository repository;

    private static final String BUCKET = "absence";
    private static final String PREFIX = "attachments";

    public AbsenceRequest getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Absence request not found"));
    }

    public String uploadTemp(MultipartFile file) {
        return fileStorageService.uploadFile(BUCKET, file, PREFIX);
    }

    public void deleteTemp(String objectKey) {
        fileStorageService.deleteFile(BUCKET, objectKey);
    }

    /* ------------------------------------------
     * Replace attachment
     * ------------------------------------------ */
    public AttachmentUploadResponse replaceAttachment(UUID requestId, MultipartFile file) {

        AbsenceRequest req = getById(requestId);
        String oldKey = req.getAttachmentUrl();

        // Xóa file cũ
        if (oldKey != null) {
            fileStorageService.deleteFile(BUCKET, oldKey);
        }

        // Upload file mới
        String newKey = fileStorageService.uploadFile(BUCKET, file, PREFIX);
        String newUrl = fileStorageService.getPresignedUrl(BUCKET, newKey);

        // Update DB trực tiếp
        req.setAttachmentUrl(newKey);
        repository.save(req);

        return new AttachmentUploadResponse(newKey, newUrl);
    }

    /* ------------------------------------------
     * Delete attachment
     * ------------------------------------------ */
    public void deleteAttachment(UUID requestId) {

        AbsenceRequest req = getById(requestId);
        String key = req.getAttachmentUrl();

        if (key != null) {
            fileStorageService.deleteFile(BUCKET, key);
        }

        req.setAttachmentUrl(null);
        repository.save(req);
    }

    /* ------------------------------------------
     * Download
     * ------------------------------------------ */
    public InputStream downloadAttachment(UUID requestId) {

        AbsenceRequest req = getById(requestId);
        return fileStorageService.downloadFile(BUCKET, req.getAttachmentUrl());
    }

    public String detectContentType(String key) {
        try {
            return Files.probeContentType(Path.of(key));
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}