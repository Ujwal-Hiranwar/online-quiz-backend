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
# online-quiz-backend
