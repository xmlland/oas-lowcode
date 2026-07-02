package com.jeestudio.bpm.modules.oas.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordAttendanceStatusEnum;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordClockOutStatusEnum;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 月度考勤统计服务
 * 每月1日凌晨2点自动统计上月考勤数据，也支持手动触发
 */
@Service
@EnableScheduling
public class OaAttendanceMonthlyService {

    private static final Logger logger = LoggerFactory.getLogger(OaAttendanceMonthlyService.class);

    private static final String RECORD_FORM = "oa_attendance_record";
    private static final String MONTHLY_FORM = "oa_attendance_monthly";
    private static final String HOLIDAY_FORM = "oa_attendance_holiday";
    private static final String SHIFT_FORM = "oa_attendance_shift";
    private static final String SYSTEM_USER = "guest";

    private static final DateTimeFormatter YM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ZformService zformService;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_LOCK_KEY = "attendance:monthly:lock";
    private static final long LOCK_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 每月1日凌晨2点自动统计上月考勤（带分布式锁）
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void scheduledMonthlyStatistics() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String yearMonth = lastMonth.format(YM_FORMATTER);

        // 尝试获取分布式锁
        if (!acquireLock(yearMonth)) {
            logger.warn("月度考勤统计[{}]已在执行中，跳过本次调度", yearMonth);
            return;
        }

