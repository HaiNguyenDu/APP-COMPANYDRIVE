package BLL;

import DTO.*;
import DAL.ConnectWindowServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSHExample {
    static public void setAccount(String Host, String User, String Password) {
        ConnectWindowServer.setAccount(Host, User, Password);
    }

    public static ArrayList<File_Folder> FindFolder(String FolderName) throws Exception {
        String information = ConnectWindowServer.FindFolder(FolderName);
        System.out.println(information);
        String[] lines = information.split("\n");
        ArrayList<File_Folder> items = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a");
    
        // Bắt đầu từ dòng thứ 2 để bỏ qua phần tiêu đề
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) { // Kiểm tra dòng không rỗng
                // Sử dụng regex để tách tên và thời gian, cho phép bất kỳ khoảng trắng nào
                Pattern pattern = Pattern.compile("(.+?)\\s+([0-9]{2}/[0-9]{2}/[0-9]{4} [0-9]{1,2}:[0-9]{2}:[0-9]{2} [AP]M)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String name = matcher.group(1).trim();
                    String lastWriteTimeStr = matcher.group(2).trim();
                    try {
                        LocalDateTime lastWriteTime = LocalDateTime.parse(lastWriteTimeStr, formatter);
                        items.add(new File_Folder(name, lastWriteTime));
                    } catch (Exception e) {
                        System.err.println("Lỗi phân tích ngày giờ: " + e.getMessage());
                    }
                } else {
                    System.err.println("Không tìm thấy khớp cho dòng: " + line);
                }
            }
        }
    
        if (!items.isEmpty()) {
            System.err.println(items.get(0).toString());
        } else {
            System.err.println("Danh sách items rỗng.");
        }
        return items;
    }
    // Phương thức chia sẻ thư mục với người dùng trong domain với quyền truy cập
    // xác định
    static public void ShareFolder(String folderPath, String username, String access) throws Exception {
        String reString = ConnectWindowServer.SharedFolder(folderPath, username, access);
        System.err.println(reString);
    }

    static public ArrayList<String> listDomainUsers() throws Exception {
        ArrayList<String> users = new ArrayList<>();
        String output = ConnectWindowServer.listDomainUsers();
        String[] lines = output.toString().split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                users.add(line);
                System.err.println(line);
            }
        }
        return users;
    }
}
