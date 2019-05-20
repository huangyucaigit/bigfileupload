package yucai.action.sec.file;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huangyucai on 2017/3/28.
 */
public class FileUserMapping implements Comparable<FileUserMapping>{
    String username;
    String filename;
    String MD5;
    Long size;
    Date date;
    String  fullname;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMD5() {
        return MD5;
    }

    public FileUserMapping(String username, String filename, String MD5) {
        this.username = username;
        this.filename = filename;
        this.MD5 = MD5;
        this.date = new Date();
    }

    public FileUserMapping(String username, String filename, String MD5, Long size) {
        this.username = username;
        this.filename = filename;
        this.MD5 = MD5;
        this.date = new Date();
        this.size = size;
    }
    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    @Override
    public int compareTo(FileUserMapping obj) {
        if(this.getDate().getTime() > obj.getDate().getTime()){
            return  -1;
        }else if(this.getDate().getTime() < obj.getDate().getTime()){
            return 1;
        }else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileUserMapping that = (FileUserMapping) o;

        if (!username.equals(that.username)) return false;
        if (!filename.equals(that.filename)) return false;
        return MD5.equals(that.MD5);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + filename.hashCode();
        result = 31 * result + MD5.hashCode();
        return result;
    }

    public static void main(String[] args) {
        Set<FileUserMapping> ss = new HashSet<>();
//        ss.add(new FileUserMapping("1","1","2",1l));
//        ss.add(new FileUserMapping("1","1","2",2l));
        System.out.println(ss.size());
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
