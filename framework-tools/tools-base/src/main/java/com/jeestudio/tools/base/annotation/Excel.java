package com.jeestudio.tools.base.annotation;

import com.jeestudio.tools.base.enums.AlignEnum;
import com.jeestudio.tools.base.enums.ImportValidEnum;

import java.lang.annotation.*;

/**
 * @Description: Excel导出注解
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {
    /**
     * 导出数据时是否显示
     *
     * @return
     */
    boolean exportField() default true;

    /**
     * 导出数据时是否隐藏
     *
     * @return
     */
    boolean exportHidden() default false;

    /**
     * 标题
     *
     * @return
     */
    String title() default "";

    /**
     * 文字位置
     *
     * @return
     */
    AlignEnum alignEnum() default AlignEnum.CENTER;

    /**
     * 导入模板时是否显示
     *
     * @return
     */
    boolean importField() default true;

    /**
     * 导入模板时是否隐藏
     *
     * @return
     */
    boolean importHidden() default false;

    /**
     * 导入校验规则
     *
     * @return
     */

    ImportValidEnum[] importValid() default {ImportValidEnum.NOT_EMPTY};

    /**
     * 跳过校验规则
     *
     * @return
     */
    boolean skipImportValid() default false;
    /**
     * 字典单独放一个sheet
     *
     * @return
     */
    boolean dictSingleSheet() default false;
}
