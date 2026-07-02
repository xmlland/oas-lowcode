package com.jeestudio.bpm.modules.oas.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.oas.service.AttendanceStatisticsService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 考勤统计
 * 供大屏图表数据绑定
 */
@RestController
@RequestMapping("${adminPath}/oas/attendance/statistics")
@Tag(name = "考勤统计")
@Slf4j
public class AttendanceStatisticsController extends BaseController {

    @Autowired
    private AttendanceStatisticsService attendanceStatisticsService;

    /**
     * 考勤总览
     * 获取指定月份的考勤汇总数据
     *
     * @param month 统计月份，格式 yyyy-MM，不传则默认当月
     * @return { attendanceRate, lateCount, leaveCount, absenceCount, totalEmployees }
     */
    @Operation(summary = "考勤总览")
    @RequiresPermissions("user")
    @GetMapping("/overview")
    public ResultJson getOverview(
            @Parameter(description = "统计月份，格式yyyy-MM，默认当月", example = "2026-03")
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> data = attendanceStatisticsService.getOverview(month);
            return ResultJson.success().put("data", data);
        } catch (Exception e) {
            log.error("获取考勤总览失败", e);
            return ResultJson.failed("获取考勤总览失败");
        }
    }

    /**
     * 部门出勤统计
     * 按部门分组统计出勤情况
     *
     * @param month 统计月份，格式 yyyy-MM，不传则默认当月
     * @return List：[{ departmentName, attendanceRate, employeeCount, lateCount }]
     */
    @Operation(summary = "部门出勤统计")
    @RequiresPermissions("user")
    @GetMapping("/department")
    public ResultJson getDepartmentStats(
            @Parameter(description = "统计月份，格式yyyy-MM，默认当月", example = "2026-03")
            @RequestParam(required = false) String month) {
        try {
            List<Map<String, Object>> data = attendanceStatisticsService.getDepartmentStats(month);
            return ResultJson.success().put("list", data);
        } catch (Exception e) {
            log.error("获取部门出勤统计失败", e);
            return ResultJson.failed("获取部门出勤统计失败");
        }
    }

    /**
     * 加班排行 Top N
     * 查询指定月份加班时长最高的员工
     *
     * @param month 统计月份，格式 yyyy-MM，不传则默认当月
     * @param limit 返回数量，默认 10
     * @return List：[{ userName, departmentName, overtimeHours }]
     */
    @Operation(summary = "加班排行")
    @RequiresPermissions("user")
    @GetMapping("/overtime-ranking")
    public ResultJson getOvertimeRanking(
            @Parameter(description = "统计月份，格式yyyy-MM，默认当月", example = "2026-03")
            @RequestParam(required = false) String month,
            @Parameter(description = "返回数量，默认10", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> data = attendanceStatisticsService.getOvertimeRanking(month, limit);
            return ResultJson.success().put("list", data);
        } catch (Exception e) {
            log.error("获取加班排行失败", e);
            return ResultJson.failed("获取加班排行失败");
        }
    }

    /**
     * 考勤状态分布
     * 统计各考勤状态的人次
     *
     * @param month 统计月份，格式 yyyy-MM，不传则默认当月
     * @return Map：{ normal, late, earlyLeave, absent, leave, businessTrip }
     */
    @Operation(summary = "考勤状态分布")
    @RequiresPermissions("user")
    @GetMapping("/status-distribution")
    public ResultJson getStatusDistribution(
            @Parameter(description = "统计月份，格式yyyy-MM，默认当月", example = "2026-03")
            @RequestParam(required = false) String month) {
        try {
            Map<String, Object> data = attendanceStatisticsService.getStatusDistribution(month);
            return ResultJson.success().put("data", data);
        } catch (Exception e) {
            log.error("获取考勤状态分布失败", e);
            return ResultJson.failed("获取考勤状态分布失败");
        }
    }

    /**
     * 月度考勤趋势
     * 查询近 N 个月的出勤率变化
     *
     * @param months 查询月数，默认 12
     * @return List：[{ month, attendanceRate, lateRate }]
     */
    @Operation(summary = "月度考勤趋势")
    @RequiresPermissions("user")
    @GetMapping("/monthly-trend")
    public ResultJson getMonthlyTrend(
            @Parameter(description = "查询月数，默认12", example = "12")
            @RequestParam(defaultValue = "12") int months) {
        try {
            List<Map<String, Object>> data = attendanceStatisticsService.getMonthlyTrend(months);
            return ResultJson.success().put("list", data);
        } catch (Exception e) {
            log.error("获取月度考勤趋势失败", e);
            return ResultJson.failed("获取月度考勤趋势失败");
        }
    }
}
