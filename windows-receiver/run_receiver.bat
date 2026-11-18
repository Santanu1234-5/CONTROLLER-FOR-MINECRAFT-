@echo off
title Minecraft & Free Fire PC Receiver (Administrator Mode)
echo ========================================================
echo  Checking Administrator privileges for MSI/BlueStacks...
echo ========================================================

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo Requesting Administrator permission so Windows does NOT block inputs
    echo inside MSI App Player / BlueStacks...
    powershell -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

echo ========================================================
echo  Starting Controller PC Receiver (Administrator)...
echo ========================================================
cd /d "%~dp0"
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH!
    echo Please install Python 3 from https://www.python.org/
    echo Make sure to check "Add Python to PATH" during installation.
    pause
    exit /b 1
)

python "%~dp0receiver.py"
pause
