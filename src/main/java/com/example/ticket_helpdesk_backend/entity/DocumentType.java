package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "document_type")
@Getter
@Setter
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code; // Ví dụ: CONTRACT, CERTIFICATE, INSURANCE

    @Column(nullable = false, length = 255)
    private String name; // Tên hiển thị: "Hợp đồng lao động", "Chứng chỉ", ...

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}
