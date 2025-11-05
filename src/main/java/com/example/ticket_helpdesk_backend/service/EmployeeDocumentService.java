package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.BucketName;
import com.example.ticket_helpdesk_backend.dto.DocumentMetaDto;
import com.example.ticket_helpdesk_backend.dto.DocumentTypeDto;
import com.example.ticket_helpdesk_backend.entity.DocumentType;
import com.example.ticket_helpdesk_backend.entity.EmployeeDocument;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;

    @Autowired
    EmployeeProfileRepository employeeProfileRepository;

    @Autowired
    DocumentTypeRepository documentTypeRepository;

    @Autowired
    EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    JwtUtil jwtUtil;

    public EmployeeDocument getById(UUID id) throws ResourceNotFoundException {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu với id: " + id));
    }

    /**
     * Tải file từ đường dẫn fileUrl trong DB.
     * Nếu fileUrl là đường dẫn tuyệt đối trong server (VD: "uploads/documents/..."),
     * ta đọc trực tiếp từ ổ đĩa.
     */
    public Resource downloadFile(EmployeeDocument document) throws Exception {
        String filePath = document.getFileUrl();

        // ✅ Trường hợp: fileUrl là đường dẫn hệ thống
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Không tìm thấy file trên hệ thống: " + filePath);
        }

        return new FileSystemResource(path);
    }

    @Transactional
    public boolean uploadDocuments(String token, List<MultipartFile> files, List<DocumentMetaDto> metaList) throws IOException, ResourceNotFoundException {
        UUID userId = jwtUtil.getUserId(token);

        EmployeeProfile profile = employeeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            DocumentMetaDto meta = metaList.get(i);

            // Lưu file vật lý hoặc cloud
            String fileUrl = fileStorageService.uploadFile(BucketName.EMPLOYEE_DOCUMENT.getBucketName(), file, profile.getFullName());

            EmployeeDocument doc = new EmployeeDocument();
            doc.setEmployeeProfile(profile);
            doc.setDocumentType(documentTypeRepository.findById(meta.getDocumentTypeId()).orElse(null));
            doc.setTitle(meta.getTitle());
            doc.setDescription(meta.getDescription());
            doc.setFileUrl(fileUrl);
            doc.setFileName(file.getOriginalFilename());
            doc.setFileType(file.getContentType());
            employeeDocumentRepository.save(doc);
        }

        return true;
    }

    public List<DocumentTypeDto> getAllDocumentTypes() {
        return documentTypeRepository.findAll()
                .stream()
                .map(DocumentTypeDto::fromEntity)
                .collect(Collectors.toList());
    }
}
