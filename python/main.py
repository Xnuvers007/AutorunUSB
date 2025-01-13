import os, sys, re
import shutil
import win32api
import win32file
from prettytable import PrettyTable

table = PrettyTable()
table.title = "USB Autorun Creator by Xnuvers007"
table2 = PrettyTable()
table2.title = "System Information"

class architecture_info:
    def get_architecture_info():
        match = re.search(r'\[(.*)\]', sys.version)
        if match:
            return match.group(1)
        else:
            return "Unknown Architecture"

    def get_windows_version():
        windows = win32api.GetVersionEx()
        if windows[0] == 10:
            return "Windows 10"
        elif windows[0] == 6:
            if windows[1] == 3:
                return "Windows 8.1"
            elif windows[1] == 2:
                return "Windows 8"
            elif windows[1] == 1:
                return "Windows 7"
            elif windows[1] == 0:
                return "Windows Vista"
        elif windows[0] == 5:
            if windows[1] == 1:
                return "Windows XP"
            elif windows[1] == 0:
                return "Windows 2000"
        return "Unknown Windows version"

def autorun(drive_letter, name, apps):
    try:
        if not os.path.exists(drive_letter):
            raise ValueError(f"The drive {drive_letter} is not accessible.")

        drive_name, _, _, _, drive_file_system = win32api.GetVolumeInformation(drive_letter)
        
        if drive_file_system in ["FAT32", "NTFS", "exFAT"]:
            autorun_file_path = os.path.join(drive_letter, "autorun.inf")
            with open(autorun_file_path, "w") as autorun_file:
                autorun_file.write("[autorun]\n")
                autorun_file.write(f"label={drive_name}\n")
                autorun_file.write(f"icon={name}\n")                
                autorun_file.write(f"open={apps}\n")
                autorun_file.write("action=Run the program\n")
                autorun_file.write(f"shell\\open\\command={apps}\n")
                autorun_file.write(f"shell\\explore\\command={apps}\n")

            # Copy icon to the USB drive
            current_path = os.path.dirname(os.path.abspath(__file__))
            # icon_source_path = os.path.join(current_path, f"{name}")
            # icon_source_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "Autorun.ico")
            icon_source_path = os.path.join(current_path, "..", "Autorun.ico")
            icon_source_path = os.path.abspath(icon_source_path)

            icon_destination_path = os.path.join(drive_letter, f"{name}")

            if os.path.exists(icon_source_path):
                if icon_source_path != icon_destination_path:
                    shutil.copyfile(icon_source_path, icon_destination_path)
                else:
                    print(f"Source and destination for icon are the same, skipping copy.")
            else:
                print(f"Icon file {name} not found, skipping icon copy.")

            # Copy main.py to the USB drive            
            script_source_path = os.path.join(current_path, "..", "output", f"{apps}")
            script_destination_path = os.path.join(drive_letter, f"{apps}")

            shutil.copyfile(script_source_path, script_destination_path)

            print("Autorun files created successfully.")
        else:
            print(f"The selected drive has an unsupported file system ({drive_file_system}). Supported file systems: FAT32, NTFS, exFAT.")
    except Exception as e:
        print(f"An error occurred: {e}")

def pernyataan():
    # drives = []
    # # Cek drive yang tersedia dari A hingga Z
    # for letter in range(ord('A'), ord('Z') + 1):
    #     drive = f"{chr(letter)}:\\"
    #     if os.path.exists(drive):
    #         drives.append(drive)
    
    drives = [f"{chr(letter)}:\\" for letter in range(ord('A'), ord('Z') + 1) if os.path.exists(f"{chr(letter)}:\\")]

    print("Available drives:")
    for drive in drives:
        print(drive)

    global drive_letter, name, apps
    while True:
        drive_letter = input("Enter the drive letter (e.g., E:\\): ").upper()

        # Pastikan drive letter sudah benar
        if drive_letter == "":
            drive_letter = drives[1]

        if len(drive_letter) == 1:  # F
            drive_letter = drive_letter + ":\\"  # F -> F:\\
        elif drive_letter[-1] == ":":  # F:
            drive_letter = drive_letter + "\\"  # F: -> F:\\
        elif drive_letter[-1] != "\\":  # F:/, F:\
            drive_letter = drive_letter + "\\"  # F:/, F:\ -> F:\\

        if drive_letter not in drives:
            print(f"The selected drive {drive_letter} is not available.")
            continue
        else:
            break

    name = input("\n[Default = Autorun.ico]\n[Example = E:\\Autorun.ico]\nEnter name location Icon: ")
    if name == "":
        name = "autorun.ico"

    if ":" in name:
        name = name.replace("/", "\\")
    
    while True:
        apps = input("\nEnter name Apps: ")
        if apps != "":
            break
        else:
            print("Name Apps cannot be empty.")

    return drive_letter, name, apps

def handle_exit():
    while True:
        user_input = input("Are you sure you want to exit? (y/n): ")
        if user_input.lower() == "y":
            print("Exiting the program...")
            sys.exit(0)
        elif user_input.lower() == "n":
            return main()
        else:
            print("Invalid input. Please enter 'y' to exit or 'n' to continue.")

def main():
    drive_letter = ""
    name = ""
    apps = ""
    
    try:
        drive_letter, name, apps = pernyataan()
        arch_info = architecture_info.get_architecture_info().replace("32", "x86").replace("64", "x64")

        table.field_names = ["To Drive", "Icon Name", "App Name"]
        table.add_row([drive_letter, name, apps])

        table2.field_names = ["Architecture", "Platform", "Version"]
        table2.add_row([arch_info, sys.platform, architecture_info.get_windows_version()])

        print(table , "\n" , table2)

        tanya = input("Is the above information correct? (y/n): ")
        if tanya.lower() == "y":
            autorun(drive_letter, name, apps)
        elif tanya.lower() == "n":
            pernyataan()
        else:
            print("Invalid choice")
            sys.exit(1)
    except KeyboardInterrupt:
        print("\nCtrl+C detected!")
        handle_exit()
    except EOFError:
        print("\nCtrl+Z detected!")
        handle_exit()

if __name__ == "__main__":
    main()
