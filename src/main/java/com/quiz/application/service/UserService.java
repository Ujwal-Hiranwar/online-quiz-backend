package com.quiz.application.service;

import com.quiz.application.dto.UserDTO;
import com.quiz.application.dto.UserStatsDTO;
import com.quiz.application.entity.QuizAttempt;
import com.quiz.application.entity.User;
import com.quiz.application.exception.ResourceNotFoundException;
import com.quiz.application.repository.QuizAttemptRepository;
import com.quiz.application.repository.UserRepository;
import com.quiz.application.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;


    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateCurrentUserProfile(UserDTO userDTO) {
        User user = getCurrentUser();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        // Add other fields to update as needed
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats() {
        User currentUser = getCurrentUser();
        List<QuizAttempt> completedAttempts = quizAttemptRepository.findCompletedAttemptsByUserId(currentUser.getId());

        if (completedAttempts.isEmpty()) {
            return UserStatsDTO.builder()
                    .totalQuizzesTaken(0)
                    .averageScore(0)
                    .bestScore(0)
                    .totalPoints(0)
                    .build();
        }

        long totalQuizzesTaken = completedAttempts.size();

        double averageScore = completedAttempts.stream()
                .mapToDouble(QuizAttempt::getPercentageScore)
                .average()
                .orElse(0.0);

        double bestScore = completedAttempts.stream()
                .mapToDouble(QuizAttempt::getPercentageScore)
                .max()
                .orElse(0.0);

        int totalPoints = completedAttempts.stream()
                .mapToInt(QuizAttempt::getScoreObtained)
                .sum();

        return UserStatsDTO.builder()
                .totalQuizzesTaken(totalQuizzesTaken)
                .averageScore(averageScore)
                .bestScore(bestScore)
                .totalPoints(totalPoints)
                .build();
    }
    
    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}
