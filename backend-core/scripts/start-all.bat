@echo off
REM ============================================================
REM  OmniBot - Start everything (MySQL, then the app)
REM ============================================================

call "%~dp0start-mysql.bat"
echo.
echo Waiting a few seconds for MySQL to be ready...
timeout /t 5 >nul

call "%~dp0run-app.bat"
