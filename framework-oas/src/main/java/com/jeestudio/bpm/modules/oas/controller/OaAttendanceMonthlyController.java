package com.jeestudio.bpm.modules.oas.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.oas.service.OaAttendanceMonthlyService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 考勤管理
 */
@RestController
@RequestMapping("${adminPath}/oas/attendanceMonthly")
@Tag(name = "考勤管理")
@Slf4j
public class OaAttendanceMonthlyController extends BaseController {

    @Autowired
    private OaAttendanceMonthlyService oaAttendanceMonthlyService;

    @Operation(summary = "手动触发月度考勤统计")
    @RequiresPermissions("user")
    @PostMapping("/calculate")
    public ResultJson calculate(
            @Parameter(description = "统计月份，格式yyyy-MM", required = true, example = "2026-02")
            @RequestParam String yearMonth) {
        if (StringUtil.isBlank(yearMonth) || !yearMonth.matches("\\d{4}-\\d{2}")) {
            return ResultJson.failed("参数格式错误，请使用yyyy-MM格式");
        }
        try {
            return oaAttendanceMonthlyService.calculateMonthlyStatistics(yearMonth);
        } catch (Exception e) {
            log.error("月度考勤统计失败", e);
            return ResultJson.failed("统计失败: " + e.getMessage());
        }
    }
}
