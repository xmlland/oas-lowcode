import {postAction} from "@/api/action";

const API_PREFIX = 'gen/aiFormDraft'

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

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

const parseJsonValue = (value, fallback) => {
  if (value === undefined || value === null || value === '') {
    return clonePlain(fallback)
  }
  if (typeof value !== 'string') {
    return clonePlain(value)
  }
  try {
    return JSON.parse(value)
  } catch (error) {
    console.warn('FormDesignRemoteDraft json parse failed', error)
    return clonePlain(fallback)
  }
}

const stringifyJsonValue = (value, fallback) => {
  const target = value === undefined || value === null ? fallback : value
  try {
    return JSON.stringify(target)
  } catch (error) {
    console.warn('FormDesignRemoteDraft json stringify failed', error)
    return JSON.stringify(fallback)
  }
}

const toIsoLikeText = (value) => {
  if (!value) {
    return ''
  }
  const text = String(value)
  return text.indexOf('T') >= 0 ? text : text.replace(' ', 'T')
}

const createChecksum = (value) => {
  const text = typeof value === 'string' ? value : JSON.stringify(value || {})
  let hash = 5381
  for (let index = 0; index < text.length; index += 1) {
    hash = ((hash << 5) + hash) + text.charCodeAt(index)
    hash >>>= 0
  }
  return `djb2-${hash.toString(16)}-${text.length}`
}

const getRuntimeDraftDslText = (runtimeDraft = {}) => JSON.stringify(runtimeDraft.dsl || {})

const buildRemoteDraftPayload = (runtimeDraft = {}, options = {}) => {
  const dslForm = runtimeDraft.dsl?.form || {}
  const target = options.target || {}
  return {
    id: normalizeText(options.draftId),
    title: normalizeText(options.title || runtimeDraft.summary?.title || dslForm.title || dslForm.name || 'AI表单草稿'),
    status: options.status || 'saved',
    targetMode: target.mode || (target.genTableId ? 'optimize' : 'create'),
    genTableId: normalizeText(target.genTableId),
    formName: normalizeText(target.formName || dslForm.name),
    formTitle: normalizeText(target.formTitle || dslForm.title),
    moduleName: normalizeText(target.module || dslForm.module),
    sourceType: options.sourceType || runtimeDraft.sourceType || runtimeDraft.dsl?.raw?.sourceType || 'natural_language',
    sourceContent: normalizeText(options.sourceContent || runtimeDraft.sourceContent || runtimeDraft.dsl?.raw?.sourceContent),
    sourceMode: options.mode || runtimeDraft.summary?.style || runtimeDraft.dsl?.layout?.style || 'auto',
    tagsJson: stringifyJsonValue(options.tags, []),
  }
}

const buildRemoteVersionPayload = (draftId, runtimeDraft = {}, options = {}) => {
  const dsl = clonePlain(runtimeDraft.dsl || {})
  const dslText = JSON.stringify(dsl)
  return {
    draftId,
    parentVersionId: normalizeText(options.parentVersionId),
    name: normalizeText(options.versionName),
    prompt: normalizeText(options.prompt || runtimeDraft.sourceContent || dsl.raw?.sourceContent),
    clarifyAnswersJson: stringifyJsonValue(options.clarifyAnswers || runtimeDraft.clarifyAnswers, []),
    dslVersion: dsl.version || '1.0',
    dslJson: dslText,
    schemaIssuesJson: stringifyJsonValue(runtimeDraft.schemaIssues, []),
    designIssuesJson: stringifyJsonValue(runtimeDraft.designIssues, []),
    schemaSummaryJson: stringifyJsonValue(runtimeDraft.summary?.schema, {}),
    designSummaryJson: stringifyJsonValue(runtimeDraft.designSummary, {}),
    previewSummaryJson: stringifyJsonValue(options.previewSummary, {}),
    checksum: options.checksum || createChecksum(dsl),
    dslSize: dslText.length,
  }
}

