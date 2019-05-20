package yucai.action.sec.file;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangyucai on 2017/3/21.
 */
@WebServlet(name = "UploadAction")
public class UploadAction extends HttpServlet {

   public  static Map<String,Map<String,UploadData>> fileList;
    public static Map<String,Integer> status;
    public static Map<String,Set<FileUserMapping>> uf;
    public  static String doneFileDirStr ="";
    public static String userfilename = "userfile.json";
    @Override
    public void init() throws ServletException {
        super.init();
        String webroot = getServletContext().getRealPath("");
        doneFileDirStr = webroot + File.separator+"upload";
        if(fileList == null){
            File tempDir = new File(doneFileDirStr);
            if (!tempDir.exists()) tempDir.mkdirs();
            FileMapping file = new FileFind();
            fileList = file.find(doneFileDirStr);
        }
        if(status == null){
            status =  new ConcurrentHashMap<String,Integer>();
        }
        if(uf == null){
            try {
                File file = new File(doneFileDirStr + File.separator + userfilename);
                if(file.exists()){
                    String json = UploadUtils.readJson(file);
                    Map<String,Set<FileUserMapping>> fum =  new GsonBuilder().setPrettyPrinting().create().fromJson(json,new TypeToken<Map<String,Set<FileUserMapping>>>(){}.getType());
                    uf = fum;
                }else{
                    uf = new LinkedHashMap<>();
                }
            } catch (IOException e) {
                System.out.println("userfile.json 加载失败！");
                e.printStackTrace();
            }
        }
    }


