const STORAGE_KEY = 'ai-form-design-drafts:v1'
const STORE_VERSION = '1.0'
const MAX_DRAFT_COUNT = 50
const MAX_VERSION_COUNT = 10
const MAX_DSL_TEXT_LENGTH = 500 * 1024

const isBrowser = () => typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'

const nowIso = () => new Date().toISOString()

const clonePlain = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (error) {
    return value
  }
}

const createLocalId = (prefix = 'local') => {
  const randomPart = typeof crypto !== 'undefined' && crypto.randomUUID
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${prefix}-${randomPart}`
}

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const createChecksum = (value) => {
  const text = typeof value === 'string' ? value : JSON.stringify(value || {})
  let hash = 5381
  for (let index = 0; index < text.length; index += 1) {
    hash = ((hash << 5) + hash) + text.charCodeAt(index)
    hash >>>= 0
  }
  return `djb2-${hash.toString(16)}-${text.length}`
}

const createEmptyStore = () => ({
  version: STORE_VERSION,
  updatedAt: nowIso(),
  drafts: [],
})

const sortDrafts = (drafts = []) => drafts
    .slice()
    .sort((left, right) => String(right.updatedAt || '').localeCompare(String(left.updatedAt || '')))

const trimDraftVersions = (versions = []) => versions
    .slice(-MAX_VERSION_COUNT)
    .map((version, index) => ({
      ...version,
      versionNo: index + 1,
      name: version.name || `V${index + 1}`,
    }))

const compactStore = (store) => {
  const drafts = sortDrafts(toArray(store.drafts))
      .slice(0, MAX_DRAFT_COUNT)
      .map(draft => ({
        ...draft,
        versions: trimDraftVersions(draft.versions),
      }))

  return {
    version: STORE_VERSION,
    updatedAt: nowIso(),
    drafts,
  }
}

export const readLocalDraftStore = () => {
  if (!isBrowser()) {
    return createEmptyStore()
  }
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return createEmptyStore()
    }
    const parsed = JSON.parse(raw)
    return compactStore({
      ...parsed,
      drafts: toArray(parsed?.drafts),
    })
  } catch (error) {
    console.warn('FormDesignLocalDraftStore read failed', error)
    return createEmptyStore()
  }
}

export const writeLocalDraftStore = (store) => {
  if (!isBrowser()) {
    return createEmptyStore()
  }
  const nextStore = compactStore(store || createEmptyStore())
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextStore))
  return nextStore
}

export const listLocalDrafts = () => readLocalDraftStore().drafts

export const getLocalDraft = (draftId) => {
  const id = normalizeText(draftId)
  if (!id) {
    return null
  }
  return listLocalDrafts().find(draft => draft.id === id) || null
}

const getRuntimeDraftDslText = (runtimeDraft = {}) => JSON.stringify(runtimeDraft.dsl || {})

const assertDraftCanSave = (runtimeDraft = {}) => {
  if (!runtimeDraft || !runtimeDraft.dsl) {
    throw new Error('缺少可保存的草稿 DSL')
  }
  const dslText = getRuntimeDraftDslText(runtimeDraft)
  if (dslText.length > MAX_DSL_TEXT_LENGTH) {
    throw new Error('草稿内容过大，请先导出 DSL 文件保存')
  }
}

const buildTarget = (runtimeDraft = {}, options = {}) => {
  const dslForm = runtimeDraft.dsl?.form || {}
  const target = options.target || {}
  return {
    mode: target.mode || (target.genTableId ? 'optimize' : 'create'),
    genTableId: normalizeText(target.genTableId),
    formName: normalizeText(target.formName || dslForm.name),
    module: normalizeText(target.module || dslForm.module),
  }
}

const buildSource = (runtimeDraft = {}, options = {}) => ({
  type: options.sourceType || runtimeDraft.sourceType || runtimeDraft.dsl?.raw?.sourceType || 'natural_language',
  content: normalizeText(options.sourceContent || runtimeDraft.sourceContent || runtimeDraft.dsl?.raw?.sourceContent),
  mode: options.mode || runtimeDraft.summary?.style || runtimeDraft.dsl?.layout?.style || 'auto',
})

const buildVersion = (runtimeDraft = {}, draftId, versionNo, options = {}) => {
  const dsl = clonePlain(runtimeDraft.dsl || {})
  const checksum = createChecksum(dsl)
  return {
    id: createLocalId('local-version'),
    draftId,
    versionNo,
    parentVersionId: normalizeText(options.parentVersionId),
    name: options.versionName || `V${versionNo}`,
    prompt: normalizeText(options.prompt || runtimeDraft.sourceContent || dsl.raw?.sourceContent),
    clarifyAnswers: toArray(options.clarifyAnswers),
    dsl,
    schemaIssues: clonePlain(toArray(runtimeDraft.schemaIssues)),
    designIssues: clonePlain(toArray(runtimeDraft.designIssues)),
    schemaSummary: clonePlain(runtimeDraft.summary?.schema || {}),
    designSummary: clonePlain(runtimeDraft.designSummary || {}),
    previewSummary: clonePlain(options.previewSummary || {}),
    checksum,
    createdAt: nowIso(),
  }
}

const buildNewDraft = (runtimeDraft = {}, options = {}) => {
  const draftId = createLocalId('local-draft')
  const dslForm = runtimeDraft.dsl?.form || {}
  const firstVersion = buildVersion(runtimeDraft, draftId, 1, options)
  const createdAt = nowIso()
  return {
    id: draftId,
    title: normalizeText(options.title || runtimeDraft.summary?.title || dslForm.title || dslForm.name || 'AI表单草稿'),
    status: 'saved',
    target: buildTarget(runtimeDraft, options),
    source: buildSource(runtimeDraft, options),
    currentVersionId: firstVersion.id,
    versionCount: 1,
    tags: toArray(options.tags),
    createdAt,
    updatedAt: createdAt,
    lastAppliedAt: '',
    versions: [firstVersion],
  }
}

export const saveLocalDraft = (runtimeDraft = {}, options = {}) => {
  assertDraftCanSave(runtimeDraft)

  const store = readLocalDraftStore()
  const draftId = normalizeText(options.draftId)
  const drafts = toArray(store.drafts)
  const existingIndex = drafts.findIndex(item => item.id === draftId)

  if (existingIndex < 0) {
    const nextDraft = buildNewDraft(runtimeDraft, options)
    const nextStore = writeLocalDraftStore({
      ...store,
      drafts: [nextDraft].concat(drafts),
    })
    return {
      draft: nextDraft,
      version: nextDraft.versions[0],
      store: nextStore,
      created: true,
    }
  }

  const existing = drafts[existingIndex]
  const existingVersions = toArray(existing.versions)
  const nextVersionNo = existingVersions.length + 1
  const nextVersion = buildVersion(runtimeDraft, existing.id, nextVersionNo, {
    ...options,
    parentVersionId: options.parentVersionId || existing.currentVersionId,
  })
  const nextVersions = trimDraftVersions(existingVersions.concat(nextVersion))
  const nextDraft = {
    ...existing,
    title: normalizeText(options.title || runtimeDraft.summary?.title || existing.title),
    status: 'saved',
    target: buildTarget(runtimeDraft, {
      ...options,
      target: {
        ...(existing.target || {}),
        ...(options.target || {}),
      },
    }),
    source: buildSource(runtimeDraft, options),
    currentVersionId: nextVersion.id,
    versionCount: nextVersions.length,
    updatedAt: nowIso(),
    versions: nextVersions,
  }
  const nextDrafts = drafts.slice()
  nextDrafts[existingIndex] = nextDraft
  const nextStore = writeLocalDraftStore({
    ...store,
    drafts: nextDrafts,
  })
  return {
    draft: nextDraft,
    version: nextVersion,
    store: nextStore,
    created: false,
  }
}

export const deleteLocalDraft = (draftId) => {
  const id = normalizeText(draftId)
  const store = readLocalDraftStore()
  const drafts = toArray(store.drafts).filter(draft => draft.id !== id)
  return writeLocalDraftStore({
    ...store,
    drafts,
  })
}

export const getCurrentDraftVersion = (localDraft = {}) => {
  const versions = toArray(localDraft.versions)
  return versions.find(version => version.id === localDraft.currentVersionId) || versions[versions.length - 1] || null
}

export const createRuntimeDraftFromLocalDraft = (localDraft = {}, versionId = '') => {
  const versions = toArray(localDraft.versions)
  const version = versions.find(item => item.id === versionId) || getCurrentDraftVersion(localDraft)
  if (!version || !version.dsl) {
    throw new Error('草稿版本不存在或内容不完整')
  }

  const fields = toArray(version.dsl.fields)
  const groups = toArray(version.dsl.layout?.groups)
  return {
    id: version.id,
    sourceType: localDraft.source?.type || 'local-draft',
    sourceContent: version.prompt || localDraft.source?.content || '',
    generator: version.dsl.generator || 'local-draft',
    createdAt: version.createdAt || localDraft.createdAt,
    dsl: clonePlain(version.dsl),
    schemaIssues: clonePlain(toArray(version.schemaIssues)),
    designIssues: clonePlain(toArray(version.designIssues)),
    clarifyAnswers: clonePlain(toArray(version.clarifyAnswers)),
    designSummary: clonePlain(version.designSummary || {}),
    summary: {
      title: version.dsl.form?.title || localDraft.title,
      style: version.dsl.layout?.style || localDraft.source?.mode || '',
      fieldCount: fields.length,
      listCount: fields.filter(field => field.isList).length,
      queryCount: fields.filter(field => field.isQuery).length,
      groupCount: groups.length,
      sourceLength: normalizeText(version.prompt || localDraft.source?.content).length,
      schema: clonePlain(version.schemaSummary || {}),
    },
  }
}

export const LOCAL_DRAFT_LIMITS = {
  maxDraftCount: MAX_DRAFT_COUNT,
  maxVersionCount: MAX_VERSION_COUNT,
  maxDslTextLength: MAX_DSL_TEXT_LENGTH,
}
