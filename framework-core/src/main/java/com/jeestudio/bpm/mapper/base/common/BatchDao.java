package com.jeestudio.bpm.mapper.base.common;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 批量数据处理访问接口
 */
@Component
@Mapper
public interface BatchDao {
	void batchInsert(@Param("sourceTable") String sourceTable, @Param("targetTable") String targetTable, @Param("targetFieldList") List<String> targetFieldList, @Param("sourceFieldMap") LinkedHashMap<String,String> sourceFieldMap, @Param(Constants.WRAPPER) Wrapper wrapper,@Param("joinSql") String joinSql,@Param("exSql") String exSql);
}
