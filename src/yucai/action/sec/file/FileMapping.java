package yucai.action.sec.file;

import java.util.Map;
import java.util.Set;

/**
 * Created by huangyucai on 2017/3/27.
 */
public interface FileMapping {
    Map<String,Map<String,UploadData>> find(String fileDir);
}
