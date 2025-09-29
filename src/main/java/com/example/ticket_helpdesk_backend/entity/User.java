package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "[user]")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String username; // hoặc workEmail

    @Column(nullable = false)
    private String password; // hashed (BCrypt)

    @Column(nullable = false)
    private Boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Size(max = 20)
    @Nationalized
    @Column(name = "otp", length = 20)
    private String otp;

    @Column(name = "expired", nullable = false)
    private LocalDateTime expired;

    // Liên kết 1-1 tới EmployeeProfile
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private EmployeeProfile employeeProfile;
}