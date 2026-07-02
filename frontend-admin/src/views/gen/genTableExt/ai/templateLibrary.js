const STORAGE_KEY = 'ai-form-design-templates:v1'
const STORE_VERSION = '1.0'
const MAX_LOCAL_TEMPLATE_COUNT = 30

const nowIso = () => new Date().toISOString()

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const isBrowser = () => typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'

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

const createLocalId = (prefix = 'template') => {
  const randomPart = typeof crypto !== 'undefined' && crypto.randomUUID
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${prefix}-${randomPart}`
}

const getFields = (dsl = {}) => toArray(dsl.fields)

const summarizeDsl = (dsl = {}) => {
  const fields = getFields(dsl)
  const groups = toArray(dsl.layout?.groups)
  return {
    fieldCount: fields.length,
    listCount: fields.filter(field => field.isList).length,
    queryCount: fields.filter(field => field.isQuery).length,
    groupCount: groups.length,
  }
}

const createBuiltInTemplate = (config) => ({
  source: 'builtin',
  createdAt: '',
  updatedAt: '',
  dsl: null,
  schemaIssues: [],
  schemaSummary: {},
  designIssues: [],
  designSummary: {},
  ...config,
})

export const BUILTIN_FORM_TEMPLATES = [
  createBuiltInTemplate({
    id: 'builtin-oas-receive',
    title: '收文处理笺',
    category: '公文',
    style: 'document-form',
    tags: ['收文', '公文文单'],
    description: '适合登记来文单位、文号、收文日期和领导批示等收文处理流程。',
    requirement: '收文处理笺，需要来文单位、文号、标题、收文日期、缓急、局领导批示、办公室批分、附件',
    fields: ['来文单位', '文号', '标题', '收文日期', '缓急', '局领导批示', '办公室批分', '附件'],
  }),
  createBuiltInTemplate({
    id: 'builtin-oas-send',
    title: '发文处理笺',
    category: '公文',
    style: 'document-form',
    tags: ['发文', '公文文单'],
    description: '适合发文拟稿、核稿、主送抄送和附件管理。',
    requirement: '发文处理笺，需要标题、文号、发文日期、缓急、密级、主送单位、抄送单位、拟稿人、核稿人、领导意见、附件',
    fields: ['标题', '文号', '发文日期', '缓急', '密级', '主送单位', '抄送单位', '拟稿人', '核稿人', '领导意见', '附件'],
  }),
  createBuiltInTemplate({
    id: 'builtin-contract-approval',
    title: '合同审批表',
    category: '审批',
    style: 'approval',
    tags: ['合同', '审批'],
    description: '适合合同签订前的金额、甲乙方和部门/领导意见审批。',
    requirement: '合同审批表，需要合同名称、合同编号、甲方、乙方、金额、签订日期、经办人、附件、部门意见、领导意见',
    fields: ['合同名称', '合同编号', '甲方', '乙方', '金额', '签订日期', '经办人', '附件', '部门意见', '领导意见'],
  }),
  createBuiltInTemplate({
    id: 'builtin-purchase-apply',
    title: '采购申请表',
    category: '审批',
    style: 'approval',
    tags: ['采购', '申请'],
    description: '适合采购事项、金额数量和审批意见流转。',
    requirement: '采购申请表，需要名称、编号、申请人、申请部门、申请日期、采购金额、采购数量、说明、部门意见、领导意见、附件',
    fields: ['名称', '编号', '申请人', '申请部门', '申请日期', '采购金额', '采购数量', '说明', '部门意见', '领导意见', '附件'],
  }),
  createBuiltInTemplate({
    id: 'builtin-archive-register',
    title: '档案登记表',
    category: '登记',
    style: 'normal',
    tags: ['档案', '登记'],
    description: '适合档案题名、编号、年度、保管期限和附件登记。',
    requirement: '档案登记表，需要名称、编号、年度、类别、保管期限、责任部门、经办人、备注、附件',
    fields: ['名称', '编号', '年度', '类别', '保管期限', '责任部门', '经办人', '备注', '附件'],
  }),
]

const createEmptyStore = () => ({
  version: STORE_VERSION,
  updatedAt: nowIso(),
  templates: [],
})

const compactStore = (store = {}) => ({
  version: STORE_VERSION,
  updatedAt: nowIso(),
  templates: toArray(store.templates)
      .slice()
      .sort((left, right) => String(right.updatedAt || '').localeCompare(String(left.updatedAt || '')))
      .slice(0, MAX_LOCAL_TEMPLATE_COUNT),
})

export const readLocalTemplateStore = () => {
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
      templates: toArray(parsed?.templates),
    })
  } catch (error) {
    console.warn('FormDesignTemplateStore read failed', error)
    return createEmptyStore()
  }
}

export const writeLocalTemplateStore = (store = {}) => {
  if (!isBrowser()) {
    return createEmptyStore()
  }
  const nextStore = compactStore(store)
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextStore))
  return nextStore
}

export const listLocalTemplates = () => readLocalTemplateStore().templates

export const listAllTemplates = () => BUILTIN_FORM_TEMPLATES.concat(listLocalTemplates())

export const saveLocalTemplateFromDraft = (runtimeDraft = {}, options = {}) => {
  const dsl = clonePlain(runtimeDraft.dsl || {})
  const summary = summarizeDsl(dsl)
  const form = dsl.form || {}
  const layout = dsl.layout || {}
  const now = nowIso()
  const template = {
    id: createLocalId('local-template'),
    source: 'local',
    title: normalizeText(options.title || form.title || runtimeDraft.summary?.title || '未命名模板'),
    category: normalizeText(options.category || (layout.style === 'document-form' ? '公文' : layout.style === 'approval' ? '审批' : '自定义')),
    style: normalizeText(options.style || layout.style || runtimeDraft.summary?.style || 'normal'),
    tags: toArray(options.tags),
    description: normalizeText(options.description || `从草稿「${form.title || form.name || '未命名'}」保存`),
    requirement: normalizeText(options.requirement || runtimeDraft.sourceContent || dsl.raw?.sourceContent),
    fields: getFields(dsl).map(field => field.label || field.name).filter(Boolean),
    dsl,
    schemaIssues: clonePlain(toArray(runtimeDraft.schemaIssues)),
    schemaSummary: clonePlain(runtimeDraft.summary?.schema || {}),
    designIssues: clonePlain(toArray(runtimeDraft.designIssues)),
    designSummary: clonePlain(runtimeDraft.designSummary || {}),
    summary,
    createdAt: now,
    updatedAt: now,
  }
  const store = readLocalTemplateStore()
  const nextTemplates = [template].concat(toArray(store.templates))
  const nextStore = writeLocalTemplateStore({
    ...store,
    templates: nextTemplates,
  })
  return {
    template,
    store: nextStore,
  }
}

export const deleteLocalTemplate = (templateId) => {
  const id = normalizeText(templateId)
  const store = readLocalTemplateStore()
  const templates = toArray(store.templates).filter(template => template.id !== id)
  return writeLocalTemplateStore({
    ...store,
    templates,
  })
}

export const getTemplateFieldCount = (template = {}) => {
  if (template.dsl) {
    return summarizeDsl(template.dsl).fieldCount
  }
  return toArray(template.fields).length
}

export const createRuntimeDraftFromTemplate = (template = {}) => {
  if (!template.dsl) {
    return null
  }
  const dsl = clonePlain(template.dsl)
  const summary = summarizeDsl(dsl)
  return {
    id: createLocalId('template-draft'),
    sourceType: 'template',
    sourceContent: template.requirement || '',
    generator: 'template-library',
    createdAt: nowIso(),
    dsl,
    schemaIssues: clonePlain(toArray(template.schemaIssues)),
    designIssues: clonePlain(toArray(template.designIssues)),
    designSummary: clonePlain(template.designSummary || {}),
    clarifyAnswers: [],
    summary: {
      title: dsl.form?.title || template.title,
      style: dsl.layout?.style || template.style,
      ...summary,
      sourceLength: normalizeText(template.requirement).length,
      schema: clonePlain(template.schemaSummary || {}),
    },
  }
}

export const TEMPLATE_LIMITS = {
  maxLocalTemplateCount: MAX_LOCAL_TEMPLATE_COUNT,
}
