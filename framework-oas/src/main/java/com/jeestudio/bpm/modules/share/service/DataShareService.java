package com.jeestudio.bpm.modules.share.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.around.AroundUtil;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.DateUtil;
import com.jeestudio.bpm.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 数据共享服务
 */
@Service
@Slf4j
public class DataShareService {

    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    public Page<Zform> findDataSharePageMap(JSONObject zformMap,
                                        String formNo,
                                        String timestampColumnName,
                                        String startDate,
                                        String endDate,
                                        Integer pageNo,
                                        Integer pageSize,
                                        String extFlag,
                                        String loginName) throws Exception {
        String path = "";
        String traceFlag = "";
        String parentId = "";
        zformMap.put("formNo", formNo);
        Zform zform = zformService.getZformFromMap(zformMap, loginName, true);
        GenTable zformTable = new GenTable();
        zformTable.setName("z_form");
        AroundServiceI zformAroundService = AroundUtil.getAroundServiceI(zformTable);
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        zformService.checkSelectPermission(loginName, genTable.getName());
        List<GenTableColumn> columnList = genTable.getColumnList();

        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }

        zform.getPageParam().setPageSize(pageSize);
        zform.getPageParam().setPageNo(pageNo);
        zform.getPageParam().setOrderBy("a." + timestampColumnName);
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        if (queryWrapper == null) {
            queryWrapper = new QueryWrapper<>();
        }
        queryWrapper.between("a." + timestampColumnName, DateUtil.strToDate(startDate), DateUtil.strToDate(endDate));

        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        Page<Zform> pageMap = zformService.findPageMap(page,
                zform,
                path,
                loginName,
                genTable,
                traceFlag,
                parentId,
                extFlag);
        if (zformAroundService != null) {
            for (GenTableColumn column : columnList) {
                String settings = column.getSettings();
                if (StringUtil.isEmpty(settings)) continue;
                JSONObject jsonObject = JSONObject.parseObject(settings);
                if (!jsonObject.containsKey("attachment")) continue;
                String attachment = jsonObject.getString("attachment");
                if (StringUtil.isEmpty(attachment)) continue;
                String columnJSONString = JSON.toJSONString(column);
                zformAroundService.afterFindPageMap(pageMap, page, zform, path, loginName, genTable, traceFlag, parentId, columnJSONString);
            }
        }
        return pageMap;
    }
}
