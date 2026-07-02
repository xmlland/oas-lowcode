package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: AI模块表单DSL生成请求
 */
public class AiModuleFormDslGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块蓝图中的模块信息，用于给单个表单生成提供上级上下文。 */
    private Map<String, Object> module;
    /** 当前表单所属页面信息。 */
    private Map<String, Object> page;
    /** 当前待生成 FormDesignDSL 的表单蓝图。 */
    private Map<String, Object> form;
    /** 模块级字典建议，生成字段组件和字典绑定时使用。 */
    private List<Map<String, Object>> dictionaries;
    /** 模块内页面、表单或字段之间的关联关系。 */
    private List<Map<String, Object>> relations;
    /** 原始材料来源类型，例如 text、excel、docx、pdf、url。 */
    private String sourceType;
    /** 原型或外部材料 URL，用于日志追踪和缓存键生成。 */
    private String sourceUrl;
    /** 表单生成场景，用于选择提示词和默认布局策略。 */
    private String scene;
    /** 单表单 DSL 生成的可选参数。 */
    private Options options;

    public Map<String, Object> getModule() {
        return module;
    }

    public void setModule(Map<String, Object> module) {
        this.module = module;
    }

    public Map<String, Object> getPage() {
        return page;
    }

    public void setPage(Map<String, Object> page) {
        this.page = page;
    }

    public Map<String, Object> getForm() {
        return form;
    }

    public void setForm(Map<String, Object> form) {
        this.form = form;
    }

    public List<Map<String, Object>> getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(List<Map<String, Object>> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public List<Map<String, Object>> getRelations() {
        return relations;
    }

    public void setRelations(List<Map<String, Object>> relations) {
        this.relations = relations;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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
        /** 当前已有 DSL JSON，用于重新生成或增量修正。 */
        private String currentDslJson;

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public String getCurrentDslJson() {
            return currentDslJson;
        }

        public void setCurrentDslJson(String currentDslJson) {
            this.currentDslJson = currentDslJson;
        }
    }
}
