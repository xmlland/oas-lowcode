package com.jeestudio.bpm.modules.oas.service;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 流程效率统计服务
 * 提供流程总览、高频流程、用户效率、时间趋势、状态分布等统计功能
 */
@Service
public class ProcessStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessStatisticsService.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    /**
     * 获取流程总览统计
     * @return 包含今日新增、完成率、平均处理时长、进行中数量、已完成数量的Map
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            // 今日开始时间
            Date todayStart = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // 进行中流程数
            long totalRunning = runtimeService.createProcessInstanceQuery().count();

            // 已完成流程数
            long totalCompleted = historyService.createHistoricProcessInstanceQuery().finished().count();

            // 今日新增流程数
            long todayNewProcess = historyService.createHistoricProcessInstanceQuery()
                    .startedAfter(todayStart)
                    .count();

            // 总流程数
            long totalProcess = totalRunning + totalCompleted;

            // 完成率
            double completionRate = totalProcess > 0 ? (double) totalCompleted / totalProcess * 100 : 0;

            // 平均处理时长（仅统计已完成的流程）
            double avgProcessTimeMinutes = calculateAverageProcessTime();

            result.put("todayNewProcess", todayNewProcess);
            result.put("completionRate", Math.round(completionRate * 100) / 100.0);
            result.put("avgProcessTimeMinutes", Math.round(avgProcessTimeMinutes * 100) / 100.0);
            result.put("totalRunning", totalRunning);
            result.put("totalCompleted", totalCompleted);
        } catch (Exception e) {
            logger.error("获取流程总览统计失败", e);
        }
        return result;
    }

    /**
     * 获取高频流程 Top N
     * @param limit 返回数量限制
     * @return 按流程数量降序排列的流程列表
     */
    public List<Map<String, Object>> getTopProcesses(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 查询所有历史流程实例
            List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                    .orderByProcessInstanceStartTime().desc()
                    .list();

            // 按流程定义名称分组统计
            Map<String, Long> processCountMap = instances.stream()
                    .collect(Collectors.groupingBy(
                            instance -> instance.getProcessDefinitionName() != null ?
                                    instance.getProcessDefinitionName() : instance.getProcessDefinitionKey(),
                            Collectors.counting()
                    ));

            // 按数量降序排序并取前N个
            result = processCountMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("name", entry.getKey());
                        item.put("count", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取高频流程统计失败", e);
        }
        return result;
    }

    /**
     * 获取用户办结效率排行
     * @param limit 返回数量限制
     * @return 按完成任务数降序排列的用户列表
     */
    public List<Map<String, Object>> getUserPerformance(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 查询所有已完成的任务
            List<HistoricTaskInstance> finishedTasks = historyService.createHistoricTaskInstanceQuery()
                    .finished()
                    .list();

            // 按 assignee 分组
            Map<String, List<HistoricTaskInstance>> userTasksMap = finishedTasks.stream()
                    .filter(task -> task.getAssignee() != null && !task.getAssignee().isEmpty())
                    .collect(Collectors.groupingBy(HistoricTaskInstance::getAssignee));

            // 计算每个用户的完成数和平均处理时长
            result = userTasksMap.entrySet().stream()
                    .map(entry -> {
                        String userName = entry.getKey();
                        List<HistoricTaskInstance> tasks = entry.getValue();
                        long completedCount = tasks.size();

                        // 计算平均处理时长（毫秒转分钟）
                        double avgDuration = tasks.stream()
                                .filter(task -> task.getDurationInMillis() != null)
                                .mapToLong(HistoricTaskInstance::getDurationInMillis)
                                .average()
                                .orElse(0);
                        double avgDurationMinutes = avgDuration / (1000 * 60);

                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("userName", userName);
                        item.put("completedCount", completedCount);
                        item.put("avgDurationMinutes", Math.round(avgDurationMinutes * 100) / 100.0);
                        return item;
                    })
                    .sorted((a, b) -> Long.compare(
                            (Long) b.get("completedCount"),
                            (Long) a.get("completedCount")))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取用户办结效率统计失败", e);
        }
        return result;
    }

    /**
     * 获取时间趋势数据
     * @param period 时间周期: day(近30天), week(近12周), month(近12月)
     * @return 按时间分组的新增和完成流程数
     */
    public List<Map<String, Object>> getTimeline(String period) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            LocalDate now = LocalDate.now();
            LocalDate startDate;
            DateTimeFormatter formatter;
            ChronoUnit unit;

            switch (period.toLowerCase()) {
                case "week":
                    startDate = now.minusWeeks(12);
                    formatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
                    unit = ChronoUnit.WEEKS;
                    break;
                case "month":
                    startDate = now.minusMonths(12);
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                    unit = ChronoUnit.MONTHS;
                    break;
                case "day":
                default:
                    startDate = now.minusDays(30);
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    unit = ChronoUnit.DAYS;
                    break;
            }

            Date queryStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // 查询指定时间段内的所有流程实例
            List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                    .startedAfter(queryStartDate)
                    .list();

            // 按日期分组统计新增
            Map<String, Long> newCountMap = instances.stream()
                    .collect(Collectors.groupingBy(
                            instance -> {
                                LocalDate date = instance.getStartTime().toInstant()
                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                                return formatDateByPeriod(date, period);
                            },
                            Collectors.counting()
                    ));

            // 按日期分组统计完成
            Map<String, Long> completedCountMap = instances.stream()
                    .filter(instance -> instance.getEndTime() != null)
                    .collect(Collectors.groupingBy(
                            instance -> {
                                LocalDate date = instance.getEndTime().toInstant()
                                        .atZone(ZoneId.systemDefault()).toLocalDate();
                                return formatDateByPeriod(date, period);
                            },
                            Collectors.counting()
                    ));

            // 生成完整的日期序列
            Set<String> allDates = new TreeSet<>();
            allDates.addAll(newCountMap.keySet());
            allDates.addAll(completedCountMap.keySet());

            for (String date : allDates) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("date", date);
                item.put("newCount", newCountMap.getOrDefault(date, 0L));
                item.put("completedCount", completedCountMap.getOrDefault(date, 0L));
                result.add(item);
            }
        } catch (Exception e) {
            logger.error("获取时间趋势统计失败", e);
        }
        return result;
    }

    /**
     * 获取流程状态分布
     * @return 包含进行中、已完成、已挂起数量的Map
     */
    public Map<String, Object> getStatusDistribution() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            // 进行中（不含挂起）
            long running = runtimeService.createProcessInstanceQuery().active().count();

            // 已完成
            long completed = historyService.createHistoricProcessInstanceQuery().finished().count();

            // 已挂起
            long suspended = runtimeService.createProcessInstanceQuery().suspended().count();

            result.put("running", running);
            result.put("completed", completed);
            result.put("suspended", suspended);
        } catch (Exception e) {
            logger.error("获取流程状态分布统计失败", e);
        }
        return result;
    }

    /**
     * 计算平均流程处理时长（分钟）
     */
    private double calculateAverageProcessTime() {
        try {
            List<HistoricProcessInstance> completedInstances = historyService.createHistoricProcessInstanceQuery()
                    .finished()
                    .orderByProcessInstanceEndTime().desc()
                    .listPage(0, 1000); // 取最近1000条计算平均值

            if (completedInstances.isEmpty()) {
                return 0;
            }

            double totalDuration = completedInstances.stream()
                    .filter(instance -> instance.getDurationInMillis() != null)
                    .mapToLong(HistoricProcessInstance::getDurationInMillis)
                    .average()
                    .orElse(0);

            return totalDuration / (1000 * 60); // 毫秒转分钟
        } catch (Exception e) {
            logger.error("计算平均流程处理时长失败", e);
            return 0;
        }
    }

    /**
     * 根据周期格式化日期
     */
    private String formatDateByPeriod(LocalDate date, String period) {
        switch (period.toLowerCase()) {
            case "week":
                // 获取该日期所在周的周一
                LocalDate monday = date.with(java.time.DayOfWeek.MONDAY);
                return monday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "month":
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            case "day":
            default:
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}
