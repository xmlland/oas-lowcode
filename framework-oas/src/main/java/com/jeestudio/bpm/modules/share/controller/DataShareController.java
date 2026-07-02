package com.jeestudio.bpm.modules.share.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.share.service.DataShareService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * @Description: 数据共享
 */
@RestController
@RequestMapping("${adminPath}/share")
@Tag(name = "数据共享")
@Slf4j
public class DataShareController extends BaseController {
    private static final DateTimeFormatter STRICT_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT); // 关键：启用严格模式

    @Autowired
    private DataShareService dataShareService;

    @Autowired
    private SecLogService secLogService;

    /**
     * 通用对外共享数据接口，需要分配表单的查询权限
     * @param zformMap 组合查询条件
     * @param formNo 表名
     * @param timestampColumnName 时间戳字段名
     * @param startDate 起始时间
     * @param endDate 结束时间
     * @param offsetValue 起始记录索引
     * @return
     */
    @Operation(summary = "通用数据查询")
    @RequiresPermissions("app:share")
    @PostMapping("/datamap")
    public ResultJson datamap(@RequestBody JSONObject zformMap,
                              @RequestParam("table_name") String formNo,
                              @RequestParam("timestamp_column_name") String timestampColumnName,
                              @RequestParam("start_date") String startDate,
                              @RequestParam("end_date") String endDate,
                              @RequestParam("offsetValue") Integer offsetValue) {
        try {
            SqlInjectionUtil.filterContent(new String[]{formNo, timestampColumnName, startDate, endDate});
            int pageSize = 1000;
            String invalidMessage = "";
            if (offsetValue % pageSize != 0) {
                invalidMessage += " offsetValue应输入1000的整数倍，值输入错误" + offsetValue;
            }
            if (false == isValidFormat(startDate)) {
                invalidMessage += " 开始时间格式错误，正确的格式为yyyy-MM-dd HH:mm:ss";
            }
            if (false == isValidFormat(endDate)) {
                invalidMessage += " 结束时间格式错误，正确的格式为yyyy-MM-dd HH:mm:ss";
            }
            if (StringUtil.isNotEmpty(invalidMessage)) {
                return ResultJson.failed("查询数据异常：" + invalidMessage);
            }
            Integer pageNo = offsetValue % pageSize + 1;
            String extFlag = GenTable.EXT_FLAG_OUTER;
            Page<Zform> pageMap = dataShareService.findDataSharePageMap(zformMap, formNo, timestampColumnName, startDate, endDate, pageNo, pageSize, extFlag, currentUserName.get());
            ResultJson resultJson = ResultJson.success().setRows(pageMap.getMap()).setTotal(pageMap.getCount());
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.YES, formNo, secLogService.ACTION_QUERY);
            return resultJson;
        } catch (Exception e) {
            log.error("Error occurred while trying to get shared data: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLogZform(currentUserName.get(), ip.get(), Global.NO, formNo, secLogService.ACTION_QUERY);
            return ResultJson.failed("查询数据异常：" + e.getMessage());
        }
    }

    private static boolean isValidFormat(String input) {
        try {
            LocalDateTime.parse(input, STRICT_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false; // 格式错误或日期无效
        }
    }
}
