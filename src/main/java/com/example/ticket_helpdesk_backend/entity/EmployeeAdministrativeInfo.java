package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_admin_info")
@Getter
@Setter
public class EmployeeAdministrativeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private EmployeeProfile employeeProfile;

    // 🪪 Thông tin định danh
    @Column(length = 20)
    private String identityNumber; // CMND / CCCD

    private LocalDate identityIssuedDate;

    @Column(length = 100)
    private String identityIssuedPlace;

    // 💰 Thông tin tài chính
    @Column(length = 50)
    private String taxCode;

    @Column(length = 50)
    private String socialInsuranceNumber;

    @Column(length = 50)
    private String bankAccountNumber;

    @Column(length = 100)
    private String bankName;

    // ❤️ Tình trạng cá nhân
    @Column(length = 50)
    private String maritalStatus; // Độc thân / Kết hôn / Khác

    // 📦 Dự phòng mở rộng (nếu cần sau này)
    @Column(length = 255)
    private String note;
}

