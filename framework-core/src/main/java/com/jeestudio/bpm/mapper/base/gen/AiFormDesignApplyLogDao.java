package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignApplyLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * @Description: AI表单应用日志数据访问接口
 */

@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface AiFormDesignApplyLogDao extends CrudDao<AiFormDesignApplyLog> {

    List<AiFormDesignApplyLog> findByDraftId(@Param("draftId") String draftId);

    List<AiFormDesignApplyLog> findByGenTableId(@Param("genTableId") String genTableId);
}
