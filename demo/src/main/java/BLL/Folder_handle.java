package BLL;

import java.io.*;

import DAL.ConnectWindowServer;
import DTO.Host;

public class Folder_handle {
    public static void listSharedFiles(String remoteServer) {
        try {
            Process process = Runtime.getRuntime().exec("net view \\\\" + remoteServer);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            System.out.println("Danh sách thư mục được chia sẻ từ máy chủ " + remoteServer + ":");
            while ((line = reader.readLine()) != null) {
                if (line.contains("Share name")) {
                    continue; // Bỏ qua dòng tiêu đề
                }
                if (line.trim().isEmpty()) {
                    continue; // Bỏ qua dòng trống
                }
                System.out.println(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void listSharedFolders() {
        try {
            // Chạy lệnh net share để liệt kê các thư mục đã được chia sẻ
            Process process = Runtime.getRuntime().exec("net share");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            System.out.println("Danh sách thư mục đã được chia sẻ:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Đợi cho lệnh hoàn thành
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void shareFolder(String username, String folderPath, String access) {
        try {
            String shareName = new File(folderPath).getName();
            String command = "icacls \"" + folderPath + "\" /grant \"" + username + "\":" + access;
            System.err.println(command);
            Process process = Runtime.getRuntime().exec("cmd /c " + command);
            process.waitFor();
            
            if (process.exitValue() == 0) {
                System.out.println("Thư mục đã được chia sẻ thành công!");
    
                // Define destination path
                String destinationPath = "\\\\" + Host.dnsServer + "\\Share\\" + ConnectWindowServer.user + "\\" + shareName;
                File sourceDir = new File(folderPath);
                File destDir = new File(destinationPath);
    
                // Copy folder
                copyDirectory(sourceDir, destDir);
                System.out.println("Đã sao chép thư mục vào " + destinationPath);
                setPermissions(destDir.getAbsolutePath(), ConnectWindowServer.user, "F");
                setPermissions(destDir.getAbsolutePath(), "Administrators", "F");
                // Set permissions: Read-only for "Thanhan"
                setPermissions(destDir.getAbsolutePath(), "PBL4\\Thanhan", access);
                // Grant full control to admins
                
            } else {
                System.out.println("Không thể chia sẻ thư mục.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static void copyDirectory(File sourceDir, File destDir) throws IOException {
    if (!destDir.exists()) {
        destDir.mkdirs();
    }
    for (String file : sourceDir.list()) {
        File srcFile = new File(sourceDir, file);
        File destFile = new File(destDir, file);

        // Kiểm tra quyền truy cập trước khi sao chép
        if (srcFile.canRead()) {
            if (srcFile.isDirectory()) {
                copyDirectory(srcFile, destFile); 
            } else {
                java.nio.file.Files.copy(srcFile.toPath(), destFile.toPath());
            }
        } else {
            System.out.println("Không thể đọc tệp: " + srcFile.getAbsolutePath());
        }
    }
}

private static void setPermissions(String folderPath, String username, String access) {
    try {
        // Tạo file batch
        String batchFilePath = "set_permissions.bat";
        FileWriter fileWriter = new FileWriter(batchFilePath);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        // Ghi lệnh vào file batch
        printWriter.println("@echo off");
        printWriter.println("icacls \"" + folderPath + "\" /inheritance:r");
        printWriter.println("icacls \"" + folderPath + "\" /grant \"Everyone:R\"");

        // Thêm quyền cho người dùng cụ thể
        String domainUser = "PBL4\\" + username;
        switch (access) {
            case "F": // Full control
                printWriter.println("icacls \"" + folderPath + "\" /grant \"" + domainUser + "\":F");
                break;
            case "R": // Read-only access
                printWriter.println("icacls \"" + folderPath + "\" /grant \"" + domainUser + "\":R");
                break;
            case "D": // Deny access entirely
                printWriter.println("icacls \"" + folderPath + "\" /deny \"" + domainUser + "\":(F)");
                break;
            case "RW": // Custom read and write access, denying modify
                printWriter.println("icacls \"" + folderPath + "\" /grant \"" + domainUser + "\":R /deny \"" + domainUser + "\":W");
                break;
            default: // Nếu không nhận diện được, mặc định là chỉ đọc
                printWriter.println("icacls \"" + folderPath + "\" /grant \"" + domainUser + "\":R");
                System.out.println("Loại quyền không nhận diện được. Mặc định thành chỉ đọc.");
                break;
        }

        // Cấm quyền Read cho các tài khoản không mong muốn
        printWriter.println("icacls \"" + folderPath + "\" /deny \"PBL4\\XPhuc:(R)\""); // Cấm quyền Read cho XPhuc
        printWriter.println("icacls \"" + folderPath + "\" /deny \"Administrators:(R)\""); // Cấm quyền Read cho Administrators

        // Đóng file writer
        printWriter.close();

        // Chạy file batch
        Process process = Runtime.getRuntime().exec("cmd /c start " + batchFilePath);
        process.waitFor();

        System.out.println("Đã tạo và thực thi file batch để thiết lập quyền truy cập cho thư mục " + folderPath);
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
}


}
