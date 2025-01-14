@echo off

setlocal EnableDelayedExpansion
 
PUSHD %~DP0 & cd /d "%~dp0"
%1 %2
mshta vbscript:createobject("shell.application").shellexecute("%~s0","goto :runas","","runas",1)(window.close)&goto :eof
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

set /p drive_letter=Enter the drive letter (e.g., E:\): 
set drive_letter=%drive_letter:~0,2%

if not exist %drive_letter%\ (
    echo The selected drive %drive_letter% does not exist.
    pause
    exit /b
)

set /p name=Enter name location Icon ex: \Folder\Subfolder\autorun.ico:
if "%name%"=="" set name=autorun.ico

set /p apps=Enter name of Location path App to run: 

echo [autorun]> %drive_letter%\autorun.inf
echo label=%drive_letter%>> %drive_letter%\autorun.inf
echo icon=%name%>> %drive_letter%\autorun.inf
echo open=%apps%>> %drive_letter%\autorun.inf
echo action=Run the program>> %drive_letter%\autorun.inf
echo shell\open\command=%apps%>> %drive_letter%\autorun.inf
echo shell\explore\command=%apps%>> %drive_letter%\autorun.inf
echo shellexecute=%apps%>> %drive_letter%\autorun.inf
echo shell\open=Open>> %drive_letter%\autorun.inf
echo shell\open=%apps%>> %drive_letter%\autorun.inf
echo shell\explore=Explore>> %drive_letter%\autorun.inf
echo shell\explore=%apps%>> %drive_letter%\autorun.inf
echo shell\open\default=1>> %drive_letter%\autorun.inf
echo shell\explore\default=1>> %drive_letter%\autorun.inf
echo shell\open\default=%apps%>> %drive_letter%\autorun.inf
echo shell\explore\default=%apps%>> %drive_letter%\autorun.inf
echo UseAutoPlay=1>> %drive_letter%\autorun.inf
echo UseAutoRun=1>> %drive_letter%\autorun.inf

if exist "%name%" (
    copy /Y "%name%" "%drive_letter%\%name%"
) else (
    echo Icon file %name% not found, skipping icon copy.
)

if exist "%apps%" (
    copy /Y "%apps%" "%drive_letter%\%apps%"
) else (
    echo Application %apps% not found, skipping application copy.
)

echo "Do You Want to Hide the Files? (Y/N)"
set /p hide=
if "%hide%"=="Y" attrib +h %drive_letter%\*.* /s /d
if "%hide%"=="N" attrib -h %drive_letter%\*.* /s /d

echo.
echo Autorun files created successfully on %drive_letter%.
pause
endlocal
