package com.jeestudio.bpm.modules.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.utils.ConvertUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 安全配置管理接口
 */
@RestController
@RequestMapping("${adminPath}/sys/secIConfig")
@Tag(name = "安全配置")
@Slf4j
public class SysSecIConfigController extends BaseController {

    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    GenTableService genTableService;

    @Autowired
    ZformService zformService;

    /**
     * syncData
     */
    @Operation(summary = "同步安全配置数据")
    @RequiresPermissions("user")
    @PostMapping("/syncData")
    public ResultJson syncData(@RequestBody JSONObject jsonObject) throws Exception {
        new Thread(() -> {
            String tableName = jsonObject.getString("tableName");
            boolean all = jsonObject.getBoolean("all");
            if (StrUtil.isBlank(tableName)) {
                List<LinkedHashMap> mapList = zformService.findMapList("sys_sec_iconfig");
                for (LinkedHashMap map : mapList) {
                    this.syncTable(ConvertUtil.getString(map.get("table_name")), all);
                }
            } else {
                syncTable(tableName, all);
            }

        }).start();
        return success();
    }


    private void syncTable(String tableName, boolean all) {
        Map<String, Map<String, String>> map = sysSecIConfigService.getColumns(tableName);
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            GenTable genTable = genTableService.getGenTableWithDefination(tableName);
            QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
            if (!all) {
                queryWrapper.apply("a.id  in (select data_id from sys_sec_idata where parent_id = {0} and  valid_pass = '0' and env = {1} )", entry.getKey(), sysSecIConfigService.getActive());
            }
            List<LinkedHashMap> mapList = zformService.findMapList(tableName, queryWrapper);
            zformService.appendExtManyToMany(mapList, genTable);
            sysSecIConfigService.saveMapRecord(entry.getKey(), new ArrayList<>(entry.getValue().keySet()), mapList, genTable);
        }
    }
}
