package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employee_profile")
@Getter
@Setter
public class EmployeeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50, unique = true)
    private String code; // Mã nhân viên được tạo dựa trên họ và tên + số nguời trùng tên trong db

    @Nationalized
    @Column(nullable = false, length = 150)
    private String fullName;

    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String personalEmail;

    private String avatar;

    @Nationalized
    @Column(length = 255)
    private String address;

    private LocalDate joinDate;

    /** 🔹 Chức danh hiện tại (đã thay từ String -> Position) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    /** 🔹 Quản lý trực tiếp */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private EmployeeProfile manager;

    @Lob
    @Nationalized
    private String about; // mô tả cá nhân

    /** 🔹 Phòng ban hiện tại */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** 🔹 Tài khoản đăng nhập (User) */
    @OneToOne(mappedBy = "employeeProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    /** 🔹 Thông tin hành chính */
    @OneToOne(mappedBy = "employeeProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private EmployeeAdministrativeInfo administrativeInfo;

    /** 🔹 Lịch sử hợp đồng / trạng thái làm việc */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeJobHistory> jobHistories = new ArrayList<>();

    /** 🔹 Kinh nghiệm trước khi vào công ty */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExternalWorkHistory> externalWorkHistories = new ArrayList<>();

    /** 🔹 Lịch sử công tác nội bộ (thăng chức / chuyển phòng) */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternalWorkHistory> internalWorkHistories = new ArrayList<>();

    /** 🔹 Kỹ năng, chứng chỉ */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeCompetency> competencies = new ArrayList<>();

    /** 🔹 Tài liệu nhân viên */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeDocument> documents = new ArrayList<>();

    /** 🔹 Dòng thời gian sự nghiệp */
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeTimeline> timelines = new ArrayList<>();
}
