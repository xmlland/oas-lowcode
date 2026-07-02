package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

import jakarta.validation.constraints.NotNull;

/**
 * @Description: 职务级别，用于综合办公工作流程
 */
public class Level extends DataEntity<Level> {

    private static final long serialVersionUID = 1L;

    /** 职务级别名称。 */
    private String name;
    /** 职务级别编码。 */
    private String code;
    /** 排序号。 */
    private Integer sort;
    /** 是否启用。 */
    private String useable;

    public Level() {
        super();
    }

    public Level(String id){
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

    @NotNull(message="Sort can not be blank")
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
}
