package com.jeestudio.bpm.common.entity.system;

import java.io.Serializable;

/**
 * @Description: 字典生成视图
 */
public class DictGenView implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典项值或编码。 */
    private String key;
    /** 字典项显示文本。 */
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
