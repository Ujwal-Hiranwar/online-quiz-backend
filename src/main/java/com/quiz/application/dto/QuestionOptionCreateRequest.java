package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionCreateRequest {
    
    @NotBlank(message = "Option text is required")
    private String optionText;
    
    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
    
    private Integer optionOrder;
}
