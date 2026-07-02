package com.jeestudio.bpm.service.oauth2;

import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.pojo.UserInfo;

/**
 * @Description: 用户角色同步处理器
 */
public abstract class UserRoleSyncHandler {

    /**
     * 是否同步
     *
     * @param user 用户
     * @param role 角色
     * @return
     */
    public boolean isSync(UserInfo user, Role role) {
        return true;
    }
}
