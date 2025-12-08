package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.EmployeeProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {

    List<EmployeeProfile> findByDepartment_Id(UUID departmentId);
    Page<EmployeeProfile> findByFullNameContainingIgnoreCase(String keyword, Pageable pageable);
    // Lấy 1 HR_MANAGER bất kỳ (hoặc sau này custom rule)
    Optional<EmployeeProfile> findFirstByUserRoleName(String roleName);

    // Lấy tất cả admin (nếu cần)
    List<EmployeeProfile> findByUserRoleName(String roleName);
}
