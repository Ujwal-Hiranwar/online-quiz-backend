package com.quiz.application.service;

import com.quiz.application.dto.QuestionCreateRequest;
import com.quiz.application.dto.QuestionDTO;
import com.quiz.application.dto.QuestionOptionDTO;
import com.quiz.application.entity.Question;
import com.quiz.application.entity.QuestionOption;
import com.quiz.application.entity.Quiz;
import com.quiz.application.entity.User;
import com.quiz.application.exception.BadRequestException;
import com.quiz.application.exception.ResourceNotFoundException;
import com.quiz.application.exception.UnauthorizedException;
import com.quiz.application.repository.QuestionRepository;
import com.quiz.application.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public QuestionDTO createQuestion(QuestionCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + request.getQuizId()));
        
        // Only the creator or admin can add questions
        if (currentUser.getRole() != User.Role.ADMIN && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to add questions to this quiz");
        }
        
        // Validate that at least one option is correct
        boolean hasCorrectOption = request.getOptions().stream()
                .anyMatch(opt -> opt.getIsCorrect());
        if (!hasCorrectOption) {
            throw new BadRequestException("At least one option must be marked as correct");
        }
        
        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .questionType(Question.QuestionType.valueOf(request.getQuestionType()))
                .points(request.getPoints())
                .questionOrder(request.getQuestionOrder())
                .explanation(request.getExplanation())
                .quiz(quiz)
                .build();
        
        // Add options
        for (int i = 0; i < request.getOptions().size(); i++) {
            var optReq = request.getOptions().get(i);
            QuestionOption option = QuestionOption.builder()
                    .optionText(optReq.getOptionText())
                    .isCorrect(optReq.getIsCorrect())
                    .optionOrder(optReq.getOptionOrder() != null ? optReq.getOptionOrder() : i + 1)
                    .build();
            question.addOption(option);
        }
        
        question = questionRepository.save(question);
        return convertToDTO(question);
    }
    
    @Transactional
    public void deleteQuestion(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        
        Quiz quiz = question.getQuiz();
        
        // Only the creator or admin can delete questions
        if (currentUser.getRole() != User.Role.ADMIN && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this question");
        }
        
        questionRepository.delete(question);
    }
    
    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        return convertToDTO(question);
    }
    
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizIdWithOptions(quizId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public QuestionDTO convertToDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .points(question.getPoints())
                .questionOrder(question.getQuestionOrder())
                .explanation(question.getExplanation())
                .quizId(question.getQuiz().getId())
                .options(question.getOptions().stream()
                        .map(this::convertOptionToDTO)
                        .collect(Collectors.toList()))
                .build();
    }
    
    private QuestionOptionDTO convertOptionToDTO(QuestionOption option) {
        return QuestionOptionDTO.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .isCorrect(option.getIsCorrect())
                .optionOrder(option.getOptionOrder())
                .build();
    }
}
