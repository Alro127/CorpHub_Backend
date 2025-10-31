package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.service.DepartmentService;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {
    @Autowired
    private final DepartmentService departmentService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllDepartments() {
        List<DepartmentDto> departmentDtoList = departmentService.getDepartmentDtoList();
        ApiResponse<List<DepartmentDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All departments found",
                LocalDateTime.now(),
                departmentDtoList);

        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/users")
    public ResponseEntity<?> getUsersDepartment(@RequestHeader("Authorization") String authHeader) throws ResourceNotFoundException {
        String token = authHeader.substring(7);
        UUID userId = jwtUtil.getUserId(token);
        List<UserDto> usersDepartmentList = departmentService.getUsersByDepartment(userId);
        ApiResponse<List<UserDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All users of Department found",
                LocalDateTime.now(),
                usersDepartmentList
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/with-users")
    public ResponseEntity<?> getAllDepartmentsWithUsers() {
        List<DepartmentUsersGroupDto> result = departmentService.getAllDepartmentsWithUsers();

        ApiResponse<List<DepartmentUsersGroupDto>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All Departments with users found",
                LocalDateTime.now(),
                result
        );
        return ResponseEntity.ok(response);
    }

//    @GetMapping
//    public ResponseEntity<List<Department>> getAllDepartments() {
//        return ResponseEntity.ok(departmentService.getAllDepartments());
//    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Department> getDepartmentById(@PathVariable UUID id) {
//        return ResponseEntity.ok(departmentService.getDepartmentById(id));
//    }

    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentManagementDto department) throws ResourceNotFoundException {

        ApiResponse<DepartmentManagementDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Create department successfully",
                LocalDateTime.now(),
                departmentService.createDepartment(department)
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentManagementDto department
    ) throws ResourceNotFoundException {
        ApiResponse<DepartmentManagementDto> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Update department successfully",
                LocalDateTime.now(),
                departmentService.updateDepartment(id, department)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) throws ResourceNotFoundException {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign-manager/{managerId}")
    public ResponseEntity<DepartmentManagementDto> assignManager(
            @PathVariable UUID id,
            @PathVariable UUID managerId
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(departmentService.assignManager(id, managerId));
    }

}
