package yucai.action.sec.file;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by huangyucai on 2017/3/28.
 */
public class UploadUtils {

    public static synchronized void  writeJson(String json,File file) throws IOException {
        org.apache.commons.io.FileUtils.write(file,json,"utf-8");
    }
    public static synchronized String  readJson(File file) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(file,"utf-8");
    }
    public static List<String> find(File file,String regFilter){
        List<String> filelist = new ArrayList<>();
        if(file.isDirectory()){
            File[] fs = file.listFiles();
            for (int i = 0; i < fs.length; i++) {
                if(fs[i].isFile() ){
                    if(regFilter!=null && fs[i].getName().matches(regFilter)){
                        filelist.add(fs[i].getName());
                        System.out.println(fs[i].getName());
                    }
                }
            }
        }
        return filelist;
    }

    public static Map<String,Map<String,Object>>  bfsfind(File file){
        Map<String,Map<String,Object>> ds = new LinkedHashMap<>();
        Queue<File> qs = new LinkedList<>();
        qs.add(file);
        while(!qs.isEmpty()){
            File f = qs.poll();
            if(f.isDirectory()){
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    qs.add(fs[i]);
                }
            }else{
               // f.fi
                String name = f.getName();
                if(!name.endsWith(".part") && name.indexOf("_")!= -1){
                    Map<String,Object> data = new LinkedHashMap<>();
                    String md5 = name.substring(0,name.indexOf("_"));
                    data.put("path",f.getPath().replace(file.getPath(),""));
                    data.put("size",f.length());
                    data.put("date",new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date(f.lastModified())));
                    ds.put(md5,data);
                }
            }
        }
        return ds;
    }

    public static String getUsername(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
