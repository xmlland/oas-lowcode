package com.jeestudio.bpm.service.act.ext;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.utils.ActUtil;
import com.jeestudio.bpm.utils.UserUtil;
import org.flowable.idm.api.Group;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 工作流用户组实体服务（提供工作流身份集成的用户组查询）
 */
@Service
public class ActGroupEntityService {

    public List<Group> findGroupsByUser(String userId) {
        List<Group> list = Lists.newArrayList();
        User user = UserUtil.getByLoginName(userId);
        if (user != null && user.getRoleList() != null) {
            for (Role role : user.getRoleList()) {
                list.add(ActUtil.toActivitiGroup(role));
            }
        }
        return list;
    }
}
