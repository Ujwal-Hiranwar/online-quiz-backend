# Online Quiz Application - Backend Development Plan

## Project Overview
**Project ID:** 65HIBKJS  
**Technology Stack:** Spring Boot 3.x, MySQL, Spring Security, JPA/Hibernate  
**Timeline:** 25 days  
**Purpose:** College Internship Selection Project

---

## Backend Architecture Overview

### Technology Stack
- **Framework:** Spring Boot 3.x
- **Database:** MySQL 8.x
- **ORM:** Spring Data JPA (Hibernate)
- **Security:** Spring Security with JWT
- **Build Tool:** Maven
- **Java Version:** 17 or higher

### Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/quiz/application/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   └── QuizApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml
└── README.md
```

---

## PHASE 1: Project Setup and Configuration

### Step 1.1: Create Spring Boot Project from Spring Initializr
Go to https://start.spring.io/ and configure:
- **Project:** Maven
- **Language:** Java
- **Spring Boot:** 3.2.x (latest stable)
- **Project Metadata:**
  - Group: `com.quiz`
  - Artifact: `application`
  - Name: `Online Quiz Application`
  - Package name: `com.quiz.application`
  - Packaging: `Jar`
  - Java: `17`

**Dependencies to Add:**
1. Spring Web
2. Spring Data JPA
3. MySQL Driver
4. Spring Security
5. Validation
6. Lombok
7. Spring Boot DevTools

Download and extract the project to your `backend/` folder.

### Step 1.2: Configure MySQL Database

**1.2.1: Create Database in MySQL**
Open MySQL Workbench or command line and execute:
```sql
CREATE DATABASE quiz_application;
CREATE USER 'quiz_user'@'localhost' IDENTIFIED BY 'quiz_password123';
GRANT ALL PRIVILEGES ON quiz_application.* TO 'quiz_user'@'localhost';
FLUSH PRIVILEGES;
```

**1.2.2: Configure application.properties**
Location: `src/main/resources/application.properties`

```properties
# Application Name
spring.application.name=Online Quiz Application

# Server Configuration
server.port=8080
server.servlet.context-path=/api

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/quiz_application?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=quiz_user
spring.datasource.password=quiz_password123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Logging Configuration
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=INFO
logging.level.com.quiz.application=DEBUG

# File Upload Configuration (for future use)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Jackson Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC
```

**1.2.3: Create application-dev.properties** (for development)
Location: `src/main/resources/application-dev.properties`

```properties
# Development specific settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
logging.level.org.springframework.web=TRACE
```

### Step 1.3: Verify Project Setup
Run the Spring Boot application:
```bash
cd backend
./mvnw spring-boot:run
```

Expected output: Application starts on port 8080 without errors.

---

## PHASE 2: Database Entity Design and Creation

### Step 2.1: Create Base Entity Class

**Purpose:** All entities will extend this base class for common fields.

**Location:** `src/main/java/com/quiz/application/entity/BaseEntity.java`

```java
package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Step 2.2: Create User Entity

**Location:** `src/main/java/com/quiz/application/entity/User.java`

```java
package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"quizAttempts"})
@ToString(exclude = {"quizAttempts"})
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<QuizAttempt> quizAttempts = new HashSet<>();
    
    public enum Role {
        USER, ADMIN
    }
}
```

### Step 2.3: Create Quiz Entity

**Location:** `src/main/java/com/quiz/application/entity/Quiz.java`

```java
package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"questions", "quizAttempts"})
@ToString(exclude = {"questions", "quizAttempts"})
public class Quiz extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 100)
    private String topic;
    
    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
    
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;
    
    @Column(name = "passing_score")
    private Integer passingScore;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionOrder ASC")
    private List<Question> questions = new ArrayList<>();
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<QuizAttempt> quizAttempts = new HashSet<>();
    
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
    
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }
    
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }
}
```

### Step 2.4: Create Question Entity

**Location:** `src/main/java/com/quiz/application/entity/Question.java`

```java
package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"quiz", "options"})
@ToString(exclude = {"quiz", "options"})
public class Question extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;
    
    @Column(nullable = false)
    private Integer points = 1;
    
    @Column(name = "question_order")
    private Integer questionOrder;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("optionOrder ASC")
    private List<QuestionOption> options = new ArrayList<>();
    
    public enum QuestionType {
        SINGLE_CHOICE, MULTIPLE_CHOICE
    }
    
    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }
    
    public void removeOption(QuestionOption option) {
        options.remove(option);
        option.setQuestion(null);
    }
}
```

### Step 2.5: Create QuestionOption Entity

**Location:** `src/main/java/com/quiz/application/entity/QuestionOption.java`

```java
package com.quiz.application.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"question"})
@ToString(exclude = {"question"})
public class QuestionOption extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;
    
    @Column(nullable = false)
    private Boolean isCorrect = false;
    
    @Column(name = "option_order")
    private Integer optionOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
```

### Step 2.6: Create QuizAttempt Entity

**Location:** `src/main/java/com/quiz/application/entity/QuizAttempt.java`

