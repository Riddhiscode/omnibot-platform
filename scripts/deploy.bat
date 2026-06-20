@echo off
REM ============================================================
REM  OmniBot - deploy.bat
REM  One-click: start MySQL, run the app, and (optionally) push
REM  your latest changes to GitHub.
REM
REM  Usage:
REM    deploy.bat            -> starts MySQL + app only
REM    deploy.bat push       -> also commits & pushes to GitHub first
REM
REM  First-time setup: fill in the paths below to match your machine.
REM ============================================================

setlocal

REM ---- EDIT THESE TO MATCH YOUR MACHINE -----------------------
set JAVA_HOME=C:\Users\riddh\java\jdk-26.0.1
set MAVEN_HOME=C:\Users\riddh\maven\apache-maven-3.9.16
set MYSQL_HOME=C:\Users\riddh\mysql-9.7.0-winx64
set MYSQL_DATA=%MYSQL_HOME%\data
REM ---------------------------------------------------------------

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
cd /d "%~dp0\.."

echo ============================================
echo  OmniBot Deploy
echo ============================================

REM ---- Step 1: optional git push -------------------------------
if /I "%1"=="push" (
    where git >nul 2>nul
    if errorlevel 1 (
        echo [WARN] git not found on PATH - skipping push step.
    ) else (
        echo.
        echo --- Pushing latest changes to GitHub ---
        git add .
        set /p COMMIT_MSG="Commit message (or press Enter for default): "
        if "%COMMIT_MSG%"=="" set COMMIT_MSG=chore: update OmniBot %date% %time%
        git commit -m "%COMMIT_MSG%"
        git push
        echo --- Push step complete ---
    )
)

REM ---- Step 2: start MySQL if not already running ---------------
echo.
echo --- Starting MySQL ---
tasklist /FI "IMAGENAME eq mysqld.exe" 2>nul | find /I "mysqld.exe" >nul
if errorlevel 1 (
    if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
        echo [ERROR] mysqld.exe not found at %MYSQL_HOME%\bin
        echo Edit MYSQL_HOME at the top of this script.
        pause
        exit /b 1
    )
    start "OmniBot MySQL" "%MYSQL_HOME%\bin\mysqld.exe" --console --datadir="%MYSQL_DATA%"
    echo Waiting for MySQL to be ready...
    timeout /t 6 >nul
) else (
    echo MySQL is already running.
)

REM ---- Step 3: run the app ---------------------------------------
echo.
echo --- Starting OmniBot Platform ---
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 'mvn' not found. Check MAVEN_HOME at the top of this script.
    pause
    exit /b 1
)

mvn spring-boot:run

pause
