package yucai.action.sec.file;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangyucai on 2017/3/31.
 */
public class Result {
    String status = "1";
    String next;
    Map<String,Object> data;

    public Result(String next) {
        this.next = next;
    }
    public Result(String next,String key,Object value) {
        this.next = next;
        this.data = new HashMap<>();
        this.data.put(key,value);
    }

    public Result(String next, Map<String, Object> data) {
        this.next = next;
        this.data = data;
    }
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
