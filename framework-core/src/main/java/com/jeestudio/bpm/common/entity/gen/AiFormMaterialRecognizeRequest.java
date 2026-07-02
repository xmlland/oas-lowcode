package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: AI表单材料识别请求
 */
public class AiFormMaterialRecognizeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已抽取或用户直接输入的材料文本。 */
    private String rawText;
    /** 材料来源类型，如 text、excel、word、pdf、image。 */
    private String sourceType;
    /** 表单业务场景，用于影响字段类型、布局和列表查询建议。 */
    private String scene;
    /** 用户给出的材料标题或表单标题提示。 */
    private String title;
    /** 当前表单上下文，编辑已有表单时用于继承名称和模块。 */
    private CurrentForm currentForm;
    /** AI 识别参数。 */
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CurrentForm getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(CurrentForm currentForm) {
        this.currentForm = currentForm;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static class CurrentForm implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 已有关联的表单配置 ID。 */
        private String genTableId;
        /** 表单英文名称。 */
        private String formName;
        /** 表单中文标题。 */
        private String formTitle;
        /** 所属模块名称。 */
        private String moduleName;

        public String getGenTableId() {
            return genTableId;
        }

        public void setGenTableId(String genTableId) {
            this.genTableId = genTableId;
        }

        public String getFormName() {
            return formName;
        }

        public void setFormName(String formName) {
            this.formName = formName;
        }

        public String getFormTitle() {
            return formTitle;
        }

        public void setFormTitle(String formTitle) {
            this.formTitle = formTitle;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }
    }

    public static class Options implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 大模型温度参数，值越高识别结果越发散。 */
        private Double temperature;

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }
}
