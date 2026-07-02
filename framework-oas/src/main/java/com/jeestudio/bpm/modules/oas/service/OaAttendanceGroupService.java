package com.jeestudio.bpm.modules.oas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 考勤组服务
 * 管理用户与班次、考勤地点的关联关系
 */
@Service
public class OaAttendanceGroupService {

    private static final Logger logger = LoggerFactory.getLogger(OaAttendanceGroupService.class);

    private static final String GROUP_FORM = "oa_attendance_group";
    private static final String GROUP_USER_FORM = "oa_attendance_group_user";

    @Autowired
    private ZformService zformService;

    /**
     * 根据用户ID查询其当前生效的考勤组信息
     *
     * @param userId 用户ID
     * @return 考勤组记录（含 shift_id、location_ids 等），未找到返回 null
     */
    public LinkedHashMap findActiveGroupByUserId(String userId) {
        if (StringUtil.isBlank(userId)) {
            return null;
        }

        try {
            // 1. 查询用户关联的考勤组
            QueryWrapper<Zform> guQw = new QueryWrapper<>();
            guQw.eq("a.user_id", userId);
            List<LinkedHashMap> groupUserList = zformService.findMapList(GROUP_USER_FORM, guQw);

            if (groupUserList == null || groupUserList.isEmpty()) {
                logger.debug("用户[{}]未关联任何考勤组", userId);
                return null;
            }

            // 2. 取第一个关联的考勤组（后续可扩展为按生效日期/优先级选择）
            String groupId = getStr(groupUserList.get(0), "group_id");
            if (StringUtil.isBlank(groupId)) {
                return null;
            }

            // 3. 查询考勤组详情
            QueryWrapper<Zform> gQw = new QueryWrapper<>();
            gQw.eq("a.id", groupId);
            List<LinkedHashMap> groupList = zformService.findMapList(GROUP_FORM, gQw);

            if (groupList != null && !groupList.isEmpty()) {
                return groupList.get(0);
            }

        } catch (Exception e) {
            logger.error("查询用户[{}]考勤组失败", userId, e);
        }

        return null;
    }

    /**
     * 获取考勤组的班次ID
     */
    public String getShiftId(LinkedHashMap group) {
        return group != null ? getStr(group, "shift_id") : null;
    }

    /**
     * 获取考勤组的地点ID列表（逗号分隔）
     */
    public String getLocationIds(LinkedHashMap group) {
        return group != null ? getStr(group, "location_ids") : null;
    }

    private String getStr(LinkedHashMap map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
