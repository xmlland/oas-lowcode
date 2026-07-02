package com.jeestudio.bpm.common.view.system;

/**
 * @Description: 机构视图
 */
public class OfficeView {

    private static final long serialVersionUID = 1L;

    /** 机构 ID。 */
    private String id;
    /** 父级机构 ID。 */
    private String parentId;
    /** 机构编码。 */
    private String code;
    /** 机构名称。 */
    private String name;
    /** 机构简称。 */
    private String shortName;
    /** 排序号。 */
    private int sort;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
