package com.quiz.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteQuizRequest {
    
    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
}
