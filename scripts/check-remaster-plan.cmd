@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0check-remaster-plan.ps1"
exit /b %errorlevel%
