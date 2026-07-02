package com.jeestudio.bpm.common.entity.gen;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: AI表单设计应用日志
 */
public class AiFormDesignApplyLog extends DataEntity<AiFormDesignApplyLog> {

    private static final long serialVersionUID = 1L;

    public static final String APPLY_TO_DESIGNER = "apply_to_designer";
    public static final String FORMAL_SAVE = "formal_save";
    public static final String EXPORT_DSL = "export_dsl";
    public static final String IMPORT_LOCAL = "import_local";

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAILED = "failed";

    /** 关联的 AI 表单设计草稿 ID。 */
    private String draftId;
    /** 关联的草稿版本 ID。 */
    private String versionId;
    /** 应用目标表单配置 ID。 */
    private String genTableId;
    /** 应用目标表单英文名称或业务表名。 */
    private String formName;
    /** 应用类型，例如应用到设计器、正式保存、导出 DSL。 */
    private String applyType;
    /** 应用结果：成功或失败。 */
    private String applyResult;
    /** 应用过程提示或失败原因。 */
    private String message;
    /** 应用前 DSL 或目标配置校验和。 */
    private String beforeChecksum;
    /** 应用后 DSL 或目标配置校验和。 */
    private String afterChecksum;
    /** 应用过程详情 JSON，用于追溯正式化结果。 */
    private String detailJson;

    public AiFormDesignApplyLog() {
        super();
    }

    public AiFormDesignApplyLog(String id) {
        super(id);
    }

    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
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

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public String getApplyResult() {
        return applyResult;
    }

    public void setApplyResult(String applyResult) {
        this.applyResult = applyResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBeforeChecksum() {
        return beforeChecksum;
    }

    public void setBeforeChecksum(String beforeChecksum) {
        this.beforeChecksum = beforeChecksum;
    }

    public String getAfterChecksum() {
        return afterChecksum;
    }

    public void setAfterChecksum(String afterChecksum) {
        this.afterChecksum = afterChecksum;
    }

    public String getDetailJson() {
        return detailJson;
    }

    public void setDetailJson(String detailJson) {
        this.detailJson = detailJson;
    }
}
