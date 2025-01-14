$myWindowsVersion = [System.Environment]::OSVersion.Version
$adminCheck = [Security.Principal.WindowsIdentity]::GetCurrent()
$adminRole = New-Object Security.Principal.WindowsPrincipal($adminCheck)
$IsAdmin = $adminRole.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $IsAdmin) {
    Start-Process powershell -ArgumentList "Start-Process powershell -Verb RunAs -ArgumentList $($myWindowsVersion.ToString())" -Verb RunAs
    exit
}

Clear-Host
Write-Host "USB Autorun Creator by Xnuvers007" -ForegroundColor Cyan
Write-Host ""

Write-Host "Available Drives:"
Get-WmiObject -Class Win32_LogicalDisk | ForEach-Object {Write-Host "$($_.DeviceID)"}

$driveLetter = Read-Host "Enter the drive letter (e.g., E:)"
$driveLetter = $driveLetter.Trim()

if (-not (Test-Path "$driveLetter\")) {
    Write-Host "The selected drive $driveLetter does not exist." -ForegroundColor Red
    exit
}

$name = Read-Host "Enter name location Icon file (e.g., \path\to\icon.ico)"
if (-not $name) { $name = "Autorun.ico" }

$apps = Read-Host "Enter name of Application to run"

$autorunFilePath = "$driveLetter\autorun.inf"
@"
[autorun]
label=$driveLetter
icon=$name
open=$apps
action=Run the program
shell\open\command=$apps
shell\explore\command=$apps
shell\open=Open
shell\open=$apps
shell\explore=Explore
shell\explore=$apps
shell\open\default=1
shell\explore\default=1
shell\open\default=$apps
shell\explore\default=$apps
UseAutoPlay=1
UseAutoRun=1
"@ | Set-Content -Path $autorunFilePath

# if (Test-Path "Autorun.ico") {
#     Copy-Item -Path "Autorun.ico" -Destination "$driveLetter\$name" -Force
# } else {
#     Write-Host "Icon file Autorun.ico not found, skipping icon copy." -ForegroundColor Yellow
# }

if (Test-Path $name) {
    Copy-Item -Path $name -Destination "$driveLetter\$name" -Force
} else {
    Write-Host "Icon file $name not found, skipping icon copy." -ForegroundColor Yellow
}

if (Test-Path $apps) {
    Copy-Item -Path $apps -Destination "$driveLetter\$apps" -Force
} else {
    Write-Host "Application $apps not found, skipping application copy." -ForegroundColor Yellow
}

Write-Host "Autorun files created successfully on $driveLetter."

# make the file hidden
$autorunFile = Get-Item $autorunFilePath
$autorunFile.Attributes = "Hidden"

Write-Host "Autorun files created successfully on $driveLetter."
Pause