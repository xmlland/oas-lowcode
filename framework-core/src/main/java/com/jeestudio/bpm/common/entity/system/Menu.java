package com.jeestudio.bpm.common.entity.system;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeestudio.bpm.common.entity.common.TreeEntity;
import com.jeestudio.bpm.utils.Global;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: 菜单
 */
public class Menu extends TreeEntity<Menu> {

    private static final long serialVersionUID = 1L;

    /** 子菜单列表。 */
    private List<Menu> children;
    /** 菜单路由或外部链接地址。 */
    private String href;
    /** 链接打开目标，例如当前页或新窗口。 */
    private String target;
    /** 菜单图标。 */
    private String icon;
    /** 是否在菜单中显示。 */
    private String isShow;
    /** 权限标识，供按钮和接口权限判断使用。 */
    private String permission;
    /** 查询用户菜单时使用的用户 ID。 */
    private String userId;
    /** 菜单归属子系统编码集合。 */
    private String subSystemCodeList;
    /** 菜单类型，区分目录、菜单、按钮等。 */
    private String type;
    /** 菜单标记，供特殊业务入口识别。 */
    private String sign;
    /** 是否移动端菜单。 */
    private String appMenu;

    public Menu() {
        super();
        this.sort = 30;
        this.isShow = Global.YES;
    }

    public Menu(String id) {
        super(id);
    }

    @JsonBackReference
    @NotNull
    public Menu getParent() {
        return parent;
    }

    @Override
    public void setParent(Menu parent) {
        this.parent = parent;
    }

    public String getParentIds() {
        return parentIds;
    }

    @Override
    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @NotNull
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getParentId() {
        return parent != null && parent.getId() != null ? parent.getId() : "0";
    }

    @JsonIgnore
    public static String getRootId() {
        return "1";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubSystemCodeList() {
        return subSystemCodeList;
    }

    public void setSubSystemCodeList(String subSystemCodeList) {
        this.subSystemCodeList = subSystemCodeList;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }

    public List<Menu> getChildren() {
        return children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAppMenu() {
        return appMenu;
    }

    public void setAppMenu(String appMenu) {this.appMenu = appMenu;}
}
