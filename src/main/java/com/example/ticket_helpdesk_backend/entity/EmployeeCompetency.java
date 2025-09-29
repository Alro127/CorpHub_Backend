package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_competency")
@Getter
@Setter
public class EmployeeCompetency {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile;

    private String type;   // SKILL, DEGREE, CERTIFICATION, LANGUAGE
    private String name;
    private String level;
    private String issuedBy;
    private LocalDate issuedDate;
    private String note;
}
