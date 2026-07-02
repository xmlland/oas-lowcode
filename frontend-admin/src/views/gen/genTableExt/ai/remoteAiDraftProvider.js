import {postAction} from "@/api/action";
import {summarizeSchemaIssues, validateFormDesignDslSchema} from "@/views/gen/genTableExt/ai/dslSchema";
import {ensureDslDictionarySuggestions} from "@/views/gen/genTableExt/ai/dictionarySuggestions";

const API_PREFIX = 'gen/aiFormDesign'

const USER_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.User'
const OFFICE_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Office'
const AREA_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Area'
const ZFORM_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.common.Zform'

const JAVA_TYPE_BY_TYPE = {
  user: USER_JAVA_TYPE,
  users: USER_JAVA_TYPE,
  office: OFFICE_JAVA_TYPE,
  area: AREA_JAVA_TYPE,
  tree: ZFORM_JAVA_TYPE,
  modalSelect: ZFORM_JAVA_TYPE,
}

const TYPE_CONFIG = {
  text: {showType: 'input', jdbcType: 'varchar', javaType: 'String'},
  textarea: {showType: 'textarea', jdbcType: 'longtext', javaType: 'String'},
  integer: {showType: 'integer', jdbcType: 'int', javaType: 'Integer'},
  decimal: {showType: 'decimal', jdbcType: 'decimal', javaType: 'BigDecimal'},
  select: {showType: 'select', jdbcType: 'varchar', javaType: 'String'},
  radio: {showType: 'radiobox', jdbcType: 'varchar', javaType: 'String'},
  checkbox: {showType: 'checkbox', jdbcType: 'varchar', javaType: 'String'},
  switch: {showType: 'switch', jdbcType: 'varchar', javaType: 'String'},
  date: {showType: 'dateselect', jdbcType: 'datetime', javaType: 'Date'},
  user: {showType: 'treeselectRedio', jdbcType: 'varchar', javaType: USER_JAVA_TYPE},
  users: {showType: 'treeselectCheck', jdbcType: 'varchar', javaType: USER_JAVA_TYPE},
  office: {showType: 'officeselectTree', jdbcType: 'varchar', javaType: OFFICE_JAVA_TYPE},
  area: {showType: 'areaselect', jdbcType: 'varchar', javaType: AREA_JAVA_TYPE},
  tree: {showType: 'treeSelect', jdbcType: 'varchar', javaType: ZFORM_JAVA_TYPE},
  modalSelect: {showType: 'gridselect', jdbcType: 'varchar', javaType: ZFORM_JAVA_TYPE},
  modalMultiSelect: {showType: 'modalMultiSelect', jdbcType: 'varchar', javaType: 'String'},
  upload: {showType: 'fileupload', jdbcType: 'varchar', javaType: 'String'},
  imageUpload: {showType: 'fileuploadpic', jdbcType: 'varchar', javaType: 'String'},
  onlineFile: {showType: 'fileuploadonline', jdbcType: 'varchar', javaType: 'String'},
  richText: {showType: 'richText', jdbcType: 'longtext', javaType: 'String'},
  serialNo: {showType: 'serialNo', jdbcType: 'varchar', javaType: 'String'},
}

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

const parseDslJson = (dslJson) => {
  if (typeof dslJson !== 'string' || !dslJson.trim()) {
    throw new Error('远程 AI 未返回 DSL JSON')
  }
  try {
    return JSON.parse(dslJson)
  } catch (error) {
    throw new Error(`远程 AI DSL JSON 解析失败：${error.message}`)
  }
}

export const normalizeRemoteDsl = (dsl = {}) => {
  const normalized = clonePlain(dsl) || {}
  const fields = toArray(normalized.fields)
  normalized.fields = fields.map((field = {}, index) => {
    const type = field.type || 'text'
    const config = TYPE_CONFIG[type] || TYPE_CONFIG.text
    const span = Number(field.form?.colProps?.span || field.span || 12)
    const isList = field.list?.show !== undefined ? Boolean(field.list.show) : field.isList !== false
    const isQuery = field.query?.enabled !== undefined ? Boolean(field.query.enabled) : field.isQuery === true
    const javaType = JAVA_TYPE_BY_TYPE[type] || field.javaType || field.db?.javaType || config.javaType
    return {
      ...field,
      type,
      showType: field.showType || config.showType,
      javaField: field.javaField || '',
      jdbcType: field.jdbcType || field.db?.jdbcType || config.jdbcType,
      javaType,
      required: field.required === true,
      readonly: field.readonly === true,
      span,
      isForm: field.isForm !== false,
      isList,
      isQuery,
      formSort: field.formSort || (index + 1) * 10,
      listSort: field.listSort || (index + 1) * 10,
      dateType: field.dateType || (type === 'date' ? 'yyyy-MM-dd' : ''),
      dictType: field.dictType || '',
    }
  })
  return ensureDslDictionarySuggestions(normalized)
}

