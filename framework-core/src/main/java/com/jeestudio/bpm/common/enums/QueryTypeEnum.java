package com.jeestudio.bpm.common.enums;

/**
 * @Description: 查询类型枚举
 */
public enum QueryTypeEnum {
    /** 等于 */
    eq,
    /** 不等于 */
    ne,
    /** 小于 */
    lt,
    /** 小于等于 */
    le,
    /** 大于 */
    gt,
    /** 大于等于 */
    ge,
    /** 介于之间 */
    between,
    /** 模糊匹配 */
    like,
    /** 非模糊匹配 */
    notLike,
    /** 左模糊匹配 */
    likeLeft,
    /** 右模糊匹配 */
    likeRight,
    /** 为空串 */
    isEmpty,
    /** 为null */
    isNull,
    /** 非null */
    isNotNull,
    /** 在范围内 */
    in,
    /** 不在范围内 */
    notIn,
    /** 原生SQL条件 */
    apply
}
