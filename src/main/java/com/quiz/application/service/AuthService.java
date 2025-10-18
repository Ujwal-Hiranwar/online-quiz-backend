package com.quiz.application.service;

import com.quiz.application.dto.AuthResponse;
import com.quiz.application.dto.LoginRequest;
import com.quiz.application.dto.RegisterRequest;
import com.quiz.application.entity.User;
import com.quiz.application.exception.BadRequestException;
import com.quiz.application.exception.DuplicateResourceException;
import com.quiz.application.repository.UserRepository;
import com.quiz.application.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("AUTH-SERVICE: Attempting to register user: " + request.getUsername());

        System.out.println("AUTH-SERVICE: Checking if username exists: " + request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            System.out.println("AUTH-SERVICE: Username already exists: " + request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }
        System.out.println("AUTH-SERVICE: Username does not exist.");

        System.out.println("AUTH-SERVICE: Checking if email exists: " + request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("AUTH-SERVICE: Email already exists: " + request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }
        System.out.println("AUTH-SERVICE: Email does not exist.");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .active(true)
                .build();

        System.out.println("AUTH-SERVICE: User object created: " + user);

        try {
            System.out.println("AUTH-SERVICE: Attempting to save user to the database...");
            userRepository.save(user);
            System.out.println("AUTH-SERVICE: User saved successfully to the database.");
        } catch (DataAccessException e) {
            System.out.println("AUTH-SERVICE: ERROR: Failed to save user to the database.");
            System.out.println("AUTH-SERVICE: Exception: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception to let the global exception handler deal with it
        }

        return AuthResponse.builder()
                .message("User registered successfully")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        System.out.println("AUTH-SERVICE: Attempting to login user: " + request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        System.out.println("AUTH-SERVICE: JWT token generated for user: " + request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        System.out.println("AUTH-SERVICE: User found in database: " + user.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
