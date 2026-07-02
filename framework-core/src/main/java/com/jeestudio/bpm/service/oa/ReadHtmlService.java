package com.jeestudio.bpm.service.oa;

import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.FileUtil;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: HTML读取解析服务
 */
@Service
public class ReadHtmlService {

    protected static final Logger logger = LoggerFactory.getLogger(ReadHtmlService.class);

    @Value("${fileRoot}")
    private String fileRoot;

    @Autowired
    SysFileService sysFileService;

    public String getTable(String groupId) throws Exception {
        String filePath = getFilePath(groupId);//"C:\\Users\\msi\\Desktop\\发文.htm";
        String charset = FileUtil.getCharset(filePath);
        return getTable(groupId, charset);
    }

    public String getTable(String groupId, String charset) throws Exception {
        String content = "";
        String filePath = getFilePath(groupId);//"C:\\Users\\msi\\Desktop\\发文.htm";
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        String fileType = StringUtil.isEmpty(file.getName())?"":file.getName().substring(file.getName().lastIndexOf("."));
        switch (fileType){
            case ".htm":
            case ".html":
                content = htmlToStr(fileInputStream, charset); break;
            case ".pdf": content = pdfToStr(fileInputStream, charset); break;
            case ".xls": content = xlsToStr(fileInputStream, charset); break;
            case ".xlsx": content = xlsxToStr(fileInputStream, charset); break;
            case ".doc": content = docToStr(fileInputStream, charset); break;
            case ".docx": content = docxToStr(fileInputStream, charset); break;
            default: break;
        }
        return content;
    }

    private String xlsxToStr(FileInputStream fileInputStream, String charset) {
        return "";
    }

    private String docxToStr(FileInputStream fileInputStream, String charset) {
        return "";
    }

    private String docToStr(FileInputStream fileInputStream, String charset) {
        return "";
    }

    private String xlsToStr(FileInputStream fileInputStream, String charset) {
        return "";
    }

    private String pdfToStr(FileInputStream fileInputStream, String charset) {
        return "";
    }

    private String getFilePath(String groupId) {
        String filePath;
        ResultJson resultJson = this.getFileListOk(groupId);
        logger.info("ReadHtmlService getFilePath resultJson.getCode() = " + resultJson.getCode());
        if (ResultJson.CODE_SUCCESS == resultJson.getCode()) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) resultJson.getData().get("fileListMap");
            logger.info("ReadHtmlService getFilePath resultJson.getData().get(\"fileListMap\") = " + map.size());
            List<SysFile> sysFileListInDb = (List<SysFile>) map.get("files");
            //logger.info("ReadHtmlService getFilePath resultJson.getData().get(\"fileListMap\").get(\"files\") = " + sysFileListInDb.size());
            //LinkedHashMap<String, Object> sysFile = sysFileListInDb.get(0);
            //logger.info("ReadHtmlService getFilePath sysFile != null =  " + (sysFile != null));
            //filePath = fileRoot + sysFile.get("path");
            filePath = fileRoot + sysFileListInDb.get(0).getPath();
        } else {
            filePath = null;
        }
        return filePath;
    }

    private ResultJson getFileListOk(String groupId) {
        ResultJson resultJson = new ResultJson();
        try {
            LinkedHashMap<String, Object> map = sysFileService.getFileList(groupId);
            resultJson.put("fileListMap", map);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("获取文件列表成功");
            resultJson.setMsg_en("Get file list success");
        } catch (Exception e) {
            logger.error("Error while getting file list:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("获取文件列表失败");
            resultJson.setMsg_en("Get file list failed");
        }
        return resultJson;
    }

    private String htmlToStr(FileInputStream fileInputStream, String charset) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charset);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            boolean b0 = StringUtil.isNotBlank(line);
            if (b0) {
                boolean b1 = line.trim().contains("<o:p>&nbsp;</o:p>");
                boolean b3 = line.trim().contains("<o:p></o:p>");
                boolean b2 = line.trim().contains("&nbsp;");
                if (b1) {
                    line = line.replace("<o:p>&nbsp;</o:p>", "");
                }
                if (b2) {
                    line = line.replace("&nbsp;", "");
                }
                if(b3){
                    line = line.replace("<o:p></o:p>", "");
                }
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
        }
        String html = stringBuffer.toString();
        if(StringUtils.isNotBlank(html)) {
            try {
                Document doc = Jsoup.parseBodyFragment(html);
                Elements td = doc.getElementsByTag("td");
                td.addClass("td-name");
                Element div = doc.selectFirst("div");
                html = div.html();
            } catch (Exception e) {
                logger.warn("解析HTML内容失败", e);
            }
        }
        return html;
    }
}
