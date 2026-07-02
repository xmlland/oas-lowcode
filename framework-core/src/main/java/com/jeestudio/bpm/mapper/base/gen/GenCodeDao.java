package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.GenCode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description: 代码生成记录数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface GenCodeDao extends CrudDao<GenCode> {
}
