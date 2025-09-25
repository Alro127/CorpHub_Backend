package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByDepartment_Id(UUID departmentId);

    @Query("SELECT u FROM User u " +
            "WHERE LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByeFullNameOrEmail(@Param("keyword") String keyword);

}