
import BLL.Folder_handle;
import DAL.ConnectWindowServer;
import View.Login;

public class start {
    static public void main(String[] arv) {
        /* Create and display the form */
        // java.awt.EventQueue.invokeLater(new Runnable() {
        //     public void run() {
        //         Login login = new Login();
        //         login.setVisible(true);
        //     }
        // });
       //Folder_handle.listSharedFiles("192.168.1.10");
        String username = "PBL4\\Thanhan"; // Thay bằng tên người dùng
        String folderPath = "C:\\Users\\XPhuc\\Documents\\New folder"; // Thay bằng đường dẫn thư mục cần chia sẻ
        String access = "F"; // Quyền truy cập (F: Full Control, R: Read, etc.)

        Folder_handle.shareFolder(username, folderPath, access);
        // Folder_handle.listSharedFolders();
        // try {
        //     System.out.println(ConnectWindowServer.listDomainUsers());
        // } catch (Exception e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
    }
}
