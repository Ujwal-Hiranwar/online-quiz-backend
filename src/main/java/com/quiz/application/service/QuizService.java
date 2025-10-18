package com.quiz.application.service;

import com.quiz.application.dto.QuizCreateRequest;
import com.quiz.application.dto.QuizDTO;
import com.quiz.application.entity.Question;
import com.quiz.application.entity.QuestionOption;
import com.quiz.application.entity.Quiz;
import com.quiz.application.entity.User;
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
public class QuizService {
    
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuestionService questionService;
    
    @Transactional
    public QuizDTO createQuiz(QuizCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        
        // Only admins can create quizzes
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can create quizzes");
        }
        
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .topic(request.getTopic())
                .difficultyLevel(Quiz.DifficultyLevel.valueOf(request.getDifficultyLevel()))
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .passingScore(request.getPassingScore())
                .active(request.getActive())
                .createdBy(currentUser)
                .build();
        quiz = quizRepository.save(quiz);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (var questionRequest : request.getQuestions()) {
                Question question = Question.builder()
                        .questionText(questionRequest.getQuestionText())
                        .questionType(Question.QuestionType.valueOf(questionRequest.getQuestionType()))
                        .points(questionRequest.getPoints())
                        .questionOrder(questionRequest.getQuestionOrder())
                        .explanation(questionRequest.getExplanation())
                        .quiz(quiz)
                        .build();

                for (int i = 0; i < questionRequest.getOptions().size(); i++) {
                    var optReq = questionRequest.getOptions().get(i);
                    QuestionOption option = QuestionOption.builder()
                            .optionText(optReq.getOptionText())
                            .isCorrect(optReq.getIsCorrect())
                            .optionOrder(optReq.getOptionOrder() != null ? optReq.getOptionOrder() : i + 1)
                            .build();
                    question.addOption(option);
                }
                questionRepository.save(question);
            }
        }

        return convertToDTO(quiz);
    }
    
    @Transactional
    public QuizDTO updateQuiz(Long id, QuizCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Only the creator or admin can update
        if (currentUser.getRole() != User.Role.ADMIN && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to update this quiz");
        }
        
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTopic(request.getTopic());
        quiz.setDifficultyLevel(Quiz.DifficultyLevel.valueOf(request.getDifficultyLevel()));
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setActive(request.getActive());
        
        quiz = quizRepository.save(quiz);
        return convertToDTO(quiz);
    }
    
    @Transactional
    public void deleteQuiz(Long id) {
        User currentUser = userService.getCurrentUser();
        
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        
        // Only the creator or admin can delete
        if (currentUser.getRole() != User.Role.ADMIN && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this quiz");
        }
        
        quizRepository.delete(quiz);
    }
    
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        return convertToDTOWithQuestions(quiz);
    }
    
    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesByTopic(String topic) {
        return quizRepository.findByTopic(topic).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllTopics() {
        return quizRepository.findAllDistinctTopics();
    }
    
    @Transactional(readOnly = true)
    public List<QuizDTO> getMyQuizzes() {
        User currentUser = userService.getCurrentUser();
        return quizRepository.findByCreatedBy(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private QuizDTO convertToDTO(Quiz quiz) {
        return QuizDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .topic(quiz.getTopic())
                .difficultyLevel(quiz.getDifficultyLevel().name())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .active(quiz.getActive())
                .createdById(quiz.getCreatedBy().getId())
                .createdByUsername(quiz.getCreatedBy().getUsername())
                .totalQuestions(quiz.getQuestions().size())
                .totalPoints(quiz.getQuestions().stream().mapToInt(q -> q.getPoints()).sum())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }
    
    private QuizDTO convertToDTOWithQuestions(Quiz quiz) {
        QuizDTO dto = convertToDTO(quiz);
        dto.setQuestions(quiz.getQuestions().stream()
                .map(questionService::convertToDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}
