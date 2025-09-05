package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Department;
import com.example.ticket_helpdesk_backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
  List<Ticket> findByDepartmentId(UUID id);

  @Query(value = """
    SELECT t.* 
    FROM ticket t
    JOIN [user] u ON t.requester_id = u.id
    WHERE u.department_id = :departmentId
    """, nativeQuery = true)
  List<Ticket> findSentTicketByDepartmentId(@Param("departmentId") UUID departmentId);


  List<Ticket> findMyTicketsByRequesterId(UUID id);
}