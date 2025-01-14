@echo off

setlocal EnableDelayedExpansion

PUSHD %~DP0 & cd /d "%~dp0"
%1 %2
mshta vbscript:createobject("shell.application").shellexecute("%~s0","goto :runas","","runas",1)(window.close)&goto :eof
:runas
color b
cls

Title Solve Visible Autorun.inf by Xnuvers007

echo "Set the Drive Ex: E:"
set /p drive=Enter the Drive Letter:

echo "Set the Path Ex: \Folder\Subfolder"
set /p path=Enter the Path:

echo "Set the ICO File Name with Extension Ex: Icon.ico"
set /p ico_file=Enter the ICO File Name with Extension:

echo "Set the EXE File Name with Extension Ex: App.exe"
set /p exe_file=Enter the EXE File Name with Extension:

echo "Set the Autorun.inf File Name with Extension Ex: Autorun.inf"
set /p autorun_file=Enter the Autorun.inf File Name with Extension:

:: Validate inputs
if "%drive%"=="" (
    echo "Drive letter cannot be empty."
    exit /b 1
)
if "%path%"=="" (
    echo "Path cannot be empty."
    exit /b 1
)
if "%ico_file%"=="" (
    echo "ICO file name cannot be empty."
    exit /b 1
)
if "%exe_file%"=="" (
    echo "EXE file name cannot be empty."
    exit /b 1
)
if "%autorun_file%"=="" (
    echo "Autorun.inf file name cannot be empty."
    exit /b 1
)

:: Remove hidden, read-only, and system attributes
set command=attrib -h -r -s "%drive%:\%path%\%ico_file%"
%command%

set command=attrib -h -r -s "%drive%:\%path%\%exe_file%"
%command%

set command=attrib -h -r -s "%drive%:\%path%\%autorun_file%"
%command%

echo "Do you want to delete the files? (Y/N)"
set /p delete=Enter Y/N:

if /I "%delete%"=="Y" (
    del "%drive%:\%path%\%ico_file%"
    if %errorlevel% neq 0 echo "Failed to delete %ico_file%"
    del "%drive%:\%path%\%exe_file%"
    if %errorlevel% neq 0 echo "Failed to delete %exe_file%"
    del "%drive%:\%path%\%autorun_file%"
    if %errorlevel% neq 0 echo "Failed to delete %autorun_file%"
) else (
    echo "Files are not deleted."
)

endlocal