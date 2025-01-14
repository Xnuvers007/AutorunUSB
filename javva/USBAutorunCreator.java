package javva;

// import com.sun.jna.platform.win32.Kernel32;
// import com.sun.jna.platform.win32.WinNT;

// import java.io.File;
// import java.io.PrintStream;
// import java.nio.file.Files;
// import java.nio.file.LinkOption;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Objects;
// import java.util.Scanner;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class USBAutorunCreator {
    private static class PrettyTable {
        private String title;
        private List<String> fieldNames;
        private List<List<String>> rows;
        private int[] columnWidths;

        public PrettyTable() {
            this.fieldNames = new ArrayList<>();
            this.rows = new ArrayList<>();
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setFieldNames(String... names) {
            fieldNames = Arrays.asList(names);
            updateColumnWidths();
        }

        public void addRow(String... values) {
            rows.add(Arrays.asList(values));
            updateColumnWidths();
        }

        private void updateColumnWidths() {
            int columns = Math.max(
                fieldNames.size(),
                rows.stream().mapToInt(List::size).max().orElse(0)
            );
            columnWidths = new int[columns];
            
            // Check headers
            for (int i = 0; i < fieldNames.size(); i++) {
                columnWidths[i] = Math.max(columnWidths[i], fieldNames.get(i).length());
            }
            
            // Check all rows
            for (List<String> row : rows) {
                for (int i = 0; i < row.size(); i++) {
                    columnWidths[i] = Math.max(columnWidths[i], row.get(i).length());
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            // Title
            if (title != null) {
                sb.append(title).append("\n");
            }
            
            // Header
            sb.append("|");
            for (int i = 0; i < fieldNames.size(); i++) {
                sb.append(String.format(" %-" + columnWidths[i] + "s |", fieldNames.get(i)));
            }
            sb.append("\n");
            
            // Separator
            sb.append("+");
            for (int width : columnWidths) {
                sb.append("-".repeat(width + 2)).append("+");
            }
            sb.append("\n");
            
            // Rows
            for (List<String> row : rows) {
                sb.append("|");
                for (int i = 0; i < row.size(); i++) {
                    sb.append(String.format(" %-" + columnWidths[i] + "s |", row.get(i)));
                }
                sb.append("\n");
            }
            
            return sb.toString();
        }
    }

    private static class ArchitectureInfo {
        public static String getArchitectureInfo() {
            String arch = System.getProperty("os.arch");
            if (arch.contains("64")) {
                return "x64";
            } else if (arch.contains("86")) {
                return "x86";
            }
            return "Unknown Architecture";
        }

        public static String getWindowsVersion() {
            String version = System.getProperty("os.version");
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("windows")) {
                if (version.startsWith("10.")) return "Windows 10";
                if (version.startsWith("6.3")) return "Windows 8.1";
                if (version.startsWith("6.2")) return "Windows 8";
                if (version.startsWith("6.1")) return "Windows 7";
                if (version.startsWith("6.0")) return "Windows Vista";
                if (version.startsWith("5.1")) return "Windows XP";
                if (version.startsWith("5.0")) return "Windows 2000";
            }
            return "Unknown Windows version";
        }
    }

    private static void createAutorun(String driveLetter, String name, String apps) {
        try {
            if (!Files.exists(Paths.get(driveLetter))) {
                throw new IllegalArgumentException("The drive " + driveLetter + " is not accessible.");
            }

            // Get drive information
            File drive = new File(driveLetter);
            String fileSystem = Files.getFileStore(drive.toPath()).type();

            if (Arrays.asList("FAT32", "NTFS", "exFAT").contains(fileSystem.toUpperCase())) {
                // Create autorun.inf
                Path autorunPath = Paths.get(driveLetter, "autorun.inf");
                List<String> autorunContent = Arrays.asList(
                    "[autorun]",
                    "label=" + drive.getName(),
                    "icon=" + name,
                    "open=" + apps,
                    "action=Run the program",
                    "shell\\open\\command=" + apps,
                    "shell\\explore\\command=" + apps
                );
                Files.write(autorunPath, autorunContent);

                // Copy icon
                String currentPath = new File(".").getCanonicalPath();
                Path iconSource = Paths.get(currentPath, "..", "Autorun.ico");
                Path iconDest = Paths.get(driveLetter, name);

                if (Files.exists(iconSource)) {
                    if (!iconSource.equals(iconDest)) {
                        Files.copy(iconSource, iconDest, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        System.out.println("Source and destination for icon are the same, skipping copy.");
                    }
                } else {
                    System.out.println("Icon file " + name + " not found, skipping icon copy.");
                }

                // Copy app
                Path appSource = Paths.get(currentPath, "..", "output", apps);
                Path appDest = Paths.get(driveLetter, apps);
                Files.copy(appSource, appDest, StandardCopyOption.REPLACE_EXISTING);

                // System.out.println("Autorun files created successfully.");
                // make it hidden
                try (Scanner scanner = new Scanner(System.in)) {

                    System.out.print("Do you want to hide autorun.inf? (y/n): ");
                    String hideAutorun = scanner.nextLine().toLowerCase();

                    System.out.print("Do you want to hide the icon and app files? (y/n): ");
                    String hideFiles = scanner.nextLine().toLowerCase();

                    System.out.print("Do you want to hide the icon and app files but show autorun.inf? (y/n): ");
                    String hideFilesShowAutorun = scanner.nextLine().toLowerCase();

                    if (hideAutorun.equals("y")) {
                        ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command("cmd.exe", "/c", "attrib +h +s " + driveLetter + "autorun.inf");
                        processBuilder.start();
                    }

                    if (hideFiles.equals("y")) {
                        ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command("cmd.exe", "/c", "attrib +h +s " + driveLetter + name);
                        processBuilder.start();
                        processBuilder.command("cmd.exe", "/c", "attrib +h +s " + driveLetter + apps);
                        processBuilder.start();
                    }

                    if (hideFilesShowAutorun.equals("y")) {
                        ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command("cmd.exe", "/c", "attrib +h +s " + driveLetter + name);
                        processBuilder.start();
                        processBuilder.command("cmd.exe", "/c", "attrib +h +s " + driveLetter + apps);
                        processBuilder.start();
                        processBuilder.command("cmd.exe", "/c", "attrib -h -s " + driveLetter + "autorun.inf");
                        processBuilder.start();
                    }

                    System.out.println("Autorun files created successfully.");
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e.getMessage());
                }
            } else {
                System.out.println("The selected drive has an unsupported file system (" + fileSystem + 
                                 "). Supported file systems: FAT32, NTFS, exFAT.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static String[] getStatement() {
        try (Scanner scanner = new Scanner(System.in)) {
            String driveLetter = "", name = "", apps = "";
            
            // Get available drives
            List<String> drives = new ArrayList<>();
            for (File root : File.listRoots()) {
                drives.add(root.getAbsolutePath());
            }

            System.out.println("Available drives:");
            drives.forEach(System.out::println);

            // Get drive letter
            while (true) {
                System.out.print("Enter the drive letter (e.g., E:\\): ");
                driveLetter = scanner.nextLine().toUpperCase();

                if (driveLetter.isEmpty() && !drives.isEmpty()) {
                    driveLetter = drives.get(0);
                }

                if (driveLetter.length() == 1) {
                    driveLetter += ":\\";
                } else if (driveLetter.endsWith(":")) {
                    driveLetter += "\\";
                } else if (!driveLetter.endsWith("\\")) {
                    driveLetter += "\\";
                }

                if (drives.contains(driveLetter)) {
                    break;
                }
                System.out.println("The selected drive " + driveLetter + " is not available.");
            }

            // Get icon name
            System.out.println("\n[Default = Autorun.ico]");
            System.out.println("[Example = E:\\Autorun.ico]");
            System.out.print("Enter name location Icon: ");
            name = scanner.nextLine();
            
            if (name.isEmpty()) {
                name = "autorun.ico";
            }

            if (name.contains(":")) {
                name = name.replace("/", "\\");
            }

            // Get app name
            while (true) {
                System.out.print("\nEnter name Apps: ");
                apps = scanner.nextLine();
                if (!apps.isEmpty()) {
                    break;
                }
                System.out.println("Name Apps cannot be empty.");
            }
        return new String[]{driveLetter, name, apps};
    } catch (Exception e) {
        System.out.println("An error occurred: " + e.getMessage());
        handleExit();
        return new String[0];
    }
}

    private static void handleExit() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Are you sure you want to exit? (y/n): ");
                String input = scanner.nextLine().toLowerCase();
                if (input.equals("y")) {
                    System.out.println("Exiting the program...");
                    System.exit(0);
                } else if (input.equals("n")) {
                    main(new String[0]);
                    return;
                } else {
                    System.out.println("Invalid input. Please enter 'y' to exit or 'n' to continue.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        PrettyTable table = new PrettyTable();
        table.setTitle("USB Autorun Creator by Xnuvers007");
        
        PrettyTable table2 = new PrettyTable();
        table2.setTitle("System Information");

        try {
            String[] info = getStatement();
            String driveLetter = info[0];
            String name = info[1];
            String apps = info[2];

            String archInfo = ArchitectureInfo.getArchitectureInfo();

            table.setFieldNames("To Drive", "Icon Name", "App Name");
            table.addRow(driveLetter, name, apps);

            table2.setFieldNames("Architecture", "Platform", "Version");
            table2.addRow(archInfo, System.getProperty("os.name"), ArchitectureInfo.getWindowsVersion());

            System.out.println(table);
            System.out.println(table2);

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Is the above information correct? (y/n): ");
                String answer = scanner.nextLine();

                if (answer.toLowerCase().equals("y")) {
                    createAutorun(driveLetter, name, apps);
                } else if (answer.toLowerCase().equals("n")) {
                    getStatement();
                } else {
                    System.out.println("Invalid choice");
                    System.exit(1);
                }
            } catch (Exception e) {
                System.out.println("\nAn error occurred: " + e.getMessage());
                handleExit();
            }
        } catch (Exception e) {
            System.out.println("\nAn error occurred: " + e.getMessage());
            handleExit();
        }
    }
}