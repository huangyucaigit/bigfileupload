package yucai.action.sec.file;

import java.io.File;

/**
 * Created by huangyucai on 2017/3/22.
 */
public class UploadData implements Comparable<UploadData> {
    String id;
    String fileName;
    Integer chunks;
    Integer chunk;
    String chunkMD5;
    Long size;
    Integer status;

    public UploadData(String id, String fileName, Integer chunks, Integer chunk,String chunkMD5) {
        this.id = id;
        this.fileName = fileName;
        this.chunks = chunks;
        this.chunk = chunk;
        this.chunkMD5 = chunkMD5;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTempFileName() {
        return chunk + "_"+chunkMD5+".part";
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getChunks() {
        return chunks;
    }

    public void setChunks(Integer chunks) {
        this.chunks = chunks;
    }

    public Integer getChunk() {
        return chunk;
    }

    public void setChunk(Integer chunk) {
        this.chunk = chunk;
    }

    public String getChunkMD5() {
        return chunkMD5;
    }

    public void setChunkMD5(String chunkMD5) {
        this.chunkMD5 = chunkMD5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UploadData that = (UploadData) o;

        if (!id.equals(that.id)) return false;
        if (!fileName.equals(that.fileName)) return false;
        if (!chunks.equals(that.chunks)) return false;
        return chunk.equals(that.chunk);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + chunks.hashCode();
        result = 31 * result + chunk.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UploadData{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", chunks=" + chunks +
                ", chunk=" + chunk +
                '}';
    }
    @Override
    public int compareTo(UploadData uploadData) {
        if(this.chunk > uploadData.getChunk()){
            return  1;
        }else if(this.chunk < uploadData.getChunk()){
            return -1;
        }else {
            return 0;
        }
    }

}
