package com.jeestudio.bpm.service.gen;

import com.alibaba.fastjson.JSON;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignApplyLog;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDraft;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDraftVersion;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.mapper.base.gen.AiFormDesignApplyLogDao;
import com.jeestudio.bpm.mapper.base.gen.AiFormDesignDraftDao;
import com.jeestudio.bpm.mapper.base.gen.AiFormDesignDraftVersionDao;
import com.jeestudio.bpm.mapper.base.gen.GenTableDao;
import com.jeestudio.bpm.service.common.CrudService;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: AI表单草稿服务
 */
@Service
public class AiFormDesignDraftService extends CrudService<AiFormDesignDraftDao, AiFormDesignDraft> {

    private static final int MAX_DSL_JSON_LENGTH = 1024 * 1024;

    private static final List<String> DRAFT_STATUS_LIST = Arrays.asList(
            AiFormDesignDraft.STATUS_SAVED,
            AiFormDesignDraft.STATUS_APPLIED,
            AiFormDesignDraft.STATUS_FORMAL_SAVED,
            AiFormDesignDraft.STATUS_ARCHIVED,
            AiFormDesignDraft.STATUS_REJECTED
    );

    private static final List<String> TARGET_MODE_LIST = Arrays.asList(
            AiFormDesignDraft.TARGET_MODE_CREATE,
            AiFormDesignDraft.TARGET_MODE_OPTIMIZE
    );

    @Autowired
    private AiFormDesignDraftDao aiFormDesignDraftDao;

    @Autowired
    private AiFormDesignDraftVersionDao aiFormDesignDraftVersionDao;

    @Autowired
    private AiFormDesignApplyLogDao aiFormDesignApplyLogDao;

    @Autowired
    private GenTableDao genTableDao;

    public AiFormDesignDraft get(String id) {
        return super.get(id);
    }

    public Page<AiFormDesignDraft> findPage(Page<AiFormDesignDraft> page, AiFormDesignDraft query) {
        AiFormDesignDraft safeQuery = query == null ? new AiFormDesignDraft() : query;
        restrictQueryToCurrentUser(safeQuery);
        return super.findPage(page, safeQuery);
    }

    public AiFormDesignDraft getDetail(String draftId) {
        AiFormDesignDraft draft = getOwnedDraft(draftId);
        List<AiFormDesignDraftVersion> versions = aiFormDesignDraftVersionDao.findByDraftId(draft.getId());
        draft.setVersions(versions);
        if (versions != null && !versions.isEmpty()) {
            AiFormDesignDraftVersion currentVersion = null;
            for (AiFormDesignDraftVersion version : versions) {
                if (StringUtil.equals(version.getId(), draft.getCurrentVersionId())) {
                    currentVersion = version;
                    break;
                }
            }
            draft.setCurrentVersion(currentVersion == null ? versions.get(versions.size() - 1) : currentVersion);
        }
        return draft;
    }

