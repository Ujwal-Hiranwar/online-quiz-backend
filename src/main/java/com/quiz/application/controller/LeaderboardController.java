package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.LeaderboardEntryDTO;
import com.quiz.application.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDTO> leaderboard = leaderboardService.getGlobalLeaderboard(limit);
        return ResponseEntity.ok(ApiResponse.success(leaderboard, "Global leaderboard retrieved successfully"));
    }
    
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDTO>>> getQuizLeaderboard(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDTO> leaderboard = leaderboardService.getQuizLeaderboard(quizId, limit);
        return ResponseEntity.ok(ApiResponse.success(leaderboard, "Quiz leaderboard retrieved successfully"));
    }
}
