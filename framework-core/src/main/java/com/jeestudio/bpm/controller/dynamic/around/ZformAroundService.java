package com.jeestudio.bpm.controller.dynamic.around;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 动态表单扩展服务，处理附件关联字段的列表展示数据
 */
@Component("z_formAroundService")
@Slf4j
public class ZformAroundService implements AroundServiceI {
    @Autowired
    private ZformService zformService;
    @Override
    public Page<Zform> afterFindPageMap(Page<Zform> pageMap, Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        GenTableColumn parsedColumn = JSON.parseObject(extFlag, GenTableColumn.class);
        JSONObject columnJSONObject = JSONObject.parseObject(parsedColumn.getSettings());
        String attachment = columnJSONObject.getString("attachment");
        JSONObject jsonObject = JSONObject.parseObject(attachment);
        if (!jsonObject.containsKey("showTableList")) {
            return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
        String showTableList = jsonObject.getString("showTableList");
        if (!showTableList.equals("true")) {
            return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
        if (!jsonObject.containsKey("relevanceType")) {
            return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
        String relevanceType = jsonObject.getString("relevanceType");
        switch (relevanceType) {
            case "GROUP":
                // 1.1 获取关联表
                List<String> groupIdList = pageMap.getMap().stream().map(k -> ConvertUtil.getString(k.get(parsedColumn.getName()))).collect(Collectors.toList());
                if (groupIdList.size()>0){
                    QueryWrapper<SysFile> queryWrapper = new QueryWrapper<>();
                    queryWrapper.in("group_id_", groupIdList);
                    List<LinkedHashMap> sysFiles = zformService.findMapList("sys_file_", queryWrapper);
                    Map<String, List<LinkedHashMap>> groupIdFileListMap = sysFiles.stream().collect(Collectors.groupingBy(k -> ConvertUtil.getString(k.get("group_id_"))));
                    for (LinkedHashMap item : pageMap.getMap()) {
                        String keyName = parsedColumn.getName();
                        String groupId = ConvertUtil.getString(item.get(keyName));
                        if (groupId.isEmpty()) { continue; }
                        item.put(keyName + "__file", groupIdFileListMap.get(groupId));
                    }
                }
                return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
            case "SINGLE":
                // 1.2 获取关联表
                List<String> groupIdList2 = pageMap.getMap().stream().map(k -> ConvertUtil.getString(k.get(parsedColumn.getName()))).collect(Collectors.toList());
                if (groupIdList2.size()>0){
                    QueryWrapper<SysFile> queryWrapper = new QueryWrapper<>();
                    queryWrapper.in("group_id_", groupIdList2);
                    List<LinkedHashMap> sysFiles = zformService.findMapList("sys_file_", queryWrapper);
                    Map<String, List<LinkedHashMap>> groupIdFileListMap = sysFiles.stream().collect(Collectors.groupingBy(k -> ConvertUtil.getString(k.get("group_id_"))));
                    for (LinkedHashMap item : pageMap.getMap()) {
                        String keyName = parsedColumn.getName();
                        String groupId = ConvertUtil.getString(item.get(keyName));
                        if (groupId.isEmpty()) { continue; }
                        item.put(keyName + "__file", groupIdFileListMap.get(groupId));
                    }
                }
                return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
            default:
                return AroundServiceI.super.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, extFlag);
        }
    }
}
