package com.jeestudio.bpm.common.entity.system;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jeestudio.bpm.common.entity.common.TreeEntity;

/**
 * @Description: 字典
 */
public class Dict extends TreeEntity<Dict> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码，用于机构级字典隔离。 */
    private String ownerCode;
    /** 字典项编码。 */
    private String code;
    /** 父级字典项编码。 */
    private String parentCode;
    /** 是否可见。 */
    private String viewFlag;
    /** 是否允许编辑。 */
    private String editFlag;
    /** 扩展编码 1，供业务自定义使用。 */
    private String code1;
    /** 扩展编码 2，供业务自定义使用。 */
    private String code2;

    public Dict() {
        super();
    }

    public Dict(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getViewFlag() {
        return viewFlag;
    }

    public void setViewFlag(String viewFlag) {
        this.viewFlag = viewFlag;
    }

    public String getEditFlag() {
        return editFlag;
    }

    public void setEditFlag(String editFlag) {
        this.editFlag = editFlag;
    }

    public String getCode1() {
        return code1;
    }

    public void setCode1(String code1) {
        this.code1 = code1;
    }

    public String getCode2() {
        return code2;
    }

    public void setCode2(String code2) {
        this.code2 = code2;
    }

    @JsonBackReference
    public Dict getParent() {
        return parent;
    }

    @Override
    public void setParent(Dict parent) {
        this.parent = parent;
    }

    public String getParentId() {
        return parent != null && parent.getId() != null && false == "".equals(parent.getId()) ? parent.getId() : "0";
    }
}
