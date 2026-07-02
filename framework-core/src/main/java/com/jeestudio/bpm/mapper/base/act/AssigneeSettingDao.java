package com.jeestudio.bpm.mapper.base.act;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.act.AssigneeSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 流程办理人设置数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface AssigneeSettingDao extends CrudDao<AssigneeSetting> {

    List<AssigneeSetting> getAssigneeListByUserId(@Param("userId")String userId);
}
