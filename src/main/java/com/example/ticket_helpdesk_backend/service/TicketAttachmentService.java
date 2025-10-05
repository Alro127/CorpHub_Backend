package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.TicketAttachmentDTO;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.TicketAttachment;
import com.example.ticket_helpdesk_backend.repository.TicketAttachmentRepository;
import com.example.ticket_helpdesk_backend.repository.TicketRepository;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketAttachmentService {

    private final ModelMapper modelMapper;
    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final MinioClient minioClient;

    private final String bucketName = "ticket-attachments";

    public List<TicketAttachmentDTO> getAttachmentsByTicketId(UUID ticketId) {
        List<TicketAttachment> attachments = attachmentRepository.findByTicket_Id(ticketId);

        return attachments.stream()
                .map(att -> {
                    TicketAttachmentDTO dto = modelMapper.map(att, TicketAttachmentDTO.class);
                    dto.setTicketId(att.getTicket().getId()); // map thủ công
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public List<TicketAttachment> saveAttachments(UUID ticketId, List<MultipartFile> files) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        return files.stream().map(file -> {
            try {
                String objectName = ticketId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

                try (InputStream is = file.getInputStream()) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucketName)
                                    .object(objectName)
                                    .stream(is, file.getSize(), -1)
                                    .contentType(file.getContentType())
                                    .build()
                    );
                }

                TicketAttachment attachment = new TicketAttachment();
                attachment.setTicket(ticket);
                attachment.setPath(objectName); // lưu objectName, không cần lưu file vật lý local
                attachment.setOriginalName(file.getOriginalFilename());
                attachment.setCreatedAt(LocalDateTime.now());
                return attachmentRepository.save(attachment);

            } catch (Exception e) {
                throw new RuntimeException("Error uploading file to MinIO", e);
            }
        }).collect(Collectors.toList());
    }

    public InputStream downloadAttachment(UUID attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(attachment.getPath())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }

    public void deleteAttachment(UUID attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(attachment.getPath())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from MinIO", e);
        }

        attachmentRepository.delete(attachment);
    }

    public String getFileName(UUID attachmentId) {


        Optional<TicketAttachment> ticketAttachment = attachmentRepository.findById(attachmentId);

        String fileName = ticketAttachment.get().getOriginalName();

        return fileName;
    }
}
