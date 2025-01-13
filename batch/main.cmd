@echo off

setlocal EnableDelayedExpansion
PUSHD %~DP0 & cd /d "%~dp0"

if not defined IS_ADMIN (
    set IS_ADMIN=1
    mshta vbscript:createobject("shell.application").shellexecute("%~s0", "goto :runas", "", "runas", 1)(window.close)
    goto :eof
)

:runas
color b
cls
Title USB Autorun Creator by Xnuvers007

echo USB Autorun Creator by Xnuvers007
echo.

echo Available Drives:
for /f "tokens=1 delims=\" %%a in ('wmic logicaldisk get caption') do (
    echo %%a
)

set /p drive_letter=Enter the drive letter (e.g., E\): 
set drive_letter=%drive_letter:~0,2%

if not exist %drive_letter%\ (
    echo The selected drive %drive_letter% does not exist.
    pause
    exit /b
)

set /p name=Enter name location Icon (default=Autorun.ico): 
if "%name%"=="" set name=autorun.ico

set /p apps=Enter name of Location path App to run: 

echo [autorun]> %drive_letter%\autorun.inf
echo label=%drive_letter%>> %drive_letter%\autorun.inf
echo icon=%name%>> %drive_letter%\autorun.inf
echo open=%apps%>> %drive_letter%\autorun.inf
echo action=Run the program>> %drive_letter%\autorun.inf
echo shell\open\command=%apps%>> %drive_letter%\autorun.inf
echo shell\explore\command=%apps%>> %drive_letter%\autorun.inf

if exist "Autorun.ico" (
    copy /Y "Autorun.ico" "%drive_letter%\%name%"
) else (
    echo Icon file Autorun.ico not found, skipping icon copy.
)

if exist "%apps%" (
    copy /Y "%apps%" "%drive_letter%\%apps%"
) else (
    echo Application %apps% not found, skipping application copy.
)

echo.
echo Autorun files created successfully on %drive_letter%.
pause
endlocal