```java
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
```

### Step 2.7: Create UserAnswer Entity

**Location:** `src/main/java/com/quiz/application/entity/UserAnswer.java`

```java
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
```

### Step 2.8: Enable JPA Auditing

**Location:** `src/main/java/com/quiz/application/config/JpaConfig.java`

```java
package com.quiz.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

---

## PHASE 3: Create Repository Layer

### Step 3.1: Create UserRepository

**Location:** `src/main/java/com/quiz/application/repository/UserRepository.java`

```java
package com.quiz.application.repository;

import com.quiz.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.active = true")
    Optional<User> findActiveUserByUsername(String username);
}
```

### Step 3.2: Create QuizRepository

**Location:** `src/main/java/com/quiz/application/repository/QuizRepository.java`

```java
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
    
    List<Quiz> findByActiveTrue();
    
    List<Quiz> findByTopic(String topic);
    
    List<Quiz> findByActiveTrueAndTopic(String topic);
    
    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
    
    @Query("SELECT DISTINCT q.topic FROM Quiz q WHERE q.active = true ORDER BY q.topic")
    List<String> findAllDistinctTopics();
    
    @Query("SELECT q FROM Quiz q WHERE q.createdBy.id = :userId")
    List<Quiz> findByCreatedBy(@Param("userId") Long userId);
}
```

### Step 3.3: Create QuestionRepository

**Location:** `src/main/java/com/quiz/application/repository/QuestionRepository.java`

```java
package com.quiz.application.repository;

import com.quiz.application.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByQuizId(Long quizId);
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.questionOrder")
    List<Question> findByQuizIdWithOptions(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId")
    Long countByQuizId(@Param("quizId") Long quizId);
}
```

### Step 3.4: Create QuestionOptionRepository

**Location:** `src/main/java/com/quiz/application/repository/QuestionOptionRepository.java`

```java
package com.quiz.application.repository;

import com.quiz.application.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    List<QuestionOption> findByQuestionId(Long questionId);
    
    @Query("SELECT o FROM QuestionOption o WHERE o.question.id = :questionId AND o.isCorrect = true")
    List<QuestionOption> findCorrectOptionsByQuestionId(@Param("questionId") Long questionId);
}
```

### Step 3.5: Create QuizAttemptRepository

**Location:** `src/main/java/com/quiz/application/repository/QuizAttemptRepository.java`

```java
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
```

### Step 3.6: Create UserAnswerRepository

**Location:** `src/main/java/com/quiz/application/repository/UserAnswerRepository.java`

```java
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
```

---

## PHASE 4: Create DTO (Data Transfer Objects)

### Step 4.1: Create Authentication DTOs

**Location:** `src/main/java/com/quiz/application/dto/LoginRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}
```

**Location:** `src/main/java/com/quiz/application/dto/RegisterRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
}
```

**Location:** `src/main/java/com/quiz/application/dto/AuthResponse.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;
    private String message;
}
```

### Step 4.2: Create User DTOs

**Location:** `src/main/java/com/quiz/application/dto/UserDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean active;
}
```

### Step 4.3: Create Quiz DTOs

**Location:** `src/main/java/com/quiz/application/dto/QuizDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {
    private Long id;
    private String title;
    private String description;
    private String topic;
    private String difficultyLevel;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private Boolean active;
    private Long createdById;
    private String createdByUsername;
    private Integer totalQuestions;
    private Integer totalPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionDTO> questions;
}
```

**Location:** `src/main/java/com/quiz/application/dto/QuizCreateRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Topic is required")
    private String topic;
    
    private String difficultyLevel = "MEDIUM";
    
    private Integer timeLimitMinutes;
    
    private Integer passingScore;
    
    @NotNull(message = "Active status is required")
    private Boolean active = true;
}
```

### Step 4.4: Create Question DTOs

**Location:** `src/main/java/com/quiz/application/dto/QuestionDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer points;
    private Integer questionOrder;
    private String explanation;
    private Long quizId;
    private List<QuestionOptionDTO> options;
}
```

**Location:** `src/main/java/com/quiz/application/dto/QuestionCreateRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {
    
    @NotBlank(message = "Question text is required")
    private String questionText;
    
    @NotNull(message = "Question type is required")
    private String questionType;
    
    private Integer points = 1;
    
    private Integer questionOrder;
    
    private String explanation;
    
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
    
    @NotNull(message = "Options are required")
    private List<QuestionOptionCreateRequest> options;
}
```

### Step 4.5: Create QuestionOption DTOs

**Location:** `src/main/java/com/quiz/application/dto/QuestionOptionDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionDTO {
    private Long id;
    private String optionText;
    private Boolean isCorrect;
    private Integer optionOrder;
}
```

**Location:** `src/main/java/com/quiz/application/dto/QuestionOptionCreateRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionCreateRequest {
    
    @NotBlank(message = "Option text is required")
    private String optionText;
    
    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
    
    private Integer optionOrder;
}
```

### Step 4.6: Create Quiz Attempt DTOs

**Location:** `src/main/java/com/quiz/application/dto/QuizAttemptDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long quizId;
    private String quizTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer scoreObtained;
    private Integer totalScore;
    private Double percentageScore;
    private String status;
    private Integer timeTakenMinutes;
    private List<UserAnswerDTO> answers;
}
```

**Location:** `src/main/java/com/quiz/application/dto/StartQuizRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartQuizRequest {
    
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
}
```

**Location:** `src/main/java/com/quiz/application/dto/SubmitAnswerRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerRequest {
    
    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotNull(message = "Selected options are required")
    private Set<Long> selectedOptionIds;
}
```

**Location:** `src/main/java/com/quiz/application/dto/CompleteQuizRequest.java`

```java
package com.quiz.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteQuizRequest {
    
    @NotNull(message = "Attempt ID is required")
    private Long attemptId;
}
```

### Step 4.7: Create UserAnswer DTOs

**Location:** `src/main/java/com/quiz/application/dto/UserAnswerDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswerDTO {
    private Long id;
    private Long questionId;
    private String questionText;
    private Set<Long> selectedOptionIds;
    private Boolean isCorrect;
    private Integer pointsEarned;
    private String explanation;
}
```

### Step 4.8: Create Leaderboard DTOs

**Location:** `src/main/java/com/quiz/application/dto/LeaderboardEntryDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDTO {
    private Integer rank;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Integer totalScore;
    private Double averageScore;
    private Integer attemptCount;
    private Long quizId;
    private String quizTitle;
}
```

### Step 4.9: Create Generic Response DTO

**Location:** `src/main/java/com/quiz/application/dto/ApiResponse.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

