package com.jeestudio.bpm.common.entity.system;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;

import java.util.List;

/**
 * @Description: 角色
 */
public class Role extends DataEntity<Role> {

    private static final long serialVersionUID = 1L;

    /** 角色名称。 */
    private String name;
    /** 角色英文名或权限标识。 */
    private String enname;
    /** 是否系统数据，系统数据通常限制删除或修改。 */
    private String sysData;
    /** 是否启用。 */
    private String useable;

    /** 查询角色用户关系时使用的用户对象。 */
    private User user;
    /** 角色授权菜单列表。 */
    private List<Menu> menuList = Lists.newArrayList();

    /** 数据权限：全部数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_ALL = "1";
    /** 数据权限：所在公司及以下数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_COMPANY_AND_CHILD = "2";
    /** 数据权限：所在公司数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_COMPANY = "3";
    /** 数据权限：所在部门及以下数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_OFFICE_AND_CHILD = "4";
    /** 数据权限：所在部门数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_OFFICE = "5";
    /** 数据权限：仅本人数据。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_SELF = "8";
    /** 数据权限：按明细配置。已弃用，保留用于兼容旧数据。 */
    public static final String DATA_SCOPE_CUSTOM = "9";

    public Role() {
        super();
        this.useable = Global.YES;
    }

    public Role(String id) {
        super(id);
    }

    public Role(User user) {
        this();
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnname() {
        return enname;
    }

    public void setEnname(String enname) {
        this.enname = enname;
    }

    public String getSysData() {
        return sysData;
    }

    public void setSysData(String sysData) {
        this.sysData = sysData;
    }

    public String getUseable() {
        return useable;
    }

    public void setUseable(String useable) {
        this.useable = useable;
    }

    public List<Menu> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<Menu> menuList) {
        this.menuList = menuList;
    }

    public List<String> getMenuIdList() {
        List<String> menuIdList = Lists.newArrayList();
        for (Menu menu : menuList) {
            menuIdList.add(menu.getId());
        }
        return menuIdList;
    }

    public void setMenuIdList(List<String> menuIdList) {
        menuList = Lists.newArrayList();
        for (String menuId : menuIdList) {
            Menu menu = new Menu();
            menu.setId(menuId);
            menuList.add(menu);
        }
    }

    public String getMenuIds() {
        return StringUtil.join(getMenuIdList(), ",");
    }

    public void setMenuIds(String menuIds) {
        menuList = Lists.newArrayList();
        if (menuIds != null) {
            String[] ids = StringUtil.split(menuIds, ",");
            setMenuIdList(Lists.newArrayList(ids));
        }
    }

    public List<String> getPermissions() {
        List<String> permissions = Lists.newArrayList();
        for (Menu menu : menuList) {
            if (StringUtil.isNotEmpty(menu.getPermission())) {
                permissions.add(menu.getPermission());
            }
        }
        return permissions;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
