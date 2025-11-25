package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Entity
@Table(name = "position")
@Getter
@Setter
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** 🔹 Phòng ban mà chức danh thuộc về */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** 🔹 Tên chức danh (Senior Developer, Tech Lead…) */
    @Nationalized
    @Column(nullable = false, length = 255)
    private String name;

    /** 🔹 Mã chức danh (DEV_SENIOR, QA_LEAD…) – optional */
    @Column(length = 100)
    private String code;

    /** 🔹 Mô tả trách nhiệm – optional */
    @Nationalized
    @Column(columnDefinition = "nvarchar(max)")
    private String description;

    /** 🔹 Thứ tự cấp bậc trong phòng ban (dùng cho kéo–thả, sort) */
    @Column(name = "level_order")
    private Integer levelOrder;
}
