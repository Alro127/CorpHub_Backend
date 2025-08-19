package com.example.ticket_helpdesk_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "user_db")
public class UserDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Size(max = 150)
    @NotNull
    @Nationalized
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Size(max = 20)
    @Nationalized
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 200)
    @Nationalized
    @Column(name = "pass_word", length = 200)
    private String passWord;

    @Size(max = 10)
    @NotNull
    @Nationalized
    @ColumnDefault("'offline'")
    @Column(name = "status", nullable = false, length = 10)
    private String status;

}