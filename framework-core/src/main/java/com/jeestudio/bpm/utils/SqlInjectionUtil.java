package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: SQL注入防护工具类
 * #14修复：增强黑名单 + 使用正则词边界匹配，防止绕过
 */
@Slf4j
public class SqlInjectionUtil {

    /**
     * SQL注入关键词列表（使用正则词边界 \b 匹配，防止空格/Tab/注释绕过）
     */
    private static final String[] SQL_KEYWORDS = {
            "\\band\\b", "\\bor\\b", "\\bexec\\b", "\\bexecute\\b",
            "\\binsert\\b", "\\bselect\\b", "\\bunion\\b", "\\bdelete\\b",
            "\\bupdate\\b", "\\bdrop\\b", "\\bcount\\b", "\\bchr\\b",
            "\\bmid\\b", "\\bmaster\\b", "\\btruncate\\b", "\\bchar\\b",
            "\\bdeclare\\b", "\\bcreate\\b", "\\balter\\b",
            "\\binformation_schema\\b", "\\bhaving\\b",
            "\\bsleep\\b", "\\bbenchmark\\b", "\\bload_file\\b",
            "\\boutfile\\b", "\\bdumpfile\\b", "\\binto\\b",
            "\\bwaitfor\\b", "\\bdelay\\b"
    };

    /**
     * 特殊字符/符号模式
     */
    private static final String[] SPECIAL_PATTERNS = {
            ";",            // SQL语句终结符
            "'",            // 单引号注入
            "--",           // SQL单行注释
            "/\\*",         // SQL块注释开始
            "\\*/",         // SQL块注释结束
            "\\bxp_\\w+",   // SQL Server扩展存储过程
            "0x[0-9a-fA-F]+", // 十六进制编码绕过
    };

    /** 禁止在 dsf 中出现的危险关键字（大小写不敏感） */
    private static final String[] DANGEROUS_KEYWORDS = {
            ";", "--", "/*", "*/", "DROP ", "DELETE ", "INSERT ", "UPDATE ",
            "TRUNCATE ", "ALTER ", "CREATE ", "EXEC ", "EXECUTE ", "UNION ",
            "xp_", "SLEEP(", "BENCHMARK(", "WAITFOR "
    };

    /** dsf 允许的字符白名单：字母数字、空格、常见 SQL 操作符、函数等 */
    private static final String ALLOWED_PATTERN =
            "^[a-zA-Z0-9_\\s().,'<>=!%+\\-|:&*@$\"\\\\]+$";

    /**
     * 预编译的正则模式（性能优化：只编译一次）
     */
    private static final Pattern[] KEYWORD_PATTERNS;
    private static final Pattern[] SPECIAL_PATTERNS_COMPILED;

    static {
        KEYWORD_PATTERNS = new Pattern[SQL_KEYWORDS.length];
        for (int i = 0; i < SQL_KEYWORDS.length; i++) {
            KEYWORD_PATTERNS[i] = Pattern.compile(SQL_KEYWORDS[i], Pattern.CASE_INSENSITIVE);
        }
        SPECIAL_PATTERNS_COMPILED = new Pattern[SPECIAL_PATTERNS.length];
        for (int i = 0; i < SPECIAL_PATTERNS.length; i++) {
            SPECIAL_PATTERNS_COMPILED[i] = Pattern.compile(SPECIAL_PATTERNS[i], Pattern.CASE_INSENSITIVE);
        }
    }

    /**
     * sql注入过滤处理，遇到注入关键字抛异常
     *
     * @param values
     */
    public static void filterContent(String[] values) {
        for (String value : values) {
            filterContent(value);
        }
    }

