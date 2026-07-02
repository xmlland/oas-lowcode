package com.jeestudio.bpm.mapper.base.common;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 动态表单数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface ZformDao extends CrudDao<Zform> {

    List<Zform> getList(Zform entity);
    LinkedHashMap getMap(Zform zform);

    List<LinkedHashMap> getMapList(Zform zform);

    void deleteChildren(Zform zform);

    void deleteChildrenForTree(Zform zform);

    List<Zform> findChildrenForDelete(Zform zform);

    /**
     * 根据list查询子节点
     * @param zform
     * @param list
     * @return
     */
    List<Zform> findChildrenForDeleteByList(Zform zform, List<String> list);

    List<Zform> findListByProc(Zform zform);

    List<LinkedHashMap> findListByProcMap(Zform zform);

    Boolean hasChildren(Zform zform);

    List<Zform> findListCount(Zform zform);

    List<Zform> findListByProcCount(Zform zform);

    IPage<LinkedHashMap> findListMap(IPage page , Zform zform,@Param(value = "tableOrViewName") String tableOrViewName,@Param(Constants.WRAPPER) Wrapper queryWrapper,  @Param("sqlMap") Map sqlMap);
    List<LinkedHashMap> findListMapWith(IPage page , Zform zform,@Param(value = "tableOrViewName") String tableOrViewName,@Param(Constants.WRAPPER) Wrapper queryWrapper,  @Param("sqlMap") Map sqlMap);

    String getNameById(@Param("formNo") String formNo, @Param("columnName") String columnName, @Param("id") String id, @Param("sqlMap") Map sqlMap);

    void updateSysMenuIsShowCascade(@Param("parentIds") String parentIds, @Param("isShow") String isShow);

    List<String> findValueList(@Param("formNo") String formNo, @Param("valueColumn") String valueColumn, @Param("keyColumn") String keyColumn, @Param("key") String key);

    //common
    @Deprecated
    int insertSql(@Param("sql") String sql);

    // 带参数的查询 例如：insert sys_user(id) values(#{param.id})
    int insertSqlParm(@Param("sql") String sql, @Param("param") Map param);
    int insertSqlWarpper(@Param("sql") String sql,@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Deprecated
    int updateSql(@Param("sql") String sql);

    // 带参数的查询 例如：update sys_user set id = #{param.id2}  WHERE id = #{param.id}
    int updateSqlParm(@Param("sql") String sql, @Param("param") Map param);
    int updateSqlWarpper(@Param("sql") String sql,@Param(Constants.WRAPPER) Wrapper queryWrapper);

    //
    @Deprecated
    int deleteSql(@Param("sql") String sql);

    // 带参数的查询 例如：delete from sys_user WHERE id = #{param.id}
    int deleteSqlParm(@Param("sql") String sql, @Param("param") Map param);
    int deleteSqlWarpper(@Param("sql") String sql,@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Deprecated
    List<LinkedHashMap> findMapBySql(@Param("sql") String sql);

    // 带参数的查询 例如：SELECT * FROM sys_user WHERE id = #{param.id}
    List<LinkedHashMap> findMapBySqlParm(@Param("sql") String sql, @Param("param") Map param);
    List<LinkedHashMap> findMapList(@Param("querySql") String querySql,@Param(Constants.WRAPPER) Wrapper queryWrapper);

    long count(@Param("querySql") String querySql, @Param("countColumn") String countColumn, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Deprecated
    String findStringBySql(@Param("sql") String sql);
    // 带参数的查询 例如：SELECT id FROM sys_user WHERE id = #{param.id}
    String findStringBySqlParm(@Param("sql") String sql, @Param("param") Map param);
    String findStringBySqlWarpper(@Param("sql") String sql, @Param("param") Map param);

    @Deprecated
    List<String> findStringListBySql(@Param("sql") String sql);

    // 带参数的查询 例如：SELECT id FROM sys_user WHERE id = #{param.id}
    List<String> findStringListBySqlParm(@Param("sql") String sql, @Param("param") Map param);
    List<String> findStringListSqlWarpper(@Param("sql") String sql, @Param("param") Map param);
}
