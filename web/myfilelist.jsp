<%@ page import="yucai.action.sec.file.UploadParams" %>
<%@ page import="yucai.action.sec.file.UploadAction" %>
<%@ page import="com.google.gson.GsonBuilder" %>
<%@ page import="yucai.action.sec.file.UploadUtils" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="yucai.action.sec.file.FileUserMapping" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Set" %><%-- Created by IntelliJ IDEA. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <title>HTML5大文件分片上传示例</title>
    <script src="js/jquery.js"></script>
    <script>
        function remove(md5, filename) {
            $.ajax({
                url: "file/remove",
                type: "POST",
                data: {"MD5": md5, "name": filename},
                success: function (msg) {
                    if (msg == "1") {
                        window.location.reload();
                    } else {
                        alert("remove error！");
                    }
                }
            });
        }
    </script>
</head>
<body>
<h3/>
我的文件列表<a href="javascript: window.location.reload();">刷新</a>
<br/>
<br/>
<a href="upload/userfile.json" target="_blank">查看userfile.json</a>
<%
    if (UploadAction.doneFileDirStr != null) {
        Map<String, Map<String, Object>> ds = UploadUtils.bfsfind(new File(UploadAction.doneFileDirStr));
        Set<FileUserMapping> ls = UploadAction.myfiles(request);
        if (ls != null) {

            for (FileUserMapping u : ls) {
                Map<String, Object> d = ds.get(u.getMD5());
                if (d != null) {
%>
<h4/>
<%= u.getFilename() %>
<%
if(u.getSize().equals(d.get("size"))){
%><a href="upload<%= d.get("path") %>">下载</a><%
}else{
%>文件正处理中..<%

}
%>
<a id="remove" href="javascript:remove('<%= u.getMD5() %>','<%= u.getFilename() %>')">删除</a>
<span style="color: dimgray"><%= new SimpleDateFormat("MM-dd hh:mm:ss").format(u.getDate()) %></span>
<span style="color: dimgray"><%= d.get("size")  %>/<%= u.getSize()  %></span>
<%


                } else {

%>
<h4/>
<%= u.getFilename() %> <span style="color: red">文件异常</span> <a id="remove" href="javascript:remove('<%= u.getMD5() %>','<%= u.getFilename() %>')">删除</a>
<span style="color: dimgray"><%= new SimpleDateFormat("MM-dd hh:mm:ss").format(u.getDate()) %></span>
<%

                }

            }
        }
    }
%>
</body>
</html>