    /**
     * sql注入过滤处理，遇到注入关键字抛异常
     *
     * @param value
     */
    public static void filterContent(String value) {
        if (value == null || "".equals(value)) {
            return;
        }
        // 先对输入进行规范化处理：去除多余空白、Tab等
        String normalizedValue = value.replaceAll("[\\t\\r\\n]", " ").trim();

        // 检查SQL关键词（使用词边界正则）
        for (int i = 0; i < KEYWORD_PATTERNS.length; i++) {
            if (KEYWORD_PATTERNS[i].matcher(normalizedValue).find()) {
                log.error("请注意，存在SQL注入关键词---> {}", SQL_KEYWORDS[i]);
                log.error("请注意，值可能存在SQL注入风险!---> 长度:{}", value.length());
                throw new RuntimeException("非法操作！");
            }
        }
        // 检查特殊字符模式
        for (int i = 0; i < SPECIAL_PATTERNS_COMPILED.length; i++) {
            if (SPECIAL_PATTERNS_COMPILED[i].matcher(normalizedValue).find()) {
                log.error("请注意，存在SQL注入特殊字符---> {}", SPECIAL_PATTERNS[i]);
                log.error("请注意，值可能存在SQL注入风险!---> 长度:{}", value.length());
                throw new RuntimeException("非法操作！");
            }
        }
    }

    /**
     * SQL标识符（表名、列名）正则校验模式
     * 只允许字母、数字、下划线和点号，必须以字母或下划线开头
     */
    private static final Pattern SQL_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*$");

    /**
     * 校验 SQL 标识符（表名、列名）只含合法字符
     * @param identifier 表名或列名
     * @return 校验通过的标识符
     * @throws IllegalArgumentException 如果包含非法字符
     */
    public static String validateSqlIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or empty");
        }
        if (!SQL_IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            log.error("Invalid SQL identifier detected: {}", identifier.length() > 50 ? identifier.substring(0, 50) + "..." : identifier);
            throw new IllegalArgumentException("Invalid SQL identifier: contains illegal characters");
        }
        return identifier;
    }

    /**
     * 验证表名/列名是否为合法SQL标识符
     * 只允许字母、数字、下划线，且必须以字母或下划线开头
     * @param identifier 表名或列名
     * @throws RuntimeException 如果标识符不合法
     */
    public static void validateIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new RuntimeException("SQL标识符不能为空");
        }
        // 只允许字母、数字、下划线
        if (!identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            log.error("请注意，SQL标识符不合法---> {}", identifier);
            throw new RuntimeException("非法SQL标识符: " + identifier);
        }
    }

    /**
     * 验证多个表名/列名是否为合法SQL标识符
     * @param identifiers 表名或列名数组
     */
    public static void validateIdentifiers(String... identifiers) {
        for (String identifier : identifiers) {
            validateIdentifier(identifier);
        }
    }

    /**
     * sql注入过滤处理，遇到注入关键字抛异常
     */
    public static void filterContent(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        List<String> noneKey = new ArrayList<String>(){{
            add("queryParamType");
        }};
        jsonObject.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if ( noneKey.contains(key)) {
                return;
            }
            filterContent(String.valueOf(value));
        });

    }

    /**
     * 校验 dsf 字符串是否安全
     * <p>
     * 通过则返回原字符串，不通过则抛出 SecurityException
     * </p>
     *
     * @param dsf 数据范围过滤字符串
     * @return 校验通过的原始 dsf 字符串
     * @throws SecurityException 如果 dsf 包含危险内容
     */
    public static String validateDsf(String dsf) {
        if (dsf == null || dsf.isEmpty()) {
            return "";
        }
        // 1. 检查是否包含危险关键字
        String upper = dsf.toUpperCase();
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upper.contains(keyword.toUpperCase())) {
                log.error("Security: dsf包含危险关键字 '{}', dsf={}", keyword, dsf);
                throw new SecurityException("DataScopeFilter包含非法内容: " + keyword);
            }
        }
        // 2. 检查字符白名单
        if (!dsf.matches(ALLOWED_PATTERN)) {
            log.error("Security: dsf包含非法字符, dsf={}", dsf);
            throw new SecurityException("DataScopeFilter包含非法字符");
        }
        return dsf;
    }

}
