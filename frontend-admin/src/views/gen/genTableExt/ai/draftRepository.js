import {
  createRuntimeDraftFromLocalDraft,
  deleteLocalDraft,
  getLocalDraft,
  listLocalDrafts,
  saveLocalDraft,
} from "@/views/gen/genTableExt/ai/draftStorage";
import {
  createRuntimeDraftFromRemoteDraft,
  deleteRemoteDraft,
  getRemoteDraft,
  listRemoteDrafts,
  saveRemoteDraft,
} from "@/views/gen/genTableExt/ai/remoteDraftProvider";

const STORAGE_REMOTE = 'remote'

export const listDrafts = async (options = {}) => {
  if (options.storage === STORAGE_REMOTE) {
    const result = await listRemoteDrafts(options.query || {})
    return result.rows
  }
  return listLocalDrafts()
}

export const getDraft = async (draftId, options = {}) => {
  if (options.storage === STORAGE_REMOTE) {
    return getRemoteDraft(draftId)
  }
  return getLocalDraft(draftId)
}

export const saveDraft = async (runtimeDraft = {}, options = {}) => {
  if (options.storage === STORAGE_REMOTE) {
    return saveRemoteDraft(runtimeDraft, options)
  }
  return saveLocalDraft(runtimeDraft, options)
}

export const deleteDraft = async (draftId, options = {}) => {
  if (options.storage === STORAGE_REMOTE) {
    return deleteRemoteDraft(draftId)
  }
  return deleteLocalDraft(draftId)
}

export const restoreDraft = async (draftId, versionId = '', options = {}) => {
  if (options.storage === STORAGE_REMOTE) {
    const remoteDraft = await getRemoteDraft(draftId)
    return createRuntimeDraftFromRemoteDraft(remoteDraft, versionId)
  }
  const localDraft = getLocalDraft(draftId)
  return createRuntimeDraftFromLocalDraft(localDraft, versionId)
}
