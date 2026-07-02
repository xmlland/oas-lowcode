package com.jeestudio.bpm.service.oauth2;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.pojo.OfficeInfo;
import com.jeestudio.bpm.common.pojo.UserInfo;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.mapper.base.system.RoleDao;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.RoleService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: OAuth2用户服务
 */
@Service
@Slf4j
public class OAuthUserService {
    @Autowired
    OAuthOfficeService oAuthOfficeService;

    @Autowired
    UserService userService;

    @Autowired
    ZformService zformService;

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    RoleService roleService;

    @Autowired
    private RoleDao roleDao;

    public List<UserInfo> list(List<String> ids, boolean syncRole) {
        List<OfficeInfo> officeInfoList = oAuthOfficeService.list(ids);
        if (officeInfoList.isEmpty()) {
            return new ArrayList<>();
        }
        //后续需要考虑机构和用户特别多的情况，这里先不考虑
        List<User> userListByParentIdList = userService.findUserListByParentIdList(officeInfoList.stream().map(OfficeInfo::getId).collect(Collectors.toList()));
        return userListByParentIdList.stream().map(k -> {
            UserInfo userInfo = new UserInfo(k);
            if (syncRole) {
                Role theRole = new Role();
                theRole.setUser(new User(userInfo.getId()));
                List<Role> roleList = roleDao.findList(theRole);
                userInfo.setRoleCodes(roleList.stream().map(Role::getEnname).distinct().collect(Collectors.toList()));
            }
            return userInfo;
        }).collect(Collectors.toList());
    }


    public void save(List<UserInfo> userInfos) {
        List<String> ids = userInfos.stream().map(UserInfo::getParent_id).distinct().collect(Collectors.toList());
        List<OfficeInfo> officeInfoList = oAuthOfficeService.list(ids);
        if (officeInfoList.isEmpty()) {
            return;
        }
        try {
            int addCount = 0;
            int updateCount = 0;
            int skipCount = 0;
            List<User> userList = userService.findUserListByParentIdList(officeInfoList.stream().map(OfficeInfo::getId).collect(Collectors.toList()));
            Map<String, User> orgMap = ConvertUtil.listToMap(userList, User::getId);
            for (UserInfo userInfo : userInfos) {


                JSONObject jsonObject = userInfo.toJSONObject();
                Zform zform = zformService.getZformFromMap(jsonObject, "");
                if (orgMap.containsKey(userInfo.getId())) {
                    User user = orgMap.get(userInfo.getId());
                    Date updateDate = user.getUpdateDate();
                    if (updateDate == null) {
                        updateDate = user.getCreateDate();
                    }
                    if (updateDate != null) {
                        Date syncUpdateDate = userInfo.getUpdate_date();
                        if (syncUpdateDate == null) {
                            syncUpdateDate = userInfo.getCreate_date();
                        }
                        if (syncUpdateDate != null) {
                            //如果更新时间相同则不更新
                            if (DateUtil.between(updateDate, syncUpdateDate, DateUnit.SECOND) == 0) {
                                log.debug("用户信息更新时间相同,跳过,{},{}", userInfo.getId(), userInfo.getName());
                                skipCount++;
                                continue;
                            }
                        }
                    }
                    updateCount++;
                    zform.setId(userInfo.getId());
                    zform.setPreUpdateBy(new User(userInfo.getUpdate_by()));
                    zform.setPreUpdateDate(userInfo.getUpdate_date());
                    log.debug("更新用户信息,{},{}", userInfo.getId(), userInfo.getName());
                } else {
                    addCount++;
                    jsonObject.put("useable", Global.YES);
                    jsonObject.put("login_flag", Global.YES);
                    zform = zformService.getZformFromMap(jsonObject, "");
                    zform.setPreId(userInfo.getId());
                    zform.setPreCreateBy(new User(userInfo.getCreate_by()));
                    zform.setPreCreateDate(userInfo.getCreate_date());
                    zform.setPreUpdateBy(new User(userInfo.getUpdate_by()));
                    zform.setPreUpdateDate(userInfo.getUpdate_date());
                    log.debug("新增用户信息,{},{}", userInfo.getId(), userInfo.getName());
                }
                zformService.saveZform(zform, "", "");


                //处理用户角色关系
                Map<String, String> syncUserRoleMap = projectProperties.getSyncUserRoleMap();
                Class<UserRoleSyncHandler> userRoleSyncHandler = projectProperties.getUserRoleSyncHandler();
                UserRoleSyncHandler syncHandler = userRoleSyncHandler.newInstance();
                if (syncUserRoleMap.size() > 0) {
                    List<String> roleCodes = userInfo.getRoleCodes();
                    if (roleCodes != null && roleCodes.size() > 0) {
                        for (String roleCode : roleCodes) {
                            if (syncUserRoleMap.containsKey(roleCode)) {
                                String roleCodeNew = syncUserRoleMap.get(roleCode);
                                if (StringUtil.isNotEmpty(roleCodeNew)) {
                                    String[] roleList = roleCodeNew.split(",");
                                    int roleCount = 0;
                                    for (String role : roleList) {
                                        if (StringUtil.isNotEmpty(role)) {
                                            Role roleByCode = roleService.getRoleByCode(role);
                                            if (syncHandler.isSync(userInfo, roleByCode)) {
                                                roleService.saveUserRole(userInfo.getId(), roleByCode.getId());
                                                roleCount++;
                                            }

                                        }
                                    }
                                    log.debug("用户角色关系处理,用户:{},{}个角色", userInfo.getLogin_name(), roleCount);
                                }
                            }
                        }
                    }
                }
            }
            log.info("保存用户信息成功,新增{}条,更新{}条,跳过{}条", addCount, updateCount, skipCount);
        } catch (Exception e) {
            log.error("保存用户信息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
    }
}
