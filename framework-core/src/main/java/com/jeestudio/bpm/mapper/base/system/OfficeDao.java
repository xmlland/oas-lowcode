package com.jeestudio.bpm.mapper.base.system;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.jeestudio.bpm.common.entity.common.mapper.TreeDao;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.common.view.system.OfficeView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 机构数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface OfficeDao extends TreeDao<Office> {

    List<TagTree> findOfficeTagTree(Office office);

    List<TagTree> findOfficeTagTreeAll();

    List<TagTree> findOfficeTagTreeFilter(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    List<OfficeView> findOfficeViewData(OfficeView officeView);

    void updateByMasterData(Office office);

    List<Office> findOfficeByParentId(String parentId);

    IPage<Office> findPageOfficeByParentId(IPage<Office> page ,String parentId);

    void deleteAuthById(@Param("id") String id);

    void saveAuth(@Param("id") String id, @Param("asList") List<String> asList);

    List<String> getAuth(@Param("id") String id);


}
