package com.jeestudio.bpm.utils;

import com.jeestudio.tools.base.utils.ConvertUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * ConvertUtil 单元测试
 * 测试类型转换工具类的各种场景
 */
class ConvertUtilTest {

    // ==================== getString 测试 ====================

    @Test
    @DisplayName("getString - 正常对象转换为字符串")
    void testGetString_NormalObject() {
        // 字符串对象
        assertThat(ConvertUtil.getString("hello")).isEqualTo("hello");
        
        // 数字对象
        assertThat(ConvertUtil.getString(123)).isEqualTo("123");
        assertThat(ConvertUtil.getString(123.45)).isEqualTo("123.45");
        
        // Boolean 对象
        assertThat(ConvertUtil.getString(true)).isEqualTo("true");
        assertThat(ConvertUtil.getString(false)).isEqualTo("false");
    }

    @Test
    @DisplayName("getString - null 输入返回空字符串")
    void testGetString_NullInput() {
        assertThat(ConvertUtil.getString(null)).isEqualTo("");
    }

    @Test
    @DisplayName("getString - 空字符串输入返回空字符串")
    void testGetString_EmptyString() {
        assertThat(ConvertUtil.getString("")).isEqualTo("");
    }

    // ==================== getInteger 测试 ====================

    @Test
    @DisplayName("getInteger - 正常数字转换")
    void testGetInteger_NormalInput() {
        // 整数对象
        assertThat(ConvertUtil.getInteger(123)).isEqualTo(123);
        
        // 字符串数字
        assertThat(ConvertUtil.getInteger("456")).isEqualTo(456);
        
        // 负数
        assertThat(ConvertUtil.getInteger("-789")).isEqualTo(-789);
    }

    @Test
    @DisplayName("getInteger - null 输入返回 null")
    void testGetInteger_NullInput() {
        assertThat(ConvertUtil.getInteger(null)).isNull();
    }

    @Test
    @DisplayName("getInteger - 空字符串返回 null")
    void testGetInteger_EmptyString() {
        assertThat(ConvertUtil.getInteger("")).isNull();
    }

    @Test
    @DisplayName("getInteger - 无效字符串抛出异常")
    void testGetInteger_InvalidString() {
        assertThatThrownBy(() -> ConvertUtil.getInteger("abc"))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================== getLong 测试 ====================

    @Test
    @DisplayName("getLong - 正常数字转换")
    void testGetLong_NormalInput() {
        // Long 对象
        assertThat(ConvertUtil.getLong(123L)).isEqualTo(123L);
        
        // 字符串数字
        assertThat(ConvertUtil.getLong("9876543210")).isEqualTo(9876543210L);
        
        // Integer 转换
        assertThat(ConvertUtil.getLong(456)).isEqualTo(456L);
    }

    @Test
    @DisplayName("getLong - null 输入返回 null")
    void testGetLong_NullInput() {
        assertThat(ConvertUtil.getLong(null)).isNull();
    }

    @Test
    @DisplayName("getLong - 空字符串返回 null")
    void testGetLong_EmptyString() {
        assertThat(ConvertUtil.getLong("")).isNull();
    }

    @Test
    @DisplayName("getLong - 无效字符串抛出异常")
    void testGetLong_InvalidString() {
        assertThatThrownBy(() -> ConvertUtil.getLong("not-a-number"))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================== listToMap 测试 ====================

    @Test
    @DisplayName("listToMap - 正常转换列表为 Map")
    void testListToMap_NormalConversion() {
        List<User> userList = Arrays.asList(
                new User("1", "Alice"),
                new User("2", "Bob"),
                new User("3", "Charlie")
        );

        Map<String, User> userMap = ConvertUtil.listToMap(userList, User::getId);

        assertThat(userMap).hasSize(3);
        assertThat(userMap.get("1").getName()).isEqualTo("Alice");
        assertThat(userMap.get("2").getName()).isEqualTo("Bob");
        assertThat(userMap.get("3").getName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("listToMap - 重复 key 保留第一个")
    void testListToMap_DuplicateKeys() {
        List<User> userList = Arrays.asList(
                new User("1", "Alice"),
                new User("1", "Alice2"),  // 重复 key
                new User("2", "Bob")
        );

        Map<String, User> userMap = ConvertUtil.listToMap(userList, User::getId);

        // 重复 key 时保留第一个 (k1, k2) -> k1
        assertThat(userMap).hasSize(2);
        assertThat(userMap.get("1").getName()).isEqualTo("Alice");  // 保留第一个
    }

    @Test
    @DisplayName("listToMap - 空列表返回空 Map")
    void testListToMap_EmptyList() {
        List<User> emptyList = Collections.emptyList();
        Map<String, User> userMap = ConvertUtil.listToMap(emptyList, User::getId);
        assertThat(userMap).isEmpty();
    }

    // ==================== 辅助内部类 ====================

    /**
     * 用于 listToMap 测试的辅助类
     */
    static class User {
        private final String id;
        private final String name;

        User(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
