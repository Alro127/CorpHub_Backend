package com.example.ticket_helpdesk_backend.repository;

import java.util.UUID;

public interface EmployeeScopeView {
    UUID getEmployeeId();
    String getCode();
    String getFullName();
}
