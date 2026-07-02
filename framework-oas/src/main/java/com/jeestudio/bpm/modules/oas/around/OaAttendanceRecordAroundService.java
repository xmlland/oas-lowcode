package com.jeestudio.bpm.modules.oas.around;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordAttendanceStatusEnum;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordClockInStatusEnum;
import com.jeestudio.bpm.modules.oas.enums.OaAttendanceRecordClockOutStatusEnum;
import com.jeestudio.bpm.modules.oas.service.OaAttendanceGroupService;
import com.jeestudio.bpm.service.common.ZformService;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.jeestudio.bpm.service.system.SysSettingService;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 考勤记录扩展服务，处理打卡位置、班次和考勤状态
 */
@Component("oa_attendance_recordAroundService")
public class OaAttendanceRecordAroundService implements AroundServiceI {

    protected static final Logger logger = LoggerFactory.getLogger(OaAttendanceRecordAroundService.class);

    @Autowired
    ZformService zformService;

    @Autowired
    OaAttendanceGroupService groupService;

    @Autowired
    SysSettingService sysSettingService;

    private static final String LOCATION_FORM_NO = "oa_attendance_location";

    /** 地球半径，单位：米 */
    private static final double EARTH_RADIUS = 6371000;

    private static final String SHIFT_FORM = "oa_attendance_shift";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String TIANDITU_GEOCODER_URL = "http://api.tianditu.gov.cn/geocoder";
    private static final String TDT_KEY = "tdtKey";

    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        String clockOutType = zformMap.getString("clock_out_type");
        // clock_out_type 无值 → 上班打卡；有值 → 下班打卡
        boolean isClockIn = StringUtil.isBlank(clockOutType);
        logger.info("isClockIn:{}", isClockIn);

        // 0. 自动填充班次（未配置时从考勤组获取）
        autoFillShiftId(zformMap);

        // 1. 地点判定（WiFi + GPS 围栏）
        resolveLocation(zformMap, isClockIn);

        // 2. 状态判定（根据班次规则计算迟到/早退/正常）
        resolveAttendanceStatus(zformMap, isClockIn);
    }