export const normalizeRemoteVersion = (version = {}) => {
  const dsl = parseJsonValue(version.dslJson, {})
  return {
    id: version.id,
    draftId: version.draftId,
    versionNo: version.versionNo,
    parentVersionId: version.parentVersionId || '',
    name: version.name || `V${version.versionNo || 1}`,
    prompt: version.prompt || '',
    clarifyAnswers: parseJsonValue(version.clarifyAnswersJson, []),
    dsl,
    schemaIssues: parseJsonValue(version.schemaIssuesJson, []),
    designIssues: parseJsonValue(version.designIssuesJson, []),
    schemaSummary: parseJsonValue(version.schemaSummaryJson, {}),
    designSummary: parseJsonValue(version.designSummaryJson, {}),
    previewSummary: parseJsonValue(version.previewSummaryJson, {}),
    checksum: version.checksum || createChecksum(dsl),
    createdAt: toIsoLikeText(version.createDate),
  }
}

export const normalizeRemoteDraft = (remoteDraft = {}) => {
  const versions = toArray(remoteDraft.versions).map(normalizeRemoteVersion)
  return {
    id: remoteDraft.id,
    title: remoteDraft.title || 'AI表单草稿',
    status: remoteDraft.status || 'saved',
    target: {
      mode: remoteDraft.targetMode || (remoteDraft.genTableId ? 'optimize' : 'create'),
      genTableId: remoteDraft.genTableId || '',
      formName: remoteDraft.formName || '',
      formTitle: remoteDraft.formTitle || '',
      module: remoteDraft.moduleName || '',
    },
    source: {
      type: remoteDraft.sourceType || 'remote-draft',
      content: remoteDraft.sourceContent || '',
      mode: remoteDraft.sourceMode || 'auto',
    },
    currentVersionId: remoteDraft.currentVersionId || '',
    versionCount: remoteDraft.versionCount || versions.length,
    tags: parseJsonValue(remoteDraft.tagsJson, []),
    createdAt: toIsoLikeText(remoteDraft.createDate),
    updatedAt: toIsoLikeText(remoteDraft.updateDate || remoteDraft.createDate),
    lastAppliedAt: toIsoLikeText(remoteDraft.lastAppliedAt),
    versions,
    currentVersion: remoteDraft.currentVersion ? normalizeRemoteVersion(remoteDraft.currentVersion) : null,
    storage: 'remote',
  }
}

export const createRuntimeDraftFromRemoteDraft = (remoteDraft = {}, versionId = '') => {
  const versions = toArray(remoteDraft.versions)
  const version = versions.find(item => item.id === versionId)
      || versions.find(item => item.id === remoteDraft.currentVersionId)
      || remoteDraft.currentVersion
      || versions[versions.length - 1]
  if (!version || !version.dsl) {
    throw new Error('服务器草稿版本不存在或内容不完整')
  }

  const fields = toArray(version.dsl.fields)
  const groups = toArray(version.dsl.layout?.groups)
  return {
    id: version.id,
    sourceType: remoteDraft.source?.type || 'remote-draft',
    sourceContent: version.prompt || remoteDraft.source?.content || '',
    generator: version.dsl.generator || 'remote-draft',
    createdAt: version.createdAt || remoteDraft.createdAt,
    dsl: clonePlain(version.dsl),
    schemaIssues: clonePlain(toArray(version.schemaIssues)),
    designIssues: clonePlain(toArray(version.designIssues)),
    clarifyAnswers: clonePlain(toArray(version.clarifyAnswers)),
    designSummary: clonePlain(version.designSummary || {}),
    summary: {
      title: version.dsl.form?.title || remoteDraft.title,
      style: version.dsl.layout?.style || remoteDraft.source?.mode || '',
      fieldCount: fields.length,
      listCount: fields.filter(field => field.isList).length,
      queryCount: fields.filter(field => field.isQuery).length,
      groupCount: groups.length,
      sourceLength: normalizeText(version.prompt || remoteDraft.source?.content).length,
      schema: clonePlain(version.schemaSummary || {}),
    },
  }
}

