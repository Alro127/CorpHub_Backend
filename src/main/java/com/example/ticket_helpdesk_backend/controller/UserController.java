package com.example.ticket_helpdesk_backend.controller;

import com.example.ticket_helpdesk_backend.dto.ApiResponse;
import com.example.ticket_helpdesk_backend.dto.NameInfoDto;
import com.example.ticket_helpdesk_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/name-info")
    public ResponseEntity<?> getUser() {
        List<NameInfoDto> nameInfoDtoList = userService.getNameInfo();
        ApiResponse<List<NameInfoDto>> apiResponse = new ApiResponse<>(
                HttpStatus.OK.value(),
                "All user found",
                LocalDateTime.now(),
                nameInfoDtoList
        );

        return ResponseEntity.ok(apiResponse);
    }
}
