package com.jeestudio.bpm.mapper.base.gen;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.gen.GenRealmPropertyView;
import com.jeestudio.bpm.common.entity.gen.GenTableColumnView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 领域属性数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface GenRealmPropertyDao extends CrudDao<GenRealmPropertyView> {

    List<GenTableColumnView> getByName(@Param("name") String name);

    List<GenTableColumnView> realmData(@Param("types") String types, @Param("name") String name);

    List<GenTableColumnView> realmDataDict(@Param("types") String types, @Param("name") String name);
}
