package com.jeestudio.bpm.modules.oa.util;

import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.service.system.SysFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;

/**
 * @Description: OnlyOffice文档转换工具
 */
@Component
public class OnlyOfficeUtil {

    @Autowired
    SysFileService sysFileService;

    @Value("${storage.minio.endpoint}")
    String endPoint;

    @Value("${storage.minio.bucket}")
    String bucketName;

    @Value("${storage.upload-path-format:/}")
    String minioPath;

    /**
     * 文件转换
     * @param fileId 系统文件ID
     * @param originType 原始类型，例如： .docx .pdf
     * @param toType 目标类型
     * @param sysAddress 系统IP:port，让onlyoffice能调用
     * @return 文件字节
     */
    public byte[] convertOfficeFile(String fileId, String originType, String toType, String sysAddress) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("url", sysAddress + "/jeeStudio/gtoa/a/system/sysFile/fileDownload?fileId=" + fileId + "&oss=minio%3Amain%3Auserfiles");
        requestMap.put("fileType", originType);
        requestMap.put("async", false);
        requestMap.put("outputtype", toType);
        requestMap.put("key", UUID.randomUUID().toString());
        String postUrl = endPoint + "/ConvertService.ashx";
        JSONObject result = JSONObject.parseObject(HttpRequest.post(postUrl).body(JSON.toJSONString(requestMap)).execute().body());
        String transformPdfUrl = result.getString("fileUrl");
        return HttpUtil.downloadBytes(transformPdfUrl);
    }

    public String getObjectName(String url) {
        return url.replaceFirst("/minio/" + bucketName + minioPath + "/", "");
    }

    public String getKey(String url) {
        return url.replaceFirst("/minio/" + bucketName + "/", "");
    }
}
