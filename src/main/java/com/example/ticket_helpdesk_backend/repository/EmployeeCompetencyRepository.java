package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.EmployeeCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeCompetencyRepository extends JpaRepository<EmployeeCompetency, UUID> {

    // Lấy danh sách competency theo employeeId
    List<EmployeeCompetency> findByEmployeeProfile_Id(UUID employeeId);
}
