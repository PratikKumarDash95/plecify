@echo off
REM Loads .env then starts the backend. Usage: run.cmd
setlocal enabledelayedexpansion

cd /d "%~dp0"

if exist ".env" (
  for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    set "line=%%A"
    REM skip blank lines and comments
    if not "!line!"=="" if not "!line:~0,1!"=="#" set "%%A=%%B"
  )
) else (
  echo No .env found — create .env and fill in BREVO_API_KEY.
)

call mvnw.cmd spring-boot:run
endlocal