    public String toJson(Object data){
        return new GsonBuilder().setPrettyPrinting().create().toJson(data);
    }
    public static  Set<FileUserMapping> myfiles(HttpServletRequest request){
        Map<String,Set<FileUserMapping>> ufs =  UploadAction.uf;
        if(ufs == null){
            return null;
        }
        Set<FileUserMapping> ls = ufs.get(UploadUtils.getUsername(request));
        return ls;
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  URI = request.getRequestURI();
        System.out.println("--------------------- request.getRequestURI():" + URI);
        if(URI.endsWith("/file/check")){
            String md5 = request.getParameter("md5");
            Integer chunks = Integer.parseInt(request.getParameter("chunks"));
            String name = request.getParameter("name");
            Long size = Long.parseLong(request.getParameter("size"));
            if(md5 == null || md5.length() == 0 || chunks == null || name == null || size == null){
                response.getWriter().print(toJson(new Result("paramsError")));
                return;
            }
            boolean isExist = isExistFile(md5,chunks);
            if(isExist){
                response.getWriter().print(toJson(new Result("exist")));
                String username = UploadUtils.getUsername(request);
                Set<FileUserMapping>  fums = uf.get(username);
                initFileUserMappingSet(fums,username,new FileUserMapping(username,name,md5,size));
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(uf);
                UploadUtils.writeJson(json,new File(doneFileDirStr + File.separator + userfilename));
                nTo1(md5,doneFileDirStr,name,chunks);
            }else{
                status.put(md5,-1);
                File file = new File(doneFileDirStr + File.separator + md5);
                if(file.exists() && file.isDirectory()){
                    List<String> parts = UploadUtils.find(file,".*.part");
                    List<String> partsmd5 = new ArrayList<>();
                    for (String partMD5 :parts) {
                        partsmd5.add(partMD5.substring(partMD5.indexOf("_") + 1,partMD5.length() - 5));
                    }
                    if(parts!=null && !parts.isEmpty()){
                        response.getWriter().print(toJson(new Result("continued","parts",partsmd5)));
                        return;
                    }
                }
                response.getWriter().print(toJson(new Result("upload")));
            }
        }else if(URI.endsWith("/file/upload")){
            try {
                boolean isMultipart = ServletFileUpload.isMultipartContent(request);
                if (isMultipart) {
                    //上传文件
                    final UploadData data = fileUpload(request);
                    Map<String,Map<String,UploadData>> map = addUploadData(data);
                    StringBuffer saveDataInfo = new StringBuffer();
                    for (Map<String,UploadData> m : map.values()) {
                        saveDataInfo.append(m);
                        saveDataInfo.append("\n");
                    }
                   // System.out.println("已经保存文件列表：" + saveDataInfo);
                    //是否全部上传完成
                    boolean isDoneUpload = isDoneUpload(data.getId(),data.getChunks());
                    System.out.println("文件是否全部上传完成：" + isDoneUpload);
                    if(isDoneUpload){
                        final String username = UploadUtils.getUsername(request);
                        // 通知合并文件
                        new Thread(){
                            public void run(){
                                System.out.println("准备合并文件！");
                                try {
                                    if(status.get(data.getId()).equals(-1)){
                                        status.put(data.getId(),0);
                                        Set<FileUserMapping>  fums = uf.get(username);
                                        initFileUserMappingSet(fums, username, new FileUserMapping(username,data.getFileName(),data.getId(),data.getSize()));
                                        String json = toJson(uf);
                                        UploadUtils.writeJson(json,new File(doneFileDirStr + File.separator + userfilename));
                                        nTo1(data.getId(),doneFileDirStr,data.getFileName(),data.getChunks());
                                        status.put(data.getId(),1);
                                        System.out.println("合并成功！");
                                    }else{
                                        System.out.println("文件正在合并！");
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    System.err.println("合并失败！");
                                }
                            }
                        }.start();
                    }
                    response.getWriter().print(toJson(data));
                }
            } catch (Exception e) {
                System.err.println("上传失败！");
                e.printStackTrace();
            }
        }else if(URI.endsWith("/file/remove")){
            String MD5 =  request.getParameter("MD5");
            String filename =  request.getParameter("name");
            String username = UploadUtils.getUsername(request);
            if(MD5!=null && filename!=null){
                FileUserMapping removeObj = new FileUserMapping(username,filename,MD5);
                Set<FileUserMapping> set =  uf.get(username);
               for(Iterator<FileUserMapping> it = set.iterator();it.hasNext();){
                    FileUserMapping myFile = it.next();
                    if(myFile.equals(removeObj)){
                        it.remove();
                    }
               }
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(uf);
                UploadUtils.writeJson(json,new File(doneFileDirStr + File.separator + userfilename));
                response.getWriter().print(1);
            }else{
                response.getWriter().print(0);
            }
        }else{
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private synchronized void  initFileUserMappingSet(Set<FileUserMapping> fums,String username,FileUserMapping fileUserMapping) {
        if(fums == null){
            fums = new TreeSet<>();
            uf.put(username,fums);
        }
        if(!fums.contains(fileUserMapping)){
            fums.add(fileUserMapping);
        }
    }

    protected synchronized boolean isDoneChunkUpload(String id,String chunkMd5){
        Map<String,UploadData> m = fileList.get(id);
        if(m != null && m.containsKey(chunkMd5)){
            return true;
        }
        return false;
    }

    /**
     * 如果同一文件所有分片都保存成功那么则返回true
     * 根据唯一ID 判断是否同一文件
     * @param id
     * @param chunks
     * @return
     */
    protected synchronized boolean isDoneUpload(String id,Integer chunks){
        Map<String,UploadData> m = fileList.get(id);
        if(m != null && chunks.equals(m.size())){
            return true;
        }
        return false;
    }



    protected  synchronized void  cleanUploadData(UploadData uploadData){
        if(fileList.containsKey(uploadData.getId())){
            fileList.remove(uploadData.getId());
            System.out.println(uploadData.getId() + "缓存已清除！" );
        }
    }
    protected  synchronized Map<String,UploadData>  getDataById(String id){
        return fileList.get(id);
    }

    /**
     * 分片保存成功后在SESSION中做相关记录
     * @param uploadData
     * @return
     */
    protected  synchronized Map<String,Map<String,UploadData>>  addUploadData(UploadData uploadData){
        String key = uploadData.getId();
        Map<String,UploadData> fileMap = fileList.get(key);
        if(fileMap == null){
            Map<String,UploadData> uploadList = new ConcurrentHashMap();
            uploadList.put(uploadData.getChunkMD5(),uploadData);
            fileList.put(key,uploadList);
        }else{
            if(!fileMap.containsKey(uploadData.getChunkMD5())){
                fileMap.put(uploadData.getChunkMD5(),uploadData);
            }
        }
        return fileList;
    }

    protected  synchronized boolean isExistFile(String id,Integer chunks){
        if(fileList.containsKey(id) ){
            Integer count = fileList.get(id).size();
            if(chunks.equals(count)){
                System.out.println("文件状态"+ count +"/"+chunks +" " +  id );
                return true;
            }else{
                System.out.println("文件状态"+ count +"/"+chunks +" " +  id );
            }
        }
        return false;
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * 获取分片文件并保存
     * @param request
     * @return
     * @throws Exception
     */
    public  UploadData  fileUpload(HttpServletRequest request) throws Exception {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setHeaderEncoding("utf-8");
                // 得到所有 Content-Type:multipart/form-data 的表单域
                List<FileItem> fileItems = upload.parseRequest(request);
                String id  = null,fileName = null,chunkMD5 = null;
                Integer chunk  = null,chunks  = null;
                Long size = null;
                FileItem tempFileItem = null;
                for (FileItem fileItem : fileItems) {
                    String name = fileItem.getFieldName();
                    if (UploadParams.id.equals(name)) {
                        id = fileItem.getString();
                    } else if (UploadParams.fileName.equals(name)) {
                        fileName = fileItem.getString("utf-8");
                    } else if (UploadParams.chunks.equals(name)) {
                        chunks = Integer.valueOf(fileItem.getString());
                    } else if (UploadParams.chunk.equals(name)) {
                        chunk = Integer.valueOf(fileItem.getString());
                    } else if (UploadParams.fileData.equals(name)) {
                        tempFileItem = fileItem;
                    } else if (UploadParams.chunkMD5.equals(name)) {
                        chunkMD5 = fileItem.getString();
                    }else if (UploadParams.size.equals(name)) {
                        size = Long.parseLong(fileItem.getString());;
                    }
                }
                //参数检查
                if(id == null) throw  new  IllegalArgumentException(UploadParams.id);
                if(fileName == null) throw  new  IllegalArgumentException(UploadParams.fileName);
                if(chunk == null) throw  new  IllegalArgumentException(UploadParams.chunk);
                if(chunks == null)  throw  new  IllegalArgumentException(UploadParams.chunks);
                if(tempFileItem == null) throw  new  IllegalArgumentException(UploadParams.fileData);
                if(chunkMD5 == null) throw  new  IllegalArgumentException(UploadParams.chunkMD5);
                if(size == null) throw  new  IllegalArgumentException(UploadParams.size);
                UploadData data = new UploadData(id,fileName,chunks,chunk,chunkMD5);
                data.setSize(size);
                 System.out.println("文件分片信息 " + data);
                //当前上传文件的临时文件夹
                String currentTempFileDir = doneFileDirStr + File.separator + id;
                //当前上次文件完成后的文件夹
                File tempDir = new File(currentTempFileDir);
                if (!tempDir.exists()) tempDir.mkdirs();
                // 分片处理时，前台会多次调用上传接口，每次都会上传文件的一部分到后台
                File tempPartFile = new File(tempDir, data.getTempFileName());
                if(tempPartFile.exists()){
                    System.out.println("文件分片信息 已经存在！" + data);
                }else{
                    FileUtils.copyInputStreamToFile(tempFileItem.getInputStream(), tempPartFile);
                    System.out.println("文件分片信息 保存成功！" + data);
                }
                return data;
    }

    /**
     * 合并文件，
     * 默认根据 data.getDefaultCurrentTempFileDir() 约定的目录获取临时文件分片
     * 合成文件默认存放到 data.getDefaultDoneTempFileDir()
     * @throws IOException
     */
    private void nTo1(String id,String doneFiledir,String filename,Integer chunks) throws IOException {
        Map<String,UploadData> filedata = getDataById(id);
            String comnmonfilename = id +"_"+chunks;
            int index = filename.lastIndexOf('.');
            if(index != -1){
                comnmonfilename += filename.substring(index);
            }
            File doneFile = new File(doneFiledir, comnmonfilename);
            if(doneFile.exists()){
                System.out.println("文件已经存在！" + doneFile.getPath());
            }else{
                System.out.println("文件不存在正在合并...");
                for (UploadData data: new TreeSet<UploadData>(filedata.values())) {
                    File partFile = new File(doneFileDirStr + File.separator + id, data.getTempFileName());
                    if(partFile.exists()){
                        System.out.println(partFile.getPath());
                        FileOutputStream destTempfos = new FileOutputStream(doneFile, true);
                        FileUtils.copyFile(partFile, destTempfos);
                        destTempfos.close();
                    }else{
                        System.out.println(partFile.getPath()+ "不存在!");
                        doneFile.delete();
                    }
                }
            }
    }

}
