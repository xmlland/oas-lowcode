package com.jeestudio.bpm.mapper.base.act;

import com.jeestudio.bpm.common.entity.act.Act;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description: 工作流运行数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface ActDao extends CrudDao<Act> {

    int updateProcInsIdByBusinessId(Act act);
}
