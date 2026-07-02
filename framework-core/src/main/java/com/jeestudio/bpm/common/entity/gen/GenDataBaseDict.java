package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: 数据库字典
 */
public class GenDataBaseDict implements Serializable {

    private String name;
    private String comments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
