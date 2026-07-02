package com.jeestudio.bpm.modules.oas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordAttendanceStatusEnum;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description: 考勤统计服务
 * 提供大屏图表数据绑定所需的考勤统计 API
 */
@Service
public class AttendanceStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceStatisticsService.class);

    private static final String MONTHLY_FORM = "oa_attendance_monthly";
    private static final String RECORD_FORM = "oa_attendance_record";
    private static final String OFFICE_FORM = "sys_office";
    private static final String USER_FORM = "sys_user";

    private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private ZformService zformService;

    /**
     * 获取考勤总览数据
     *
     * @param month 格式 yyyy-MM，为空则默认当月
     * @return { attendanceRate, lateCount, leaveCount, absenceCount, totalEmployees }
     */
    public Map<String, Object> getOverview(String month) {
        String yearMonth = normalizeMonth(month);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 查询指定月份的月度统计数据
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.eq("a.year_month", yearMonth);
            List<LinkedHashMap> monthlyList = zformService.findMapList(MONTHLY_FORM, qw);

            if (monthlyList == null || monthlyList.isEmpty()) {
                result.put("attendanceRate", BigDecimal.ZERO);
                result.put("lateCount", 0);
                result.put("leaveCount", 0);
                result.put("absenceCount", 0);
                result.put("totalEmployees", 0);
                result.put("month", yearMonth);
                return result;
            }

            int totalEmployees = monthlyList.size();
            int totalWorkDays = 0;
            int totalActualDays = 0;
            int lateCount = 0;       // 迟到人数
            int leaveCount = 0;      // 请假人数
            int absenceCount = 0;    // 旷工人数

            for (LinkedHashMap record : monthlyList) {
                int workDays = getInt(record, "work_days");
                int actualDays = getInt(record, "actual_days");
                int late = getInt(record, "late_count");
                int leave = getInt(record, "leave_days");
                int absent = getInt(record, "absent_count");

                totalWorkDays += workDays;
                totalActualDays += actualDays;

                if (late > 0) lateCount++;
                if (leave > 0) leaveCount++;
                if (absent > 0) absenceCount++;
            }

            // 计算出勤率 = 总实际出勤天数 / 总应出勤天数
            BigDecimal attendanceRate = BigDecimal.ZERO;
            if (totalWorkDays > 0) {
                attendanceRate = BigDecimal.valueOf(totalActualDays)
                        .divide(BigDecimal.valueOf(totalWorkDays), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            result.put("attendanceRate", attendanceRate);
            result.put("lateCount", lateCount);
            result.put("leaveCount", leaveCount);
            result.put("absenceCount", absenceCount);
            result.put("totalEmployees", totalEmployees);
            result.put("month", yearMonth);

        } catch (Exception e) {
            logger.error("获取考勤总览数据失败", e);
            throw new RuntimeException("获取考勤总览数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取部门出勤统计
     *
     * @param month 格式 yyyy-MM，为空则默认当月
     * @return List：[{ departmentName, attendanceRate, employeeCount, lateCount }]
     */
    public List<Map<String, Object>> getDepartmentStats(String month) {
        String yearMonth = normalizeMonth(month);
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 查询月度统计数据
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.eq("a.year_month", yearMonth);
            List<LinkedHashMap> monthlyList = zformService.findMapList(MONTHLY_FORM, qw);

            if (monthlyList == null || monthlyList.isEmpty()) {
                return result;
            }

            // 获取用户-部门映射
            Map<String, String> userOfficeMap = getUserOfficeMap(monthlyList);

            // 获取部门名称映射
            Map<String, String> officeNameMap = getOfficeNameMap(userOfficeMap.values());

            // 按部门分组统计
            Map<String, List<LinkedHashMap>> deptRecordsMap = new LinkedHashMap<>();
            for (LinkedHashMap record : monthlyList) {
                String userId = getStr(record, "user_id");
                String officeId = userOfficeMap.get(userId);
                if (StringUtil.isNotBlank(officeId)) {
                    deptRecordsMap.computeIfAbsent(officeId, k -> new ArrayList<>()).add(record);
                }
            }

            // 计算每个部门的统计数据
            for (Map.Entry<String, List<LinkedHashMap>> entry : deptRecordsMap.entrySet()) {
                String officeId = entry.getKey();
                List<LinkedHashMap> records = entry.getValue();

                int deptWorkDays = 0;
                int deptActualDays = 0;
                int deptLateCount = 0;

                for (LinkedHashMap record : records) {
                    deptWorkDays += getInt(record, "work_days");
                    deptActualDays += getInt(record, "actual_days");
                    if (getInt(record, "late_count") > 0) {
                        deptLateCount++;
                    }
                }

                BigDecimal attendanceRate = BigDecimal.ZERO;
                if (deptWorkDays > 0) {
                    attendanceRate = BigDecimal.valueOf(deptActualDays)
                            .divide(BigDecimal.valueOf(deptWorkDays), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }

                Map<String, Object> deptStat = new LinkedHashMap<>();
                deptStat.put("departmentId", officeId);
                deptStat.put("departmentName", officeNameMap.getOrDefault(officeId, "未知部门"));
                deptStat.put("attendanceRate", attendanceRate);
                deptStat.put("employeeCount", records.size());
                deptStat.put("lateCount", deptLateCount);
                result.add(deptStat);
            }

            // 按出勤率降序排序
            result.sort((a, b) -> {
                BigDecimal rateA = (BigDecimal) a.get("attendanceRate");
                BigDecimal rateB = (BigDecimal) b.get("attendanceRate");
                return rateB.compareTo(rateA);
            });

        } catch (Exception e) {
            logger.error("获取部门出勤统计失败", e);
            throw new RuntimeException("获取部门出勤统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取加班排行 Top N
     *
     * @param month 格式 yyyy-MM，为空则默认当月
     * @param limit 返回数量，默认 10
     * @return List：[{ userName, departmentName, overtimeHours }]
     */
    public List<Map<String, Object>> getOvertimeRanking(String month, int limit) {
        String yearMonth = normalizeMonth(month);
        if (limit <= 0) limit = 10;
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 查询月度统计数据，按加班时长降序
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.eq("a.year_month", yearMonth);
            qw.gt("a.overtime_hours", 0);
            qw.orderByDesc("a.overtime_hours");
            List<LinkedHashMap> monthlyList = zformService.findMapList(MONTHLY_FORM, qw);

            if (monthlyList == null || monthlyList.isEmpty()) {
                return result;
            }

            // 获取用户信息
            Set<String> userIds = new HashSet<>();
            for (LinkedHashMap record : monthlyList) {
                String userId = getStr(record, "user_id");
                if (StringUtil.isNotBlank(userId)) {
                    userIds.add(userId);
                }
            }

            Map<String, LinkedHashMap> userMap = getUserMap(userIds);
            Map<String, String> userOfficeMap = new HashMap<>();
            Set<String> officeIds = new HashSet<>();

            for (LinkedHashMap user : userMap.values()) {
                String userId = getStr(user, "id");
                String officeId = getStr(user, "office_id");
                if (StringUtil.isNotBlank(officeId)) {
                    userOfficeMap.put(userId, officeId);
                    officeIds.add(officeId);
                }
            }

            Map<String, String> officeNameMap = getOfficeNameMap(officeIds);

            int count = 0;
            for (LinkedHashMap record : monthlyList) {
                if (count >= limit) break;

                String userId = getStr(record, "user_id");
                LinkedHashMap user = userMap.get(userId);
                String userName = user != null ? getStr(user, "name") : "未知用户";
                String officeId = userOfficeMap.get(userId);
                String deptName = officeId != null ? officeNameMap.getOrDefault(officeId, "未知部门") : "未知部门";

                BigDecimal overtimeHours = getBigDecimal(record, "overtime_hours");

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", userId);
                item.put("userName", userName);
                item.put("departmentName", deptName);
                item.put("overtimeHours", overtimeHours);
                result.add(item);
                count++;
            }

        } catch (Exception e) {
            logger.error("获取加班排行失败", e);
            throw new RuntimeException("获取加班排行失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取考勤状态分布
     *
     * @param month 格式 yyyy-MM，为空则默认当月
     * @return Map：{ normal, late, earlyLeave, absent, leave, businessTrip }
     */
    public Map<String, Object> getStatusDistribution(String month) {
        String yearMonth = normalizeMonth(month);
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 查询月度统计数据汇总各状态人次
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.eq("a.year_month", yearMonth);
            List<LinkedHashMap> monthlyList = zformService.findMapList(MONTHLY_FORM, qw);

            int normalCount = 0;    // 正常人次（实际出勤天数）
            int lateCount = 0;      // 迟到人次
            int earlyLeaveCount = 0; // 早退人次
            int absentCount = 0;    // 缺卡/旷工人次
            int leaveCount = 0;     // 请假天数
            int businessTripCount = 0; // 出差天数

            if (monthlyList != null) {
                for (LinkedHashMap record : monthlyList) {
                    normalCount += getInt(record, "actual_days");
                    lateCount += getInt(record, "late_count");
                    earlyLeaveCount += getInt(record, "early_count");
                    absentCount += getInt(record, "absent_count");
                    leaveCount += getInt(record, "leave_days");
                    businessTripCount += getInt(record, "outside_count");
                }
            }

            result.put("normal", normalCount);
            result.put("late", lateCount);
            result.put("earlyLeave", earlyLeaveCount);
            result.put("absent", absentCount);
            result.put("leave", leaveCount);
            result.put("businessTrip", businessTripCount);
            result.put("month", yearMonth);

        } catch (Exception e) {
            logger.error("获取考勤状态分布失败", e);
            throw new RuntimeException("获取考勤状态分布失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取月度考勤趋势
     *
     * @param months 查询月数，默认 12
     * @return List：[{ month, attendanceRate, lateRate }]
     */
    public List<Map<String, Object>> getMonthlyTrend(int months) {
        if (months <= 0) months = 12;
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            YearMonth current = YearMonth.now();

            for (int i = months - 1; i >= 0; i--) {
                YearMonth ym = current.minusMonths(i);
                String yearMonth = ym.format(YM_FORMATTER);

                QueryWrapper<Zform> qw = new QueryWrapper<>();
                qw.eq("a.year_month", yearMonth);
                List<LinkedHashMap> monthlyList = zformService.findMapList(MONTHLY_FORM, qw);

                BigDecimal attendanceRate = BigDecimal.ZERO;
                BigDecimal lateRate = BigDecimal.ZERO;

                if (monthlyList != null && !monthlyList.isEmpty()) {
                    int totalWorkDays = 0;
                    int totalActualDays = 0;
                    int totalLateCount = 0;
                    int totalRecords = 0;

                    for (LinkedHashMap record : monthlyList) {
                        totalWorkDays += getInt(record, "work_days");
                        totalActualDays += getInt(record, "actual_days");
                        totalLateCount += getInt(record, "late_count");
                        totalRecords += getInt(record, "actual_days"); // 以出勤天数计算迟到率基数
                    }

                    if (totalWorkDays > 0) {
                        attendanceRate = BigDecimal.valueOf(totalActualDays)
                                .divide(BigDecimal.valueOf(totalWorkDays), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    if (totalRecords > 0) {
                        lateRate = BigDecimal.valueOf(totalLateCount)
                                .divide(BigDecimal.valueOf(totalRecords), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("month", yearMonth);
                item.put("attendanceRate", attendanceRate);
                item.put("lateRate", lateRate);
                result.add(item);
            }

        } catch (Exception e) {
            logger.error("获取月度考勤趋势失败", e);
            throw new RuntimeException("获取月度考勤趋势失败: " + e.getMessage());
        }

        return result;
    }

    // ==================== 工具方法 ====================

    /**
     * 标准化月份参数，为空则返回当月
     */
    private String normalizeMonth(String month) {
        if (StringUtil.isBlank(month)) {
            return YearMonth.now().format(YM_FORMATTER);
        }
        // 验证格式
        if (!month.matches("\\d{4}-\\d{2}")) {
            return YearMonth.now().format(YM_FORMATTER);
        }
        return month;
    }

    /**
     * 获取用户-部门映射
     */
    private Map<String, String> getUserOfficeMap(List<LinkedHashMap> monthlyList) {
        Set<String> userIds = new HashSet<>();
        for (LinkedHashMap record : monthlyList) {
            String userId = getStr(record, "user_id");
            if (StringUtil.isNotBlank(userId)) {
                userIds.add(userId);
            }
        }

        Map<String, String> userOfficeMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<Zform> userQw = new QueryWrapper<>();
            userQw.in("a.id", userIds);
            List<LinkedHashMap> userList = zformService.findMapList(USER_FORM, userQw);
            if (userList != null) {
                for (LinkedHashMap user : userList) {
                    String userId = getStr(user, "id");
                    String officeId = getStr(user, "office_id");
                    if (StringUtil.isNotBlank(userId) && StringUtil.isNotBlank(officeId)) {
                        userOfficeMap.put(userId, officeId);
                    }
                }
            }
        }
        return userOfficeMap;
    }

    /**
     * 获取部门名称映射
     */
    private Map<String, String> getOfficeNameMap(Collection<String> officeIds) {
        Map<String, String> officeNameMap = new HashMap<>();
        Set<String> uniqueIds = new HashSet<>(officeIds);
        uniqueIds.remove(null);
        uniqueIds.remove("");

        if (!uniqueIds.isEmpty()) {
            QueryWrapper<Zform> officeQw = new QueryWrapper<>();
            officeQw.in("a.id", uniqueIds);
            List<LinkedHashMap> officeList = zformService.findMapList(OFFICE_FORM, officeQw);
            if (officeList != null) {
                for (LinkedHashMap office : officeList) {
                    String id = getStr(office, "id");
                    String name = getStr(office, "name");
                    if (StringUtil.isNotBlank(id)) {
                        officeNameMap.put(id, name != null ? name : "未知部门");
                    }
                }
            }
        }
        return officeNameMap;
    }

    /**
     * 获取用户信息映射
     */
    private Map<String, LinkedHashMap> getUserMap(Set<String> userIds) {
        Map<String, LinkedHashMap> userMap = new HashMap<>();
        if (userIds != null && !userIds.isEmpty()) {
            QueryWrapper<Zform> userQw = new QueryWrapper<>();
            userQw.in("a.id", userIds);
            List<LinkedHashMap> userList = zformService.findMapList(USER_FORM, userQw);
            if (userList != null) {
                for (LinkedHashMap user : userList) {
                    String id = getStr(user, "id");
                    if (StringUtil.isNotBlank(id)) {
                        userMap.put(id, user);
                    }
                }
            }
        }
        return userMap;
    }

    private String getStr(Map map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }

    private int getInt(Map map, String key) {
        Object val = map.get(key);
        if (val == null) return 0;
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal getBigDecimal(Map map, String key) {
        Object val = map.get(key);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        }
        try {
            return new BigDecimal(val.toString().trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
