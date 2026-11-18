@echo off
title Autonomous YouTube Shorts Publishing Agent
echo ============================================================
echo   AUTONOMOUS YOUTUBE SHORTS PUBLISHING AGENT (AGY 2.0)
echo ============================================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1/3] Checking environment...
if not exist "data" mkdir data

echo [2/3] Launching Spring Boot Backend (Port 8080)...
start "YouTube Agent Backend (Port 8080)" cmd /k "cd /d "%~dp0backend" && java -jar target\youtube-publishing-agent-1.0.0.jar"

timeout /t 6 /nobreak > nul

echo [3/3] Launching Frontend Dashboard (Port 5173)...
start "YouTube Agent Dashboard (Port 5173)" cmd /k "cd /d "%~dp0frontend" && npm run dev -- --host 127.0.0.1 --port 5173"

timeout /t 3 /nobreak > nul

echo.
echo ============================================================
echo   Agent System Running!
echo   Dashboard URL: http://localhost:5173
echo   Backend URL:   http://localhost:8080
echo   H2 Console:    http://localhost:8080/h2-console
echo ============================================================
start http://localhost:5173

pause
