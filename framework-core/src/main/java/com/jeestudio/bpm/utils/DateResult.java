package com.jeestudio.bpm.utils;

import lombok.Data;

/**
 * @Description: 日期校验结果
 */
@Data
public class DateResult {
    /** 日期值是否有效。 */
    Boolean isValid;
    /** 建议修正后的日期值。 */
    String suggestedValue;

    public DateResult(Boolean isValid, String suggestedValue) {
        this.isValid = isValid;
        this.suggestedValue = suggestedValue;
    }
}
