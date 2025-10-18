package com.quiz.application.controller;

import com.quiz.application.dto.*;
import com.quiz.application.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attempts")
public class QuizAttemptController {
    
    @Autowired
    private QuizAttemptService quizAttemptService;
    
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> startQuiz(@Valid @RequestBody StartQuizRequest request) {
        QuizAttemptDTO attempt = quizAttemptService.startQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attempt, "Quiz started successfully"));
    }
    
    @PostMapping("/submit-answer")
    public ResponseEntity<ApiResponse<UserAnswerDTO>> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        UserAnswerDTO answer = quizAttemptService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponse.success(answer, "Answer submitted successfully"));
    }
    
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> completeQuiz(@Valid @RequestBody CompleteQuizRequest request) {
        QuizAttemptDTO attempt = quizAttemptService.completeQuiz(request);
        return ResponseEntity.ok(ApiResponse.success(attempt, "Quiz completed successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> getAttemptById(@PathVariable Long id) {
        QuizAttemptDTO attempt = quizAttemptService.getAttemptById(id);
        return ResponseEntity.ok(ApiResponse.success(attempt, "Attempt retrieved successfully"));
    }
    
    @GetMapping("/my-attempts")
    public ResponseEntity<ApiResponse<List<QuizAttemptDTO>>> getMyAttempts() {
        List<QuizAttemptDTO> attempts = quizAttemptService.getMyAttempts();
        return ResponseEntity.ok(ApiResponse.success(attempts, "Your attempts retrieved successfully"));
    }
    
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuizAttemptDTO>>> getAttemptsByQuizId(@PathVariable Long quizId) {
        List<QuizAttemptDTO> attempts = quizAttemptService.getAttemptsByQuizId(quizId);
        return ResponseEntity.ok(ApiResponse.success(attempts, "Quiz attempts retrieved successfully"));
    }
}
