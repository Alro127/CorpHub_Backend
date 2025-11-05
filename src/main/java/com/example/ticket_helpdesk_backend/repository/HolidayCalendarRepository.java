package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, UUID> {
}