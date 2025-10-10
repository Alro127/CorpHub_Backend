package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.TicketAttachmentDTO;
import com.example.ticket_helpdesk_backend.entity.TicketAttachment;
import com.example.ticket_helpdesk_backend.service.TicketAttachmentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-attachments")
@RequiredArgsConstructor
public class TicketAttachmentController {

    private final TicketAttachmentService attachmentService;

    // === GET list attachments of a ticket ===
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getAttachments(@PathVariable String ticketId) {
        System.out.println("Raw ticketId from path = " + ticketId);
        UUID id = UUID.fromString(ticketId);
        List<TicketAttachmentDTO> attachmentDTOList = attachmentService.getAttachmentsByTicketId(id);
        ApiResponse<List<TicketAttachmentDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Load attachments successfully",
                LocalDateTime.now(),
                attachmentDTOList
        );
        return ResponseEntity.ok(response);
    }

    // === UPLOAD attachments ===
    @PostMapping("/{ticketId}")
    public ResponseEntity<?> uploadAttachments(
            @PathVariable UUID ticketId,
            @RequestParam("attachments") List<MultipartFile> files) {

        List<TicketAttachment> saved = attachmentService.saveAttachments(ticketId, files);
        ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Upload attachments successfully",
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.ok(response);
    }

    // === DELETE attachment ===
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{attachmentId}")
    public void downloadAttachment(@PathVariable UUID attachmentId,
                                   HttpServletResponse response) throws IOException {
        try (InputStream stream = attachmentService.downloadAttachment(attachmentId)) {
            String filename = attachmentService.getFileName(attachmentId);

            String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            String safeFileName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

            String contentDisposition = "attachment; filename=\"" + safeFileName + "\"; filename*=UTF-8''" + encodedFileName;
            response.setHeader("Content-Disposition", contentDisposition);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setContentType("application/octet-stream");

            org.springframework.util.StreamUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        }
    }

}
