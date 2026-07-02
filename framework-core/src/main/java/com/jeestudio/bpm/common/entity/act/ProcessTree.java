package com.jeestudio.bpm.common.entity.act;

import com.jeestudio.bpm.common.entity.common.ActEntity;

import java.util.Map;

/**
 * @Description: 流程分类树实体
 */
public class ProcessTree extends ActEntity<ProcessTree> {

    private static final long serialVersionUID = 1L;

    /** 流程分类树节点的扩展属性 Map。 */
    private Map<String, Object> map;

    public ProcessTree() {}
    public ProcessTree(Map<String, Object> map) {
        super();
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }
    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
