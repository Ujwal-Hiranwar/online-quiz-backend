package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"quizAttempt"})
@ToString(exclude = {"quizAttempt"})
public class UserAnswer extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_answer_options",
        joinColumns = @JoinColumn(name = "user_answer_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private Set<QuestionOption> selectedOptions = new HashSet<>();
    
    @Column(nullable = false)
    private Boolean isCorrect = false;
    
    @Column(name = "points_earned")
    private Integer pointsEarned = 0;
}
