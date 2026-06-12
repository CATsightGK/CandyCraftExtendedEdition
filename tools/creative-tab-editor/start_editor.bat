@echo off
cd /d "%~dp0..\.."
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /r /c:":4311 .*LISTENING"') do taskkill /pid %%a /f >nul 2>nul
start "CandyCraft Creative Tab Editor" /min node tools\creative-tab-editor\server.js
timeout /t 1 /nobreak >nul
start "" http://127.0.0.1:4311/?v=7
