package com.jeestudio.bpm.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.modules.admin.entity.SysSecIdataEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


/**
 * @Description: 安全数据Mapper
 */
@Mapper
public interface SysSecIdataMapper extends BaseMapper<SysSecIdataEntity> {
    int batchInsert(List<SysSecIdataEntity> list);

    int deleteByDataId(String parentId, String env, List<String> dataIdList);

    long countTable(String tableName);


    Map<String,Long> countRecord(String parentId, String env, String tableName);


    List<SysSecIdataEntity> selectDbList(String parentId, String env, String tableName);
}
