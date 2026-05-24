@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat --console=plain run
if errorlevel 1 (
    echo.
    echo Launch failed. Check the error above.
    pause
)