const buildSummary = (dsl = {}, sourceContent = '', schemaIssues = []) => {
  const fields = toArray(dsl.fields)
  const groups = toArray(dsl.layout?.groups)
  return {
    title: dsl.form?.title || dsl.form?.name || 'AI表单草稿',
    style: dsl.layout?.style || '',
    fieldCount: fields.length,
    listCount: fields.filter(field => field.isList).length,
    queryCount: fields.filter(field => field.isQuery).length,
    groupCount: groups.length,
    sourceLength: normalizeText(sourceContent).length,
    schema: summarizeSchemaIssues(schemaIssues),
  }
}

const createRemoteAiError = (result = {}) => {
  const error = new Error(result.message || result.errorCode || '远程 AI 生成失败')
  error.code = result.errorCode || 'AI_REMOTE_GENERATE_FAILED'
  error.requestId = result.requestId || ''
  error.result = result
  return error
}

const hasSchemaError = (schemaIssues = []) => {
  return toArray(schemaIssues).some(issue => issue.level === 'error')
}

const createFrontendSchemaError = (schemaIssues = [], result = {}) => {
  const error = new Error('Remote AI DSL failed frontend schema validation')
  error.code = 'AI_FRONTEND_SCHEMA_INVALID'
  error.requestId = result.requestId || ''
  error.result = result
  error.schemaIssues = schemaIssues
  return error
}

export const generateRemoteFormDesignDslDraft = async (text = '', options = {}) => {
  const sourceContent = normalizeText(text)
  if (!sourceContent) {
    throw new Error('请先输入表单需求')
  }

  const response = await postAction(`${API_PREFIX}/generateDsl`, {
    requirement: sourceContent,
    mode: options.target?.mode || 'create',
    scene: options.style || options.scene || 'normal',
    templateCode: options.templateCode || '',
    currentDslJson: options.currentDslJson || '',
    currentForm: {
      genTableId: options.target?.genTableId || '',
      formName: options.target?.formName || '',
      formTitle: options.target?.formTitle || '',
      moduleName: options.target?.module || '',
    },
    options: {
      saveAsServerDraft: false,
      fallbackToLocal: options.fallbackToLocal !== false,
      temperature: options.temperature ?? 0.2,
    },
  })

  const result = response.data?.result || {}
  if (!result.success) {
    throw createRemoteAiError(result)
  }

  const dsl = normalizeRemoteDsl(parseDslJson(result.dslJson))
  const generatedAt = new Date().toISOString()
  dsl.generator = dsl.generator || 'remote-ai'
  dsl.generatedAt = dsl.generatedAt || generatedAt
  dsl.raw = {
    ...(dsl.raw || {}),
    sourceType: 'remote-ai',
    sourceContent,
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    requestId: result.requestId || '',
  }

  const schemaIssues = validateFormDesignDslSchema(dsl)
  if (options.rejectOnSchemaError !== false && hasSchemaError(schemaIssues)) {
    throw createFrontendSchemaError(schemaIssues, result)
  }

  return {
    id: `remote-ai-${Date.now()}`,
    requestId: result.requestId || '',
    sourceType: 'remote-ai',
    sourceContent,
    generator: 'remote-ai',
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    elapsedMs: result.elapsedMs || 0,
    rawOutputPreview: result.rawOutputPreview || '',
    createdAt: dsl.generatedAt,
    dsl: clonePlain(dsl),
    schemaIssues,
    backendIssues: toArray(result.issues),
    designIssues: [],
    clarifyAnswers: toArray(options.clarifyAnswers),
    designSummary: {},
    summary: buildSummary(dsl, sourceContent, schemaIssues),
    remoteResult: result,
  }
}
