package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.DocumentMetaDto;
import com.example.ticket_helpdesk_backend.dto.DocumentRelationCheckDto;
import com.example.ticket_helpdesk_backend.dto.DocumentTypeDto;
import com.example.ticket_helpdesk_backend.entity.DocumentType;
import com.example.ticket_helpdesk_backend.entity.EmployeeDocument;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.EmployeeDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
        List<UUID>  success = employeeDocumentService.uploadDocuments(token, files, metaList);

        ApiResponse<List<UUID>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                success.isEmpty() ? "Upload documents successfully" : "Upload documents failed",
                LocalDateTime.now(),
                success
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadDocument(@PathVariable UUID id) {
        try {
            // Lấy thông tin document
            EmployeeDocument doc = employeeDocumentService.getById(id);

            // Gọi service để tải file (InputStream)
            InputStream inputStream = employeeDocumentService.downloadFile(doc);

            // Xác định content type
            String contentType = doc.getFileType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            // Đặt tên file khi tải xuống
            String filename = doc.getFileName() != null ? doc.getFileName() : doc.getTitle();

            // Chuyển InputStream thành Resource (InputStreamResource)
            InputStreamResource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Không thể tải xuống tài liệu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) throws ResourceNotFoundException {
        employeeDocumentService.delete(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete competency successfully",
                        LocalDateTime.now(),
                        null
                )
        );
    }

    @GetMapping("/{id}/check-relations")
    public ResponseEntity<?> checkRelationWithCompetency(@PathVariable UUID id) {

        DocumentRelationCheckDto dto = employeeDocumentService.checkRelations(id);
        ApiResponse<DocumentRelationCheckDto> response = new ApiResponse<>(
                200,
                "Get document types successfully",
                LocalDateTime.now(),
                dto
        );
        return ResponseEntity.ok(response);
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
