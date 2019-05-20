package yucai.action.sec.file;

import java.io.File;

/**
 * Created by huangyucai on 2017/3/21.
 */
public class UploadParams {
    /**
     * 文件唯一标识
     */
    public static  String id = "id";
    /**
     * 文件名称
     */
    public static  String fileName = "name";
    /**
     * 总分片数量
     */
    public static  String chunks = "chunks";
    /**
     * 当前分片，从1开始
     */
    public static  String chunk = "chunk";
    /**
     * 分片的数据
     */
    public static  String fileData = "file";
    /**
     * 分片的数据
     */
    public static  String chunkMD5 = "chunkId";

    public static  String size = "size";


}
