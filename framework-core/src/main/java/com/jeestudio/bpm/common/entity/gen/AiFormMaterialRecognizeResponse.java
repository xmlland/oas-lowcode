package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI表单材料识别响应
 */
public class AiFormMaterialRecognizeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROVIDER_REMOTE = "remote";
    public static final String PROVIDER_LOCAL = "local-heuristic";

    public static final String ERROR_MATERIAL_EMPTY = "AI_MATERIAL_EMPTY";
    public static final String ERROR_NOT_CONFIGURED = "AI_NOT_CONFIGURED";
    public static final String ERROR_REQUEST_FAILED = "AI_REQUEST_FAILED";
    public static final String ERROR_OUTPUT_INVALID_JSON = "AI_OUTPUT_INVALID_JSON";
    public static final String ERROR_MATERIAL_SCHEMA_INVALID = "AI_MATERIAL_SCHEMA_INVALID";
    public static final String ERROR_EXCEL_FILE_EMPTY = "AI_EXCEL_FILE_EMPTY";
    public static final String ERROR_EXCEL_FILE_INVALID = "AI_EXCEL_FILE_INVALID";
    public static final String ERROR_FILE_EMPTY = "AI_FILE_EMPTY";
    public static final String ERROR_FILE_TOO_LARGE = "AI_FILE_TOO_LARGE";
    public static final String ERROR_FILE_TYPE_UNSUPPORTED = "AI_FILE_TYPE_UNSUPPORTED";
    public static final String ERROR_FILE_PARSE_FAILED = "AI_FILE_PARSE_FAILED";
    public static final String ERROR_FILE_TEXT_EMPTY = "AI_FILE_TEXT_EMPTY";
    public static final String ERROR_OCR_NOT_CONFIGURED = "AI_OCR_NOT_CONFIGURED";
    public static final String ERROR_OCR_PROVIDER_UNAVAILABLE = "AI_OCR_PROVIDER_UNAVAILABLE";
    public static final String ERROR_OCR_FAILED = "AI_OCR_FAILED";
    public static final String ERROR_OCR_LOW_CONFIDENCE = "AI_OCR_LOW_CONFIDENCE";

    /** 是否识别成功。 */
    private Boolean success;
    /** 单次请求 ID，用于日志追踪。 */
    private String requestId;
    /** 识别来源，如 remote 或 local-heuristic。 */
    private String provider;
    /** 实际使用的大模型名称。 */
    private String model;
    /** 提示词版本。 */
    private String promptVersion;
    /** 识别后的 FormMaterial v1 JSON。 */
    private String materialJson;
    /** 材料识别摘要。 */
    private Summary summary;
    /** 文件抽取过程信息，如页数、表格、OCR 元数据等。 */
    private Map<String, Object> extraction;
    /** 结构校验或材料质量提示。 */
    private List<Issue> issues;
    /** 大模型原始输出预览。 */
    private String rawOutputPreview;
    /** 本次请求总耗时，单位毫秒。 */
    private Long elapsedMs;
    /** 失败时的稳定错误码。 */
    private String errorCode;
    /** 面向前端展示的结果消息。 */
    private String message;

    public static AiFormMaterialRecognizeResponse failed(String errorCode, String message) {
        AiFormMaterialRecognizeResponse response = new AiFormMaterialRecognizeResponse();
        response.setSuccess(false);
        response.setProvider(PROVIDER_REMOTE);
        response.setPromptVersion("form-material-v1");
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

    public String getMaterialJson() {
        return materialJson;
    }

    public void setMaterialJson(String materialJson) {
        this.materialJson = materialJson;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Map<String, Object> getExtraction() {
        return extraction;
    }

    public void setExtraction(Map<String, Object> extraction) {
        this.extraction = extraction;
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
        /** 识别出的表单场景。 */
        private String scene;
        /** 字段数量。 */
        private Integer fieldCount;
        /** 材料中识别出的表格数量。 */
        private Integer tableCount;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getScene() {
            return scene;
        }

        public void setScene(String scene) {
            this.scene = scene;
        }

        public Integer getFieldCount() {
            return fieldCount;
        }

        public void setFieldCount(Integer fieldCount) {
            this.fieldCount = fieldCount;
        }

        public Integer getTableCount() {
            return tableCount;
        }

        public void setTableCount(Integer tableCount) {
            this.tableCount = tableCount;
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
        /** 关联字段 ID。 */
        private String fieldId;
        /** 关联字段标签。 */
        private String fieldLabel;

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

        public String getFieldId() {
            return fieldId;
        }

        public void setFieldId(String fieldId) {
            this.fieldId = fieldId;
        }

        public String getFieldLabel() {
            return fieldLabel;
        }

        public void setFieldLabel(String fieldLabel) {
            this.fieldLabel = fieldLabel;
        }
    }
}
