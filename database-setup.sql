CREATE DATABASE quiz_application;
CREATE USER 'quiz_user'@'localhost' IDENTIFIED BY 'quiz_password123';
GRANT ALL PRIVILEGES ON quiz_application.* TO 'quiz_user'@'localhost';
FLUSH PRIVILEGES;
