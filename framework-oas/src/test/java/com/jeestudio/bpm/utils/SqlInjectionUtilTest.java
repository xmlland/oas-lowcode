package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SqlInjectionUtil 单元测试
 * 测试 SQL 注入防护工具类的各种场景
 */
class SqlInjectionUtilTest {

    // ==================== filterContent(String) 测试 ====================

    @Test
    @DisplayName("过滤正常内容 - 不抛异常")
    void testFilterContent_NormalInput() {
        // 正常输入不包含任何 SQL 注入关键词，应该不抛异常
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent("normal_value_123")
        );
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent("user_name")
        );
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent("hello world")
        );
    }

    @Test
    @DisplayName("过滤包含 SELECT 关键词的内容 - 抛出异常")
    void testFilterContent_SelectKeyword() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("select * from users")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("非法操作");

        // 测试大小写混合
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("SELECT id FROM table")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤包含 UNION 关键词的内容 - 抛出异常")
    void testFilterContent_UnionKeyword() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("1 union all select 1")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤包含 DELETE 关键词的内容 - 抛出异常")
    void testFilterContent_DeleteKeyword() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("delete from users")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤包含 SQL 注释 '--' 的内容 - 抛出异常")
    void testFilterContent_SqlComment() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("1--comment")
        ).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("admin'--")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤 NULL 输入 - 不抛异常")
    void testFilterContent_NullInput() {
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent((String) null)
        );
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent("")
        );
    }

    @Test
    @DisplayName("过滤包含分号的内容 - 抛出异常")
    void testFilterContent_SemicolonCharacter() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("value; drop table")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤包含单引号的内容 - 抛出异常")
    void testFilterContent_SingleQuote() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent("admin' OR '1'='1")
        ).isInstanceOf(RuntimeException.class);
    }

    // ==================== validateSqlIdentifier 测试 ====================

    @Test
    @DisplayName("验证合法 SQL 标识符 - 通过")
    void testValidateSqlIdentifier_ValidInput() {
        // 合法标识符：字母、数字、下划线、点号，以字母或下划线开头
        assertThat(SqlInjectionUtil.validateSqlIdentifier("user_name")).isEqualTo("user_name");
        assertThat(SqlInjectionUtil.validateSqlIdentifier("_private")).isEqualTo("_private");
        assertThat(SqlInjectionUtil.validateSqlIdentifier("TableName123")).isEqualTo("TableName123");
        assertThat(SqlInjectionUtil.validateSqlIdentifier("schema.table")).isEqualTo("schema.table");
        assertThat(SqlInjectionUtil.validateSqlIdentifier("db.schema.table")).isEqualTo("db.schema.table");
    }

    @Test
    @DisplayName("验证非法 SQL 标识符 - 抛出异常")
    void testValidateSqlIdentifier_InvalidInput() {
        // 包含分号
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier("table;drop")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Invalid SQL identifier");

        // 包含引号
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier("table'name")
        ).isInstanceOf(IllegalArgumentException.class);

        // 以数字开头
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier("123table")
        ).isInstanceOf(IllegalArgumentException.class);

        // 包含空格
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier("table name")
        ).isInstanceOf(IllegalArgumentException.class);

        // null 输入
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier(null)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("cannot be null or empty");

        // 空字符串
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateSqlIdentifier("")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== validateIdentifier 测试 ====================

    @Test
    @DisplayName("验证合法表名标识符 - 通过")
    void testValidateIdentifier_ValidTableName() {
        // 合法标识符：字母、数字、下划线，以字母或下划线开头（不含点号）
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("user_table")
        );
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("_hidden")
        );
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("Table123")
        );
    }

    @Test
    @DisplayName("验证非法表名标识符 - 抛出异常")
    void testValidateIdentifier_InvalidTableName() {
        // 以数字开头
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("123abc")
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("非法SQL标识符");

        // 包含分号
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("table;drop")
        ).isInstanceOf(RuntimeException.class);

        // 包含点号（与 validateSqlIdentifier 不同，此方法不允许点号）
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("schema.table")
        ).isInstanceOf(RuntimeException.class);

        // null 输入
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifier(null)
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("不能为空");

        // 空字符串
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifier("")
        ).isInstanceOf(RuntimeException.class);
    }

    // ==================== validateIdentifiers 批量验证测试 ====================

    @Test
    @DisplayName("批量验证全部合法标识符 - 通过")
    void testValidateIdentifiers_AllValid() {
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.validateIdentifiers("table1", "column_name", "_field", "ABC123")
        );
    }

    @Test
    @DisplayName("批量验证包含非法标识符 - 抛出异常")
    void testValidateIdentifiers_HasInvalid() {
        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifiers("valid_table", "123invalid", "another_valid")
        ).isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() ->
                SqlInjectionUtil.validateIdentifiers("table;drop")
        ).isInstanceOf(RuntimeException.class);
    }

    // ==================== filterContent(JSONObject) 测试 ====================

    @Test
    @DisplayName("过滤 JSONObject - 正常内容通过")
    void testFilterContent_JsonObject_Normal() {
        JSONObject json = new JSONObject();
        json.put("name", "normal_value");
        json.put("age", 25);
        json.put("active", true);

        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent(json)
        );
    }

    @Test
    @DisplayName("过滤 JSONObject - 包含 SQL 关键词抛异常")
    void testFilterContent_JsonObject_WithSqlKeyword() {
        JSONObject json = new JSONObject();
        json.put("name", "test");
        json.put("query", "select * from users");

        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent(json)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("过滤 JSONObject - null 输入不抛异常")
    void testFilterContent_JsonObject_Null() {
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent((JSONObject) null)
        );
    }

    @Test
    @DisplayName("过滤 JSONObject - queryParamType 字段跳过检查")
    void testFilterContent_JsonObject_SkipQueryParamType() {
        JSONObject json = new JSONObject();
        json.put("queryParamType", "select something");  // 此字段应被跳过
        json.put("name", "normal");

        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent(json)
        );
    }

    // ==================== filterContent(String[]) 测试 ====================

    @Test
    @DisplayName("过滤字符串数组 - 全部正常通过")
    void testFilterContent_StringArray_AllNormal() {
        String[] values = {"normal1", "normal2", "value_123"};
        assertThatNoException().isThrownBy(() ->
                SqlInjectionUtil.filterContent(values)
        );
    }

    @Test
    @DisplayName("过滤字符串数组 - 包含非法值抛异常")
    void testFilterContent_StringArray_HasInvalid() {
        String[] values = {"normal", "select * from x", "another"};
        assertThatThrownBy(() ->
                SqlInjectionUtil.filterContent(values)
        ).isInstanceOf(RuntimeException.class);
    }
}
