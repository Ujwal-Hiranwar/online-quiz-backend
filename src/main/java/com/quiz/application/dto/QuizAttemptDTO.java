package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long quizId;
    private String quizTitle;
    private Integer timeLimitMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer scoreObtained;
    private Integer totalScore;
    private Double percentageScore;
    private Boolean isPassed;
    private String status;
    private Integer timeTakenMinutes;
    private List<UserAnswerDTO> answers;
    private List<QuestionDTO> questions;
    private Integer totalQuestions;
    private Integer attemptCount;
}
