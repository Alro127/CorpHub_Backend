package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.BucketName;
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
    private final FileStorageService fileStorageService;

    public List<TicketAttachmentDTO> getAttachmentsByTicketId(UUID ticketId) {
        return attachmentRepository.findByTicket_Id(ticketId)
                .stream()
                .map(att -> {
                    TicketAttachmentDTO dto = modelMapper.map(att, TicketAttachmentDTO.class);
                    dto.setTicketId(att.getTicket().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TicketAttachment> saveAttachments(UUID ticketId, List<MultipartFile> files) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        return files.stream().map(file -> {
            String objectName = fileStorageService.uploadFile(BucketName.TICKET_ATTACHMENT.getBucketName(), file, ticketId.toString());

            TicketAttachment attachment = new TicketAttachment();
            attachment.setTicket(ticket);
            attachment.setPath(objectName);
            attachment.setOriginalName(file.getOriginalFilename());
            attachment.setCreatedAt(LocalDateTime.now());

            return attachmentRepository.save(attachment);
        }).collect(Collectors.toList());
    }

    public InputStream downloadAttachment(UUID attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        return fileStorageService.downloadFile(BucketName.TICKET_ATTACHMENT.getBucketName(), attachment.getPath());
    }

    public void deleteAttachment(UUID attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        fileStorageService.deleteFile(BucketName.TICKET_ATTACHMENT.getBucketName(), attachment.getPath());
        attachmentRepository.delete(attachment);
    }

    public String getFileName(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(TicketAttachment::getOriginalName)
                .orElse(null);
    }

}
