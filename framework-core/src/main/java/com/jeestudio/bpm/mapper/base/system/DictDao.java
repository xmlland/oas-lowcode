package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.TreeDao;
import com.jeestudio.bpm.common.entity.system.Dict;
import com.jeestudio.bpm.common.entity.system.DictGenView;
import com.jeestudio.bpm.common.entity.system.DictResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 系统字典数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface DictDao extends TreeDao<Dict> {

    DictResult getDictResult(@Param("id") String id);

    List<DictResult> dictTypes(@Param("type") String type);

    List<DictGenView> getDictGenView(@Param("dictType") String dictType);

    List<DictGenView> findDictListLike(@Param("key") String key);

    List<DictResult> findDictTree(@Param("parentId") String parentId, @Param("editFlag") String editFlag);

    List<DictResult> findDictTreeDataParams(@Param("editFlag") String editFlag);

    void deleteCascade(@Param("code") String code);

    DictResult getDataDictListByType(@Param("type") String type);

    Integer getDataDictChildrenCount(@Param("parentId") String parentId, @Param("code") String code);
}
