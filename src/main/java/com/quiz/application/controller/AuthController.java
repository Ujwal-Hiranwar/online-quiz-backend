package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.AuthResponse;
import com.quiz.application.dto.LoginRequest;
import com.quiz.application.dto.RegisterRequest;
import com.quiz.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("AUTH-CONTROLLER: Received request to register user: " + request.getUsername());
        AuthResponse response = authService.register(request);
        System.out.println("AUTH-CONTROLLER: User registered successfully: " + request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("AUTH-CONTROLLER: Received request to login user: " + request.getUsername());
        AuthResponse response = authService.login(request);
        System.out.println("AUTH-CONTROLLER: User logged in successfully: " + request.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}
