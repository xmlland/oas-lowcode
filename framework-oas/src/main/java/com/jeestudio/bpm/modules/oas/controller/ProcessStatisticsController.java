package com.jeestudio.bpm.modules.oas.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.oas.service.ProcessStatisticsService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 流程效率统计
 * 提供流程统计相关的 REST API，供大屏图表数据绑定
 */
@RestController
@RequestMapping("${adminPath}/oas/process/statistics")
@Tag(name = "流程效率统计")
@Slf4j
public class ProcessStatisticsController extends BaseController {

    @Autowired
    private ProcessStatisticsService processStatisticsService;

    /**
     * 获取流程总览统计
     * 包含今日新增、完成率、平均处理时长、进行中数量、已完成数量
     */
    @Operation(summary = "流程总览统计")
    @RequiresPermissions("user")
    @GetMapping("/overview")
    public ResultJson getOverview() {
        try {
            Map<String, Object> overview = processStatisticsService.getOverview();
            ResultJson result = ResultJson.success();
            overview.forEach(result::put);
            return result;
        } catch (Exception e) {
            log.error("获取流程总览统计失败", e);
            return ResultJson.failed("获取流程总览统计失败");
        }
    }

    /**
     * 获取高频流程 Top N
     * 按流程数量降序排列
     */
    @Operation(summary = "高频流程排行")
    @RequiresPermissions("user")
    @GetMapping("/top-processes")
    public ResultJson getTopProcesses(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> topProcesses = processStatisticsService.getTopProcesses(limit);
            return ResultJson.success().put("list", topProcesses);
        } catch (Exception e) {
            log.error("获取高频流程统计失败", e);
            return ResultJson.failed("获取高频流程统计失败");
        }
    }

    /**
     * 获取用户办结效率排行
     * 按完成任务数降序排列
     */
    @Operation(summary = "用户办结效率排行")
    @RequiresPermissions("user")
    @GetMapping("/user-performance")
    public ResultJson getUserPerformance(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> userPerformance = processStatisticsService.getUserPerformance(limit);
            return ResultJson.success().put("list", userPerformance);
        } catch (Exception e) {
            log.error("获取用户办结效率统计失败", e);
            return ResultJson.failed("获取用户办结效率统计失败");
        }
    }

    /**
     * 获取时间趋势数据
     * 按时间分组统计新增和完成流程数
     */
    @Operation(summary = "流程时间趋势")
    @RequiresPermissions("user")
    @GetMapping("/timeline")
    public ResultJson getTimeline(
            @Parameter(description = "时间周期: day(近30天), week(近12周), month(近12月)", example = "day")
            @RequestParam(defaultValue = "day") String period) {
        try {
            // 校验 period 参数
            if (!period.equalsIgnoreCase("day") && 
                !period.equalsIgnoreCase("week") && 
                !period.equalsIgnoreCase("month")) {
                return ResultJson.failed("参数错误，period 只能是 day、week 或 month");
            }
            List<Map<String, Object>> timeline = processStatisticsService.getTimeline(period);
            return ResultJson.success().put("list", timeline);
        } catch (Exception e) {
            log.error("获取流程时间趋势统计失败", e);
            return ResultJson.failed("获取流程时间趋势统计失败");
        }
    }

    /**
     * 获取流程状态分布
     * 包含进行中、已完成、已挂起数量
     */
    @Operation(summary = "流程状态分布")
    @RequiresPermissions("user")
    @GetMapping("/status-distribution")
    public ResultJson getStatusDistribution() {
        try {
            Map<String, Object> distribution = processStatisticsService.getStatusDistribution();
            ResultJson result = ResultJson.success();
            distribution.forEach(result::put);
            return result;
        } catch (Exception e) {
            log.error("获取流程状态分布统计失败", e);
            return ResultJson.failed("获取流程状态分布统计失败");
        }
    }
}
