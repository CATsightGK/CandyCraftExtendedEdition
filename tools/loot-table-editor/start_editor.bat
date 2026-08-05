@echo off
cd /d "%~dp0..\.."
set "CANDYCRAFT_LOOT_PORT=4327"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /r /c:":%CANDYCRAFT_LOOT_PORT% .*LISTENING"') do taskkill /pid %%a /f >nul 2>nul
start "CandyCraft Loot Table Editor" /min cmd /c "set PORT=%CANDYCRAFT_LOOT_PORT%&& node tools\loot-table-editor\server.js"
timeout /t 1 /nobreak >nul
start "" "http://127.0.0.1:%CANDYCRAFT_LOOT_PORT%/"