---

## PHASE 5: Exception Handling

### Step 5.1: Create Custom Exceptions

**Location:** `src/main/java/com/quiz/application/exception/ResourceNotFoundException.java`

```java
package com.quiz.application.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Location:** `src/main/java/com/quiz/application/exception/BadRequestException.java`

```java
package com.quiz.application.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

**Location:** `src/main/java/com/quiz/application/exception/UnauthorizedException.java`

```java
package com.quiz.application.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

**Location:** `src/main/java/com/quiz/application/exception/DuplicateResourceException.java`

```java
package com.quiz.application.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

### Step 5.2: Create Global Exception Handler

**Location:** `src/main/java/com/quiz/application/exception/GlobalExceptionHandler.java`

```java
package com.quiz.application.exception;

import com.quiz.application.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error("An error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

---

## PHASE 6: Security Configuration (JWT Authentication)

### Step 6.1: Add JWT Dependencies
Add to `pom.xml`:

```xml
<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### Step 6.2: Add JWT Properties to application.properties

```properties
# JWT Configuration
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
jwt.expiration=86400000
```

### Step 6.3: Create UserDetailsService Implementation

**Location:** `src/main/java/com/quiz/application/security/CustomUserDetailsService.java`

```java
package com.quiz.application.security;

import com.quiz.application.entity.User;
import com.quiz.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return UserPrincipal.create(user);
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return UserPrincipal.create(user);
    }
}
```

### Step 6.4: Create UserPrincipal Class

**Location:** `src/main/java/com/quiz/application/security/UserPrincipal.java`

```java
package com.quiz.application.security;

import com.quiz.application.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### Step 6.5: Create JWT Utility Class

**Location:** `src/main/java/com/quiz/application/security/JwtTokenProvider.java`

```java
package com.quiz.application.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return Long.parseLong(claims.getSubject());
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
```

### Step 6.6: Create JWT Authentication Filter

**Location:** `src/main/java/com/quiz/application/security/JwtAuthenticationFilter.java`

```java
package com.quiz.application.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### Step 6.7: Create JWT Authentication Entry Point

**Location:** `src/main/java/com/quiz/application/security/JwtAuthenticationEntryPoint.java`

```java
package com.quiz.application.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                         AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
```

### Step 6.8: Create Security Configuration

**Location:** `src/main/java/com/quiz/application/config/SecurityConfig.java`

```java
package com.quiz.application.config;

import com.quiz.application.security.CustomUserDetailsService;
import com.quiz.application.security.JwtAuthenticationEntryPoint;
import com.quiz.application.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## PHASE 7: Service Layer Implementation

### Step 7.1: Create AuthService

**Location:** `src/main/java/com/quiz/application/service/AuthService.java`

```java
package com.quiz.application.service;

import com.quiz.application.dto.AuthResponse;
import com.quiz.application.dto.LoginRequest;
import com.quiz.application.dto.RegisterRequest;
import com.quiz.application.entity.User;
import com.quiz.application.exception.BadRequestException;
import com.quiz.application.exception.DuplicateResourceException;
import com.quiz.application.repository.UserRepository;
import com.quiz.application.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .active(true)
                .build();
        
        userRepository.save(user);
        
        return AuthResponse.builder()
                .message("User registered successfully")
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
```

### Step 7.2: Create UserService

**Location:** `src/main/java/com/quiz/application/service/UserService.java`

```java
package com.quiz.application.service;

