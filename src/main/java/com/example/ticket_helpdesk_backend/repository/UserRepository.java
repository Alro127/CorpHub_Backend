package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Ticket;
import com.example.ticket_helpdesk_backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    // lấy danh sách user theo phòng ban
    List<User> findByEmployeeProfile_Department_Id(UUID departmentId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN u.employeeProfile ep " +
            "WHERE LOWER(ep.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByFullNameOrUsername(@Param("keyword") String keyword);

    @Query("""
       SELECT u FROM User u
       JOIN FETCH u.employeeProfile e
       JOIN FETCH e.department d
       JOIN FETCH e.position p
       WHERE u.id = :id
       """)
    Optional<User> findDetailById(@Param("id") UUID id);
}