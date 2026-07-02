package com.jeestudio.tools.base.annotation;

import java.lang.annotation.*;

/**
 * @Description: 字典注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dict {

    String dictCode() default "";

    String dictTable() default "";

    String dictValue() default "";

    String dictText() default "";

    Class enumClass() default String.class;

    String orderCondition() default "";

    String begin() default "";

    String end() default "";

    String mapping() default "";

    Class dataDicClass() default String.class;

    boolean multiple() default false;

    String separator() default ",";
}
