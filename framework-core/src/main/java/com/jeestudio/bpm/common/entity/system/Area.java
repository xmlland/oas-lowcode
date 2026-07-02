package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.TreeEntity;
import com.jeestudio.bpm.utils.StringUtil;

/**
 * @Description: 行政区
 */
public class Area extends TreeEntity<Area> {

    private static final long serialVersionUID = 1L;

    /** 父级行政区 ID，未指定时默认根节点。 */
    private String parentId;

    /** 行政区编码。 */
    private String code;
    /** 行政区类型或级别集合，按业务字典解释。 */
    private String types;

    public Area() {
        super();
    }

    public Area(String id){
        super(id);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public  Area getParent() {
        return parent;
    }

    @Override
    public void setParent(Area parent) {
        this.parent = parent;
    }

   /* public String getParentId() {
        return parent != null && parent.getId() != null ? parent.getId() : "0";
    }*/

   @Override
   public String getParentId() {
       return StringUtil.isNotEmpty(parentId) ? parentId : "0";
   }
}
