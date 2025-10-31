package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_document")
@Getter
@Setter
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Liên kết với EmployeeProfile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile;

    // Loại tài liệu: CONTRACT, CERTIFICATE, DECISION, ID_COPY, INSURANCE, OTHER,...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    // Tên tài liệu hiển thị
    @Column(nullable = false, length = 255)
    private String title;

    private String description;   // Ghi chú thêm
    @CreationTimestamp
    @Column(name = "upload_date", columnDefinition = "datetime2(7)")
    private LocalDateTime uploadDate;  // Ngày tải lên

    // 🗂️ Thông tin file upload
    private String fileUrl;       // Đường dẫn hoặc URL
    private String fileName;      // Tên file gốc
    private String fileType;      // MIME type

    @Column(nullable = false)
    private Boolean active = true; // Dùng để ẩn/hiện tài liệu
}
