package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.UserDTO;
import com.quiz.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved successfully"));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<com.quiz.application.dto.UserStatsDTO>> getCurrentUserStats() {
        com.quiz.application.dto.UserStatsDTO stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "User statistics retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUserProfile(@Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateCurrentUserProfile(userDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User profile updated successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