/*    @Override
    public void beforeSaveZform(Zform zform, String loginName, String businessKey) throws Exception {
        JSONObject map = zformService.getMapByZform(zform);
        String clockOutType = map.getString("clock_out_type");
        // clock_out_type 无值 → 上班打卡；有值 → 下班打卡
        boolean isClockIn = StringUtil.isBlank(clockOutType);
        logger.info("isClockIn:{}", isClockIn);

        // 0. 自动填充班次（未配置时从考勤组获取）
        autoFillShiftId(map);

        // 1. 地点判定（WiFi + GPS 围栏）
        resolveLocation(map, isClockIn);

        // 2. 状态判定（根据班次规则计算迟到/早退/正常）
        resolveAttendanceStatus(map, isClockIn);
        zform.setZformMap(map);
        zform = zformService.getZformFromMap(map, loginName);
    }*/

    /**
     * 自动填充班次ID：若打卡记录未传 shift_id，则从用户所属考勤组获取
     * 支持上班打卡（新记录）和下班打卡（更新记录）两种场景
     */
    private void autoFillShiftId(JSONObject map) {
        String existingShiftId = map.getString("shift_id");

        // 如果是更新记录（下班打卡），先从数据库查询原记录的 shift_id
        if (StringUtil.isBlank(existingShiftId)) {
            String recordId = map.getString("id");
            if (StringUtil.isNotBlank(recordId)) {
                try {
                    LinkedHashMap existingRecord = zformService.getMap(recordId, "oa_attendance_record");
                    if (existingRecord != null) {
                        existingShiftId = getStr(existingRecord, "shift_id");
                    }
                } catch (Exception e) {
                    logger.debug("查询原记录班次信息失败: {}", recordId);
                }
            }
        }

        if (StringUtil.isNotBlank(existingShiftId)) {
            map.put("shift_id", existingShiftId);
            return; // 已有班次，无需再查询
        }

        // 新记录或原记录也没有班次，从考勤组获取
        String userId = map.getString("user_id");
        if (StringUtil.isBlank(userId)) {
            logger.warn("打卡记录缺少 user_id，无法获取考勤组");
            return;
        }

        LinkedHashMap group = groupService.findActiveGroupByUserId(userId);
        if (group != null) {
            String shiftId = groupService.getShiftId(group);
            if (StringUtil.isNotBlank(shiftId)) {
                map.put("shift_id", shiftId);
                logger.info("自动填充班次ID: {} (用户: {})", shiftId, userId);
            }
        }
    }

    /**
     * 地点判定：WiFi 优先，未匹配时 fallback 到 GPS 围栏
     * 若 GPS 坐标存在但地址为空，调用天地图逆地理编码 API 获取地址
     */
    private void resolveLocation(JSONObject map, boolean isClockIn) {
        if (isClockIn) {
            String inWifiName = map.getString("in_wifi_name");
            String inWifiMac  = map.getString("in_wifi_mac");
            Double inLongitude = map.getDouble("clock_in_longitude");
            Double inLatitude  = map.getDouble("clock_in_latitude");
            String inLocation = map.getString("clock_in_location");

            String locationName = null;
            if (StringUtil.isNotBlank(inWifiName) && StringUtil.isNotBlank(inWifiMac)) {
                locationName = findLocationName(inWifiName, inWifiMac);
                logger.info("wifi locationName:{}", locationName);
            }
            if (StringUtil.isBlank(locationName) && inLongitude != null && inLatitude != null) {
                locationName = findLocationByGps(inLongitude, inLatitude);
                logger.info("gps locationName:{}", locationName);
            }
            // 若已有地址或围栏匹配到地址，直接写入；否则尝试天地图逆地理编码
            if (StringUtil.isNotBlank(locationName)) {
                map.put("clock_in_location", locationName);
            } else if (StringUtil.isBlank(inLocation) && inLongitude != null && inLatitude != null) {
                String address = reverseGeocode(inLongitude, inLatitude);
                if (StringUtil.isNotBlank(address)) {
                    map.put("clock_in_location", address);
                    logger.info("天地图逆编码地址: {} (lon:{}, lat:{})", address, inLongitude, inLatitude);
                }
            }
        } else {
            String outWifiName = map.getString("out_wifi_name");
            String outWifiMac  = map.getString("out_wifi_mac");
            Double outLongitude = map.getDouble("clock_out_longitude");
            Double outLatitude  = map.getDouble("clock_out_latitude");
            String outLocation = map.getString("clock_out_location");

            String locationName = null;
            if (StringUtil.isNotBlank(outWifiName) && StringUtil.isNotBlank(outWifiMac)) {
                locationName = findLocationName(outWifiName, outWifiMac);
                logger.info("wifi locationName:{}", locationName);
            }
            if (StringUtil.isBlank(locationName) && outLongitude != null && outLatitude != null) {
                locationName = findLocationByGps(outLongitude, outLatitude);
                logger.info("gps locationName:{}", locationName);
            }
            if (StringUtil.isNotBlank(locationName)) {
                map.put("clock_out_location", locationName);
            } else if (StringUtil.isBlank(outLocation) && outLongitude != null && outLatitude != null) {
                String address = reverseGeocode(outLongitude, outLatitude);
                if (StringUtil.isNotBlank(address)) {
                    map.put("clock_out_location", address);
                    logger.info("天地图逆编码地址: {} (lon:{}, lat:{})", address, outLongitude, outLatitude);
                }
            }
        }
    }

    /**
     * 天地图逆地理编码：根据经纬度获取地址
     * @return 地址字符串，失败返回 null
     */
    private String reverseGeocode(double longitude, double latitude) {
        String tk = getTiandituKey();
        if (StringUtil.isBlank(tk)) {
            logger.warn("天地图 API Key 未配置，跳过逆地理编码");
            return null;
        }
        try {
            JSONObject postData = new JSONObject();
            postData.put("lon", longitude);
            postData.put("lat", latitude);
            postData.put("ver", 1);

            HttpResponse execute = HttpUtil.createGet(TIANDITU_GEOCODER_URL)
                    .form("postStr", postData.toJSONString())
                    .form("tk", tk)
                    .form("type", "geocode")
                    .execute();

            if (execute.isOk()) {
                JSONObject json = JSONObject.parseObject(execute.body());
                if ("0".equals(json.getString("status"))) {
                    JSONObject result = json.getJSONObject("result");
                    if (result != null) {
                        String address = result.getString("formatted_address");
                        if (StringUtil.isNotBlank(address)) {
                            return address;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("天地图逆地理编码失败: lon={}, lat={}", longitude, latitude, e);
        }
        return null;
    }

    /**
     * 考勤状态实时判定
     * 支持多班次：根据打卡时间匹配最近的班次时段
     */
    private void resolveAttendanceStatus(JSONObject map, boolean isClockIn) {
        String shiftId = map.getString("shift_id");
        if (StringUtil.isBlank(shiftId)) {
            logger.warn("未配置班次，跳过状态判定");
            return;
        }

        // 查询班次信息
        LinkedHashMap shift = findShiftById(shiftId);
        if (shift == null) {
            logger.warn("班次不存在: {}", shiftId);
            return;
        }

        // 解析班次时段（支持多班次）
        List<ShiftPeriod> periods = parseShiftPeriods(shift);
        if (periods.isEmpty()) {
            logger.warn("班次时段配置不完整");
            return;
        }

        if (isClockIn) {
            // 上班打卡：匹配最近的上班开始时间
            String clockInTimeStr = map.getString("clock_in_time");
            LocalTime clockInTime = parseDateTime(clockInTimeStr);
            if (clockInTime == null) {
                logger.warn("上班打卡时间解析失败: {}", clockInTimeStr);
                return;
            }

            ShiftPeriod matched = findNearestPeriod(clockInTime, periods, true);
            if (matched == null) {
                logger.warn("未匹配到合适的上班时段");
                return;
            }

            String clockInStatus;
            if (clockInTime.isAfter(matched.workStart)) {
                clockInStatus = OaAttendanceRecordClockInStatusEnum.CD.getCode(); // 迟到
            } else {
                clockInStatus = OaAttendanceRecordClockInStatusEnum.ZC.getCode(); // 正常
            }
            map.put("clock_in_status", clockInStatus);
            logger.info("上班打卡状态: {}, 时间: {}, 规定: {}", clockInStatus, clockInTime, matched.workStart);

            // 记录匹配的班次时段，供下班打卡使用
            map.put("_matched_work_start", matched.workStart.toString());
            map.put("_matched_work_end", matched.workEnd.toString());

            // 更新 attendance_status（仅上班打卡时，可能后续会覆盖）
            map.put("attendance_status", deriveAttendanceStatus(clockInStatus, null));

        } else {
            // 下班打卡：匹配最近的下班结束时间
            String clockOutTimeStr = map.getString("clock_out_time");
            LocalTime clockOutTime = parseDateTime(clockOutTimeStr);
            if (clockOutTime == null) {
                logger.warn("下班打卡时间解析失败: {}", clockOutTimeStr);
                return;
            }

            ShiftPeriod matched = findNearestPeriod(clockOutTime, periods, false);
            if (matched == null) {
                logger.warn("未匹配到合适的下班时段");
                return;
            }

            String clockOutStatus;
            if (clockOutTime.isBefore(matched.workEnd)) {
                clockOutStatus = OaAttendanceRecordClockOutStatusEnum.ZT.getCode(); // 早退
            } else {
                clockOutStatus = OaAttendanceRecordClockOutStatusEnum.ZC.getCode(); // 正常
            }
            map.put("clock_out_status", clockOutStatus);
            logger.info("下班打卡状态: {}, 时间: {}, 规定: {}", clockOutStatus, clockOutTime, matched.workEnd);

            // 综合生成 attendance_status
            String clockInStatus = map.getString("clock_in_status");
            String attendanceStatus = deriveAttendanceStatus(clockInStatus, clockOutStatus);
            map.put("attendance_status", attendanceStatus);
            logger.info("综合考勤状态: {}", attendanceStatus);
        }
    }

    /**
     * 班次时段对象
     */
    private static class ShiftPeriod {
        LocalTime workStart;
        LocalTime workEnd;
        LocalTime restStart;
        LocalTime restEnd;

        ShiftPeriod(LocalTime workStart, LocalTime workEnd, LocalTime restStart, LocalTime restEnd) {
            this.workStart = workStart;
            this.workEnd = workEnd;
            this.restStart = restStart;
            this.restEnd = restEnd;
        }
    }

    /**
     * 解析班次时段（支持多班次：work_start/work_end、work_start_2/work_end_2）
     */
    private List<ShiftPeriod> parseShiftPeriods(LinkedHashMap shift) {
        List<ShiftPeriod> periods = new java.util.ArrayList<>();

        // 第一时段
        LocalTime ws1 = parseTime(getStr(shift, "work_start"));
        LocalTime we1 = parseTime(getStr(shift, "work_end"));
        LocalTime rs1 = parseTime(getStr(shift, "rest_start"));
        LocalTime re1 = parseTime(getStr(shift, "rest_end"));
        if (ws1 != null && we1 != null) {
            periods.add(new ShiftPeriod(ws1, we1, rs1, re1));
        }

        // 第二时段（多班次支持）
        LocalTime ws2 = parseTime(getStr(shift, "work_start_2"));
        LocalTime we2 = parseTime(getStr(shift, "work_end_2"));
        LocalTime rs2 = parseTime(getStr(shift, "rest_start_2"));
        LocalTime re2 = parseTime(getStr(shift, "rest_end_2"));
        if (ws2 != null && we2 != null) {
            periods.add(new ShiftPeriod(ws2, we2, rs2, re2));
        }

        return periods;
    }

    /**
     * 查找最近的班次时段
     * @param clockTime 打卡时间
     * @param periods 班次时段列表
     * @param isClockIn true=上班（匹配 workStart），false=下班（匹配 workEnd）
     */
    private ShiftPeriod findNearestPeriod(LocalTime clockTime, List<ShiftPeriod> periods, boolean isClockIn) {
        ShiftPeriod nearest = null;
        long minDiff = Long.MAX_VALUE;

        for (ShiftPeriod p : periods) {
            long diff;
            if (isClockIn) {
                // 上班：计算打卡时间与 workStart 的绝对差值
                diff = Math.abs(java.time.Duration.between(p.workStart, clockTime).toMinutes());
            } else {
                // 下班：计算打卡时间与 workEnd 的绝对差值
                diff = Math.abs(java.time.Duration.between(p.workEnd, clockTime).toMinutes());
            }

            // 跨天班次处理（如夜班 22:00-06:00）
            if (p.workEnd.isBefore(p.workStart) && !isClockIn) {
                // 下班时间跨天，用 24:00 后的时间计算
                LocalTime adjustedEnd = p.workEnd.plusHours(24);
                LocalTime adjustedClock = clockTime.isBefore(p.workStart) ? clockTime.plusHours(24) : clockTime;
                diff = Math.abs(java.time.Duration.between(adjustedEnd, adjustedClock).toMinutes());
            }

            if (diff < minDiff) {
                minDiff = diff;
                nearest = p;
            }
        }

        return nearest;
    }

    /**
     * 根据上下班状态推导最终考勤状态
     */
    private String deriveAttendanceStatus(String clockInStatus, String clockOutStatus) {
        String zcIn  = OaAttendanceRecordClockInStatusEnum.ZC.getCode();
        String cdIn  = OaAttendanceRecordClockInStatusEnum.CD.getCode();
        String zcOut = OaAttendanceRecordClockOutStatusEnum.ZC.getCode();
        String ztOut = OaAttendanceRecordClockOutStatusEnum.ZT.getCode();

        // 仅上班打卡时
        if (clockOutStatus == null) {
            if (cdIn.equals(clockInStatus)) {
                return OaAttendanceRecordAttendanceStatusEnum.CD.getCode();
            }
            return OaAttendanceRecordAttendanceStatusEnum.ZC.getCode();
        }

        // 上下班都有
        boolean isNormalIn = zcIn.equals(clockInStatus);
        boolean isNormalOut = zcOut.equals(clockOutStatus);

        if (isNormalIn && isNormalOut) {
            return OaAttendanceRecordAttendanceStatusEnum.ZC.getCode();
        }
        if (!isNormalIn && isNormalOut) {
            return OaAttendanceRecordAttendanceStatusEnum.CD.getCode();
        }
        if (isNormalIn && !isNormalOut) {
            return OaAttendanceRecordAttendanceStatusEnum.ZT.getCode();
        }
        return OaAttendanceRecordAttendanceStatusEnum.CDZT.getCode();
    }

    /**
     * 根据班次ID查询班次信息
     */
    private LinkedHashMap findShiftById(String shiftId) {
        QueryWrapper<Zform> qw = new QueryWrapper<>();
        qw.eq("a.id", shiftId);
        List<LinkedHashMap> list = zformService.findMapList(SHIFT_FORM, qw);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 解析完整日期时间字符串中的时间部分（如 "2026-05-24 09:30:00"）
     */
    private LocalTime parseDateTime(String dateTimeStr) {
        if (StringUtil.isBlank(dateTimeStr)) return null;
        try {
            int spaceIdx = dateTimeStr.indexOf(' ');
            String timePart = spaceIdx >= 0 ? dateTimeStr.substring(spaceIdx + 1).trim() : dateTimeStr.trim();
            if (timePart.length() >= 8) {
                return LocalTime.parse(timePart.substring(0, 8));
            }
        } catch (Exception e) {
            logger.debug("时间解析失败: {}", dateTimeStr);
        }
        return null;
    }

    /**
     * 解析班次时间字段（支持 HH:mm 或 HH:mm:ss）
     */
    private LocalTime parseTime(String timeStr) {
        if (StringUtil.isBlank(timeStr)) return null;
        try {
            String t = timeStr.trim();
            if (t.length() == 5) {
                return LocalTime.parse(t + ":00");
            }
            if (t.length() >= 8) {
                return LocalTime.parse(t.substring(0, 8));
            }
        } catch (Exception e) {
            logger.debug("班次时间解析失败: {}", timeStr);
        }
        return null;
    }

    /**
     * 根据 wifi_name 和 wifi_mac 查询考勤地点名称，找不到返回 null
     */
    private String findLocationName(String wifiName, String wifiMac) {
        QueryWrapper<Zform> qw = new QueryWrapper<>();
        qw.eq("a.wifi_name", wifiName);
        qw.eq("a.wifi_mac", wifiMac);
        List<LinkedHashMap> list = zformService.findMapList(LOCATION_FORM_NO, qw);
        if (list != null && !list.isEmpty()) {
            Object locationName = list.get(0).get("location_name");
            return locationName != null ? locationName.toString() : null;
        }
        return null;
    }

    /**
     * 根据 GPS 坐标查找最近的考勤地点（在围栏范围内）
     * 按距离升序，返回第一个在 radius 范围内的地点名称
     */
    private String findLocationByGps(double longitude, double latitude) {
        List<LinkedHashMap> allLocations = zformService.findMapList(LOCATION_FORM_NO);
        if (allLocations == null || allLocations.isEmpty()) {
            return null;
        }

        String nearestName = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LinkedHashMap loc : allLocations) {
            Object lonObj = loc.get("longitude");
            Object latObj = loc.get("latitude");
            Object radiusObj = loc.get("radius");
            if (lonObj == null || latObj == null || radiusObj == null) {
                continue;
            }

            double locLon, locLat, radius;
            try {
                locLon = new BigDecimal(lonObj.toString()).doubleValue();
                locLat = new BigDecimal(latObj.toString()).doubleValue();
                radius = new BigDecimal(radiusObj.toString()).doubleValue();
            } catch (NumberFormatException e) {
                continue;
            }

            double distance = calculateDistance(longitude, latitude, locLon, locLat);
            if (distance <= radius && distance < nearestDistance) {
                nearestDistance = distance;
                Object locationName = loc.get("location_name");
                nearestName = locationName != null ? locationName.toString() : null;
            }
        }

        return nearestName;
    }

    /**
     * 从 LinkedHashMap 中安全获取字符串值
     */
    private String getStr(LinkedHashMap map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }

    /**
     * 获取天地图 API Key
     * 从 sys_setting 中读取 tdtKey 配置项，支持 PATH: 前缀读取环境变量
     */
    private String getTiandituKey() {
        try {
            String value = sysSettingService.getSettingValueByKey(TDT_KEY, "");
            if (value != null && value.startsWith("PATH:")) {
                String envVar = value.substring(5).trim();
                String envValue = System.getenv(envVar);
                if (envValue != null && !envValue.isEmpty()) {
                    value = envValue;
                } else {
                    logger.warn("环境变量 {} 未配置", envVar);
                    value = "";
                }
            }
            return value != null ? value : "";
        } catch (Exception e) {
            logger.error("获取天地图Key失败", e);
            return "";
        }
    }

    /**
     * Haversine 公式计算两点间距离（单位：米）
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
