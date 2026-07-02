package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.common.entity.system.SysMsg;
import com.jeestudio.bpm.mapper.base.system.SysMsgDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: 系统消息服务
 */
@Service
public class SysMsgService extends CrudService<SysMsgDao, SysMsg> {

    @Autowired
    private SysMsgDao sysMsgDao;

    public void sendSysMsg(String userId) {
        // 后续扩展：按业务场景补充系统消息发送逻辑
    }

    public int getUnreadCount(String currentUserId, String status){
        int count = sysMsgDao.getUnreadCount(currentUserId, status);
        return count;
    }
}
