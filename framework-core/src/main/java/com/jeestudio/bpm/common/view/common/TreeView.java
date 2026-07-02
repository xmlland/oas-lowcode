package com.jeestudio.bpm.common.view.common;

/**
 * @Description: 树视图
 */
public class TreeView {

    private static final long serialVersionUID = 1L;

    /** 节点 ID。 */
    private String id;
    /** 节点中文名称。 */
    private String name;
    /** 父级节点 ID。 */
    private String parentId;
    /** 排序号。 */
    private int sort;
    /** 是否存在子节点。 */
    private boolean hasChildren;
    /** 节点英文名称。 */
    private String name_EN;
    /** 当前节点编号。 */
    private String no;

    /** 当前节点对应的数据量。 */
    private int dataCount;

    public String getName_EN() {
        return name_EN;
    }

    public void setName_EN(String name_EN) {
        this.name_EN = name_EN;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }
}
