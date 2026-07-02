package com.jeestudio.bpm.modules.admin.around;

import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.service.system.AreaService;
import com.jeestudio.bpm.utils.ResultJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: 行政区域扩展服务
 */
@Component("sys_areaAroundService")
public class SysAreaAroundService implements AroundServiceI {

    @Autowired
    AreaService areaService;

    @Override
    public ResultJson afterSaveZform(ResultJson resultJson, Zform zform, String loginName, String businessKey) throws Exception {
        areaService.cleanCache();
        return resultJson;
    }

    @Override
    public void afterDeleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {
        areaService.cleanCache();
    }

    @Override
    public void afterImportData(List<Zform> importData, String ownerCode, List<Map<String, Object>> mapList, String formNo, String parentFormNo, GenTable genTable, String parentId, String uniqueId, User currentUser) {
        areaService.cleanCache();
    }
}
