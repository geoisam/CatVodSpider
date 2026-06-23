@echo off
chcp 65001 >nul

call "%~dp0\gradlew" assembleRelease --no-daemon

call "%~dp0\jar\genJar.bat" %1

exit