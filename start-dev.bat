@echo off
echo Starting AI-Yunxun Development Environment...

echo.
echo Starting Backend...
start "Backend" cmd /k "cd backend && mvn spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 10 /nobreak > nul

echo.
echo Starting Frontend...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo Development environment started!
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo.
pause
