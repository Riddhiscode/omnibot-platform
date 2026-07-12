@echo off
REM ============================================================
REM  OmniBot - Start MySQL
REM
REM  Edit MYSQL_HOME below to match where you extracted MySQL.
REM  Default assumes MySQL was extracted to %USERPROFILE%\mysql
REM ============================================================

setlocal

if "%MYSQL_HOME%"=="" set MYSQL_HOME=%USERPROFILE%\mysql
if "%MYSQL_DATA%"=="" set MYSQL_DATA=%MYSQL_HOME%\data

if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo [ERROR] mysqld.exe not found at %MYSQL_HOME%\bin
    echo Set MYSQL_HOME to your MySQL install folder, e.g.:
    echo     set MYSQL_HOME=C:\Users\YourName\mysql-9.7.0-winx64
    pause
    exit /b 1
)

echo Starting MySQL from %MYSQL_HOME% ...
start "OmniBot MySQL" "%MYSQL_HOME%\bin\mysqld.exe" --console --datadir="%MYSQL_DATA%"

timeout /t 3 >nul
echo MySQL starting in a new window. Keep that window open.
echo You can now run scripts\run-app.bat
pause
