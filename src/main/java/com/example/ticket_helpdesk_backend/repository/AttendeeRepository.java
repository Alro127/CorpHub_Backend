package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.Attendee;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, UUID> {
    List<Attendee> findByMeeting_Id(UUID meetingId);

    List<Attendee> findByMeeting(Meeting saved);

    Attendee findByMeeting_IdAndEmail(UUID id, @Size(max = 255) String email);
}