package com.jeestudio.bpm.common.entity.system;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @Description: 字典结果
 */
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
public class DictResult  implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典记录 ID。 */
    private String dictionaryID;
    /** 字典类型编码。 */
    private String type = "";
    /** 字典项编码。 */
    private String member;
    /** 字典项中文名称。 */
    private String memberName;
    @JsonProperty(value = "memberName_EN")
    @JSONField(name = "memberName_EN")
    /** 字典项英文名称。 */
    private String memberNameEn = "";
    /** 父级字典类型或父级编码。 */
    private String parentType;
    /** 是否存在子字典项。 */
    private boolean hasChildren = false;
    /** 排序号。 */
    private int sort;
    /** 父级 ID 路径。 */
    private String parentIds;

    public String getDictionaryID() {
        return dictionaryID;
    }

    public void setDictionaryID(String dictionaryID) {
        this.dictionaryID = dictionaryID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberNameEn() {
        return memberNameEn;
    }

    public void setMemberNameEn(String memberNameEn) {
        this.memberNameEn = memberNameEn;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }
}
