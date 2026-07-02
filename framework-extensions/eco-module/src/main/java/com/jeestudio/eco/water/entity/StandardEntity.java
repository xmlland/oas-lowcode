package com.jeestudio.eco.water.entity;

import java.util.List;
/**
 * @Description: 水环境标准数据实体
 */

public class StandardEntity {
    private String standardName;

    private List<String> standardLevel;

    private String otherLevel;

    private List<StandardWryEntity> wrw;

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public List<String> getStandardLevel() {
        return standardLevel;
    }

    public void setStandardLevel(List<String> standardLevel) {
        this.standardLevel = standardLevel;
    }

    public List<StandardWryEntity> getWrw() {
        return wrw;
    }

    public void setWrw(List<StandardWryEntity> wrw) {
        this.wrw = wrw;
    }

    public String getOtherLevel() {
        return otherLevel;
    }

    public void setOtherLevel(String otherLevel) {
        this.otherLevel = otherLevel;
    }
}

