package com.jeestudio.bpm.modules.admin.around;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 安全数据扩展服务
 */
@Component("sys_sec_idataAroundService")
public class SysSecIDataAroundService implements AroundServiceI {
    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    @Override
    public void beforeFindPageMap(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        zform.getQueryWrapper().eq("env", sysSecIConfigService.getActive());
    }

    @Override
    public LinkedHashMap afterGetMap(LinkedHashMap map, String id, GenTable genTable, String extFlag, String loginName) {
        String data_id = ConvertUtil.getString(map.get("data_id"));
        String table_name = ConvertUtil.getString(map.get("table_name"));
        if (StrUtil.isNotBlank(data_id) && StrUtil.isNotBlank(table_name)) {
            GenTable genTableData = genTableService.getGenTableWithDefination(table_name);
            LinkedHashMap extData = zformService.getMap(table_name, data_id);
            zformService.appendExtManyToMany(Collections.singletonList(extData), genTableData);
            Map<String, Map<String, String>> columnMap = sysSecIConfigService.getColumns(table_name);
            for (Map.Entry<String, Map<String, String>> entry : columnMap.entrySet()) {
                map.put("raw_data_now", sysSecIConfigService.mergeColumn(extData, new ArrayList<>(entry.getValue().keySet())));
            }
        }

        return map;
    }
}
