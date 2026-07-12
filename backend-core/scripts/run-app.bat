@echo off
REM ============================================================
REM  OmniBot - Run the Spring Boot app
REM
REM  If 'java' or 'mvn' are not on your permanent PATH, set
REM  JAVA_HOME and MAVEN_HOME below to match your install folders.
REM ============================================================

setlocal

if not "%JAVA_HOME%"=="" set "PATH=%JAVA_HOME%\bin;%PATH%"
if not "%MAVEN_HOME%"=="" set "PATH=%MAVEN_HOME%\bin;%PATH%"

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 'java' not found on PATH.
    echo Set JAVA_HOME at the top of this script, e.g.:
    echo     set JAVA_HOME=C:\Users\YourName\java\jdk-21
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 'mvn' not found on PATH.
    echo Set MAVEN_HOME at the top of this script, e.g.:
    echo     set MAVEN_HOME=C:\Users\YourName\maven\apache-maven-3.9.16
    pause
    exit /b 1
)

REM Move to the project root (parent of this scripts/ folder)
cd /d "%~dp0\.."

echo Make sure MySQL is running first (scripts\start-mysql.bat)
echo Starting OmniBot Platform...
echo.

mvn spring-boot:run

pause
