package com.jeestudio.bpm.service.act.ext;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.service.system.RoleService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.ActUtil;
import com.jeestudio.bpm.utils.UserUtil;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 工作流用户实体服务（提供工作流身份集成的用户与组查询）
 */
@Service
public class ActUserEntityService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    public User findUserById(String userId) {
        return ActUtil.toActivitiUser(UserUtil.getByLoginName(userId));
    }

    public void deleteUser(String userId) {
        User user = findUserById(userId);
        if (user != null) {
            userService.deleteUser(new com.jeestudio.bpm.common.entity.system.User(user.getId()));
        }
    }

    public List<Group> findGroupsByUser(String userId) {
        List<Group> list = Lists.newArrayList();
        for (Role role : roleService.findList(new Role(new com.jeestudio.bpm.common.entity.system.User(null, userId)))) {
            list.add(ActUtil.toActivitiGroup(role));
        }
        return list;
    }
}
