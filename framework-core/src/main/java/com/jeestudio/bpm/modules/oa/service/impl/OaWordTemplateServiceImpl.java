package com.jeestudio.bpm.modules.oa.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.feign.WordExportFeignClient;
import com.jeestudio.bpm.modules.oa.dao.OaWordTemplateMapper;
import com.jeestudio.bpm.modules.oa.entity.OaWordTemplateEntity;
import com.jeestudio.bpm.modules.oa.service.OaWordTemplateServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: Word模板服务实现
 */
@Service
@Transactional
public class OaWordTemplateServiceImpl extends ServiceImpl<OaWordTemplateMapper, OaWordTemplateEntity> implements OaWordTemplateServiceI {


    @Autowired
    ZformService zformService;

    @Autowired
    WordExportFeignClient wordExportFeignClient;

    @Autowired
    private SysFileService sysFileService;

    @Value("${sys.file.datasource:master}")
    private String sysFileDatasource;

    private boolean isDatahouseMode() {
        return GenTable.MODULE_DATAHOUSE.equalsIgnoreCase(sysFileDatasource);
    }


    @Override
    public LinkedHashMap<String, Object> checkTemplate() {
        LinkedHashMap<String, Object> resultData = new LinkedHashMap<>();

        List<Map<String, String>> allFileList;
        if (isDatahouseMode()) {
            // datahouse 模式：分开查询模板和文件，避免跨数据源 JOIN
            allFileList = findAllFileDatahouse();
        } else {
            // master 模式：直接 JOIN 查询
            allFileList = baseMapper.findAllFile();
        }
        List<String> fileNameList = new LinkedList<>();
        Map<String, Map<String,String>> fileNameMap = new HashMap<>();

        for (Map<String, String> map : allFileList) {
            String fileName = map.get("id") + "." + map.get("ext_");
            fileNameList.add(fileName);
            fileNameMap.put(fileName,map);
        }

        JSONObject jsonObject = wordExportFeignClient.checkTemplate(fileNameList);
        Integer code = jsonObject.getInteger("code");
        if(code!=0){
            throw new BusinessException(jsonObject.getString("msg"));
        }

        JSONArray existFile = jsonObject.getJSONArray("existFile");
        JSONArray notExistFile = jsonObject.getJSONArray("notExistFile");
        JSONArray unknownFile = jsonObject.getJSONArray("unknownFile");
        resultData.put("existFile",getMapList(fileNameMap,existFile));
        resultData.put("notExistFile",getMapList(fileNameMap,notExistFile));
        resultData.put("unknownFile",unknownFile);
        return resultData;
    }

    /**
     * datahouse 模式：查询所有模板，逐个通过 SysFileService 获取文件信息
     * 输出格式与 findAllFile() 保持完全一致: {id, name_, ext_, word_code, word_name}
     */
    private List<Map<String,String>> findAllFileDatahouse() {
        List<Map<String,String>> result = new LinkedList<>();
        List<Map<String,String>> templates = baseMapper.findAllTemplateRecords();
        for (Map<String, String> tpl : templates) {
            String templateFile = tpl.get("template_file");
            if (templateFile != null && !templateFile.isEmpty()) {
                List<SysFile> files = sysFileService.getFileListByGroupId(templateFile);
                for (SysFile f : files) {
                    Map<String, String> item = new HashMap<>();
                    item.put("id", f.getId());
                    item.put("name_", f.getName());
                    item.put("ext_", f.getExt());
                    item.put("word_code", tpl.get("word_code"));
                    item.put("word_name", tpl.get("word_name"));
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * 根据模板 ID 获取文件
     * datahouse 模式下：先查 template_file，再通过 SysFileService 获取文件
     */
    public SysFile getFileByTemplateId(String templateId) {
        if (isDatahouseMode()) {
            String templateFile = baseMapper.getTemplateFileByTemplateId(templateId);
            if (templateFile != null) {
                List<SysFile> files = sysFileService.getFileListByGroupId(templateFile);
                if (files != null && !files.isEmpty()) {
                    return files.get(0);
                }
            }
            return null;
        }
        return baseMapper.getFileByTemplateId(templateId);
    }

    private List<Map<String,String>> getMapList(Map<String, Map<String,String>> fileNameMap, JSONArray fileList){
        List<Map<String,String>> mapList = new LinkedList<>();
        for (int i = 0; i < fileList.size(); i++) {
            String fileName = fileList.getString(i);
            if(fileNameMap.containsKey(fileName)){
                mapList.add(fileNameMap.get(fileName));
            }else {
                log.error("未找到此文件名：" + fileName);
            }
        }
        return mapList;
    }
}
