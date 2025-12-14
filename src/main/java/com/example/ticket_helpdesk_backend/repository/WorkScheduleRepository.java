package com.example.ticket_helpdesk_backend.repository;

import com.example.ticket_helpdesk_backend.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID>, JpaSpecificationExecutor<WorkSchedule> {

    @Query("""
        select 
            ep.id as employeeId,
            ep.code as employeeCode,
            ep.fullName as employeeFullName,

            ws.workDate as workDate,

            s.id as shiftId,
            s.name as shiftName,
            s.startTime as shiftStartTime,
            s.endTime as shiftEndTime,

            cast(ws.status as string) as status
        from WorkSchedule ws
        join ws.user u
        join u.employeeProfile ep
        join ws.shift s
        where ws.workDate between :fromDate and :toDate
          and (:departmentId is null or ep.department.id = :departmentId)
          and (:employeeIds is null or ep.id in :employeeIds)
        order by ep.code asc, ws.workDate asc
    """)
    List<WorkScheduleExportRowView> findRowsForExport(
            LocalDate fromDate,
            LocalDate toDate,
            UUID departmentId,
            List<UUID> employeeIds
    );
}