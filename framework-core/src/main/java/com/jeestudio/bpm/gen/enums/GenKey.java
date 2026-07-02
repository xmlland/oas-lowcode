package com.jeestudio.bpm.gen.enums;

/**
 * @Description: 代码生成配置键名枚举
 */
public enum GenKey {
    // 枚举常量，对应各个字符串值
    FOLDER("gen.folder"),
    JAVA("gen.java"),
    VUE("gen.vue"),
    KEY("gen.key"),
    URL("gen.url"),
    AI_URI("gen.AIUri"),
    AI_API_KEY("gen.AIApiKey"),
    AI_MODEL("gen.AIModel"),
    VUE_SOURCE_FOLDER("/antvue3/src"),;

    // 存储字符串值的成员变量
    private final String value;

    // 私有构造方法，初始化字符串值
    GenKey(String value) {
        this.value = value;
    }

    // 获取字符串值的方法
    public String getValue() {
        return value;
    }
}
