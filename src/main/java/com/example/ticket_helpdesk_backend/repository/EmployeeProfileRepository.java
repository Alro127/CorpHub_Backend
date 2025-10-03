package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {

    List<EmployeeProfile> findByDepartment_Id(UUID departmentId);
}
