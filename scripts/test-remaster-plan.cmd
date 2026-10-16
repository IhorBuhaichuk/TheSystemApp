@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0test-remaster-plan.ps1"
exit /b %errorlevel%
