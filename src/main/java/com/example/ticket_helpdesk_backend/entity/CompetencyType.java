package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "competency_type")
@Getter
@Setter
public class CompetencyType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;     // "SKILL", "DEGREE", "CERTIFICATE"

    @Column(nullable = false)
    private String name;     // Tên hiển thị: "Kỹ năng", "Bằng cấp"

    private String description; // Mô tả ngắn, optional
}

