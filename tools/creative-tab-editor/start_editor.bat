@echo off
cd /d "%~dp0..\.."
start "" http://127.0.0.1:4311
node tools\creative-tab-editor\server.js
