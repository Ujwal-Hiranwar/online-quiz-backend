package com.quiz.application.repository;

import com.quiz.application.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    List<QuizAttempt> findByUserId(Long userId);
    
    List<QuizAttempt> findByQuizId(Long quizId);
    
    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.status = 'COMPLETED' ORDER BY qa.endTime DESC")
    List<QuizAttempt> findCompletedAttemptsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT qa FROM QuizAttempt qa LEFT JOIN FETCH qa.answers WHERE qa.id = :id")
    Optional<QuizAttempt> findByIdWithAnswers(@Param("id") Long id);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.status = 'COMPLETED' ORDER BY qa.scoreObtained DESC")
    List<QuizAttempt> findTopScoresByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.status = 'COMPLETED' ORDER BY qa.scoreObtained DESC")
    List<QuizAttempt> findAllCompletedOrderedByScore();
}