import com.quiz.application.dto.UserDTO;
import com.quiz.application.entity.User;
import com.quiz.application.exception.ResourceNotFoundException;
import com.quiz.application.repository.UserRepository;
import com.quiz.application.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}
```

### Step 7.3: Create QuizService

**Location:** `src/main/java/com/quiz/application/service/QuizService.java`

```java
package com.quiz.application.service;

import com.quiz.application.dto.QuizCreateRequest;
import com.quiz.application.dto.QuizDTO;
import com.quiz.application.entity.Quiz;
import com.quiz.application.entity.User;
import com.quiz.application.exception.ResourceNotFoundException;
import com.quiz.application.exception.UnauthorizedException;
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
    public List<QuizDTO> getAllActiveQuizzes() {
        return quizRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesByTopic(String topic) {
        return quizRepository.findByActiveTrueAndTopic(topic).stream()
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
```

### Step 7.4: Create QuestionService

**Location:** `src/main/java/com/quiz/application/service/QuestionService.java`

```java
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
    public QuestionDTO updateQuestion(Long id, QuestionCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Question question = questionRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        
        Quiz quiz = question.getQuiz();
        
        // Only the creator or admin can update questions
        if (currentUser.getRole() != User.Role.ADMIN && !quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to update this question");
        }
        
        // Validate that at least one option is correct
        boolean hasCorrectOption = request.getOptions().stream()
                .anyMatch(opt -> opt.getIsCorrect());
        if (!hasCorrectOption) {
            throw new BadRequestException("At least one option must be marked as correct");
        }
        
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(Question.QuestionType.valueOf(request.getQuestionType()));
        question.setPoints(request.getPoints());
        question.setQuestionOrder(request.getQuestionOrder());
        question.setExplanation(request.getExplanation());
        
        // Remove old options and add new ones
        question.getOptions().clear();
        
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
```

### Step 7.5: Create QuizAttemptService

**Location:** `src/main/java/com/quiz/application/service/QuizAttemptService.java`

```java
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
import java.util.HashSet;
import java.util.List;
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
    
    @Transactional
    public QuizAttemptDTO startQuiz(StartQuizRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Quiz quiz = quizRepository.findByIdWithQuestions(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + request.getQuizId()));
        
        if (!quiz.getActive()) {
            throw new BadRequestException("This quiz is not active");
        }
        
        QuizAttempt attempt = QuizAttempt.builder()
                .user(currentUser)
                .quiz(quiz)
                .startTime(LocalDateTime.now())
                .status(QuizAttempt.AttemptStatus.IN_PROGRESS)
                .totalScore(quiz.getQuestions().stream().mapToInt(Question::getPoints).sum())
                .build();
        
        attempt = quizAttemptRepository.save(attempt);
        return convertToDTO(attempt);
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
            throw new BadRequestException("This quiz attempt is already completed");
        }
        
        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus(QuizAttempt.AttemptStatus.COMPLETED);
        
        // Calculate score
        int scoreObtained = attempt.getAnswers().stream()
                .mapToInt(UserAnswer::getPointsEarned)
                .sum();
        
        attempt.setScoreObtained(scoreObtained);
        
        // Calculate percentage
        double percentage = (double) scoreObtained / attempt.getTotalScore() * 100;
        attempt.setPercentageScore(percentage);
        
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
        return quizAttemptRepository.findCompletedAttemptsByUserId(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDTO> getAttemptsByQuizId(Long quizId) {
        User currentUser = userService.getCurrentUser();
        return quizAttemptRepository.findByUserIdAndQuizId(currentUser.getId(), quizId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private QuizAttemptDTO convertToDTO(QuizAttempt attempt) {
        return QuizAttemptDTO.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .username(attempt.getUser().getUsername())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .startTime(attempt.getStartTime())
                .endTime(attempt.getEndTime())
                .scoreObtained(attempt.getScoreObtained())
                .totalScore(attempt.getTotalScore())
                .percentageScore(attempt.getPercentageScore())
                .status(attempt.getStatus().name())
                .timeTakenMinutes(attempt.getTimeTakenMinutes())
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
```

### Step 7.6: Create LeaderboardService

**Location:** `src/main/java/com/quiz/application/service/LeaderboardService.java`

```java
package com.quiz.application.service;

import com.quiz.application.dto.LeaderboardEntryDTO;
import com.quiz.application.entity.QuizAttempt;
import com.quiz.application.repository.QuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
                        (a1, a2) -> a1.getScoreObtained() > a2.getScoreObtained() ? a1 : a2
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
                        .build())
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
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
```

---

## PHASE 8: Controller Layer Implementation

### Step 8.1: Create AuthController

**Location:** `src/main/java/com/quiz/application/controller/AuthController.java`

```java
package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.AuthResponse;
import com.quiz.application.dto.LoginRequest;
import com.quiz.application.dto.RegisterRequest;
import com.quiz.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}
```

### Step 8.2: Create UserController

**Location:** `src/main/java/com/quiz/application/controller/UserController.java`

```java
package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.UserDTO;
import com.quiz.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
```

### Step 8.3: Create QuizController

**Location:** `src/main/java/com/quiz/application/controller/QuizController.java`

```java
package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.QuizCreateRequest;
import com.quiz.application.dto.QuizDTO;
import com.quiz.application.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    
    @Autowired
    private QuizService quizService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizDTO>> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        QuizDTO quiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(quiz, "Quiz created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizDTO>> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizCreateRequest request) {
        QuizDTO quiz = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(ApiResponse.success(quiz, "Quiz updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Quiz deleted successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuizById(@PathVariable Long id) {
        QuizDTO quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(ApiResponse.success(quiz, "Quiz retrieved successfully"));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getAllActiveQuizzes() {
        List<QuizDTO> quizzes = quizService.getAllActiveQuizzes();
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Quizzes retrieved successfully"));
    }
    
    @GetMapping("/topic/{topic}")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getQuizzesByTopic(@PathVariable String topic) {
        List<QuizDTO> quizzes = quizService.getQuizzesByTopic(topic);
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Quizzes retrieved successfully"));
    }
    
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getAllTopics() {
        List<String> topics = quizService.getAllTopics();
        return ResponseEntity.ok(ApiResponse.success(topics, "Topics retrieved successfully"));
    }
    
    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getMyQuizzes() {
        List<QuizDTO> quizzes = quizService.getMyQuizzes();
        return ResponseEntity.ok(ApiResponse.success(quizzes, "Your quizzes retrieved successfully"));
    }
}
```

### Step 8.4: Create QuestionController

**Location:** `src/main/java/com/quiz/application/controller/QuestionController.java`

```java
package com.quiz.application.controller;

import com.quiz.application.dto.ApiResponse;
import com.quiz.application.dto.QuestionCreateRequest;
import com.quiz.application.dto.QuestionDTO;
import com.quiz.application.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionDTO>> createQuestion(@Valid @RequestBody QuestionCreateRequest request) {
        QuestionDTO question = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(question, "Question created successfully"));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionDTO>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionCreateRequest request) {
        QuestionDTO question = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success(question, "Question updated successfully"));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Question deleted successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionDTO>> getQuestionById(@PathVariable Long id) {
        QuestionDTO question = questionService.getQuestionById(id);
        return ResponseEntity.ok(ApiResponse.success(question, "Question retrieved successfully"));
    }
    
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuestionDTO> questions = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(ApiResponse.success(questions, "Questions retrieved successfully"));
    }
}
```

### Step 8.5: Create QuizAttemptController

**Location:** `src/main/java/com/quiz/application/controller/QuizAttemptController.java`

```java
package com.quiz.application.controller;

import com.quiz.application.dto.*;
import com.quiz.application.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attempts")
public class QuizAttemptController {
    
    @Autowired
    private QuizAttemptService quizAttemptService;
    
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> startQuiz(@Valid @RequestBody StartQuizRequest request) {
        QuizAttemptDTO attempt = quizAttemptService.startQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attempt, "Quiz started successfully"));
    }
    
    @PostMapping("/submit-answer")
    public ResponseEntity<ApiResponse<UserAnswerDTO>> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        UserAnswerDTO answer = quizAttemptService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponse.success(answer, "Answer submitted successfully"));
    }
    
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> completeQuiz(@Valid @RequestBody CompleteQuizRequest request) {
        QuizAttemptDTO attempt = quizAttemptService.completeQuiz(request);
        return ResponseEntity.ok(ApiResponse.success(attempt, "Quiz completed successfully"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizAttemptDTO>> getAttemptById(@PathVariable Long id) {
        QuizAttemptDTO attempt = quizAttemptService.getAttemptById(id);
        return ResponseEntity.ok(ApiResponse.success(attempt, "Attempt retrieved successfully"));
    }
    
    @GetMapping("/my-attempts")
    public ResponseEntity<ApiResponse<List<QuizAttemptDTO>>> getMyAttempts() {
        List<QuizAttemptDTO> attempts = quizAttemptService.getMyAttempts();
        return ResponseEntity.ok(ApiResponse.success(attempts, "Your attempts retrieved successfully"));
    }
    
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<QuizAttemptDTO>>> getAttemptsByQuizId(@PathVariable Long quizId) {
        List<QuizAttemptDTO> attempts = quizAttemptService.getAttemptsByQuizId(quizId);
        return ResponseEntity.ok(ApiResponse.success(attempts, "Quiz attempts retrieved successfully"));
    }
}
```

### Step 8.6: Create LeaderboardController

**Location:** `src/main/java/com/quiz/application/controller/LeaderboardController.java`

```java
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
```

---

## PHASE 9: Testing and Data Seeding

### Step 9.1: Create Data Initialization Class (Optional)

**Location:** `src/main/java/com/quiz/application/config/DataInitializer.java`

```java
package com.quiz.application.config;

import com.quiz.application.entity.*;
import com.quiz.application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuizRepository quizRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists
        if (!userRepository.existsByUsername("admin")) {
            // Create admin user
            User admin = User.builder()
                    .username("admin")
                    .email("admin@quiz.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            
            // Create a test user
            User testUser = User.builder()
                    .username("testuser")
                    .email("test@quiz.com")
                    .password(passwordEncoder.encode("test123"))
                    .firstName("Test")
                    .lastName("User")
                    .role(User.Role.USER)
                    .active(true)
                    .build();
            userRepository.save(testUser);
            
            // Create sample quiz
            Quiz quiz = Quiz.builder()
                    .title("Java Basics Quiz")
                    .description("Test your knowledge of Java fundamentals")
                    .topic("Java")
                    .difficultyLevel(Quiz.DifficultyLevel.MEDIUM)
                    .timeLimitMinutes(30)
                    .passingScore(70)
                    .active(true)
                    .createdBy(admin)
                    .build();
            quiz = quizRepository.save(quiz);
            
            // Create sample question
            Question question1 = Question.builder()
                    .questionText("What is the default value of a boolean variable in Java?")
                    .questionType(Question.QuestionType.SINGLE_CHOICE)
                    .points(10)
                    .questionOrder(1)
                    .explanation("The default value of a boolean variable is false")
                    .quiz(quiz)
                    .build();
            
            QuestionOption opt1 = QuestionOption.builder()
                    .optionText("true")
                    .isCorrect(false)
                    .optionOrder(1)
                    .build();
            
            QuestionOption opt2 = QuestionOption.builder()
                    .optionText("false")
                    .isCorrect(true)
                    .optionOrder(2)
                    .build();
            
            QuestionOption opt3 = QuestionOption.builder()
                    .optionText("0")
                    .isCorrect(false)
                    .optionOrder(3)
                    .build();
            
            QuestionOption opt4 = QuestionOption.builder()
                    .optionText("null")
                    .isCorrect(false)
                    .optionOrder(4)
                    .build();
            
            question1.addOption(opt1);
            question1.addOption(opt2);
            question1.addOption(opt3);
            question1.addOption(opt4);
            
            questionRepository.save(question1);
            
            System.out.println("Sample data initialized successfully!");
            System.out.println("Admin credentials - Username: admin, Password: admin123");
            System.out.println("Test user credentials - Username: testuser, Password: test123");
        }
    }
}
```

### Step 9.2: Create Application Properties for Testing

**Location:** `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## PHASE 10: Final Configuration and Documentation

### Step 10.1: Update pom.xml with All Dependencies

Ensure your `pom.xml` has all required dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.quiz</groupId>
    <artifactId>application</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Online Quiz Application</name>
    <description>Online Quiz Application Backend</description>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT Dependencies -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 10.2: Create README.md for Backend

**Location:** `backend/README.md`

```markdown
# Online Quiz Application - Backend

## Technology Stack
- **Spring Boot**: 3.2.x
- **Java**: 17
- **Database**: MySQL 8.x
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Maven

## Prerequisites
- JDK 17 or higher
- MySQL 8.x
- Maven 3.6+

## Database Setup

1. Install MySQL and create database:
```sql
CREATE DATABASE quiz_application;
CREATE USER 'quiz_user'@'localhost' IDENTIFIED BY 'quiz_password123';
GRANT ALL PRIVILEGES ON quiz_application.* TO 'quiz_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Update `application.properties` if you use different credentials.

## Running the Application

1. Clone the repository
2. Navigate to backend directory:
```bash
cd backend
```

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080/api`

## Default Credentials

After first run, the following users are created:

**Admin User:**
- Username: `admin`
- Password: `admin123`

**Test User:**
- Username: `testuser`
- Password: `test123`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Users
- `GET /api/users/me` - Get current user profile
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users (Admin only)

### Quizzes
- `POST /api/quizzes` - Create quiz (Admin only)
- `PUT /api/quizzes/{id}` - Update quiz (Admin only)
- `DELETE /api/quizzes/{id}` - Delete quiz (Admin only)
- `GET /api/quizzes/{id}` - Get quiz by ID
- `GET /api/quizzes` - Get all active quizzes
- `GET /api/quizzes/topic/{topic}` - Get quizzes by topic
- `GET /api/quizzes/topics` - Get all topics
- `GET /api/quizzes/my-quizzes` - Get my quizzes (Admin only)

### Questions
- `POST /api/questions` - Create question (Admin only)
- `PUT /api/questions/{id}` - Update question (Admin only)
- `DELETE /api/questions/{id}` - Delete question (Admin only)
- `GET /api/questions/{id}` - Get question by ID
- `GET /api/questions/quiz/{quizId}` - Get questions by quiz ID

### Quiz Attempts
- `POST /api/attempts/start` - Start a quiz
- `POST /api/attempts/submit-answer` - Submit answer
- `POST /api/attempts/complete` - Complete quiz
- `GET /api/attempts/{id}` - Get attempt by ID
- `GET /api/attempts/my-attempts` - Get my attempts
- `GET /api/attempts/quiz/{quizId}` - Get attempts by quiz ID

### Leaderboard
- `GET /api/leaderboard/global?limit=10` - Get global leaderboard
- `GET /api/leaderboard/quiz/{quizId}?limit=10` - Get quiz leaderboard

## API Request Examples

### Register User
```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login
```json
POST /api/auth/login
{
  "username": "john_doe",
  "password": "password123"
}
```

### Create Quiz (Admin)
```json
POST /api/quizzes
Authorization: Bearer <token>
{
  "title": "Java Basics",
  "description": "Test your Java knowledge",
  "topic": "Java",
  "difficultyLevel": "MEDIUM",
  "timeLimitMinutes": 30,
  "passingScore": 70,
  "active": true
}
```

### Create Question (Admin)
```json
POST /api/questions
Authorization: Bearer <token>
{
  "questionText": "What is polymorphism?",
  "questionType": "SINGLE_CHOICE",
  "points": 10,
  "questionOrder": 1,
  "explanation": "Polymorphism allows objects to take many forms",
  "quizId": 1,
  "options": [
    {
      "optionText": "The ability to take many forms",
      "isCorrect": true,
      "optionOrder": 1
    },
    {
      "optionText": "Data hiding",
      "isCorrect": false,
      "optionOrder": 2
    }
  ]
}
```

### Start Quiz
```json
POST /api/attempts/start
Authorization: Bearer <token>
{
  "quizId": 1
}
```

### Submit Answer
```json
POST /api/attempts/submit-answer
Authorization: Bearer <token>
{
  "attemptId": 1,
  "questionId": 1,
  "selectedOptionIds": [1]
}
```

### Complete Quiz
```json
POST /api/attempts/complete
Authorization: Bearer <token>
{
  "attemptId": 1
}
```

## Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/quiz/application/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── security/        # Security components
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── QuizApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml
└── README.md
```

## Testing with Postman/Insomnia

1. Register a new user using `/api/auth/register`
2. Login using `/api/auth/login` to get JWT token
3. Use the token in Authorization header: `Bearer <token>`
4. Test all endpoints with the token

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database exists

### Port Already in Use
- Change port in `application.properties`: `server.port=8081`

### JWT Token Issues
- Ensure token is sent in Authorization header
- Token expires after 24 hours (configurable in application.properties)

## Security Features
- Password hashing using BCrypt
- JWT-based authentication
- Role-based access control (USER, ADMIN)
- CORS configuration for frontend integration

## Future Enhancements
- Email verification
- Password reset functionality
- Quiz categories
- Timer-based quiz enforcement
- Random question selection
- Quiz analytics and statistics
- Export quiz results
```

### Step 10.3: Create .gitignore File

**Location:** `backend/.gitignore`

```
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Maven ###
.mvn/
mvnw
mvnw.cmd

### Application ###
application-local.properties
application-prod.properties
```

---

## PHASE 11: Running and Testing Guide

### Step 11.1: Verify Installation

1. **Check Java Version:**
```bash
java -version
# Should show Java 17 or higher
```

2. **Check Maven:**
```bash
mvn -version
# Should show Maven 3.6+
```

3. **Check MySQL:**
```bash
mysql --version
# Should show MySQL 8.x
```

### Step 11.2: Complete Setup Checklist

**Database Setup:**
- [ ] MySQL installed and running
- [ ] Database `quiz_application` created
- [ ] User `quiz_user` created with proper privileges
- [ ] Connection details match `application.properties`

**Application Setup:**
- [ ] All files created in correct locations
- [ ] No compilation errors
- [ ] Dependencies downloaded successfully
- [ ] Application starts without errors

### Step 11.3: Start the Application

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

**Expected Console Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

[           main] c.q.a.QuizApplication                    : Starting QuizApplication
[           main] c.q.a.QuizApplication                    : No active profile set
[           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080
[           main] c.q.a.QuizApplication                    : Started QuizApplication in 5.234 seconds
Sample data initialized successfully!
Admin credentials - Username: admin, Password: admin123
Test user credentials - Username: testuser, Password: test123
```

### Step 11.4: Test API Endpoints

**Test 1: Register User**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@test.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "User"
  }'
```

**Test 2: Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Test 3: Get All Quizzes (Save token from login)**
```bash
curl -X GET http://localhost:8080/api/quizzes \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Step 11.5: Common Issues and Solutions

**Issue 1: Port 8080 Already in Use**
```
Solution: Change port in application.properties
server.port=8081
```

**Issue 2: Database Connection Failed**
```
Solution: Verify MySQL is running and credentials are correct
sudo service mysql start
mysql -u quiz_user -p
```

**Issue 3: JWT Secret Key Error**
```
Solution: Ensure JWT secret in application.properties is at least 256 bits
jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
```

**Issue 4: Lombok Not Working**
```
Solution: Enable annotation processing in your IDE
- IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
- Eclipse: Install Lombok plugin
```

---

## PHASE 12: Additional Features (Optional Enhancements)

### Step 12.1: Add Quiz Statistics

**Location:** `src/main/java/com/quiz/application/dto/QuizStatisticsDTO.java`

```java
package com.quiz.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizStatisticsDTO {
    private Long quizId;
    private String quizTitle;
    private Integer totalAttempts;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    private Double passRate;
}
```

### Step 12.2: Add Search Functionality

**Add to QuizRepository:**
```java
@Query("SELECT q FROM Quiz q WHERE q.active = true AND " +
       "(LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
       "LOWER(q.topic) LIKE LOWER(CONCAT('%', :keyword, '%')))")
List<Quiz> searchQuizzes(@Param("keyword") String keyword);
```

### Step 12.3: Add Pagination Support

**Update QuizService:**
```java
@Transactional(readOnly = true)
public Page<QuizDTO> getAllActiveQuizzesPaginated(Pageable pageable) {
    Page<Quiz> quizzes = quizRepository.findByActiveTrue(pageable);
    return quizzes.map(this::convertToDTO);
}
```

---

## PHASE 13: Deployment Preparation

### Step 13.1: Create Production Profile

**Location:** `src/main/resources/application-prod.properties`

```properties
# Production Configuration
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA Configuration for Production
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.quiz.application=INFO
```

### Step 13.2: Create Docker Configuration (Optional)

**Location:** `backend/Dockerfile`

```dockerfile
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Location:** `backend/docker-compose.yml`

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: quiz_application
      MYSQL_USER: quiz_user
      MYSQL_PASSWORD: quiz_password123
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/quiz_application
      SPRING_DATASOURCE_USERNAME: quiz_user
      SPRING_DATASOURCE_PASSWORD: quiz_password123
    depends_on:
      - mysql

volumes:
  mysql_data:
```

---

## PHASE 14: Final Checklist and Submission

### Pre-Submission Checklist

**Code Quality:**
- [ ] All files are properly organized in correct packages
- [ ] No compilation errors or warnings
- [ ] All imports are correct and necessary
- [ ] Code is properly formatted
- [ ] Meaningful variable and method names used

**Functionality:**
- [ ] User registration works
- [ ] User login works and returns JWT token
- [ ] Admin can create/edit/delete quizzes
- [ ] Admin can create/edit/delete questions
- [ ] Users can take quizzes
- [ ] Answers are evaluated correctly
- [ ] Scores are calculated properly
- [ ] Leaderboard displays correctly

**Database:**
- [ ] All tables are created
- [ ] Relationships are properly established
- [ ] Sample data is loaded
- [ ] Queries execute without errors

**Security:**
- [ ] Passwords are hashed
- [ ] JWT authentication works
- [ ] Role-based access control works
- [ ] CORS is configured for frontend

**Documentation:**
- [ ] README.md is complete
- [ ] API endpoints are documented
- [ ] Setup instructions are clear
- [ ] Code comments where necessary

**Testing:**
- [ ] All API endpoints tested
- [ ] Different user roles tested
- [ ] Error scenarios handled
- [ ] Edge cases considered

### Submission Package Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/quiz/application/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   ├── security/
│   │   │   ├── exception/
│   │   │   └── QuizApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml
├── README.md
├── .gitignore
└── SETUP_GUIDE.md
```

### Success Criteria

Your backend is complete when:
1. Application starts without errors
2. All CRUD operations work for quizzes and questions
3. Users can register, login, and take quizzes
4. Scores are calculated and stored correctly
5. Leaderboard functionality works
6. Security is properly implemented
7. API documentation is clear
8. Code follows best practices

---

## Appendix: Quick Reference

### Entity Relationships
- User → Quiz (One-to-Many) via `createdBy`
- Quiz → Question (One-to-Many)
- Question → QuestionOption (One-to-Many)
- User → QuizAttempt (One-to-Many)
- Quiz → QuizAttempt (One-to-Many)
- QuizAttempt → UserAnswer (One-to-Many)
- UserAnswer → QuestionOption (Many-to-Many)

### Important Commands

**Build Project:**
```bash
./mvnw clean install
```

**Run Application:**
```bash
./mvnw spring-boot:run
```

**Run with Profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Build JAR:**
```bash
./mvnw clean package
```

**Run JAR:**
```bash
java -jar target/application-0.0.1-SNAPSHOT.jar
```

### Environment Variables for Production
```bash
export DB_URL=jdbc:mysql://localhost:3306/quiz_application
export DB_USERNAME=quiz_user
export DB_PASSWORD=quiz_password123
export JWT_SECRET=your-secret-key-here
```

---

## Support and Resources

- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **JWT**: https://jwt.io/
- **MySQL Documentation**: https://dev.mysql.com/doc/
- **JPA/Hibernate**: https://hibernate.org/orm/documentation/

---

**END OF BACKEND DEVELOPMENT PLAN**

This completes your backend development for the Online Quiz Application. Follow each phase sequentially, test thoroughly, and ensure all functionality works before proceeding to frontend development.Exception("This attempt does not belong to you");
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
                .orElse(null);
        
        if (userAnswer == null) {
            userAnswer = new UserAnswer();
            userAnswer.setQuizAttempt(attempt);
            userAnswer.setQuestion(question);
        }
        
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
            throw new BadRequest