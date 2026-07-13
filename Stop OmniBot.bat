@echo off
title Stop OmniBot
color 0C

echo Stopping OmniBot...
taskkill /f /im java.exe >nul 2>&1
echo [OK] OmniBot stopped.

echo MySQL is still running (needed by other things).
echo To stop MySQL too, run: taskkill /f /im mysqld.exe
echo.
pause
