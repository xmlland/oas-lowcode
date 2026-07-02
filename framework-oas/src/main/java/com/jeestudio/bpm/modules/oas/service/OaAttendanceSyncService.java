package com.jeestudio.bpm.modules.oas.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordAttendanceStatusEnum;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 考勤同步服务
 * 每日定时同步请假、出差审批记录到考勤表，避免重复打卡
 */
@Service
public class OaAttendanceSyncService {

    private static final Logger logger = LoggerFactory.getLogger(OaAttendanceSyncService.class);

    private static final String RECORD_FORM = "oa_attendance_record";
    private static final String LEAVE_FORM = "oa_attendance_leave";   // 请假表单
    private static final String TRIP_FORM = "oa_attendance_trip";     // 出差表单
    private static final String SYSTEM_USER = "system";

    @Autowired
    private ZformService zformService;

    /**
     * 每日凌晨1点执行同步
     * 扫描前一天的请假/出差记录，生成对应的考勤记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledSync() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("开始同步{}的请假/出差记录到考勤表...", yesterday);

        int leaveCount = syncLeaveRecords(yesterday);
        int tripCount = syncTripRecords(yesterday);

        logger.info("同步完成，请假记录{}条，出差记录{}条", leaveCount, tripCount);
    }

    /**
     * 同步请假记录
     */
    private int syncLeaveRecords(LocalDate date) {
        int count = 0;
        try {
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.le("a.start_date", Timestamp.valueOf(date.atTime(LocalTime.MAX)));
            qw.ge("a.end_date", Timestamp.valueOf(date.atStartOfDay()));
            qw.eq("a.status", "1"); // 审批通过

            List<LinkedHashMap> leaveList = zformService.findMapList(LEAVE_FORM, qw);
            if (leaveList == null || leaveList.isEmpty()) {
                return 0;
            }

            for (LinkedHashMap leave : leaveList) {
                String userId = getStr(leave, "user_id");
                if (StringUtil.isBlank(userId)) continue;

                // 检查该日是否已有考勤记录
                if (hasAttendanceRecord(userId, date)) {
                    logger.debug("用户[{}]在{}已有考勤记录，跳过请假同步", userId, date);
                    continue;
                }

                // 生成请假考勤记录
                createAttendanceRecord(userId, date, OaAttendanceRecordAttendanceStatusEnum.QJ.getCode(), leave);
                count++;
            }
        } catch (Exception e) {
            logger.error("同步请假记录失败", e);
        }
        return count;
    }

    /**
     * 同步出差记录
     */
    private int syncTripRecords(LocalDate date) {
        int count = 0;
        try {
            QueryWrapper<Zform> qw = new QueryWrapper<>();
            qw.le("a.start_date", Timestamp.valueOf(date.atTime(LocalTime.MAX)));
            qw.ge("a.end_date", Timestamp.valueOf(date.atStartOfDay()));
            qw.eq("a.status", "1"); // 审批通过

            List<LinkedHashMap> tripList = zformService.findMapList(TRIP_FORM, qw);
            if (tripList == null || tripList.isEmpty()) {
                return 0;
            }

            for (LinkedHashMap trip : tripList) {
                String userId = getStr(trip, "user_id");
                if (StringUtil.isBlank(userId)) continue;

                if (hasAttendanceRecord(userId, date)) {
                    logger.debug("用户[{}]在{}已有考勤记录，跳过出差同步", userId, date);
                    continue;
                }

                createAttendanceRecord(userId, date, OaAttendanceRecordAttendanceStatusEnum.CC.getCode(), trip);
                count++;
            }
        } catch (Exception e) {
            logger.error("同步出差记录失败", e);
        }
        return count;
    }

    /**
     * 检查用户在某日是否已有考勤记录
     */
    private boolean hasAttendanceRecord(String userId, LocalDate date) {
        QueryWrapper<Zform> qw = new QueryWrapper<>();
        qw.eq("a.user_id", userId);
        qw.ge("a.attendance_date", Timestamp.valueOf(date.atStartOfDay()));
        qw.le("a.attendance_date", Timestamp.valueOf(date.atTime(LocalTime.MAX)));
        List<LinkedHashMap> list = zformService.findMapList(RECORD_FORM, qw);
        return list != null && !list.isEmpty();
    }

    /**
     * 创建考勤记录
     */
    private void createAttendanceRecord(String userId, LocalDate date, String status, LinkedHashMap source) {
        try {
            JSONObject data = new JSONObject();
            data.put("formNo", RECORD_FORM);
            data.put("user_id", userId);
            data.put("attendance_date", date.atStartOfDay().toString());
            data.put("attendance_status", status);
            data.put("create_by", SYSTEM_USER);
            data.put("create_date", LocalDateTime.now().toString());

            // 复制来源表单的相关字段
            String userName = getStr(source, "user_name");
            if (StringUtil.isNotBlank(userName)) {
                data.put("user_name", userName);
            }
            String deptId = getStr(source, "dept_id");
            if (StringUtil.isNotBlank(deptId)) {
                data.put("dept_id", deptId);
            }
            String deptName = getStr(source, "dept_name");
            if (StringUtil.isNotBlank(deptName)) {
                data.put("dept_name", deptName);
            }

            Zform zform = zformService.getZformFromMap(data, SYSTEM_USER);
            zformService.saveZform(zform, SYSTEM_USER, "");
            logger.info("生成考勤记录: 用户={}, 日期={}, 状态={}", userId, date, status);
        } catch (Exception e) {
            logger.error("生成考勤记录失败: 用户={}, 日期={}", userId, date, e);
        }
    }

    private String getStr(LinkedHashMap map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
