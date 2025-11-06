package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Entity
@Table(name = "timeline_type")
@Getter
@Setter
public class TimelineType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String code; // Mã loại: JOIN, PROMOTION, AWARD, TRANSFER, PROJECT,...

    @Nationalized
    @Column(nullable = false, length = 100)
    private String name; // Tên hiển thị: Gia nhập công ty, Thăng chức, Khen thưởng,...

    @Nationalized
    @Column(length = 255)
    private String description;

    /** 🧩 Tên icon (ví dụ: "UserRound", "ShieldCheck", "Star", ...) */
    @Column(length = 100)
    private String icon;

    @Column(nullable = false)
    private Boolean active = true;
}
