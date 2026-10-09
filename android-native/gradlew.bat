@echo off
setlocal
set VERSION=8.9
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set BASE=%GRADLE_USER_HOME%\datzon-wrapper
set DIST=%BASE%\gradle-%VERSION%
set ARCHIVE=%BASE%\gradle-%VERSION%-bin.zip
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%ARCHIVE%" (
    if not exist "%BASE%" mkdir "%BASE%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%ARCHIVE%'"
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ARCHIVE%' '%BASE%'"
)
call "%DIST%\bin\gradle.bat" %*