        try {
            logger.info("开始自动统计{}月度考勤...", yearMonth);
            ResultJson result = calculateMonthlyStatistics(yearMonth);
            logger.info("月度考勤统计完成: {}", result.getMsg());
        } finally {
            releaseLock(yearMonth);
        }
    }

    /**
     * 尝试获取分布式锁
     */
    private boolean acquireLock(String yearMonth) {
        if (redisTemplate == null) {
            return true; // 无Redis时直接执行
        }
        String key = REDIS_LOCK_KEY + ":" + yearMonth;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放分布式锁
     */
    private void releaseLock(String yearMonth) {
        if (redisTemplate != null) {
            String key = REDIS_LOCK_KEY + ":" + yearMonth;
            redisTemplate.delete(key);
        }
    }

    /**
     * 核心统计方法（支持增量统计）
     *
     * @param yearMonth 格式 yyyy-MM
     * @return ResultJson
     */
    public ResultJson calculateMonthlyStatistics(String yearMonth) {
        return calculateMonthlyStatistics(yearMonth, false);
    }

    /**
     * 核心统计方法
     *
     * @param yearMonth 格式 yyyy-MM
     * @param forceFull 是否强制全量重算（true=全量，false=增量）
     * @return ResultJson
     */
    public ResultJson calculateMonthlyStatistics(String yearMonth, boolean forceFull) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, YM_FORMATTER);
            LocalDate firstDay = ym.atDay(1);
            LocalDate lastDay = ym.atEndOfMonth();

            // 1. 计算应出勤天数 = 工作日 - 节假日
            int workDays = calculateWorkDays(firstDay, lastDay, ym.getYear());

            // 2. 查询月度所有考勤记录，使用 Timestamp 比较
            QueryWrapper<Zform> recordQw = new QueryWrapper<>();
            recordQw.ge("a.attendance_date", Timestamp.valueOf(firstDay.atStartOfDay()));
            recordQw.le("a.attendance_date", Timestamp.valueOf(lastDay.atTime(LocalTime.MAX)));

            // 增量模式：只查询有变更的记录（update_date 在上次统计之后）
            if (!forceFull) {
                LocalDateTime lastCalcTime = getLastCalcTime(yearMonth);
                if (lastCalcTime != null) {
                    recordQw.ge("a.update_date", Timestamp.valueOf(lastCalcTime));
                    logger.info("增量统计模式，上次统计时间: {}", lastCalcTime);
                } else {
                    logger.info("首次统计，执行全量统计");
                }
            } else {
                logger.info("强制全量统计模式");
            }

            List<LinkedHashMap> allRecords = zformService.findMapList(RECORD_FORM, recordQw);

            if (allRecords == null || allRecords.isEmpty()) {
                return ResultJson.success("无考勤记录需要统计");
            }

            // 3. 按 user_id（打卡人）分组
            Map<String, List<LinkedHashMap>> userRecordsMap = new LinkedHashMap<>();
            for (LinkedHashMap record : allRecords) {
                String userId = getStr(record, "user_id");
                if (StringUtil.isNotBlank(userId)) {
                    userRecordsMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(record);
                }
            }

            // 4. 预加载所有班次信息
            Map<String, LinkedHashMap> shiftMap = loadShifts();

            // 5. 逐用户统计并保存
            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, List<LinkedHashMap>> entry : userRecordsMap.entrySet()) {
                String userId = entry.getKey();
                List<LinkedHashMap> records = entry.getValue();
                try {
                    processUserMonthly(userId, yearMonth, records, workDays, shiftMap);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    logger.error("统计用户[{}]月度考勤失败: {}", userId, e.getMessage(), e);
                }
            }

            String msg = String.format("统计完成，成功%d人，失败%d人", successCount, failCount);
            return ResultJson.success(msg);
        } catch (DateTimeParseException e) {
            return ResultJson.failed("日期格式错误，请使用yyyy-MM格式");
        } catch (Exception e) {
            logger.error("月度考勤统计异常", e);
            return ResultJson.failed("统计异常: " + e.getMessage());
        }
    }

    /**
     * 计算应出勤天数 = 工作日数 - 落在工作日的节假日数 + 落在周末的补班日数
     */
    private int calculateWorkDays(LocalDate firstDay, LocalDate lastDay, int year) {
        // 统计月内周一到周五天数
        int weekdays = 0;
        LocalDate date = firstDay;
        while (!date.isAfter(lastDay)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            date = date.plusDays(1);
        }

        // 查询该年份节假日
        QueryWrapper<Zform> holidayQw = new QueryWrapper<>();
        holidayQw.eq("a.year_", year);
        List<LinkedHashMap> holidays = zformService.findMapList(HOLIDAY_FORM, holidayQw);

        int holidayOnWeekday = 0;
        int makeupOnWeekend = 0;
        if (holidays != null) {
            for (LinkedHashMap holiday : holidays) {
                LocalDate holidayDate = parseDate(holiday.get("holiday_date"));
                if (holidayDate != null
                        && !holidayDate.isBefore(firstDay)
                        && !holidayDate.isAfter(lastDay)) {
                    String type = getStr(holiday, "type");
                    if (isMakeupWorkday(type)) {
                        // 补班日：若在周末，计入工作日
                        DayOfWeek dow = holidayDate.getDayOfWeek();
                        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                            makeupOnWeekend++;
                        }
                    } else {
                        // 节假日：若在工作日，从工作日扣除
                        DayOfWeek dow = holidayDate.getDayOfWeek();
                        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                            holidayOnWeekday++;
                        }
                    }
                }
            }
        }

        return weekdays - holidayOnWeekday + makeupOnWeekend;
    }

    /**
     * 判断是否为补班日，兼容多种字段值格式
     */
    private boolean isMakeupWorkday(String type) {
        if (type == null) return false;
        String t = type.trim();
        return t.equals("补班") || t.equalsIgnoreCase("workday") || t.equals("1");
    }

    /**
     * 预加载所有班次，以 id 为 key
     */
    private Map<String, LinkedHashMap> loadShifts() {
        Map<String, LinkedHashMap> shiftMap = new HashMap<>();
        List<LinkedHashMap> shifts = zformService.findMapList(SHIFT_FORM);
        if (shifts != null) {
            for (LinkedHashMap shift : shifts) {
                String id = getStr(shift, "id");
                if (StringUtil.isNotBlank(id)) {
                    shiftMap.put(id, shift);
                }
            }
        }
        return shiftMap;
    }

    /**
     * 处理单个用户的月度统计
     */
    private void processUserMonthly(String userId, String yearMonth,
                                     List<LinkedHashMap> records, int workDays,
                                     Map<String, LinkedHashMap> shiftMap) throws Exception {
        int actualDays = 0;
        int lateCount = 0;
        int earlyCount = 0;
        int absentDays = 0;
        int leaveDays = 0;
        int repairCount = 0;
        int outsideCount = 0;
        int missingPunchCount = 0;
        long totalLateMinutes = 0;
        long totalEarlyMinutes = 0;
        BigDecimal totalWorkHours = BigDecimal.ZERO;

        for (LinkedHashMap record : records) {
            String status = getStr(record, "attendance_status");
            String clockInStatus = getStr(record, "clock_in_status");
            String clockOutStatus = getStr(record, "clock_out_status");

            String zcCode = OaAttendanceRecordAttendanceStatusEnum.ZC.getCode();
            String cdCode = OaAttendanceRecordAttendanceStatusEnum.CD.getCode();
            String ztCode = OaAttendanceRecordAttendanceStatusEnum.ZT.getCode();
            String cdztCode = OaAttendanceRecordAttendanceStatusEnum.CDZT.getCode();
            String qkCode = OaAttendanceRecordAttendanceStatusEnum.QK.getCode();
            String qjCode = OaAttendanceRecordAttendanceStatusEnum.QJ.getCode();
            String ccCode = OaAttendanceRecordAttendanceStatusEnum.CC.getCode();
            String bkCode = OaAttendanceRecordClockOutStatusEnum.BK.getCode();

            // 实际出勤: 正常、迟到、早退、迟到+早退、缺卡
            if (zcCode.equals(status) || cdCode.equals(status)
                    || ztCode.equals(status) || cdztCode.equals(status)
                    || qkCode.equals(status)) {
                actualDays++;
            }

            // 迟到次数
            if (cdCode.equals(status) || cdztCode.equals(status)) {
                lateCount++;
            }

            // 早退次数
            if (ztCode.equals(status) || cdztCode.equals(status)) {
                earlyCount++;
            }

            // 缺卡
            if (qkCode.equals(status)) {
                missingPunchCount++;
            }

            // 请假
            if (qjCode.equals(status)) {
                leaveDays++;
            }

            // 补卡（签到补卡或签退补卡）
            if (bkCode.equals(clockInStatus) || bkCode.equals(clockOutStatus)) {
                repairCount++;
            }

            // 出差
            if (ccCode.equals(status)) {
                outsideCount++;
            }

            // 从班次信息计算迟到/早退分钟数及工时
            String shiftId = getStr(record, "shift_id");
            LinkedHashMap shift = StringUtil.isNotBlank(shiftId) ? shiftMap.get(shiftId) : null;
            if (shift != null) {
                LocalTime workStart = parseTime(getStr(shift, "work_start"));
                LocalTime workEnd = parseTime(getStr(shift, "work_end"));
                LocalTime restStart = parseTime(getStr(shift, "rest_start"));
                LocalTime restEnd = parseTime(getStr(shift, "rest_end"));

                // 迟到分钟数 = 实际签到时间 - 规定上班时间
                if ((cdCode.equals(status) || cdztCode.equals(status)) && workStart != null) {
                    LocalTime clockInTime = parseTime(record.get("clock_in_time"));
                    if (clockInTime != null && clockInTime.isAfter(workStart)) {
                        totalLateMinutes += Duration.between(workStart, clockInTime).toMinutes();
                    }
                }

                // 早退分钟数 = 规定下班时间 - 实际签退时间
                if ((ztCode.equals(status) || cdztCode.equals(status)) && workEnd != null) {
                    LocalTime clockOutTime = parseTime(record.get("clock_out_time"));
                    if (clockOutTime != null && clockOutTime.isBefore(workEnd)) {
                        totalEarlyMinutes += Duration.between(clockOutTime, workEnd).toMinutes();
                    }
                }

                // 计算当日有效工时
                LocalTime clockIn = parseTime(record.get("clock_in_time"));
                LocalTime clockOut = parseTime(record.get("clock_out_time"));
                if (clockIn != null && clockOut != null && workStart != null && workEnd != null) {
                    // 有效开始 = max(实际签到, 规定上班)
                    LocalTime effectiveStart = clockIn.isAfter(workStart) ? clockIn : workStart;
                    // 有效结束 = min(实际签退, 规定下班)
                    LocalTime effectiveEnd = clockOut.isBefore(workEnd) ? clockOut : workEnd;

                    if (effectiveEnd.isAfter(effectiveStart)) {
                        long workMinutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();
                        // 扣除午休时间（取重叠部分）
                        if (restStart != null && restEnd != null) {
                            LocalTime overlapStart = effectiveStart.isAfter(restStart) ? effectiveStart : restStart;
                            LocalTime overlapEnd = effectiveEnd.isBefore(restEnd) ? effectiveEnd : restEnd;
                            if (overlapEnd.isAfter(overlapStart)) {
                                workMinutes -= Duration.between(overlapStart, overlapEnd).toMinutes();
                            }
                        }
                        if (workMinutes > 0) {
                            totalWorkHours = totalWorkHours.add(
                                    BigDecimal.valueOf(workMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                        }
                    }
                }
            }
        }

        // 旷工天数 = 应出勤 - 实际出勤 - 请假 - 出差
        absentDays = Math.max(0, workDays - actualDays - leaveDays - outsideCount);

        // 查询是否已存在该用户该月汇总记录（有则更新，无则新增）
        QueryWrapper<Zform> existQw = new QueryWrapper<>();
        existQw.eq("a.user_id", userId);
        existQw.eq("a.year_month", yearMonth);
        List<LinkedHashMap> existingList = zformService.findMapList(MONTHLY_FORM, existQw);

        // 构建月度汇总数据
        JSONObject data = new JSONObject();
        data.put("formNo", MONTHLY_FORM);
        if (existingList != null && !existingList.isEmpty()) {
            // 已有记录，设置 id 以走更新逻辑
            data.put("id", existingList.get(0).get("id"));
        }
        data.put("user_id", userId);
        data.put("year_month", yearMonth);
        data.put("work_days", workDays);
        data.put("actual_days", actualDays);
        data.put("late_count", lateCount);
        data.put("late_minutes", totalLateMinutes);
        data.put("early_count", earlyCount);
        data.put("early_minutes", totalEarlyMinutes);
        data.put("absent_count", absentDays);
        data.put("leave_days", leaveDays);
        data.put("repair_count", repairCount);
        data.put("outside_count", outsideCount);
        data.put("missing_punch_count", missingPunchCount);
        data.put("work_hours", totalWorkHours);
        data.put("overtime_hours", BigDecimal.ZERO);

        Zform zform = zformService.getZformFromMap(data, SYSTEM_USER);
        zformService.saveZform(zform, SYSTEM_USER, "");

        // 记录本次统计时间（用于增量统计）
        recordCalcTime(yearMonth);
    }

    /**
     * 获取上次统计时间（用于增量模式）
     */
    private LocalDateTime getLastCalcTime(String yearMonth) {
        if (redisTemplate == null) return null;
        String key = "attendance:monthly:last_calc:" + yearMonth;
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtil.isNotBlank(value)) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception e) {
                logger.debug("解析上次统计时间失败: {}", value);
            }
        }
        return null;
    }

    /**
     * 记录本次统计时间
     */
    private void recordCalcTime(String yearMonth) {
        if (redisTemplate == null) return;
        String key = "attendance:monthly:last_calc:" + yearMonth;
        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString());
    }

    // ==================== 工具方法 ====================

    private String getStr(Map map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }

    private LocalDate parseDate(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Date) {
            return ((java.sql.Date) val).toLocalDate();
        }
        if (val instanceof Timestamp) {
            return ((Timestamp) val).toLocalDateTime().toLocalDate();
        }
        if (val instanceof Date) {
            return ((Date) val).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        try {
            String str = val.toString().trim();
            if (str.length() >= 10) {
                return LocalDate.parse(str.substring(0, 10));
            }
        } catch (Exception e) {
            logger.debug("日期解析失败: {}", val);
        }
        return null;
    }

    private LocalTime parseTime(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp) {
            return ((Timestamp) val).toLocalDateTime().toLocalTime();
        }
        if (val instanceof java.sql.Time) {
            return ((java.sql.Time) val).toLocalTime();
        }
        try {
            String str = val.toString().trim();
            if (str.isEmpty()) return null;
            // "HH:mm"
            if (str.length() == 5 && str.charAt(2) == ':') {
                return LocalTime.parse(str, TIME_FORMATTER);
            }
            // "HH:mm:ss"
            if (str.length() == 8 && str.charAt(2) == ':') {
                return LocalTime.parse(str);
            }
            // 完整日期时间字符串，取时间部分
            if (str.length() > 10 && str.contains(" ")) {
                String timePart = str.substring(str.indexOf(' ') + 1).trim();
                if (timePart.length() >= 8) {
                    return LocalTime.parse(timePart.substring(0, 8));
                } else if (timePart.length() >= 5) {
                    return LocalTime.parse(timePart.substring(0, 5), TIME_FORMATTER);
                }
            }
        } catch (Exception e) {
            logger.debug("时间解析失败: {}", val);
        }
        return null;
    }
}
