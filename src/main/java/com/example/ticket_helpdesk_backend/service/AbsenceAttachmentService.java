package com.example.ticket_helpdesk_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AbsenceAttachmentService {

    private final FileStorageService fileStorageService;

    private static final String BUCKET = "absence";
    private static final String PREFIX = "attachments";

    public String uploadProofFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File minh chứng không hợp lệ.");
        }

        return fileStorageService.uploadFile(BUCKET, file, PREFIX);
    }

    public String generatePresignedUrl(String objectKey) {
        return fileStorageService.getPresignedUrl(BUCKET, objectKey);
    }

    public void deleteProofFile(String objectKey) {
        if (objectKey != null) {
            fileStorageService.deleteFile(BUCKET, objectKey);
        }
    }

    public InputStream downloadFile(String objectKey) {
        return fileStorageService.downloadFile(BUCKET, objectKey);
    }

    public String detectContentType(String objectKey) {
        try {
            return Files.probeContentType(Path.of(objectKey));
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

}
