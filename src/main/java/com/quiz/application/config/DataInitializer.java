package com.quiz.application.config;

import com.quiz.application.entity.*;
import com.quiz.application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
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
        System.out.println("DATA-INITIALIZER: Starting data initialization process...");

        try {
            if (!userRepository.existsByUsername("admin")) {
                System.out.println("DATA-INITIALIZER: 'admin' user not found. Creating sample data...");

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
                System.out.println("DATA-INITIALIZER: Attempting to save 'admin' user...");
                userRepository.save(admin);
                System.out.println("DATA-INITIALIZER: 'admin' user saved successfully.");

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
                System.out.println("DATA-INITIALIZER: Attempting to save 'testuser' user...");
                userRepository.save(testUser);
                System.out.println("DATA-INITIALIZER: 'testuser' user saved successfully.");

                // Create gemini_test_user
                User geminiUser = User.builder()
                        .username("gemini_test_user")
                        .email("gemini@test.com")
                        .password(passwordEncoder.encode("gemini123"))
                        .firstName("Gemini")
                        .lastName("Test")
                        .role(User.Role.USER)
                        .active(true)
                        .build();
                System.out.println("DATA-INITIALIZER: Attempting to save 'gemini_test_user' user...");
                userRepository.save(geminiUser);
                System.out.println("DATA-INITIALIZER: 'gemini_test_user' user saved successfully.");


                // Create sample quiz
                Quiz quiz = Quiz.builder()
                        .title("Java Basics Quiz")
                        .description("Test your knowledge of Java fundamentals")
                        .topic("Java")
                        .difficultyLevel(Quiz.DifficultyLevel.MEDIUM)
                        .timeLimitMinutes(30)
                        .passingScore(70)
                        .createdBy(admin)
                        .build();
                System.out.println("DATA-INITIALIZER: Attempting to save sample quiz...");
                quiz = quizRepository.save(quiz);
                System.out.println("DATA-INITIALIZER: Sample quiz saved successfully.");


                // Create sample question
                Question question1 = Question.builder()
                        .questionText("What is the default value of a boolean variable in Java?")
                        .questionType(Question.QuestionType.SINGLE_CHOICE)
                        .points(10)
                        .questionOrder(1)
                        .explanation("The default value of a boolean variable is false")
                        .quiz(quiz)
                        .build();

                QuestionOption opt1 = QuestionOption.builder().optionText("true").isCorrect(false).optionOrder(1).build();
                QuestionOption opt2 = QuestionOption.builder().optionText("false").isCorrect(true).optionOrder(2).build();
                QuestionOption opt3 = QuestionOption.builder().optionText("0").isCorrect(false).optionOrder(3).build();
                QuestionOption opt4 = QuestionOption.builder().optionText("null").isCorrect(false).optionOrder(4).build();

                question1.addOption(opt1);
                question1.addOption(opt2);
                question1.addOption(opt3);
                question1.addOption(opt4);

                System.out.println("DATA-INITIALIZER: Attempting to save sample question...");
                questionRepository.save(question1);
                System.out.println("DATA-INITIALIZER: Sample question saved successfully.");


                System.out.println("DATA-INITIALIZER: Sample data initialized successfully!");
                System.out.println("Admin credentials - Username: admin, Password: admin123");
                System.out.println("Test user credentials - Username: testuser, Password: test123");
                System.out.println("Gemini test user created: gemini_test_user");

            } else {
                System.out.println("DATA-INITIALIZER: 'admin' user already exists. Skipping data initialization.");
            }
        } catch (DataAccessException e) {
            System.out.println("DATA-INITIALIZER: ERROR: A data access exception occurred during data initialization.");
            System.out.println("DATA-INITIALIZER: Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        System.out.println("DATA-INITIALIZER: Data initialization process finished.");
    }
}