export const listRemoteDrafts = async (query = {}) => {
  const response = await postAction(`${API_PREFIX}/list`, query)
  return {
    rows: toArray(response.rows).map(normalizeRemoteDraft),
    total: Number(response.total || 0),
    raw: response,
  }
}

export const getRemoteDraft = async (draftId) => {
  const response = await postAction(`${API_PREFIX}/detail`, {id: draftId})
  return normalizeRemoteDraft(response.data?.draft || {})
}

export const saveRemoteDraft = async (runtimeDraft = {}, options = {}) => {
  if (!runtimeDraft || !runtimeDraft.dsl) {
    throw new Error('缺少可保存的草稿 DSL')
  }
  const dslText = getRuntimeDraftDslText(runtimeDraft)
  if (dslText.length > 1024 * 1024) {
    throw new Error('草稿内容过大，请先导出 DSL 文件保存')
  }

  const draftResponse = await postAction(`${API_PREFIX}/save`, buildRemoteDraftPayload(runtimeDraft, options))
  const savedDraft = normalizeRemoteDraft(draftResponse.data?.draft || {})
  const versionResponse = await postAction(
      `${API_PREFIX}/saveVersion`,
      buildRemoteVersionPayload(savedDraft.id, runtimeDraft, options)
  )
  return {
    draft: normalizeRemoteDraft(versionResponse.data?.draft || savedDraft),
    version: normalizeRemoteVersion(versionResponse.data?.version || {}),
    created: !options.draftId,
    raw: {
      draftResponse,
      versionResponse,
    },
  }
}

export const deleteRemoteDraft = async (draftId) => postAction(`${API_PREFIX}/remove`, {id: draftId})

export const recordRemoteDraftApplyLog = async (payload = {}) => postAction(`${API_PREFIX}/applyLog`, payload)

export const markRemoteDraftSaved = async (payload = {}) => postAction(`${API_PREFIX}/markSaved`, payload)

export const markRemoteDraftFormalSaved = async (payload = {}) => postAction(`${API_PREFIX}/markFormalSaved`, payload)

export const importLocalDraftToRemote = async (localDraft = {}) => {
  const versions = toArray(localDraft.versions)
  const payload = {
    draft: {
      title: localDraft.title,
      targetMode: localDraft.target?.mode,
      genTableId: localDraft.target?.genTableId,
      formName: localDraft.target?.formName,
      formTitle: localDraft.target?.formTitle,
      moduleName: localDraft.target?.module,
      sourceType: 'local_draft',
      sourceContent: localDraft.source?.content,
      sourceMode: localDraft.source?.mode,
      tagsJson: stringifyJsonValue(localDraft.tags, []),
    },
    versions: versions.map(version => ({
      name: version.name,
      prompt: version.prompt,
      clarifyAnswersJson: stringifyJsonValue(version.clarifyAnswers, []),
      dslVersion: version.dsl?.version || '1.0',
      dslJson: JSON.stringify(version.dsl || {}),
      schemaIssuesJson: stringifyJsonValue(version.schemaIssues, []),
      designIssuesJson: stringifyJsonValue(version.designIssues, []),
      schemaSummaryJson: stringifyJsonValue(version.schemaSummary, {}),
      designSummaryJson: stringifyJsonValue(version.designSummary, {}),
      previewSummaryJson: stringifyJsonValue(version.previewSummary, {}),
      checksum: version.checksum || createChecksum(version.dsl),
      dslSize: JSON.stringify(version.dsl || {}).length,
    })),
  }
  const response = await postAction(`${API_PREFIX}/importLocal`, payload)
  return normalizeRemoteDraft(response.data?.draft || {})
}
