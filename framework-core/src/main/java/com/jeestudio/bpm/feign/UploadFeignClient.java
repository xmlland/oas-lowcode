package com.jeestudio.bpm.feign;

import cn.hutool.extra.spring.SpringUtil;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.service.oa.ContentService;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @Description: 文件上传Feign客户端
 */
public interface UploadFeignClient {


    default MultipartFile createMultipartFile(SysFile sysFile) {
        ContentService contentService = SpringUtil.getBean(ContentService.class);
        File file = new File(contentService.getFileRoot() + sysFile.getPath());
        try {
            return createMultipartFile(new FileInputStream(file), sysFile.getId() + "." + sysFile.getExt());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    default MultipartFile createMultipartFile(InputStream inputStream, String fileName) {
        try {
            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            return new ByteArrayMultipartFile(fileName, fileName, "multipart/form-data", content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
