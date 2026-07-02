package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.TreeEntity;

/**
 * @Description: 权限
 */
public class Auth extends TreeEntity<Area> {

    /** 授权对象 ID 集合，通常为逗号分隔字符串。 */
    private String ids;

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    @Override
    public Area getParent() {
        return null;
    }

    @Override
    public void setParent(Area parent) {

    }
}
