package com.jeestudio.bpm.common.entity.common.mapper;

import com.jeestudio.bpm.common.entity.common.TreeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description: 树形DAO接口
 */
public interface TreeDao<T extends TreeEntity<T>> extends CrudDao<T> {

    /**
     * Find all child nodes
     */
    public List<T> findByParentIdsLike(T entity);

    /**
     * Update all parent node fields
     */
    public int updateParentIds(T entity);

    public int updateSort(T entity);

    public List<T> getChildren(@Param("parentId") String parentId);
}
