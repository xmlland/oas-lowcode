package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: AI模块材料识别请求
 */
public class AiModuleMaterialRecognizeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统一抽取后的模块材料文本，可能来自手工输入、文件或原型 URL。 */
    private String rawText;
    /** 材料来源类型，例如 text、excel、docx、pdf、url。 */
    private String sourceType;
    /** 模块生成场景，用于提示词选择和默认规则控制。 */
    private String scene;
    /** 模块英文标识，通常用于表名前缀或菜单编码。 */
    private String moduleName;
    /** 模块中文名称，用于蓝图标题和菜单展示。 */
    private String moduleTitle;
    /** 原型或外部材料 URL，便于缓存命中和问题追踪。 */
    private String sourceUrl;
    /** 模块材料识别的可选参数。 */
    private Options options;

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
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

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }
}
