package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: AI表单Excel材料解析请求
 */
public class AiFormMaterialExcelParseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 上传的 Excel 文件名，用于日志、错误提示和材料来源展示。 */
    private String fileName;
    /** Excel 文件内容的 Base64 编码。 */
    private String fileBase64;
    /** 表单业务场景，例如普通表单、公文文单或审批表单。 */
    private String scene;
    /** 用户输入或文件推断出的表单标题。 */
    private String title;
    /** 当前设计器中的表单上下文，用于增量识别和避免重复字段。 */
    private AiFormMaterialRecognizeRequest.CurrentForm currentForm;
    /** Excel 抽取和 AI 识别的可选参数。 */
    private Options options;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AiFormMaterialRecognizeRequest.CurrentForm getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(AiFormMaterialRecognizeRequest.CurrentForm currentForm) {
        this.currentForm = currentForm;
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
        /** 指定解析的工作表序号；为空时由服务端按默认策略处理。 */
        private Integer sheetIndex;
        /** 单个工作表最多读取的行数，避免超大文件影响响应时间。 */
        private Integer maxRows;
        /** 单个工作表最多读取的列数，控制传给 AI 的材料规模。 */
        private Integer maxColumns;

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
    }
}
