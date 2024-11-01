package DAL;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.jcraft.jsch.*;

public class ConnectWindowServer {
    static String host = "pbl4.dut.vn";
    public static String user = "XPhuc";
    static String password = "T0il@mpbl4";

    static JSch jsch = new JSch();
    private static String readChannelOutput(InputStream in, Channel channel) throws IOException {
    StringBuilder result = new StringBuilder();
    byte[] buffer = new byte[1024];
    int readCount;
    
    while (true) {
        while (in.available() > 0) {
            readCount = in.read(buffer);
            if (readCount < 0) break;
            result.append(new String(buffer, 0, readCount));
        }
        if (channel.isClosed()) {
            if (in.available() > 0) continue;
            System.out.println("exit-status: " + channel.getExitStatus());
            break;
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Xử lý nếu cần
        }
    }
    return result.toString();
}


    static public void setAccount(String Host, String User, String Password) {
        host = Host;
        user = User;
        password = Password;
    }

    static public String FindFolder(String FolderName) throws Exception {
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        Channel channel = session.openChannel("exec");
        String command = "powershell -Command \"Get-ChildItem -Path \'" + FolderName + "\' | Select-Object Name, LastWriteTime\r\n";
        System.err.println(command);
        ((ChannelExec) channel).setCommand(command);
        InputStream in = channel.getInputStream();
        channel.connect();
        StringBuilder outputBuilder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            outputBuilder.append(new String(buffer, 0, bytesRead));
        }
        channel.disconnect();
        session.disconnect();
        return outputBuilder.toString();
    }
    

    static public String SharedFolder(String folderPath, String username, String access) throws Exception {
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        
        Channel channel = session.openChannel("exec");
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();
        
        // Xây dựng lệnh PowerShell để tạo thư mục chia sẻ
        String shareCommand = "New-SmbShare -Name \"SharedFolder\" -Path \"" + folderPath + "\" -FullAccess \"" + username + "\"\r\n" +
                              "Grant-SmbShareAccess -Name \"SharedFolder\" -AccountName \"" + username + "\" -AccessRight " + access + " -Force\r\n" +
                              "Get-SmbShare | Where-Object { $_.Name -eq \"SharedFolder\" }\r\n";
        
        System.err.println(shareCommand); // In ra lệnh để kiểm tra
        out.write(shareCommand.getBytes());
        out.flush();
    
        String result = readChannelOutput(in, channel); // Đọc đầu ra từ kênh
        channel.disconnect();
        session.disconnect();
        return result;
    }
    
    static public String listSharedFolder(String userName) throws Exception {
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
    
        // Xây dựng lệnh PowerShell để liệt kê thư mục chia sẻ
        String shareCommand = "\"C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe\" -Command \"" +
                              "$shares = Get-SmbShare; " +
                              "foreach ($share in $shares) { " +
                              "$accessList = Get-SmbShareAccess -Name $share.Name | Where-Object { $_.AccountName -eq \'" + userName + "\' -and $_.AccessControlType -eq 'Allow' }; " +
                              "if ($accessList) { " +
                              "Write-Output \\\"User \'" + userName + "\' has access to shared folder: $($share.Name) with path: $($share.Path)\\\"; " +
                              "$items = Get-ChildItem -Path $share.Path -Recurse; " +
                              "foreach ($item in $items) { " +
                              "Write-Output \\\"Accessible item for " + userName + ": $($item.FullName)\\\" " +
                              "} " +
                              "} " +
                              "}\"";
    
        System.out.println(shareCommand); // In ra lệnh để kiểm tra
        channel.setCommand(shareCommand);
    
        InputStream in = channel.getInputStream();
        channel.connect();
    
        String result = readChannelOutput(in, channel); // Đọc đầu ra từ kênh
        channel.disconnect();
        session.disconnect();
        return result;
    }
    
    

    public static String listDomainUsers() throws Exception {
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        // Thêm "powershell -Command" để chạy lệnh PowerShell từ xa
        String command = "powershell -Command \"Get-ADUser -Filter * | Select-Object SamAccountName, Name\"";
        channel.setCommand(command);
        channel.setErrStream(System.err);  // Để nhận lỗi từ luồng lỗi chuẩn

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] buffer = new byte[1024];
        StringBuilder output = new StringBuilder();
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            output.append(new String(buffer, 0, bytesRead));
        }

        channel.disconnect();
        session.disconnect();
        return output.toString();
    }
}