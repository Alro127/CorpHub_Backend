package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.DocumentMetaDto;
import com.example.ticket_helpdesk_backend.dto.DocumentTypeDto;
import com.example.ticket_helpdesk_backend.entity.DocumentType;
import com.example.ticket_helpdesk_backend.entity.EmployeeDocument;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmployeeDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee/document")
public class EmployeeDocumentController {
    @Autowired
    EmployeeDocumentService employeeDocumentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadEmployeeDocuments(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("meta") List<DocumentMetaDto> metaList
    ) throws IOException, ResourceNotFoundException {

        String token = authHeader.substring(7);
        boolean success = employeeDocumentService.uploadDocuments(token, files, metaList);

        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                success ? "Upload documents successfully" : "Upload documents failed",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID id) {
        try {
            EmployeeDocument doc = employeeDocumentService.getById(id);
            Resource resource = employeeDocumentService.downloadFile(doc);

            // Lấy content type động nếu có thể
            String contentType = doc.getFileType();
            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(resource.getFile().toPath());
                if (contentType == null) contentType = "application/octet-stream";
            }

            String filename = doc.getFileName() != null ? doc.getFileName() : doc.getTitle();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type")
    public ResponseEntity<?> getAllDocumentTypes() {
        List<DocumentTypeDto> types = employeeDocumentService.getAllDocumentTypes();
        ApiResponse<List<DocumentTypeDto>> response = new ApiResponse<>(
                200,
                "Get document types successfully",
                LocalDateTime.now(),
                types
        );
        return ResponseEntity.ok(response);
    }
}
