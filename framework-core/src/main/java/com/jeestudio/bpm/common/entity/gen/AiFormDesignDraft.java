package com.jeestudio.bpm.common.entity.gen;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;

import java.util.Date;
import java.util.List;

/**
 * @Description: AI表单设计草稿
 */
public class AiFormDesignDraft extends DataEntity<AiFormDesignDraft> {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_SAVED = "saved";
    public static final String STATUS_APPLIED = "applied";
    public static final String STATUS_FORMAL_SAVED = "formal_saved";
    public static final String STATUS_ARCHIVED = "archived";
    public static final String STATUS_REJECTED = "rejected";

    public static final String TARGET_MODE_CREATE = "create";
    public static final String TARGET_MODE_OPTIMIZE = "optimize";

    /** 草稿标题，通常由用户输入或 AI 根据需求生成。 */
    private String title;
    /** 草稿状态，例如已保存、已应用、已正式保存。 */
    private String status;
    /** 目标模式：新建表单或优化已有表单。 */
    private String targetMode;
    /** 关联的代码生成表单配置 ID。 */
    private String genTableId;
    /** 表单英文名称或业务表名。 */
    private String formName;
    /** 表单中文标题。 */
    private String formTitle;
    /** 所属模块名称。 */
    private String moduleName;
    /** 草稿来源类型，例如文本、文件、URL 或手工导入。 */
    private String sourceType;
    /** 原始需求或材料内容摘要。 */
    private String sourceContent;
    /** 来源模式，用于区分单表单创建、模块创建等入口。 */
    private String sourceMode;
    /** 当前生效的草稿版本 ID。 */
    private String currentVersionId;
    /** 草稿累计版本数量。 */
    private Integer versionCount;
    /** 草稿标签 JSON，用于分类、检索或前端展示。 */
    private String tagsJson;
    /** 最近一次应用到设计器或正式保存的时间。 */
    private Date lastAppliedAt;

    /** 草稿版本列表，详情查询时按需加载。 */
    private List<AiFormDesignDraftVersion> versions;
    /** 当前生效版本，详情查询时按需加载。 */
    private AiFormDesignDraftVersion currentVersion;

    public AiFormDesignDraft() {
        super();
    }

    public AiFormDesignDraft(String id) {
        super(id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(String targetMode) {
        this.targetMode = targetMode;
    }

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public String getCurrentVersionId() {
        return currentVersionId;
    }

    public void setCurrentVersionId(String currentVersionId) {
        this.currentVersionId = currentVersionId;
    }

    public Integer getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(Integer versionCount) {
        this.versionCount = versionCount;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getLastAppliedAt() {
        return lastAppliedAt;
    }

    public void setLastAppliedAt(Date lastAppliedAt) {
        this.lastAppliedAt = lastAppliedAt;
    }

    public List<AiFormDesignDraftVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<AiFormDesignDraftVersion> versions) {
        this.versions = versions;
    }

    public AiFormDesignDraftVersion getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(AiFormDesignDraftVersion currentVersion) {
        this.currentVersion = currentVersion;
    }
}
