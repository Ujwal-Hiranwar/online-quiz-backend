package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.QuizCreateRequest;
import com.quiz.application.dto.QuizDTO;
import com.quiz.application.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    
    @Autowired
    private QuizService quizService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizDTO>> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        QuizDTO quiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quiz, "Quiz created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizDTO>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizCreateRequest request) {
        QuizDTO quiz = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(ApiResponse.success(quiz, "Quiz updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Quiz deleted successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuizById(@PathVariable Long id) {
        QuizDTO quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(ApiResponse.success(quiz, "Quiz retrieved successfully"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getAllQuizzes() {
        List<QuizDTO> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Quizzes retrieved successfully"));
    }
    
    @GetMapping("/topic/{topic}")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getQuizzesByTopic(@PathVariable String topic) {
        List<QuizDTO> quizzes = quizService.getQuizzesByTopic(topic);
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Quizzes retrieved successfully"));
    }
    
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getAllTopics() {
        List<String> topics = quizService.getAllTopics();
        return ResponseEntity.ok(ApiResponse.success(topics, "Topics retrieved successfully"));
    }
    
    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getMyQuizzes() {
        List<QuizDTO> quizzes = quizService.getMyQuizzes();
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Your quizzes retrieved successfully"));
    }
}
