package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 岗位
 */
public class Post extends DataEntity<Post> {

    private static final long serialVersionUID = 1L;

    /** 岗位名称。 */
    private String name;
    /** 岗位编码。 */
    private String code;
    /** 岗位类型编码。 */
    private String typeCode;
    /** 排序号。 */
    private Integer sort;
    /** 是否启用。 */
    private String useable;

    /** 所属机构 ID。 */
    private String officeId;

    public Post() {
        super();
    }

    public Post(String id){
        super(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getUseable() {
        return useable;
    }

    public void setUseable(String useable) {
        this.useable = useable;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }
}
