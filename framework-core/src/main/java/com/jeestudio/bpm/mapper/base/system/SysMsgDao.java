package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.system.SysMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description: 系统消息数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface SysMsgDao extends CrudDao<SysMsg> {

    int getUnreadCount(@Param("currentUserId") String currentUserId, @Param("status") String status);

}
