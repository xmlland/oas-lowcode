package com.jeestudio.bpm.common.around;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: 业务扩展数据访问接口
 */
public interface AroundDaoI<T> {
    default boolean isCustomSqlColumnsFriendly() {
        return false;
    }

    default String getCustomSqlColumnsFriendly(T entity) {
        return null;
    }

    default boolean isCustomSqlJoins() {
        return false;
    }

    default String getCustomSqlJoins(T entity) {
        return null;
    }
}
