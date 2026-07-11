package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.GenTableDeriveConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 表单扩展运行配置数据访问接口。
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface GenTableDeriveConfigDao extends CrudDao<GenTableDeriveConfig> {

    GenTableDeriveConfig getByGenTableId(@Param("genTableId") String genTableId);

    List<GenTableDeriveConfig> findEnabledAsyncSummaryList();

    void updateRunStatus(GenTableDeriveConfig genTableDeriveConfig);

    void deleteByGenTableId(@Param("genTableId") String genTableId);
}
