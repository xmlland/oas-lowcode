package com.jeestudio.bpm.common.view.common;

import lombok.Data;

/**
 * @Description: 字典视图
 */
@Data
public class DictView {
    /** 字典记录 ID。 */
    private String dictId;
    /** 字典项编码。 */
    private String code;
    /** 字典项显示名称。 */
    private String name;
}
