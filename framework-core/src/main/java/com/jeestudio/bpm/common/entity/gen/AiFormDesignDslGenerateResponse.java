package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: AI表单设计DSL生成响应
 */
public class AiFormDesignDslGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROVIDER_REMOTE = "remote";
    public static final String PROVIDER_LOCAL = "local-heuristic";
    public static final String PROVIDER_MOCK = "mock";

    public static final String ERROR_REQUIREMENT_EMPTY = "AI_REQUIREMENT_EMPTY";
    public static final String ERROR_NOT_CONFIGURED = "AI_NOT_CONFIGURED";
    public static final String ERROR_NOT_IMPLEMENTED = "AI_GENERATE_NOT_IMPLEMENTED";
    public static final String ERROR_REQUEST_FAILED = "AI_REQUEST_FAILED";
    public static final String ERROR_OUTPUT_INVALID_JSON = "AI_OUTPUT_INVALID_JSON";
    public static final String ERROR_DSL_SCHEMA_INVALID = "AI_DSL_SCHEMA_INVALID";

    /** 是否生成成功。 */
    private Boolean success;
    /** 单次请求 ID，用于日志追踪和前后端问题定位。 */
    private String requestId;
    /** 生成来源，如 remote、local-heuristic、mock。 */
    private String provider;
    /** 实际使用的大模型名称。 */
    private String model;
    /** 提示词版本，用于追踪生成策略变化。 */
    private String promptVersion;
    /** 生成后的 FormDesignDSL JSON。 */
    private String dslJson;
    /** 生成结果摘要，供前端快速展示。 */
    private Summary summary;
    /** 结构校验或生成质量提示。 */
    private List<Issue> issues;
    /** 大模型原始输出预览，避免完整输出过长影响响应体。 */
    private String rawOutputPreview;
    /** 本次请求总耗时，单位毫秒。 */
    private Long elapsedMs;
    /** 失败时的稳定错误码。 */
    private String errorCode;
    /** 面向前端展示的结果消息。 */
    private String message;

    public static AiFormDesignDslGenerateResponse failed(String errorCode, String message) {
        AiFormDesignDslGenerateResponse response = new AiFormDesignDslGenerateResponse();
        response.setSuccess(false);
        response.setProvider(PROVIDER_REMOTE);
        response.setPromptVersion("form-dsl-v1");
        response.setErrorCode(errorCode);
        response.setMessage(message);
        response.setIssues(new ArrayList<>());
        response.setElapsedMs(0L);
        return response;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public String getDslJson() {
        return dslJson;
    }

    public void setDslJson(String dslJson) {
        this.dslJson = dslJson;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public String getRawOutputPreview() {
        return rawOutputPreview;
    }

    public void setRawOutputPreview(String rawOutputPreview) {
        this.rawOutputPreview = rawOutputPreview;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Summary implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 表单标题摘要。 */
        private String title;
        /** 字段数量。 */
        private Integer fieldCount;
        /** 布局风格摘要。 */
        private String layoutStyle;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getFieldCount() {
            return fieldCount;
        }

        public void setFieldCount(Integer fieldCount) {
            this.fieldCount = fieldCount;
        }

        public String getLayoutStyle() {
            return layoutStyle;
        }

        public void setLayoutStyle(String layoutStyle) {
            this.layoutStyle = layoutStyle;
        }
    }

    public static class Issue implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 问题级别，如 error、warning、suggestion。 */
        private String level;
        /** 稳定问题编码。 */
        private String code;
        /** 问题标题。 */
        private String title;
        /** 问题详细说明。 */
        private String description;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
