package com.quiz.application.service;

import com.quiz.application.dto.AdminStatsDTO;
import com.quiz.application.dto.AdminUserCreateRequest;
import com.quiz.application.dto.QuizAttemptDTO;
import com.quiz.application.dto.UserDTO;
import com.quiz.application.entity.User;
import com.quiz.application.exception.BadRequestException;
import com.quiz.application.exception.DuplicateResourceException;
import com.quiz.application.repository.*;
import com.quiz.application.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizAttemptService quizAttemptService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        long totalQuizzes = quizRepository.count();
        long totalQuestions = questionRepository.count();
        long totalUsers = userRepository.count();
        long totalAttempts = quizAttemptRepository.count();

        return AdminStatsDTO.builder()
                .totalQuizzes(totalQuizzes)
                .totalQuestions(totalQuestions)
                .totalUsers(totalUsers)
                .totalAttempts(totalAttempts)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO addUser(AdminUserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.valueOf(request.getRole()))
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        return userService.convertToDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getId().equals(id)) {
            throw new BadRequestException("You cannot delete your own account.");
        }

        userRepository.deleteById(id);
    }
}
