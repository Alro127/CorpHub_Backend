package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ColumnDefault("newid()")
    private UUID id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "name", length = 100)
    private String name;

    @Nationalized
    @Lob
    private String description;

    // -----------------------------
    // 🔥 Quan hệ phòng ban cha – con
    // -----------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Department> children = new ArrayList<>();

    // -----------------------------
    // 👤 Manager
    // -----------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private EmployeeProfile manager;

    // -----------------------------
    // 👥 Danh sách nhân viên
    // -----------------------------
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<EmployeeProfile> employeeProfiles = new ArrayList<>();
}
