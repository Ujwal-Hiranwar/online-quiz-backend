package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer points;
    private Integer questionOrder;
    private String explanation;
    private Long quizId;
    private List<QuestionOptionDTO> options;
}
