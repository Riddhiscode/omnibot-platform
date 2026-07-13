@echo off
title OmniBot Platform
color 0A

echo ========================================
echo    OmniBot Platform - Starting...
echo ========================================
echo.

:: Check MySQL
netstat -an | findstr ":3306.*LISTEN" >nul 2>&1
if %ERRORLEVEL%==0 (
    echo [OK] MySQL already running on port 3306
) else (
    echo [..] Starting MySQL...
    start /b "" "C:\Users\riddh\mysql-9.7.0-winx64\bin\mysqld.exe" "--defaults-file=C:\Users\riddh\mysql-9.7.0-winx64\my-omnibot.ini"
    :waitmysql
    timeout /t 1 /nobreak >nul
    netstat -an | findstr ":3306.*LISTEN" >nul 2>&1
    if %ERRORLEVEL% neq 0 goto waitmysql
    echo [OK] MySQL started on port 3306
)

echo.
echo [..] Starting OmniBot backend...
echo.
cd /d "C:\Users\riddh\omnibot-platform\backend-core"
"C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run -DskipTests
