package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.common.entity.act.AssigneeSetting;
import com.jeestudio.bpm.mapper.base.act.AssigneeSettingDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 办理人配置服务
 */
@Service
public class AssigneeSettingService extends CrudService<AssigneeSettingDao, AssigneeSetting> {

    /**
     * Get assignee list by user id
     *
     * @param userId
     * @return AssigneeSetting list
     */
    @Transactional(readOnly = false)
    public List<AssigneeSetting> getAssigneeListByUserId(String userId) {
        return dao.getAssigneeListByUserId(userId);
    }
}
