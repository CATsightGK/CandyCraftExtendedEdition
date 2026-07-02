@echo off
setlocal
cd /d "%~dp0"
if "%PORT%"=="" set PORT=4312
node server.js
