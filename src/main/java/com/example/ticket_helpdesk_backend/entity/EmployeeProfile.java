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
    private String code; // Mã nhân viên (A001,...)

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

    @Nationalized
    @Column(length = 100)
    private String position; // Chức danh hiện tại

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "manager_id")
     private EmployeeProfile manager;

    @Lob
    @Nationalized
    private String about; // mô tả cá nhân

    // ===================== Quan hệ =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(mappedBy = "employeeProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeJobHistory> jobHistories = new ArrayList<>();

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeCompetency> competencies = new ArrayList<>();

    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeDocument> documents = new ArrayList<>();
}
