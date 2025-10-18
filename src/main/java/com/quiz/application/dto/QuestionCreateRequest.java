package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {
    
    @NotBlank(message = "Question text is required")
    private String questionText;
    
    @NotNull(message = "Question type is required")
    private String questionType;
    
    private Integer points = 1;
    
    private Integer questionOrder;
    
    private String explanation;
    
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
    
    @NotNull(message = "Options are required")
    private List<QuestionOptionCreateRequest> options;
}
