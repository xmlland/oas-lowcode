package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI模块材料识别响应
 */
public class AiModuleMaterialRecognizeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String PROVIDER_REMOTE = "remote";

    public static final String ERROR_MATERIAL_EMPTY = "AI_MODULE_MATERIAL_EMPTY";
    public static final String ERROR_NOT_CONFIGURED = "AI_NOT_CONFIGURED";
    public static final String ERROR_REQUEST_FAILED = "AI_REQUEST_FAILED";
    public static final String ERROR_OUTPUT_INVALID_JSON = "AI_OUTPUT_INVALID_JSON";
    public static final String ERROR_MATERIAL_SCHEMA_INVALID = "AI_MODULE_MATERIAL_SCHEMA_INVALID";
    public static final String ERROR_FILE_EMPTY = "AI_FILE_EMPTY";
    public static final String ERROR_FILE_TOO_LARGE = "AI_FILE_TOO_LARGE";
    public static final String ERROR_FILE_TYPE_UNSUPPORTED = "AI_FILE_TYPE_UNSUPPORTED";
    public static final String ERROR_FILE_PARSE_FAILED = "AI_FILE_PARSE_FAILED";
    public static final String ERROR_FILE_TEXT_EMPTY = "AI_FILE_TEXT_EMPTY";
    public static final String ERROR_OCR_FAILED = "AI_OCR_FAILED";

    /** 本次模块材料识别是否成功。 */
    private Boolean success;
    /** 请求唯一标识，用于前端提示、日志排查和缓存链路定位。 */
    private String requestId;
    /** AI 调用提供方，例如 remote。 */
    private String provider;
    /** 实际调用的大模型名称。 */
    private String model;
    /** 提示词版本，便于后续评估生成质量变化。 */
    private String promptVersion;
    /** 模型输出并校验后的 ModuleMaterial JSON 字符串。 */
    private String materialJson;
    /** 模块蓝图摘要信息，供前端快速展示识别结果。 */
    private Summary summary;
    /** 文件、URL 或 OCR 抽取过程的诊断信息。 */
    private Map<String, Object> extraction;
    /** 材料识别过程中发现的问题和建议。 */
    private List<Issue> issues;
    /** 原始输出预览，主要用于排查 AI 输出格式异常。 */
    private String rawOutputPreview;
    /** 本次识别耗时，单位毫秒。 */
    private Long elapsedMs;
    /** 失败时的错误编码。 */
    private String errorCode;
    /** 面向前端展示或日志排查的提示信息。 */
    private String message;

    public static AiModuleMaterialRecognizeResponse failed(String errorCode, String message) {
        AiModuleMaterialRecognizeResponse response = new AiModuleMaterialRecognizeResponse();
        response.setSuccess(false);
        response.setProvider(PROVIDER_REMOTE);
        response.setPromptVersion("module-material-v1");
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

        /** 模块蓝图标题。 */
        private String title;
        /** 模块场景。 */
        private String scene;
        /** 识别出的菜单数量。 */
        private Integer menuCount;
        /** 识别出的页面数量。 */
        private Integer pageCount;
        /** 识别出的表单数量。 */
        private Integer formCount;
        /** 识别出的字段数量。 */
        private Integer fieldCount;
        /** 识别出的字典建议数量。 */
        private Integer dictionaryHintCount;
        /** 识别出的页面或表单关系数量。 */
        private Integer relationCount;

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

        public Integer getMenuCount() {
            return menuCount;
        }

        public void setMenuCount(Integer menuCount) {
            this.menuCount = menuCount;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public void setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
        }

        public Integer getFormCount() {
            return formCount;
        }

        public void setFormCount(Integer formCount) {
            this.formCount = formCount;
        }

        public Integer getFieldCount() {
            return fieldCount;
        }

        public void setFieldCount(Integer fieldCount) {
            this.fieldCount = fieldCount;
        }

        public Integer getDictionaryHintCount() {
            return dictionaryHintCount;
        }

        public void setDictionaryHintCount(Integer dictionaryHintCount) {
            this.dictionaryHintCount = dictionaryHintCount;
        }

        public Integer getRelationCount() {
            return relationCount;
        }

        public void setRelationCount(Integer relationCount) {
            this.relationCount = relationCount;
        }
    }

    public static class Issue implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 问题等级，例如 error、warning、suggestion。 */
        private String level;
        /** 问题编码，便于前端做分类展示。 */
        private String code;
        /** 问题标题。 */
        private String title;
        /** 问题说明。 */
        private String description;
        /** 问题关联的菜单标识。 */
        private String menuId;
        /** 问题关联的页面标识。 */
        private String pageId;
        /** 问题关联的表单标识。 */
        private String formId;
        /** 问题关联的字段标识。 */
        private String fieldId;
        /** 问题关联的字段中文名称。 */
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

        public String getMenuId() {
            return menuId;
        }

        public void setMenuId(String menuId) {
            this.menuId = menuId;
        }

        public String getPageId() {
            return pageId;
        }

        public void setPageId(String pageId) {
            this.pageId = pageId;
        }

        public String getFormId() {
            return formId;
        }

        public void setFormId(String formId) {
            this.formId = formId;
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
