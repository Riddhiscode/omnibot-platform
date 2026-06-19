@echo off
cd "C:\Users\riddh\omnibot-final\omnibot-backend"
mvn spring-boot:run

start cmd /k "cd C:\Users\riddh\omnibot-final\frontend && npm start"
start cmd /k "cd C:\Users\riddh\omnibot-final\backend && node server.js"
pause 