package com.quiz.application.repository;

import com.quiz.application.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    List<UserAnswer> findByQuizAttemptId(Long quizAttemptId);
    
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.quizAttempt.id = :attemptId AND ua.question.id = :questionId")
    Optional<UserAnswer> findByQuizAttemptIdAndQuestionId(@Param("attemptId") Long attemptId, @Param("questionId") Long questionId);
}
