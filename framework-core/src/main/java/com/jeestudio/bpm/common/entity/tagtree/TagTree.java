package com.jeestudio.bpm.common.entity.tagtree;

import java.io.Serializable;

/**
 * @Description: 树，用来表达机构，用户（机构树加用户），行政区等树形结构，为前端提供统一的数据结构
 */
public class TagTree implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String parenId;
    private String name;
    private String loginName;
    private Boolean hasChildren;
    private Boolean disabled = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parenId;
    }

    public void setParentId(String parenId) {
        this.parenId = parenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
