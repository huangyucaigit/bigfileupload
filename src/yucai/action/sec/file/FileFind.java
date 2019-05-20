package yucai.action.sec.file;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangyucai on 2017/3/27.
 */
public class FileFind implements FileMapping {

    @Override
    public Map<String,Map<String,UploadData>> find(String doneFileDirStr) {
        //aa
        Map<String,Map<String,UploadData>> map = new ConcurrentHashMap<>();
        File rootFile = new File(doneFileDirStr);
        File[] files =  rootFile.listFiles();
        for (File file: files) {
            String name = file.getName();
            int end = name.indexOf("_");
            if(!file.isDirectory() && end != -1){

                String md5 = name.substring(0,end);
                String filename = name.substring(end + 1);
                File item = new File(doneFileDirStr,md5);
                File[] itemfiles  = item.listFiles();
                Map<String,UploadData> itemData =  new ConcurrentHashMap<>();
                for (File itemfile: itemfiles) {
                    String itemName = itemfile.getName();
                    int s = itemName.lastIndexOf("_");
                    int e = itemName.lastIndexOf(".");
                    String itemMD5 = itemName.substring(s+1,e);
                    String chunkStr = itemName.substring(0,itemName.indexOf("_"));
                    Integer chunk = Integer.parseInt(chunkStr);
                    Integer chunks = itemfiles.length;
                    UploadData data = new UploadData(md5,filename,chunks,chunk,itemMD5);
                    itemData.put(itemMD5,data);
                }

                map.put(md5,itemData);
            }
        }
        return map;
    }

}
