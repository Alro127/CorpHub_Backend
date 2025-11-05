package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByName(String name);

    @Query("""
        SELECT DISTINCT d FROM Department d
        LEFT JOIN FETCH d.employeeProfiles ep
        LEFT JOIN FETCH ep.user u
        """)
    List<Department> findAllWithUsers();

    boolean existsByName(String name);
}