package DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class File_Folder {
    String name;
    LocalDateTime lastWriteTime;

    public File_Folder(String name, LocalDateTime lastWriteTime) {
        this.name = name;
        this.lastWriteTime = lastWriteTime;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getLastWriteTime() {
        return lastWriteTime;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
        return name +"|"+ lastWriteTime.format(formatter);
    }
}
