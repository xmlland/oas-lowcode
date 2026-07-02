package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;

/**
 * @Description: AI模块URL材料解析请求
 */
public class AiModuleMaterialUrlParseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 原型或外部系统页面地址。 */
    private String sourceUrl;
    /** 模块生成场景，用于后续材料识别提示词选择。 */
    private String scene;
    /** 模块英文标识，通常用于表名前缀或菜单编码。 */
    private String moduleName;
    /** 模块中文名称，用于蓝图标题和菜单展示。 */
    private String moduleTitle;
    /** 用户补充说明，用于弥补 URL 页面信息不足。 */
    private String supplementText;
    /** URL 采集和文本抽取的可选参数。 */
    private Options options;

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

    public String getSupplementText() {
        return supplementText;
    }

    public void setSupplementText(String supplementText) {
        this.supplementText = supplementText;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 最多采集的页面数量。 */
        private Integer maxPages;
        /** 聚合文本的最大字符数。 */
        private Integer maxChars;
        /** 单次页面访问超时时间，单位秒。 */
        private Integer timeoutSeconds;
        /** 是否采集同域链接，用于 Axure 等多页面原型。 */
        private Boolean collectSameOriginLinks;
        /** 是否启用动态渲染采集。 */
        private Boolean dynamicRender;
        /** 动态渲染失败时是否降级为静态 HTML 采集。 */
        private Boolean fallbackToStatic;
        /** 是否绕过 Redis 缓存重新采集。 */
        private Boolean forceRefresh;
        /** 动态渲染后等待页面稳定的时间，单位毫秒。 */
        private Integer dynamicWaitMillis;

        public Integer getMaxPages() {
            return maxPages;
        }

        public void setMaxPages(Integer maxPages) {
            this.maxPages = maxPages;
        }

        public Integer getMaxChars() {
            return maxChars;
        }

        public void setMaxChars(Integer maxChars) {
            this.maxChars = maxChars;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public Boolean getCollectSameOriginLinks() {
            return collectSameOriginLinks;
        }

        public void setCollectSameOriginLinks(Boolean collectSameOriginLinks) {
            this.collectSameOriginLinks = collectSameOriginLinks;
        }

        public Boolean getDynamicRender() {
            return dynamicRender;
        }

        public void setDynamicRender(Boolean dynamicRender) {
            this.dynamicRender = dynamicRender;
        }

        public Boolean getFallbackToStatic() {
            return fallbackToStatic;
        }

        public void setFallbackToStatic(Boolean fallbackToStatic) {
            this.fallbackToStatic = fallbackToStatic;
        }

        public Boolean getForceRefresh() {
            return forceRefresh;
        }

        public void setForceRefresh(Boolean forceRefresh) {
            this.forceRefresh = forceRefresh;
        }

        public Integer getDynamicWaitMillis() {
            return dynamicWaitMillis;
        }

        public void setDynamicWaitMillis(Integer dynamicWaitMillis) {
            this.dynamicWaitMillis = dynamicWaitMillis;
        }
    }
}
