package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.system.Area;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 行政区域数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface AreaDao extends CrudDao<Area> {

    List<TagTree> findAreaTagTree(Area area);
    List<TagTree> findAreaSubTree(@Param("id") String id);
    List<TagTree> findAreaTagTreeAll();
}
