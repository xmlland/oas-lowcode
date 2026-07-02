package com.jeestudio.tools.base.annotation;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.tools.base.enums.DesensitiseTypeEnum;

import java.lang.annotation.*;

/**
 * @Description: 安全脱敏注解
 */
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Security {
    /**
     * 密钥字段名称
     * @return
     */
    String encryptKey()default "encryptKey";

    /**
     * 脱敏类型
     * @return
     */
    DesensitiseTypeEnum desensitiseType()default DesensitiseTypeEnum.other;

    /**
     * 脱敏内容存储字段
     * @return
     */
    String desensitiseField()default StrUtil.NULL;
}
