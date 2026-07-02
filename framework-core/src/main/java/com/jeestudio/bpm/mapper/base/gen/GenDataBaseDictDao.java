package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 数据库字典数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface GenDataBaseDictDao extends CrudDao<GenTableColumn> {

    List<GenTable> findTableList(GenTable genTable);

    List<GenTable> findTableListSchema(GenTable genTable);

    List<GenTableColumn> findTableColumnList(GenTable genTable);

    List<String> findTablePK(GenTable genTable);
}
