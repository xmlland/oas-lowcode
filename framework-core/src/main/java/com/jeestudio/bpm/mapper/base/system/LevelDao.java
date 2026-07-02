package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.system.Level;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description: 职务级别数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface LevelDao extends CrudDao<Level> {
}
