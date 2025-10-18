package com.quiz.application.controller;

import com.quiz.application.dto.AdminStatsDTO;
import com.quiz.application.dto.AdminUserCreateRequest;
import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.UserDTO;
import com.quiz.application.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDTO>> getAdminStats() {
        AdminStatsDTO stats = adminService.getAdminStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Admin statistics retrieved successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> addUser(@Valid @RequestBody AdminUserCreateRequest request) {
        UserDTO newUser = adminService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUser, "User created successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
