package com.jeestudio.bpm.service.oa;

import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * @Description: OA内容服务
 */
@Service
public class ContentService {

    protected static final Logger logger = LoggerFactory.getLogger(ContentService.class);

    @Value("${fileRoot}")
    private String fileRoot;

    @Value("${fileRoot}/temp")
    private String tempFileRoot;

    @Value("${fileUploadFolder}")
    private String fileUploadFolder;

    @Value("${uploadPathDateFormat}")
    private String uploadPathDateFormat;

    @Autowired
    SysFileService sysFileService;

    @Autowired
    ZformService zformService;

    public String getFileRoot() {
        return fileRoot;
    }

    public void openContent(String groupId, ResultJson resultJson) throws Exception {
        String filePath = null;
        String fileName = null;
        if (StringUtil.isNotBlank(groupId)) {
            ResultJson fileResult = this.getFileListOk(groupId);
            Object fileListMap = fileResult.getData().get("fileListMap");
            if (fileListMap != null) {
                Object files = ((LinkedHashMap<String, Object>) fileListMap).get("files");
                if (files != null) {
                    List<LinkedHashMap> sysFileList = (List<LinkedHashMap>) files;
                    if (sysFileList.size() > 0) {
                        LinkedHashMap<String, Object> content = sysFileList.get(0);
                        fileName = (String) content.get("name");
                        filePath = ((String) content.get("path")).replace(fileName, "");
                    }
                }
            }
        }
        //加密文件区
        if (StringUtil.isBlank(filePath)) {
            filePath = this.getFilePath();
        }
        //加密文件名
        if (StringUtil.isBlank(fileName)) {
            fileName = "content.doc";
        }
        resultJson.put("filePath", filePath);//recordId
        resultJson.put("fileName", fileName);
        resultJson.put("fileType", fileName.substring(fileName.indexOf(".")));

        //临时文件区
        if (false == new File(tempFileRoot + filePath).exists()) {
            new File(tempFileRoot + filePath).mkdirs();
        }
        resultJson.put("tempPath", tempFileRoot + filePath);

        //临时文件
        String tempFile = tempFileRoot + filePath + fileName;
        if (false == new File(tempFile).exists()) {
            String encryptFile = fileRoot + filePath + fileName;
            if (new File(encryptFile).exists()) {
                this.copyFile(encryptFile, tempFile, true);// 配置说明：测试时可临时关闭加密，正式环境保持加密
            } else {
                ApplicationHome applicationHome = new ApplicationHome(getClass());
                String target = applicationHome.getSource().getParentFile().toString();
                String blankFile = target + "/classes/tpl/blank.doc";
                this.copyFile(blankFile, tempFile, false);
            }
        }
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

    public void deleteContentFile(String filePath, String fileName) {
        //临时文件
        String content = fileRoot + filePath + fileName;
        File file = new File(content);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteTempFile(String filePath, String fileName) {
        //临时文件
        String tempFile = tempFileRoot + filePath + fileName;
        File file = new File(tempFile);
        if (file.exists()) {
            file.delete();
        }
    }

    public void saveContent(String groupId, String filePath, String fileName, String currentUserName) throws Exception {
        String fileName_ = "";
        String encryptFile = fileRoot + filePath + fileName;
        if (new File(encryptFile).exists()) {
            fileName_ = fileName + "_" + getUuid();
            String encryptFile_ = fileRoot + filePath + fileName_;
            this.copyFile(encryptFile, encryptFile_,false);
        } else {
            new File(fileRoot + filePath).mkdirs();
            Zform sysFile_ = new Zform();
            sysFile_.setFormNo("sys_file_");
            sysFile_.setS15(groupId);
            sysFile_.setS07(filePath + fileName);
            sysFile_.setS06(fileName);
            zformService.saveZform(sysFile_, currentUserName, "/dynamic/zform");
        }
        String tempFile = tempFileRoot + filePath + fileName;
        this.copyFile(tempFile, encryptFile, true);// 配置说明：测试时可临时关闭加密，正式环境保持加密
        this.deleteTempFile(filePath, fileName);
        if (StringUtil.isNotBlank(fileName_)) {
            this.deleteContentFile(filePath, fileName_);
        }
    }

    public void loadContent(String filePath, String fileName) throws Exception {
        String encryptFile = fileRoot + filePath + fileName;
        String tempFile = tempFileRoot + filePath + fileName;
        this.copyFile(encryptFile, tempFile, true);// 配置说明：测试时可临时关闭加密，正式环境保持加密
    }

    private String getFilePath() {
        return new StringBuffer()
                .append(fileUploadFolder)
                .append("/oa/content")
                .append(new SimpleDateFormat(uploadPathDateFormat).format(new Date()))
                .append(UUID.randomUUID().toString().replaceAll("-", ""))
                .append("/")
                .toString();
    }

    private String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private void copyFile(String in, String out, boolean encrypt) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(in);
        FileOutputStream fileOutputStream = new FileOutputStream(out);
        if (encrypt) {
            byte[] bytes = new byte[fileInputStream.available()];
            int read = fileInputStream.read(bytes);
            for (int i = 0; i < read; i++) {
                bytes[i] = (byte) (bytes[i] ^ 0x99);
            }
            fileOutputStream.write(bytes);
        } else {
            IOUtils.copy(fileInputStream, fileOutputStream);
        }
        if (fileOutputStream != null) {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        if (fileInputStream != null) {
            fileInputStream.close();
        }
    }

    public void downloadContent(String groupId, HttpServletResponse response) throws Exception {
        ResultJson resultJson = this.getFileListOk(groupId);
        if (ResultJson.CODE_SUCCESS == resultJson.getCode()) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) resultJson.getData().get("fileListMap");
            if (map.get("files") != null) {
                List<SysFile> sysFileListInDb = (List<SysFile>) map.get("files");
                if (sysFileListInDb.size() > 0) {
                    SysFile sysFile = sysFileListInDb.get(0);
                    String filePath = sysFile.getPath();
                    String file = fileRoot + filePath;
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] bytes = new byte[fileInputStream.available()];
                    int read = fileInputStream.read(bytes);
                    //加密正文解密下载
                    for (int i = 0; i < read; i++) {
                        bytes[i] = (byte) (bytes[i] ^ 0x99);
                    }
                    response.getOutputStream().write(bytes);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } else {
                    logger.error("下载正文失败： sysFileListInDb.size() == 0");
                }
            } else {
                logger.error("下载正文失败： map.get(\"files\") == null");
            }
        } else {
            logger.error("下载正文失败： ResultJson.CODE_SUCCESS != datasourceFeign.getFileList(groupId).getCode()");
        }
    }
}
