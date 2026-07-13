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
start "OmniBot Backend" /min "cmd.exe" /c ""C:\Users\riddh\maven\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run -DskipTests"

:: Wait for backend to start
echo [..] Waiting for backend (approx 30s)...
:waitapp
timeout /t 3 /nobreak >nul
netstat -an | findstr ":8080.*LISTEN" >nul 2>&1
if %ERRORLEVEL% neq 0 goto waitapp
echo [OK] Backend running on http://localhost:8080
echo.

:: MySQL watchdog — restarts MySQL if it dies
echo [..] MySQL watchdog active (checks every 30s)
echo     Press Ctrl+C to stop everything.
echo.
:watchdog
timeout /t 30 /nobreak >nul
netstat -an | findstr ":3306.*LISTEN" >nul 2>&1
if %ERRORLEVEL%==0 goto watchdog

echo [!!] MySQL died! Restarting...
start /b "" "C:\Users\riddh\mysql-9.7.0-winx64\bin\mysqld.exe" "--defaults-file=C:\Users\riddh\mysql-9.7.0-winx64\my-omnibot.ini"
timeout /t 5 /nobreak >nul
netstat -an | findstr ":3306.*LISTEN" >nul 2>&1
if %ERRORLEVEL%==0 (
    echo [OK] MySQL restarted successfully
) else (
    echo [FAIL] MySQL could not restart!
)
goto watchdog
