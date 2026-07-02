package com.jeestudio.bpm.mapper.base.system;

import com.jeestudio.bpm.common.entity.common.mapper.TreeDao;
import com.jeestudio.bpm.common.entity.system.Menu;
import com.jeestudio.bpm.common.entity.system.MenuResult;
import com.jeestudio.bpm.common.view.system.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 菜单数据访问接口
 */
@Qualifier("sqlSessionFactoryBase")
@Component
@Mapper
public interface MenuDao extends TreeDao<Menu> {

    int updateSort(Menu menu);

    List<String> findAllPermissionList(Menu m);

    List<String> findPermissionByUserId(Menu m);

    List<MenuResult> findMenuAllList(Menu m,String subSystemCode);

    List<MenuResult> findMenuByUserId(@Param(value = "menu") Menu m,String subSystemCode);

    List<MenuResult> findMenuByUserOffice(@Param(value = "menu") Menu m,String subSystemCode);


    List<Menu> getMenuTagTree();

    int hasPermission(@Param("userId") String userId, @Param("permission") String permission);

    List<SysPermission> queryByUser(@Param("userId") String userId);

    List<SysPermission> queryAll();

    List<LinkedHashMap> findWorkflowListByCategory(@Param("category") String category);
    List<LinkedHashMap> findWrokflowNodeList(@Param("procDefKey") String procDefKey);
}
