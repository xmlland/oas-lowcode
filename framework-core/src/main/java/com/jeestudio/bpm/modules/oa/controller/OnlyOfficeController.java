package com.jeestudio.bpm.modules.oa.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.modules.oa.util.OnlyOfficeUtil;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.storage.IFileStorageAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @Description: OnlyOffice在线编辑回调
 */
@Tag(name = "在线文档")
@RestController
@RequestMapping("${adminPath}/onlyoffice")
public class OnlyOfficeController {

    private static final Logger logger = LoggerFactory.getLogger(OnlyOfficeController.class);

    @Autowired
    SysFileService sysFileService;

    @Autowired
    OnlyOfficeUtil onlyOfficeUtil;

    @Autowired
    private IFileStorageAdapter fileStorageAdapter;

    @Operation(summary = "文档保存回调")
    @PostMapping("/save")
    @ResponseBody
    public void save(HttpServletRequest request, HttpServletResponse response) {
        try (PrintWriter writer = response.getWriter();
             Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A")) {
            String body = scanner.hasNext() ? scanner.next() : "";
            JSONObject jsonObj = JSONObject.parseObject(body);
            if ((int) jsonObj.get("status") == 2) {
                String downloadUri = (String) jsonObj.get("url");
                String fileId = jsonObj.getString("key");
                if (fileId.indexOf("_") != -1) {
                    fileId = fileId.substring(0, fileId.indexOf("_"));
                }
                SysFile sysFile = sysFileService.get(fileId);
                String url = sysFile.getUrl();

                byte[] downloadBytes = HttpUtil.downloadBytes(downloadUri);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(downloadBytes);
                String fileName = onlyOfficeUtil.getObjectName(url);
                fileStorageAdapter.upload("main", "userfiles", fileName, inputStream, null);
            }
            writer.write("{\"error\":0}");

        } catch (Exception e) {
            logger.error("OnlyOffice保存文档失败", e);
        }
    }
}
