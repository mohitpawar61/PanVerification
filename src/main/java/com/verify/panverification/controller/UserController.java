package com.verify.panverification.controller;

import com.verify.panverification.dto.*;
import com.verify.panverification.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @RequestBody RegisterRequest request
    ) {
        log.info("User Registration Request Received for {}",
                request.FullName());

        String msg = service.register(request);

        log.info("User Registered Successfully");

        return ResponseEntity.ok(
                new ApiResponse(
                        true,
                        msg,
                        null
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginRequest request
    ) {
        log.info("Login Request Received for {}"
                );

        ResponseEntity<ApiResponse<LoginResponse>> response =
                ResponseEntity.ok(service.login(request));

        log.info("Login Successful for {}"
                );

        return response;    }
}