package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: AI表单设计DSL生成请求
 */
public class AiFormDesignDslGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String MODE_CREATE = "create";
    public static final String MODE_OPTIMIZE = "optimize";

    /** 用户输入的表单创建或优化需求，是生成 DSL 的主要语义来源。 */
    private String requirement;
    /** 生成模式：create 表示新建，optimize 表示基于当前 DSL 优化。 */
    private String mode;
    /** 表单业务场景，如普通表单、公文文单、审批表单等。 */
    private String scene;
    /** 可选模板编码，用于约束生成结果贴近指定模板。 */
    private String templateCode;
    /** 当前设计器 DSL JSON，优化模式下作为上下文传入大模型。 */
    private String currentDslJson;
    /** 当前表单基础信息，用于继承表名、标题、模块等上下文。 */
    private CurrentForm currentForm;
    /** AI 生成参数和草稿保存策略。 */
    private Options options;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getCurrentDslJson() {
        return currentDslJson;
    }

    public void setCurrentDslJson(String currentDslJson) {
        this.currentDslJson = currentDslJson;
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
        /** 表单英文名称，对应动态表单编号或物理表名。 */
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

        /** 是否将生成结果保存为服务器草稿。 */
        private Boolean saveAsServerDraft;
        /** AI 不可用时是否允许走本地规则兜底。 */
        private Boolean fallbackToLocal;
        /** 大模型温度参数，值越高输出越发散。 */
        private Double temperature;

        public Boolean getSaveAsServerDraft() {
            return saveAsServerDraft;
        }

        public void setSaveAsServerDraft(Boolean saveAsServerDraft) {
            this.saveAsServerDraft = saveAsServerDraft;
        }

        public Boolean getFallbackToLocal() {
            return fallbackToLocal;
        }

        public void setFallbackToLocal(Boolean fallbackToLocal) {
            this.fallbackToLocal = fallbackToLocal;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }
}
