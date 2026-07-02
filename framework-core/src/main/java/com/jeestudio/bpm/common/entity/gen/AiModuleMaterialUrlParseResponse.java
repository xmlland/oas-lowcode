package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI模块URL材料解析响应
 */
public class AiModuleMaterialUrlParseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_URL_EMPTY = "AI_URL_EMPTY";
    public static final String ERROR_URL_UNSUPPORTED = "AI_URL_UNSUPPORTED";
    public static final String ERROR_URL_BLOCKED = "AI_URL_BLOCKED";
    public static final String ERROR_URL_FETCH_FAILED = "AI_URL_FETCH_FAILED";
    public static final String ERROR_URL_TEXT_EMPTY = "AI_URL_TEXT_EMPTY";

    /** URL 材料采集是否成功。 */
    private Boolean success;
    /** 请求唯一标识，用于日志排查和缓存链路定位。 */
    private String requestId;
    /** 实际采集的入口 URL。 */
    private String sourceUrl;
    /** 多页面聚合后的纯文本材料。 */
    private String rawText;
    /** 采集到的页面候选列表。 */
    private List<Page> pages = new ArrayList<>();
    /** 采集过程诊断信息，例如采集方式、降级原因和页面统计。 */
    private Map<String, Object> extraction = new LinkedHashMap<>();
    /** URL 采集耗时，单位毫秒。 */
    private Long elapsedMs;
    /** 失败时的错误编码。 */
    private String errorCode;
    /** 面向前端展示或日志排查的提示信息。 */
    private String message;
    /** 是否命中 Redis 缓存。 */
    private Boolean fromCache;

    public static AiModuleMaterialUrlParseResponse failed(String errorCode, String message) {
        AiModuleMaterialUrlParseResponse response = new AiModuleMaterialUrlParseResponse();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        response.setFromCache(false);
        response.setPages(new ArrayList<>());
        response.setExtraction(new LinkedHashMap<>());
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

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public Map<String, Object> getExtraction() {
        return extraction;
    }

    public void setExtraction(Map<String, Object> extraction) {
        this.extraction = extraction;
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

    public Boolean getFromCache() {
        return fromCache;
    }

    public void setFromCache(Boolean fromCache) {
        this.fromCache = fromCache;
    }

    public static class Page implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 页面候选唯一标识。 */
        private String id;
        /** 页面标题。 */
        private String title;
        /** 页面 URL。 */
        private String url;
        /** 页面抽取出的完整文本。 */
        private String rawText;
        /** 页面文本预览，供前端候选确认使用。 */
        private String textPreview;
        /** 页面标题层级文本。 */
        private List<String> headings = new ArrayList<>();
        /** 表单标签和字段名称线索。 */
        private List<String> labels = new ArrayList<>();
        /** 输入控件线索。 */
        private List<String> inputs = new ArrayList<>();
        /** 按钮文案线索。 */
        private List<String> buttons = new ArrayList<>();
        /** 页面内链接候选。 */
        private List<Link> links = new ArrayList<>();
        /** 页面内表格线索。 */
        private List<Table> tables = new ArrayList<>();
        /** 页面候选质量评分，分数越高越适合进入模块识别。 */
        private Integer qualityScore;
        /** 页面候选质量等级。 */
        private String qualityLevel;
        /** 质量评分原因，便于用户判断是否勾选该页面。 */
        private List<String> qualityReasons = new ArrayList<>();
        /** 前端确认后是否纳入模块材料识别。 */
        private Boolean included = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getRawText() {
            return rawText;
        }

        public void setRawText(String rawText) {
            this.rawText = rawText;
        }

        public String getTextPreview() {
            return textPreview;
        }

        public void setTextPreview(String textPreview) {
            this.textPreview = textPreview;
        }

        public List<String> getHeadings() {
            return headings;
        }

        public void setHeadings(List<String> headings) {
            this.headings = headings;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<String> getInputs() {
            return inputs;
        }

        public void setInputs(List<String> inputs) {
            this.inputs = inputs;
        }

        public List<String> getButtons() {
            return buttons;
        }

        public void setButtons(List<String> buttons) {
            this.buttons = buttons;
        }

        public List<Link> getLinks() {
            return links;
        }

        public void setLinks(List<Link> links) {
            this.links = links;
        }

        public List<Table> getTables() {
            return tables;
        }

        public void setTables(List<Table> tables) {
            this.tables = tables;
        }

        public Integer getQualityScore() {
            return qualityScore;
        }

        public void setQualityScore(Integer qualityScore) {
            this.qualityScore = qualityScore;
        }

        public String getQualityLevel() {
            return qualityLevel;
        }

        public void setQualityLevel(String qualityLevel) {
            this.qualityLevel = qualityLevel;
        }

        public List<String> getQualityReasons() {
            return qualityReasons;
        }

        public void setQualityReasons(List<String> qualityReasons) {
            this.qualityReasons = qualityReasons;
        }

        public Boolean getIncluded() {
            return included;
        }

        public void setIncluded(Boolean included) {
            this.included = included;
        }
    }

    public static class Link implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 链接显示文本。 */
        private String text;
        /** 链接地址。 */
        private String url;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Table implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 表格标题或附近标题文本。 */
        private String title;
        /** 表头文本集合。 */
        private List<String> headers = new ArrayList<>();
        /** 表格行数。 */
        private Integer rowCount;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public Integer getRowCount() {
            return rowCount;
        }

        public void setRowCount(Integer rowCount) {
            this.rowCount = rowCount;
        }
    }
}
