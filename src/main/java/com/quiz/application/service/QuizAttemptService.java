package com.quiz.application.service;

import com.quiz.application.dto.*;
import com.quiz.application.entity.*;
import com.quiz.application.exception.BadRequestException;
import com.quiz.application.exception.ResourceNotFoundException;
import com.quiz.application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {
    
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private QuestionOptionRepository questionOptionRepository;
    
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;
    
    @Transactional
    public QuizAttemptDTO startQuiz(StartQuizRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Quiz quiz = quizRepository.findByIdWithQuestions(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + request.getQuizId()));
        
        QuizAttempt attempt = QuizAttempt.builder()
                .user(currentUser)
                .quiz(quiz)
                .startTime(LocalDateTime.now())
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .totalScore(quiz.getQuestions().stream().mapToInt(Question::getPoints).sum())
                .build();
        
        attempt = quizAttemptRepository.save(attempt);
        
        List<QuestionDTO> questions = questionService.getQuestionsByQuizId(quiz.getId());
        QuizAttemptDTO attemptDTO = convertToDTO(attempt);
        attemptDTO.setQuestions(questions);
        
        return attemptDTO;
    }
    
    @Transactional
    public UserAnswerDTO submitAnswer(SubmitAnswerRequest request) {
        User currentUser = userService.getCurrentUser();
        
        QuizAttempt attempt = quizAttemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found"));
        
        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("This attempt does not belong to you");
        }
        
        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This quiz attempt is not in progress");
        }
        
        Question question = questionRepository.findByIdWithOptions(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        
        if (!question.getQuiz().getId().equals(attempt.getQuiz().getId())) {
            throw new BadRequestException("This question does not belong to the quiz");
        }
        
        // Get selected options
        Set<QuestionOption> selectedOptions = new HashSet<>();
        for (Long optionId : request.getSelectedOptionIds()) {
            QuestionOption option = questionOptionRepository.findById(optionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Option not found with id: " + optionId));
            
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new BadRequestException("Option does not belong to this question");
            }
            selectedOptions.add(option);
        }
        
        // Check if answer already exists
        UserAnswer userAnswer = userAnswerRepository
                .findByQuizAttemptIdAndQuestionId(attempt.getId(), question.getId())
                .orElse(new UserAnswer());
        
        userAnswer.setQuizAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setSelectedOptions(selectedOptions);
        
        // Check if answer is correct
        Set<QuestionOption> correctOptions = question.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .collect(Collectors.toSet());
        
        boolean isCorrect = selectedOptions.equals(correctOptions);
        userAnswer.setIsCorrect(isCorrect);
        userAnswer.setPointsEarned(isCorrect ? question.getPoints() : 0);
        
        userAnswer = userAnswerRepository.save(userAnswer);
        return convertAnswerToDTO(userAnswer);
    }

    @Transactional
    public QuizAttemptDTO completeQuiz(CompleteQuizRequest request) {
        User currentUser = userService.getCurrentUser();

        QuizAttempt attempt = quizAttemptRepository.findByIdWithAnswers(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found"));

        if (!attempt.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("This attempt does not belong to you");
        }

        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This quiz attempt is already completed or abandoned");
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(QuizAttempt.AttemptStatus.COMPLETED);

        // Calculate score
        int scoreObtained = attempt.getAnswers().stream()
                .mapToInt(UserAnswer::getPointsEarned)
                .sum();

        attempt.setScoreObtained(scoreObtained);

        // Calculate percentage
        if (attempt.getTotalScore() > 0) {
            double percentage = (double) scoreObtained / attempt.getTotalScore() * 100;
            attempt.setPercentageScore(percentage);
            if (attempt.getQuiz().getPassingScore() != null) {
                attempt.setIsPassed(percentage >= attempt.getQuiz().getPassingScore());
            }
        } else {
            attempt.setPercentageScore(0.0);
            attempt.setIsPassed(false);
        }


        // Calculate time taken
        long minutes = Duration.between(attempt.getStartTime(), attempt.getEndTime()).toMinutes();
        attempt.setTimeTakenMinutes((int) minutes);

        attempt = quizAttemptRepository.save(attempt);
        return convertToDTOWithAnswers(attempt);
    }
    
    @Transactional(readOnly = true)
    public QuizAttemptDTO getAttemptById(Long id) {
        QuizAttempt attempt = quizAttemptRepository.findByIdWithAnswers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found"));
        
        User currentUser = userService.getCurrentUser();
        if (!attempt.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            throw new BadRequestException("You don't have permission to view this attempt");
        }
        
        return convertToDTOWithAnswers(attempt);
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getMyAttempts() {
        User currentUser = userService.getCurrentUser();
        Long userId = currentUser.getId();

        List<QuizAttempt> allUserAttempts = quizAttemptRepository.findByUserId(userId);

        // Filter for completed attempts first
        List<QuizAttempt> completedAttempts = allUserAttempts.stream()
                .filter(att -> att.getStatus() == QuizAttempt.AttemptStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<Long, List<QuizAttempt>> completedAttemptsByQuiz = completedAttempts.stream()
                .collect(Collectors.groupingBy(att -> att.getQuiz().getId()));

        Map<Long, Integer> attemptNumberMap = new HashMap<>();
        for (List<QuizAttempt> quizAttempts : completedAttemptsByQuiz.values()) {
            quizAttempts.sort(Comparator.comparing(QuizAttempt::getCreatedAt).thenComparing(QuizAttempt::getId));
            for (int i = 0; i < quizAttempts.size(); i++) {
                attemptNumberMap.put(quizAttempts.get(i).getId(), i + 1);
            }
        }

        return completedAttempts.stream()
                .sorted(Comparator.comparing(QuizAttempt::getEndTime).reversed())
                .map(attempt -> {
                    QuizAttemptDTO dto = convertToDTO(attempt);
                    dto.setAttemptCount(attemptNumberMap.get(attempt.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getAttemptsByQuizId(Long quizId) {
        User currentUser = userService.getCurrentUser();
        return quizAttemptRepository.findByUserIdAndQuizId(currentUser.getId(), quizId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public QuizAttemptDTO convertToDTO(QuizAttempt attempt) {
        return QuizAttemptDTO.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .username(attempt.getUser().getUsername())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .timeLimitMinutes(attempt.getQuiz().getTimeLimitMinutes())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .scoreObtained(attempt.getScoreObtained())
                .totalScore(attempt.getTotalScore())
                .percentageScore(attempt.getPercentageScore())
                .isPassed(attempt.getIsPassed())
                .status(attempt.getStatus().name())
                .timeTakenMinutes(attempt.getTimeTakenMinutes())
                .totalQuestions(attempt.getQuiz().getQuestions().size())
                .build();
    }
    
    private QuizAttemptDTO convertToDTOWithAnswers(QuizAttempt attempt) {
        QuizAttemptDTO dto = convertToDTO(attempt);
        dto.setAnswers(attempt.getAnswers().stream()
                .map(this::convertAnswerToDTO)
                .collect(Collectors.toList()));
        return dto;
    }
    
    private UserAnswerDTO convertAnswerToDTO(UserAnswer answer) {
        return UserAnswerDTO.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getQuestionText())
                .selectedOptionIds(answer.getSelectedOptions().stream()
                        .map(QuestionOption::getId)
                        .collect(Collectors.toSet()))
                .isCorrect(answer.getIsCorrect())
                .pointsEarned(answer.getPointsEarned())
                .explanation(answer.getQuestion().getExplanation())
                .build();
    }
}
