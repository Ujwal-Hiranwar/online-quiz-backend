package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswerDTO {
    private Long id;
    private Long questionId;
    private String questionText;
    private Set<Long> selectedOptionIds;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private String explanation;
}
