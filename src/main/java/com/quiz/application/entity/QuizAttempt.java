package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"user", "quiz", "answers"})
@ToString(exclude = {"user", "quiz", "answers"})
public class QuizAttempt extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "score_obtained")
    private Integer scoreObtained;
    
    @Column(name = "total_score")
    private Integer totalScore;
    
    @Column(name = "percentage_score")
    private Double percentageScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;
    
    @Column(name = "time_taken_minutes")
    private Integer timeTakenMinutes;
    
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserAnswer> answers = new ArrayList<>();
    
    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED, ABANDONED
    }
    
    public void addAnswer(UserAnswer answer) {
        answers.add(answer);
        answer.setQuizAttempt(this);
    }
}
