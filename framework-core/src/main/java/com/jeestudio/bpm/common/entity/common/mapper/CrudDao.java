package com.jeestudio.bpm.common.entity.common.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 通用CRUD DAO接口
 */
public interface CrudDao<T> extends BaseDao {

    /**
     * Get single data
     * @param id
     */
    public T get(String id);

    /**
     * Get single data
     * @param entity
     */
    public T get(T entity);

    /**
     * Get unique records based on entity name and field name and field value
     * @param propertyName
     * @param value
     */
    public  T findUniqueByProperty(@Param(value="propertyName")String propertyName, @Param(value="value")Object value);
    /**
     * Query the data list.
     * @param entity
     */
    public List<T> findList(T entity);
    /**
     * Query the data list. If paging is required, set the paging object，as：entity.setPage(new Page<T>());
     * @param entity
     */
    public IPage<T> findPageList(IPage<T> page, @Param("entity") T entity, @Param("sqlMap") Map sqlMap);

    public IPage<LinkedHashMap> findListMap(Page<T> page, T entity, @Param(value = "tableOrViewName") String tableOrViewName, @Param(Constants.WRAPPER) Wrapper queryWrapper, @Param("sqlMap") Map sqlMap);
    List<LinkedHashMap> findListMapWith(Page<T> page, T entity, @Param(value = "tableOrViewName") String tableOrViewName, @Param(Constants.WRAPPER) Wrapper queryWrapper, @Param("sqlMap") Map sqlMap);

    /**
     * Query the data list (used when managing roles). If paging is required, set the paging object，as：entity.setPage(new Page<T>());
     * @param entity
     */
    public List<T> findListForAdmin(T entity);

    /**
     * Query all data lists
     * @param entity
     */
    public List<T> findAllList(T entity);

    /**
     * Query all data lists
     * @see public List<T> findAllList(T entity)
     */
    @Deprecated
    public List<T> findAllList();

    /**
     * 插入数据。
     *
     * @param entity 实体对象
     */
    public int insert(T entity);

    /**
     * 批量插入数据。
     *
     * @param list list
     */
    int batchInsert(List<T> list, Map<String, String> sqlMap);

    /**
     * 插入归档数据。
     *
     * @param entity 实体对象
     */
    public int insertV(T entity);

    /**
     * 插入指定版本 Schema 的数据。
     *
     * @param entity 实体对象
     * @param versionSchema 版本 Schema
     */
    public int insertVersionSchema(T entity);

    /**
     * 更新数据。
     *
     * @param entity 实体对象
     */
    public int update(T entity);

    /**
     * 按主键物理删除数据。
     *
     * @param id 主键 ID
     * @see public int delete(T entity)
     */
    @Deprecated
    public int delete(String id);

    /**
     * 按主键逻辑删除数据，将 del_flag 更新为删除状态。
     *
     * @param id 主键 ID
     * @see public int delete(T entity)
     */
    @Deprecated
    public int deleteByLogic(String id);

    /**
     * 物理删除数据。
     *
     * @param entity 实体对象
     */
    public int delete(T entity);

    /**
     * 批量删除数据。
     *
     * @param tableOrViewName 表名或视图名
     * @param list 待删除数据列表
     * @return 删除数量
     */
    int batchDelete(String tableOrViewName,List<T> list);

    /**
     * Delete data (logically delete, update the Del flag field to 1, when the table contains the field del flag, you can call this method to hide the data)
     * @param entity
     */
    public int deleteByLogic(T entity);
}
