package com.jeestudio.bpm.common.entity.gen;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: AI表单设计草稿版本
 */
public class AiFormDesignDraftVersion extends DataEntity<AiFormDesignDraftVersion> {

    private static final long serialVersionUID = 1L;

    public static final String DSL_VERSION_V1 = "1.0";

    /** 所属草稿 ID。 */
    private String draftId;
    /** 草稿版本号，从 1 开始递增。 */
    private Integer versionNo;
    /** 父版本 ID，用于记录重新生成或编辑前的来源版本。 */
    private String parentVersionId;
    /** 版本名称，便于用户区分多次生成结果。 */
    private String name;
    /** 生成该版本时使用的需求或提示词。 */
    private String prompt;
    /** 需求澄清答案 JSON。 */
    private String clarifyAnswersJson;
    /** FormDesignDSL 协议版本。 */
    private String dslVersion;
    /** 当前版本完整 FormDesignDSL JSON。 */
    private String dslJson;
    /** DSL Schema 校验问题 JSON。 */
    private String schemaIssuesJson;
    /** 设计规则检查问题 JSON。 */
    private String designIssuesJson;
    /** DSL Schema 校验摘要 JSON。 */
    private String schemaSummaryJson;
    /** 设计规则检查摘要 JSON。 */
    private String designSummaryJson;
    /** 正式化预览摘要 JSON。 */
    private String previewSummaryJson;
    /** DSL 内容校验和，用于判断版本内容是否变化。 */
    private String checksum;
    /** DSL JSON 字节或字符规模，便于排查异常超大草稿。 */
    private Integer dslSize;

    public AiFormDesignDraftVersion() {
        super();
    }

    public AiFormDesignDraftVersion(String id) {
        super(id);
    }

    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getParentVersionId() {
        return parentVersionId;
    }

    public void setParentVersionId(String parentVersionId) {
        this.parentVersionId = parentVersionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getClarifyAnswersJson() {
        return clarifyAnswersJson;
    }

    public void setClarifyAnswersJson(String clarifyAnswersJson) {
        this.clarifyAnswersJson = clarifyAnswersJson;
    }

    public String getDslVersion() {
        return dslVersion;
    }

    public void setDslVersion(String dslVersion) {
        this.dslVersion = dslVersion;
    }

    public String getDslJson() {
        return dslJson;
    }

    public void setDslJson(String dslJson) {
        this.dslJson = dslJson;
    }

    public String getSchemaIssuesJson() {
        return schemaIssuesJson;
    }

    public void setSchemaIssuesJson(String schemaIssuesJson) {
        this.schemaIssuesJson = schemaIssuesJson;
    }

    public String getDesignIssuesJson() {
        return designIssuesJson;
    }

    public void setDesignIssuesJson(String designIssuesJson) {
        this.designIssuesJson = designIssuesJson;
    }

    public String getSchemaSummaryJson() {
        return schemaSummaryJson;
    }

    public void setSchemaSummaryJson(String schemaSummaryJson) {
        this.schemaSummaryJson = schemaSummaryJson;
    }

    public String getDesignSummaryJson() {
        return designSummaryJson;
    }

    public void setDesignSummaryJson(String designSummaryJson) {
        this.designSummaryJson = designSummaryJson;
    }

    public String getPreviewSummaryJson() {
        return previewSummaryJson;
    }

    public void setPreviewSummaryJson(String previewSummaryJson) {
        this.previewSummaryJson = previewSummaryJson;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Integer getDslSize() {
        return dslSize;
    }

    public void setDslSize(Integer dslSize) {
        this.dslSize = dslSize;
    }
}
