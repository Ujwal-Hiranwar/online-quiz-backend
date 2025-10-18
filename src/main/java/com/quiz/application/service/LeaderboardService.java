package com.quiz.application.service;

import com.quiz.application.dto.LeaderboardEntryDTO;
import com.quiz.application.entity.QuizAttempt;
import com.quiz.application.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;
    
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getGlobalLeaderboard(int limit) {
        List<QuizAttempt> allAttempts = quizAttemptRepository.findAllCompletedOrderedByScore();
        
        // Group by user and calculate total scores
        Map<Long, List<QuizAttempt>> attemptsByUser = allAttempts.stream()
                .collect(Collectors.groupingBy(attempt -> attempt.getUser().getId()));
        
        List<LeaderboardEntryDTO> leaderboard = new ArrayList<>();
        
        for (Map.Entry<Long, List<QuizAttempt>> entry : attemptsByUser.entrySet()) {
            List<QuizAttempt> userAttempts = entry.getValue();
            
            int totalScore = userAttempts.stream()
                    .mapToInt(QuizAttempt::getScoreObtained)
                    .sum();
            
            double averageScore = userAttempts.stream()
                    .mapToDouble(QuizAttempt::getPercentageScore)
                    .average()
                    .orElse(0.0);
            
            QuizAttempt firstAttempt = userAttempts.get(0);
            
            LeaderboardEntryDTO dto = LeaderboardEntryDTO.builder()
                    .userId(firstAttempt.getUser().getId())
                    .username(firstAttempt.getUser().getUsername())
                    .firstName(firstAttempt.getUser().getFirstName())
                    .lastName(firstAttempt.getUser().getLastName())
                    .totalScore(totalScore)
                    .averageScore(averageScore)
                    .attemptCount(userAttempts.size())
                    .build();
            
            leaderboard.add(dto);
        }
        
        // Sort by total score descending
        leaderboard.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        
        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }
        
        // Return top N
        return leaderboard.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getQuizLeaderboard(Long quizId, int limit) {
        List<QuizAttempt> quizAttempts = quizAttemptRepository.findTopScoresByQuizId(quizId);

        // Group by user and get best score
        Map<Long, QuizAttempt> bestAttemptsByUser = quizAttempts.stream()
                .collect(Collectors.toMap(
                        attempt -> attempt.getUser().getId(),
                        attempt -> attempt,
                        (a1, a2) -> a1.getScoreObtained() >= a2.getScoreObtained() ? a1 : a2
                ));

        List<LeaderboardEntryDTO> leaderboard = bestAttemptsByUser.values().stream()
                .map(attempt -> LeaderboardEntryDTO.builder()
                        .userId(attempt.getUser().getId())
                        .username(attempt.getUser().getUsername())
                        .firstName(attempt.getUser().getFirstName())
                        .lastName(attempt.getUser().getLastName())
                        .totalScore(attempt.getScoreObtained())
                        .averageScore(attempt.getPercentageScore())
                        .attemptCount(1)
                        .quizId(attempt.getQuiz().getId())
                        .quizTitle(attempt.getQuiz().getTitle())
                        .totalQuestions(attempt.getQuiz().getQuestions().size())
                        .build())
                .sorted(Comparator.comparing(LeaderboardEntryDTO::getTotalScore).reversed())
                .collect(Collectors.toList());

        // Assign ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        // Return top N
        return leaderboard.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
