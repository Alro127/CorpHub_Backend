package com.example.ticket_helpdesk_backend.entity;

import com.example.ticket_helpdesk_backend.consts.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_competency")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeProfile employeeProfile; // Chủ sở hữu năng lực

    // --- Thông tin năng lực / chứng chỉ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private CompetencyType type;     // SKILL, DEGREE, CERTIFICATION, LANGUAGE
    private String name;
    private String level;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expireDate;
    private String note;

    // --- Liên kết tài liệu minh chứng ---
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = true)
    private EmployeeDocument document;

    // --- Thông tin xác thực ---
    private String certificateCode;
    private String verifyUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private String verifiedBy;
    private LocalDate verifiedDate;

    // --- Người upload ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = true)
    private User uploadedBy; // người upload (Admin, HR, hoặc chính nhân viên)
}
