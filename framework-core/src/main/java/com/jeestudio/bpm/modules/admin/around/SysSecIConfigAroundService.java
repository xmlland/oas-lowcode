package com.jeestudio.bpm.modules.admin.around;

import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 安全配置扩展服务
 */
@Component("sys_sec_iconfigAroundService")
public class SysSecIConfigAroundService implements AroundServiceI {
    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    ZformDao zformDao;

    @Override
    public ResultJson afterSaveZform(ResultJson resultJson, Zform zform, String loginName, String businessKey) throws Exception {
        sysSecIConfigService.cleanCache();
        return resultJson;
    }

    @Override
    public void afterDeleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {
        sysSecIConfigService.cleanCache();
    }

    @Override
    public Page<Zform> afterFindPageMap(Page<Zform> pageMap, Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        for (LinkedHashMap map : pageMap.getMap()) {
            String id = ConvertUtil.getString(map.get("id"));
            String table_name = ConvertUtil.getString(map.get("table_name"));
            Map<String, Object> extInfo = sysSecIConfigService.parseExtInfo(id, table_name);
            map.putAll(extInfo);
        }
        return pageMap;
    }


}
