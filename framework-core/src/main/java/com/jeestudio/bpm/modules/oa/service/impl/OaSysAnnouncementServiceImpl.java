package com.jeestudio.bpm.modules.oa.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.modules.oa.dao.OaSysAnnouncementMapper;
import com.jeestudio.bpm.modules.oa.entity.OaSysAnnouncementEntity;
import com.jeestudio.bpm.modules.oa.service.OaSysAnnouncementServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.RoleService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: OA系统公告服务实现
 */
@Service
@Transactional
@Slf4j
public class OaSysAnnouncementServiceImpl extends ServiceImpl<OaSysAnnouncementMapper, OaSysAnnouncementEntity> implements OaSysAnnouncementServiceI {


    @Autowired
    ZformService zformService;

    @Autowired
    RoleService roleService;

    @Override
    public void sendToRole(String title, String content, boolean autoPop, String roleCode) {
        Role roleByCode = roleService.getRoleByCode(roleCode);
        if (roleByCode == null) {
            throw new BusinessException("角色不存在");
        }
        this.sendToRoleId(title, content, autoPop, roleByCode.getId());
    }

    @Override
    public void sendToRoleId(String title, String content, boolean autoPop, String roleId) {
        this.sendToRole(UserUtil.getCurrentLoginName(), title, content, autoPop, roleId);
    }

    @Override
    public void sendToRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode) {
        Role roleByCode = roleService.getRoleByCode(roleCode);
        if (roleByCode == null) {
            throw new BusinessException("角色不存在");
        }
        this.sendToRoleId(senderLoginName, title, content, autoPop, roleByCode.getId());
    }

    @Override
    public void sendToUserByRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode) {
        this.sendToUserByRole(senderLoginName, title, content, autoPop, roleCode, new QueryWrapper<>());
    }

    @Override
    public void sendToUserByRole(String senderLoginName, String title, String content, boolean autoPop, String roleCode, QueryWrapper<User> userQueryWrapper) {
        Role roleByCode = roleService.getRoleByCode(roleCode);
        if (roleByCode == null) {
            throw new BusinessException("角色不存在");
        }
        userQueryWrapper.apply("a.id in (select user_id from sys_user_role where role_id = {0})", roleByCode.getId());
        List<LinkedHashMap> userList = zformService.findMapList("sys_user", userQueryWrapper);
        List<String> userIdList = userList.stream().map(k -> ConvertUtil.getString(k.get("id"))).collect(Collectors.toList());
        this.sendToUserList(senderLoginName, title, content, autoPop, userIdList);
    }

    @Override
    public void sendToRoleId(String senderLoginName, String title, String content, boolean autoPop, String roleId) {
        this.saveAnnouncement(senderLoginName, title, content, autoPop, roleId, DateUtil.date());
    }

    @Override
    public void sendToUser(String title, String content, boolean autoPop, String userId) {
        this.sendToUsers(title, content, autoPop, userId);
    }

    @Override
    public void sendToUsers(String title, String content, boolean autoPop, String... userIds) {
        this.sendToUserList(title, content, autoPop, Arrays.asList(userIds));
    }

    @Override
    public void sendToUserList(String title, String content, boolean autoPop, List<String> userIdList) {
        this.sendToUserList(UserUtil.getCurrentLoginName(), title, content, autoPop, userIdList);
    }

    @Override
    public void sendToUser(String senderLoginName, String title, String content, boolean autoPop, String userId) {
        this.sendToUsers(senderLoginName, title, content, autoPop, userId);
    }

    @Override
    public void sendToUsers(String senderLoginName, String title, String content, boolean autoPop, String... userIds) {
        this.sendToUserList(senderLoginName, title, content, autoPop, Arrays.asList(userIds));
    }

    @Override
    public void sendToUserList(String senderLoginName, String title, String content, boolean autoPop, List<String> userIdList) {
        this.saveAnnouncement(senderLoginName, title, content, autoPop, null, userIdList);
    }

    @Override
    public void saveAnnouncement(String senderLoginName, String title, String content, boolean autoPop, String roleId, Date sendTime) {
        this.saveAnnouncement(senderLoginName, title, content, autoPop, roleId, null, sendTime);
    }

    @Override
    public void saveAnnouncement(String senderLoginName, String title, String content, boolean autoPop, List<String> userIdList, Date sendTime) {
        this.saveAnnouncement(senderLoginName, title, content, autoPop, null, userIdList, sendTime);
    }


    public void saveAnnouncement(String loginName, String title, String content, boolean autoPop, String roleId, List<String> userIdList) {
        this.saveAnnouncement(loginName, title, content, autoPop, roleId, userIdList, DateUtil.date());
    }

    private void saveAnnouncement(String loginName, String title, String content, boolean autoPop, String roleId, List<String> userIdList, Date sendTime) {
        log.info("发送消息通知,loginName:{},title:{}", loginName, title);
        User byLoginName = UserUtil.getByLoginName(loginName);
        JSONObject jsonObject = new JSONObject();
        JSONObject orgId = new JSONObject();
        assert byLoginName != null;
        orgId.put("id", byLoginName.getCompany().getId());
        orgId.put("name", byLoginName.getCompany().getName());
        jsonObject.put("org_id", orgId);

        JSONObject sender = new JSONObject();
        sender.put("id", byLoginName.getId());
        sender.put("name", byLoginName.getName());
        jsonObject.put("sender", sender);
        jsonObject.put("send_time", DateUtil.format(sendTime, DatePattern.NORM_DATETIME_PATTERN)); //发送时间
        jsonObject.put("content_", content);
        if (StrUtil.isNotEmpty(roleId)) {
            //发送给角色
            jsonObject.put("receiving_roles", roleId);
        } else {
            //发送给用户
            jsonObject.put("receiving_users", StrUtil.join(",", userIdList));
        }
        jsonObject.put("automatically_pop", autoPop ? Global.YES : Global.NO);
        jsonObject.put("title", title); //标题
        jsonObject.put("message_color", "red");
        jsonObject.put("formNo", "oa_sys_announcement");
        Zform zform = null;
        HashMap<String, Object> updateNullParamMap = jsonObject.getObject("updateNullParamMap", new TypeReference<HashMap<String, Object>>() {
        });
        try {
            zform = zformService.getZformFromMap(jsonObject, "admin");
            zformService.saveZform(zform, "admin", "/dynamic/zform", updateNullParamMap);
        } catch (Exception e) {
            log.error("发送消息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException("发送消息失败" + e.getMessage());
        }
    }

}
