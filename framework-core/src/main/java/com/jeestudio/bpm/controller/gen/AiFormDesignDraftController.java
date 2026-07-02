package com.jeestudio.bpm.controller.gen;

import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignApplyLog;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDraft;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDraftVersion;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.gen.AiFormDesignDraftService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: AI表单草稿
 */
@Tag(name = "AI表单草稿")
@RestController
@RequestMapping("${adminPath}/gen/aiFormDraft")
public class AiFormDesignDraftController extends BaseController {

    @Autowired
    private AiFormDesignDraftService aiFormDesignDraftService;

    @Operation(summary = "查询AI表单设计草稿列表")
    @RequiresPermissions("user")
    @PostMapping("/list")
    public ResultJson list(@RequestBody AiFormDesignDraft draft) {
        try {
            if (draft == null) {
                draft = new AiFormDesignDraft();
            }
            if (draft.getPageParam() == null) {
                draft.setPageParam(new PageParam());
            }
            Page<AiFormDesignDraft> page = aiFormDesignDraftService.findPage(
                    new Page<>(
                            draft.getPageParam().getPageNo(),
                            draft.getPageParam().getPageSize(),
                            draft.getPageParam().getOrderBy()
                    ),
                    draft
            );
            ResultJson resultJson = ResultJson.success().setRows(page.getList()).setTotal(page.getCount());
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "获取AI表单设计草稿详情")
    @RequiresPermissions("user")
    @PostMapping("/detail")
    public ResultJson detail(@RequestBody AiFormDesignDraft draft) {
        try {
            AiFormDesignDraft detail = aiFormDesignDraftService.getDetail(draft == null ? null : draft.getId());
            ResultJson resultJson = ResultJson.success().put("draft", detail);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "保存AI表单设计草稿")
    @RequiresPermissions("user")
    @PostMapping("/save")
    public ResultJson save(@RequestBody AiFormDesignDraft draft) {
        try {
            AiFormDesignDraft savedDraft = aiFormDesignDraftService.saveDraft(draft);
            ResultJson resultJson = ResultJson.success().put("draft", savedDraft).setInsertedId(savedDraft.getId());
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "保存AI表单设计草稿版本")
    @RequiresPermissions("user")
    @PostMapping("/saveVersion")
    public ResultJson saveVersion(@RequestBody AiFormDesignDraftVersion version) {
        try {
            AiFormDesignDraftVersion savedVersion = aiFormDesignDraftService.saveVersion(version);
            AiFormDesignDraft draft = aiFormDesignDraftService.get(savedVersion.getDraftId());
            ResultJson resultJson = ResultJson.success()
                    .put("version", savedVersion)
                    .put("draft", draft);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "删除AI表单设计草稿")
    @RequiresPermissions("user")
    @PostMapping("/remove")
    public ResultJson remove(@RequestBody AiFormDesignDraft draft) {
        try {
            aiFormDesignDraftService.removeDraft(draft == null ? null : draft.getId());
            ResultJson resultJson = ResultJson.success("Delete success");
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "导入本地AI表单设计草稿")
    @RequiresPermissions("user")
    @PostMapping("/importLocal")
    public ResultJson importLocal(@RequestBody ImportLocalDraftRequest request) {
        try {
            AiFormDesignDraft importedDraft = aiFormDesignDraftService.importLocalDraft(
                    request == null ? null : request.getDraft(),
                    request == null ? null : request.getVersions()
            );
            ResultJson resultJson = ResultJson.success().put("draft", importedDraft).setInsertedId(importedDraft.getId());
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    @Operation(summary = "标记AI表单设计草稿为已正式保存")
    @RequiresPermissions("user")
    @PostMapping("/markFormalSaved")
    public ResultJson markFormalSaved(@RequestBody FormalSaveTraceRequest request) {
        try {
            AiFormDesignApplyLog applyLog = aiFormDesignDraftService.markFormalSaved(
                    request == null ? null : request.getDraftId(),
                    request == null ? null : request.getVersionId(),
                    request == null ? null : request.getGenTableId(),
                    request == null ? null : request.getFormName(),
                    request == null ? null : request.getFormTitle(),
                    request == null ? null : request.getBeforeChecksum(),
                    request == null ? null : request.getAfterChecksum(),
                    request == null ? null : request.getDetailJson()
            );
            AiFormDesignDraft draft = aiFormDesignDraftService.get(applyLog.getDraftId());
            ResultJson resultJson = ResultJson.success()
                    .put("applyLog", applyLog)
                    .put("draft", draft);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception ex) {
            return ResultJson.failed(ex.getMessage());
        }
    }

    public static class ImportLocalDraftRequest {

        private AiFormDesignDraft draft;

        private List<AiFormDesignDraftVersion> versions;

        public AiFormDesignDraft getDraft() {
            return draft;
        }

        public void setDraft(AiFormDesignDraft draft) {
            this.draft = draft;
        }

        public List<AiFormDesignDraftVersion> getVersions() {
            return versions;
        }

        public void setVersions(List<AiFormDesignDraftVersion> versions) {
            this.versions = versions;
        }
    }

    public static class FormalSaveTraceRequest {

        private String draftId;
        private String versionId;
        private String genTableId;
        private String formName;
        private String formTitle;
        private String beforeChecksum;
        private String afterChecksum;
        private String detailJson;

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

        public String getFormTitle() {
            return formTitle;
        }

        public void setFormTitle(String formTitle) {
            this.formTitle = formTitle;
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
}
