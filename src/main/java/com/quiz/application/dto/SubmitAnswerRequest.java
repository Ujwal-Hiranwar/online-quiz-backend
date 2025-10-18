package com.quiz.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    
    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotNull(message = "Selected options are required")
    private Set<Long> selectedOptionIds;
}