    @Transactional(readOnly = false)
    public AiFormDesignDraft saveDraft(AiFormDesignDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("Draft cannot be empty");
        }
        boolean isNewDraft = StringUtil.isBlank(draft.getId());
        AiFormDesignDraft existing = null;
        if (!isNewDraft) {
            existing = getOwnedDraft(draft.getId());
        }
        normalizeDraft(draft, existing);
        validateDraft(draft);
        super.save(draft);
        return get(draft.getId());
    }

    @Transactional(readOnly = false)
    public AiFormDesignDraftVersion saveVersion(AiFormDesignDraftVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("Draft version cannot be empty");
        }
        if (StringUtil.isBlank(version.getDraftId())) {
            throw new IllegalArgumentException("Draft id cannot be empty");
        }
        AiFormDesignDraft draft = getOwnedDraft(version.getDraftId());
        normalizeVersion(version, draft);
        validateVersion(version);

        int nextVersionNo = aiFormDesignDraftVersionDao.countByDraftId(draft.getId()) + 1;
        version.setVersionNo(nextVersionNo);
        if (StringUtil.isBlank(version.getName())) {
            version.setName("V" + nextVersionNo);
        }
        version.preInsert();
        aiFormDesignDraftVersionDao.insert(version);

        draft.setCurrentVersionId(version.getId());
        draft.setVersionCount(nextVersionNo);
        if (StringUtil.isBlank(draft.getStatus()) || AiFormDesignDraft.STATUS_APPLIED.equals(draft.getStatus())) {
            draft.setStatus(AiFormDesignDraft.STATUS_SAVED);
        }
        draft.preUpdate();
        aiFormDesignDraftDao.update(draft);

        return version;
    }

    @Transactional(readOnly = false)
    public void removeDraft(String draftId) {
        AiFormDesignDraft draft = getOwnedDraft(draftId);
        aiFormDesignDraftDao.deleteByLogic(draft);
        aiFormDesignDraftVersionDao.deleteByDraftId(draft.getId());
    }

    @Transactional(readOnly = false)
    public AiFormDesignApplyLog recordApplyLog(AiFormDesignApplyLog applyLog) {
        if (applyLog == null) {
            throw new IllegalArgumentException("Apply log cannot be empty");
        }
        if (StringUtil.isBlank(applyLog.getDraftId()) || StringUtil.isBlank(applyLog.getVersionId())) {
            throw new IllegalArgumentException("Draft id and version id cannot be empty");
        }
        getOwnedDraft(applyLog.getDraftId());
        applyLog.preInsert();
        aiFormDesignApplyLogDao.insert(applyLog);
        return applyLog;
    }

    @Transactional(readOnly = false)
    public AiFormDesignApplyLog markFormalSaved(String draftId, String versionId, String genTableId,
                                                String formName, String formTitle, String beforeChecksum,
                                                String afterChecksum, String detailJson) {
        if (StringUtil.isBlank(versionId)) {
            throw new IllegalArgumentException("Draft version id cannot be empty");
        }
        if (StringUtil.isBlank(genTableId)) {
            throw new IllegalArgumentException("Formal form id cannot be empty");
        }
        AiFormDesignDraft draft = getOwnedDraft(draftId);
        AiFormDesignDraftVersion version = aiFormDesignDraftVersionDao.get(versionId);
        if (version == null || !StringUtil.equals(draft.getId(), version.getDraftId())) {
            throw new IllegalArgumentException("Draft version does not belong to this draft");
        }
        GenTable genTable = genTableDao.get(genTableId);
        if (genTable == null) {
            throw new IllegalArgumentException("Formal form does not exist or has been deleted");
        }
        validateJsonText(detailJson, "Invalid formal save detail JSON");

        String safeFormName = StringUtil.isBlank(formName) ? genTable.getName() : formName;
        String safeFormTitle = StringUtil.isBlank(formTitle) ? genTable.getComments() : formTitle;
        Date now = new Date();

        draft.setStatus(AiFormDesignDraft.STATUS_FORMAL_SAVED);
        draft.setGenTableId(genTable.getId());
        draft.setFormName(safeFormName);
        draft.setFormTitle(safeFormTitle);
        draft.setCurrentVersionId(version.getId());
        draft.setLastAppliedAt(now);
        draft.preUpdate();
        aiFormDesignDraftDao.update(draft);

        AiFormDesignApplyLog applyLog = new AiFormDesignApplyLog();
        applyLog.setDraftId(draft.getId());
        applyLog.setVersionId(version.getId());
        applyLog.setGenTableId(genTable.getId());
        applyLog.setFormName(safeFormName);
        applyLog.setApplyType(AiFormDesignApplyLog.FORMAL_SAVE);
        applyLog.setApplyResult(AiFormDesignApplyLog.RESULT_SUCCESS);
        applyLog.setMessage("Formal form metadata saved");
        applyLog.setBeforeChecksum(beforeChecksum);
        applyLog.setAfterChecksum(afterChecksum);
        applyLog.setDetailJson(detailJson);
        applyLog.preInsert();
        aiFormDesignApplyLogDao.insert(applyLog);

        return applyLog;
    }

    @Transactional(readOnly = false)
    public AiFormDesignDraft importLocalDraft(AiFormDesignDraft draft, List<AiFormDesignDraftVersion> versions) {
        if (draft == null) {
            throw new IllegalArgumentException("Draft cannot be empty");
        }
        if (versions == null || versions.isEmpty()) {
            throw new IllegalArgumentException("Local draft must contain at least one version");
        }
        draft.setId(null);
        draft.setCurrentVersionId(null);
        draft.setVersionCount(0);
        if (StringUtil.isBlank(draft.getSourceType())) {
            draft.setSourceType("local_draft");
        }
        AiFormDesignDraft savedDraft = saveDraft(draft);
        List<AiFormDesignDraftVersion> importedVersions = new ArrayList<>();
        for (AiFormDesignDraftVersion version : versions) {
            if (version == null) {
                continue;
            }
            version.setId(null);
            version.setDraftId(savedDraft.getId());
            importedVersions.add(saveVersion(version));
        }
        if (importedVersions.isEmpty()) {
            throw new IllegalArgumentException("Local draft must contain at least one valid version");
        }
        return getDetail(savedDraft.getId());
    }

    private void normalizeDraft(AiFormDesignDraft draft, AiFormDesignDraft existing) {
        if (StringUtil.isBlank(draft.getStatus())) {
            draft.setStatus(existing == null || StringUtil.isBlank(existing.getStatus())
                    ? AiFormDesignDraft.STATUS_SAVED
                    : existing.getStatus());
        }
        if (StringUtil.isBlank(draft.getTargetMode())) {
            draft.setTargetMode(existing == null || StringUtil.isBlank(existing.getTargetMode())
                    ? AiFormDesignDraft.TARGET_MODE_CREATE
                    : existing.getTargetMode());
        }
        if (draft.getVersionCount() == null) {
            draft.setVersionCount(existing == null || existing.getVersionCount() == null ? 0 : existing.getVersionCount());
        }
        if (StringUtil.isBlank(draft.getCurrentVersionId()) && existing != null) {
            draft.setCurrentVersionId(existing.getCurrentVersionId());
        }
        if (draft.getLastAppliedAt() == null && existing != null) {
            draft.setLastAppliedAt(existing.getLastAppliedAt());
        }
    }

    private void normalizeVersion(AiFormDesignDraftVersion version, AiFormDesignDraft draft) {
        if (StringUtil.isBlank(version.getDslVersion())) {
            version.setDslVersion(AiFormDesignDraftVersion.DSL_VERSION_V1);
        }
        if (StringUtil.isBlank(version.getParentVersionId())) {
            version.setParentVersionId(draft.getCurrentVersionId());
        }
        if (version.getDslSize() == null && version.getDslJson() != null) {
            version.setDslSize(version.getDslJson().length());
        }
        if (StringUtil.isBlank(version.getChecksum())) {
            version.setChecksum(createChecksum(version.getDslJson()));
        }
    }

    private void validateDraft(AiFormDesignDraft draft) {
        if (StringUtil.isBlank(draft.getTitle())) {
            throw new IllegalArgumentException("Draft title cannot be empty");
        }
        if (!DRAFT_STATUS_LIST.contains(draft.getStatus())) {
            throw new IllegalArgumentException("Invalid draft status");
        }
        if (!TARGET_MODE_LIST.contains(draft.getTargetMode())) {
            throw new IllegalArgumentException("Invalid draft target mode");
        }
        validateJsonText(draft.getTagsJson(), "Invalid draft tags JSON");
        if (StringUtil.isNotBlank(draft.getGenTableId())) {
            GenTable genTable = genTableDao.get(draft.getGenTableId());
            if (genTable == null) {
                throw new IllegalArgumentException("Related form does not exist or has been deleted");
            }
        }
    }

    private void validateVersion(AiFormDesignDraftVersion version) {
        if (StringUtil.isBlank(version.getDslJson())) {
            throw new IllegalArgumentException("Draft version DSL cannot be empty");
        }
        if (!AiFormDesignDraftVersion.DSL_VERSION_V1.equals(version.getDslVersion())) {
            throw new IllegalArgumentException("Only DSL v1.0 is supported");
        }
        if (version.getDslJson().length() > MAX_DSL_JSON_LENGTH) {
            throw new IllegalArgumentException("Draft version content is too large");
        }
        JSON.parseObject(version.getDslJson());
        validateJsonText(version.getClarifyAnswersJson(), "Invalid clarify answers JSON");
        validateJsonText(version.getSchemaIssuesJson(), "Invalid schema issues JSON");
        validateJsonText(version.getDesignIssuesJson(), "Invalid design issues JSON");
        validateJsonText(version.getSchemaSummaryJson(), "Invalid schema summary JSON");
        validateJsonText(version.getDesignSummaryJson(), "Invalid design summary JSON");
        validateJsonText(version.getPreviewSummaryJson(), "Invalid preview summary JSON");
    }

    private void validateJsonText(String jsonText, String message) {
        if (StringUtil.isBlank(jsonText)) {
            return;
        }
        try {
            JSON.parse(jsonText);
        } catch (Exception ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private void restrictQueryToCurrentUser(AiFormDesignDraft query) {
        User currentUser = requireCurrentUser();
        if (!currentUser.isAdmin()) {
            query.setCreateBy(currentUser);
        }
    }

    private AiFormDesignDraft getOwnedDraft(String draftId) {
        if (StringUtil.isBlank(draftId)) {
            throw new IllegalArgumentException("Draft id cannot be empty");
        }
        AiFormDesignDraft draft = aiFormDesignDraftDao.get(draftId);
        if (draft == null) {
            throw new IllegalArgumentException("Draft does not exist or has been deleted");
        }
        User currentUser = requireCurrentUser();
        if (!currentUser.isAdmin()
                && (draft.getCreateBy() == null || !StringUtil.equals(currentUser.getId(), draft.getCreateBy().getId()))) {
            throw new IllegalArgumentException("No permission to access this draft");
        }
        return draft;
    }

    private User requireCurrentUser() {
        User currentUser = UserUtil.getCurrentUser();
        if (currentUser == null || StringUtil.isBlank(currentUser.getId())) {
            throw new IllegalArgumentException("Please sign in first");
        }
        return currentUser;
    }

    private String createChecksum(String text) {
        String value = text == null ? "" : text;
        long hash = 5381;
        for (int i = 0; i < value.length(); i++) {
            hash = ((hash << 5) + hash) + value.charAt(i);
            hash = hash & 0xffffffffL;
        }
        return "djb2-" + Long.toHexString(hash) + "-" + value.length();
    }
}
