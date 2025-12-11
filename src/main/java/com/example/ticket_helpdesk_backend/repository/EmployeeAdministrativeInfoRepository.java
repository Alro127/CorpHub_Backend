package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.EmployeeAdministrativeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeAdministrativeInfoRepository extends JpaRepository<EmployeeAdministrativeInfo, UUID> {

    Optional<EmployeeAdministrativeInfo> findByEmployeeProfileId(UUID employeeId);

    boolean existsByEmployeeProfileId(UUID employeeId);
}