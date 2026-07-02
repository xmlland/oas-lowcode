package com.jeestudio.bpm.feign;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.feign.factory.WordExportApiFallbackFactory;
import com.jeestudio.bpm.service.system.SysFileService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Description: Word导出Feign客户端
 */
@Component
@FeignClient(url = "${export-word-url}", name = "service-WordExport", fallback = WordExportApiFallbackFactory.class)
public interface WordExportFeignClient {

    @PostMapping(value = "word/export")
    byte[] export(@RequestParam String fileName, @RequestBody RequestVo requestVo);

    @PostMapping(value = "word/mergeMultiWord")
    byte[] mergeMultiWord(@RequestBody  List<String> fileNames);

    @PostMapping(value = "word/checkTemplate")
    JSONObject checkTemplate(@RequestBody List<String> fileNameList);

    default byte[] exportByFileId(String fileId, RequestVo requestVo) {
        SysFileService fileService = SpringUtil.getBean(SysFileService.class);
        SysFile sysFile = fileService.get(fileId);
        return export(sysFile.getId() + "." + sysFile.getExt(), requestVo);
    }

    default byte[] exportBySysFile(SysFile sysFile, RequestVo requestVo) {
        return export(sysFile.getId() + "." + sysFile.getExt(), requestVo);
    }

    default byte[] exportByMergeMultiWordFSysFile( List<String> fileNames) {
        return mergeMultiWord(fileNames);
    }


}
