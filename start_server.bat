@echo off
cd "C:\Users\riddh\omnibot-final\omnibot-backend"
mvnw spring-boot:run
pause

$echo off
start cmd /k "cd frontend && npm start"
start cmd /k "cd backend && node server.js"
pause 