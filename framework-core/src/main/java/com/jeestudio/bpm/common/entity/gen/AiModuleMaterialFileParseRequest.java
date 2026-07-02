package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: AI模块文件材料解析请求
 */
public class AiModuleMaterialFileParseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 上传文件名，用于识别类型、日志追踪和前端展示。 */
    private String fileName;
    /** 浏览器上传时携带的 MIME 类型。 */
    private String mimeType;
    /** 材料来源类型，例如 excel、docx、pdf、image。 */
    private String sourceType;
    /** 文件内容的 Base64 编码。 */
    private String fileBase64;
    /** 模块生成场景，用于提示词选择和默认规则控制。 */
    private String scene;
    /** 模块英文标识，通常用于表名前缀或菜单编码。 */
    private String moduleName;
    /** 模块中文名称，用于蓝图标题和菜单展示。 */
    private String moduleTitle;
    /** 文件抽取和模块材料识别的可选参数。 */
    private Options options;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleTitle() {
        return moduleTitle;
    }

    public void setModuleTitle(String moduleTitle) {
        this.moduleTitle = moduleTitle;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 大模型采样温度，值越低输出越稳定。 */
        private Double temperature;
        /** Excel 工作表序号；为空时按默认工作表或全部工作表策略处理。 */
        private Integer sheetIndex;
        /** 是否抽取 Excel 的全部工作表。 */
        private Boolean extractAllSheets;
        /** 表格材料最多读取的行数。 */
        private Integer maxRows;
        /** 表格材料最多读取的列数。 */
        private Integer maxColumns;
        /** 文档材料最多读取的页数。 */
        private Integer maxPages;
        /** 指定读取的页码集合，适用于 PDF 等分页文档。 */
        private List<Integer> pageIndexes;
        /** 抽取文本的最大字符数，防止材料过长导致 AI 请求过大。 */
        private Integer maxChars;
        /** 是否尽量抽取文档中的表格结构。 */
        private Boolean extractTables;
        /** 是否抽取标题、段落层级等结构化线索。 */
        private Boolean extractHeaders;
        /** OCR 服务提供方，图片或扫描 PDF 识别时使用。 */
        private String ocrProvider;
        /** OCR 识别语言，例如 zh-CN 或 en。 */
        private String ocrLanguage;

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getSheetIndex() {
            return sheetIndex;
        }

        public void setSheetIndex(Integer sheetIndex) {
            this.sheetIndex = sheetIndex;
        }

        public Boolean getExtractAllSheets() {
            return extractAllSheets;
        }

        public void setExtractAllSheets(Boolean extractAllSheets) {
            this.extractAllSheets = extractAllSheets;
        }

        public Integer getMaxRows() {
            return maxRows;
        }

        public void setMaxRows(Integer maxRows) {
            this.maxRows = maxRows;
        }

        public Integer getMaxColumns() {
            return maxColumns;
        }

        public void setMaxColumns(Integer maxColumns) {
            this.maxColumns = maxColumns;
        }

        public Integer getMaxPages() {
            return maxPages;
        }

        public void setMaxPages(Integer maxPages) {
            this.maxPages = maxPages;
        }

        public List<Integer> getPageIndexes() {
            return pageIndexes;
        }

        public void setPageIndexes(List<Integer> pageIndexes) {
            this.pageIndexes = pageIndexes;
        }

        public Integer getMaxChars() {
            return maxChars;
        }

        public void setMaxChars(Integer maxChars) {
            this.maxChars = maxChars;
        }

        public Boolean getExtractTables() {
            return extractTables;
        }

        public void setExtractTables(Boolean extractTables) {
            this.extractTables = extractTables;
        }

        public Boolean getExtractHeaders() {
            return extractHeaders;
        }

        public void setExtractHeaders(Boolean extractHeaders) {
            this.extractHeaders = extractHeaders;
        }

        public String getOcrProvider() {
            return ocrProvider;
        }

        public void setOcrProvider(String ocrProvider) {
            this.ocrProvider = ocrProvider;
        }

        public String getOcrLanguage() {
            return ocrLanguage;
        }

        public void setOcrLanguage(String ocrLanguage) {
            this.ocrLanguage = ocrLanguage;
        }
    }
}
