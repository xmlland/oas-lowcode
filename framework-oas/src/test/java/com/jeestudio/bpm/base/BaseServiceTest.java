package com.jeestudio.bpm.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service 层单元测试基类
 * 使用 Mockito 进行纯单元测试，不加载 Spring Context
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    protected static final String TEST_LOGIN_NAME = "test_user";
    protected static final String TEST_USER_ID = "test_id_001";
    protected static final String TEST_OFFICE_ID = "test_office_001";
    protected static final String TEST_FORM_NO = "test_form";

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void baseSetUp() {
        // 子类可以覆盖此方法进行额外初始化
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 从 JSON 字符串转换为对象
     */
    protected <T> T fromJson(String json, Class<T> clz) throws Exception {
        return objectMapper.readValue(json, clz);
    }
}
