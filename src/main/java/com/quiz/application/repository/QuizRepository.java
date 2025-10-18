package com.quiz.application.repository;

import com.quiz.application.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByTopic(String topic);
    
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
    
    @Query("SELECT DISTINCT q.topic FROM Quiz q ORDER BY q.topic")
    List<String> findAllDistinctTopics();
    
    @Query("SELECT q FROM Quiz q WHERE q.createdBy.id = :userId")
    List<Quiz> findByCreatedBy(@Param("userId") Long userId);
}
