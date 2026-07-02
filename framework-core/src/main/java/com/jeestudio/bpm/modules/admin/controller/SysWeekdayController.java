package com.jeestudio.bpm.modules.admin.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.admin.service.SysWeekdayServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 工作日历
 */
@RestController
@RequestMapping("${adminPath}/admin/sysWeekday")
@Tag(name = "工作日历")
@Slf4j
public class SysWeekdayController extends BaseController {

    @Autowired
    private SecLogService secLogService;

    @Autowired
    ZformService zformService;

    @Autowired
    SysWeekdayServiceI sysWeekdayService;

    /**
     * calcDate
     */
    @Operation(summary = "计算工作日日期")
    @RequiresPermissions("user")
    @GetMapping("/calcDate")
    public ResultJson calcDate(String date, int days) {
        return ResultJson.success().put("data", sysWeekdayService.calcDate(date, days));
    }
}
