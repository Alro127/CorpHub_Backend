package com.example.ticket_helpdesk_backend.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
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

    private String fullName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String personalEmail;
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // Quan hệ ngược với User
    @OneToOne(mappedBy = "employeeProfile", fetch = FetchType.LAZY)
    private User user;

    // Quan hệ với JobHistory
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeJobHistory> jobHistories = new ArrayList<>();

    // Quan hệ với Competency
    @OneToMany(mappedBy = "employeeProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeCompetency> competencies = new ArrayList<>();
}

