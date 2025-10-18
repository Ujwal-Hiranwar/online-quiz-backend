package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDTO {
    private Integer rank;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Integer totalScore;
    private Double averageScore;
    private Integer attemptCount;
    private Long quizId;
    private String quizTitle;
    private Integer totalQuestions;
}
