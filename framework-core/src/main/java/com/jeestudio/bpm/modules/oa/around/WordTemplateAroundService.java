package com.jeestudio.bpm.modules.oa.around;

import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.feign.WordUploadFeignClient;
import com.jeestudio.bpm.modules.oa.entity.OaWordTemplateEntity;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: Word模板扩展服务
 */
@Component("oa_word_templateAroundService")
public class WordTemplateAroundService implements AroundServiceI {
    @Autowired
    ZformService zformService;
    @Autowired
    SysFileService sysFileService;
    @Autowired
    WordUploadFeignClient wordUploadFeignClient;

    @Override
    public void beforeSaveZform(Zform zform, String loginName, String businessKey) {
        if (StringUtil.isNotEmpty(zform.getS03()) && StringUtil.isEmpty(zform.getId())) {
            zform.setPreId(zform.getS03());
        }
    }

    @Override
    public ResultJson afterSaveZform(ResultJson resultJson, Zform zform, String loginName, String businessKey) {
        OaWordTemplateEntity entity = zformService.getEntityByZform(zform, OaWordTemplateEntity.class);
        if (StringUtil.isNotEmpty(entity.getTemplateFile())){
            List<SysFile> sysFiles = sysFileService.getFileListByGroupId(entity.getTemplateFile());
            if (sysFiles.size()>0){
                SysFile sysFile = sysFiles.get(0);
                wordUploadFeignClient.upload(wordUploadFeignClient.createMultipartFile(sysFile));
            }
        }
        System.out.println("afterSaveZform");
        return resultJson;
    }
}
