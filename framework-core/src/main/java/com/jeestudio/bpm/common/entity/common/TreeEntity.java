package com.jeestudio.bpm.common.entity.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeestudio.bpm.utils.Reflections;
import com.jeestudio.bpm.utils.StringUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @Description: 树形实体基类
 */
public abstract class TreeEntity<T> extends DataEntity<T> {

    private static final long serialVersionUID = 1L;

    /** 父级节点对象。 */
    protected T parent;
    /** 父级 ID 路径，用于树查询和层级过滤。 */
    protected String parentIds;
    /** 节点中文名称。 */
    protected String name;
    @JsonProperty(value = "name_EN")
    @JSONField(name = "comments_EN")
    /** 节点英文名称。 */
    protected String nameEn;
    /** 节点中文完整路径名称。 */
    protected String fullPathName;
    /** 节点英文完整路径名称。 */
    protected String fullPathName_EN;
    /** 排序号。 */
    protected Integer sort;
    /** 是否存在子节点。 */
    private boolean hasChildren;

    public TreeEntity() {
        super();
        this.sort = 30;
    }

    public TreeEntity(String id) {
        super(id);
    }

    /** 父级对象由子类实现，避免 MyBatis 读取泛型父类映射时丢失具体类型。 */
    @JsonBackReference
    @NotNull
    public abstract T getParent();

    /** 设置父级对象，由子类提供具体类型。 */
    public abstract void setParent(T parent);

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getFullPathName() {
        if (false == StringUtil.isEmpty(fullPathName) && fullPathName.indexOf(",") == 0) {
            return fullPathName.substring(1);
        } else {
            return fullPathName;
        }
    }

    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
    }

    public String getFullPathName_EN() {
        if (false == StringUtil.isEmpty(fullPathName_EN) && fullPathName_EN.indexOf(",") == 0) {
            return fullPathName_EN.substring(1);
        } else {
            return fullPathName_EN;
        }
    }

    public void setFullPathName_EN(String fullPathName_EN) {
        this.fullPathName_EN = fullPathName_EN;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getParentId() {
        String id = null;
        if (parent != null){
            id = (String) Reflections.getFieldValue(parent, "id");
        }
        return StringUtil.isNotBlank(id) ? id : "0";
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
}
