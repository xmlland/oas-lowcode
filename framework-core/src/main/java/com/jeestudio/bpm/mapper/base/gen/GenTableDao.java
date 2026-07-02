package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableChildren;
import com.jeestudio.bpm.common.entity.gen.GenTableView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 表单配置数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface GenTableDao extends CrudDao<GenTable> {

    int buildTable(@Param("sql") String paramString);

    GenTable getByName(@Param("name") String name);

    void saveSql(@Param("id") String id,
                 @Param("sqlColumns") String sqlColumns,
                 @Param("sqlColumnsFriendly") String sqlColumnsFriendly,
                 @Param("sqlColumnsFriendlyExt") String sqlColumnsFriendlyExt,
                 @Param("sqlJoins") String sqlJoins,
                 @Param("sqlJoinsExt") String sqlJoinsExt,
                 @Param("sqlInsert") String sqlInsert,
                 @Param("sqlUpdate") String sqlUpdate,
                 @Param("sqlSort") String sqlSort,
                 @Param("extSql02") String extSql02);

    int findCount(@Param("name") String tableName);

    List<GenTable> getChildren(@Param("name") String name);

    GenTableView getGengTableViewById(@Param("id") String id);

    List<GenTableChildren> getGenTableViewByParentTable(@Param("name") String name);

    void saveImportAndExport(GenTableView genTable);
    void saveImport(GenTableView genTable);
    void saveExport(GenTableView genTable);

    void unlockSql(@Param("id") String id);
}
