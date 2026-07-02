package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.CrudDao;
import com.jeestudio.bpm.common.entity.system.Datapermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 数据权限数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface DatapermissionDao extends CrudDao<Datapermission> {

    List<Datapermission> findListByUserId(@Param("userId") String userId);

    List<Datapermission> findListByRoleIds(@Param("roleIds") List<String> roleIds);

    List<String> getPermission(@Param("id") String id);

    void deletePermissionById(@Param("id") String id);

    void savePermission(@Param("id") String id, @Param("asList") List<String> asList);
}
