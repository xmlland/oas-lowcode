import {
  DSL_SCHEMA_VERSION,
  FIELD_TYPE_ALIAS_MAP,
  FIELD_TYPE_OPTIONS,
  summarizeSchemaIssues,
  validateFormDesignDslSchema,
} from "@/views/gen/genTableExt/ai/dslSchema";
import {
  summarizeFormMaterialIssues,
  validateFormMaterialSchema,
} from "@/views/gen/genTableExt/ai/formMaterialSchema";
import {
  ensureUniqueFieldName,
} from "@/views/gen/genTableExt/ai/fieldNameInfer";
import {
  inferMaterialDateType,
  normalizeMaterialFieldSuggestion,
} from "@/views/gen/genTableExt/ai/formMaterialQualityRules";
import {
  ensureDslDictionarySuggestions,
  normalizeDictionaryItems,
} from "@/views/gen/genTableExt/ai/dictionarySuggestions";

const GENERATOR_NAME = 'form-material-local'

const USER_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.User'
const OFFICE_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Office'
const AREA_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Area'
const ZFORM_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.common.Zform'

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

const FORM_NAME_MAP = [
  {keywords: ['合同', '审批'], name: 'contract_apply', title: '合同审批'},
  {keywords: ['收文'], name: 'oas_receive', title: '收文处理笺'},
  {keywords: ['发文'], name: 'oas_send', title: '发文处理笺'},
  {keywords: ['采购'], name: 'purchase_apply', title: '采购申请'},
  {keywords: ['设备', '台账'], name: 'device_ledger', title: '设备台账'},
  {keywords: ['档案'], name: 'archive_register', title: '档案登记'},
]

const SELECT_KEYWORDS = ['状态', '类型', '类别', '级别', '缓急', '密级', '是否', '性别']
const DATE_KEYWORDS = ['日期', '时间', '期限', '生日']
const INTEGER_KEYWORDS = ['数量', '人数', '次数', '天数', '年度', '年']
const DECIMAL_KEYWORDS = ['金额', '费用', '价格', '单价', '合计', '总额', '预算']
const UPLOAD_KEYWORDS = ['附件', '材料', '文件', '图片', '影像']
const TEXTAREA_KEYWORDS = ['意见', '说明', '备注', '描述', '内容', '批示', '批分', '结果', '原因', '要求']
const USER_KEYWORDS = ['申请人', '经办人', '负责人', '拟稿人', '核稿人', '审批人', '处理人']
const USERS_KEYWORDS = ['参与人', '协办人', '会签人']
const OFFICE_KEYWORDS = ['部门', '机构', '单位']
const TITLE_KEYWORDS = ['标题', '名称', '主题']
const QUERY_KEYWORDS = ['名称', '编号', '文号', '标题', '日期', '状态', '类型', '类别', '密级', '缓急', '申请人', '经办人']
const REQUIRED_KEYWORDS = ['名称', '编号', '标题', '日期', '金额', '申请人', '经办人', '来文单位', '文号']
const FULL_SPAN_TYPES = ['textarea', 'upload', 'imageUpload', 'onlineFile', 'richText']
const NAME_VALUE_TYPES = ['user', 'users', 'office', 'area', 'modalSelect']
const LIST_FIELD_LIMIT = 7
const QUERY_FIELD_LIMIT = 5
const MIN_LIST_PRIORITY = 55
const MIN_QUERY_PRIORITY = 65
const QUERY_MODE_OPTIONS = ['', 'input', 'like', 'exact', 'select', 'date-range', 'range']

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

const includesAny = (value, keywords = []) => {
  const text = normalizeText(value)
  return keywords.some(keyword => text.indexOf(keyword) >= 0)
}

const toPriority = (value) => {
  if (value === undefined || value === null || value === '') {
    return null
  }
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) {
    return null
  }
  return Math.max(0, Math.min(100, numberValue))
}

const toBooleanHint = (value) => {
  if (value === true || value === false) {
    return value
  }
  if (value === 'true' || value === '1' || value === 1) {
    return true
  }
  if (value === 'false' || value === '0' || value === 0) {
    return false
  }
  return null
}

const normalizeQueryMode = (value = '') => {
  const mode = normalizeText(value)
  return QUERY_MODE_OPTIONS.includes(mode) ? mode : ''
}

const toSnakeCase = (value, fallback = 'field') => {
  const text = normalizeText(value)
      .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
      .replace(/[^a-zA-Z0-9]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_+|_+$/g, '')
      .toLowerCase()
  const normalized = text || fallback
  return /^[a-z]/.test(normalized) ? normalized : `${fallback}_${normalized}`.replace(/_+/g, '_')
}

const isMeaninglessFieldName = (value = '') => {
  const text = normalizeText(value).toLowerCase()
  return /^(field|col|column)_?\d+$/.test(text)
      || ['field', 'column', 'unnamed_field'].includes(text)
}

const canonicalType = (typeHint = '') => {
  const type = FIELD_TYPE_ALIAS_MAP[typeHint] || typeHint
  return FIELD_TYPE_OPTIONS.includes(type) ? type : ''
}

const inferType = (label = '') => {
  if (includesAny(label, UPLOAD_KEYWORDS)) return label.indexOf('图片') >= 0 ? 'imageUpload' : 'upload'
  if (includesAny(label, DATE_KEYWORDS)) return 'date'
  if (includesAny(label, DECIMAL_KEYWORDS)) return 'decimal'
  if (includesAny(label, INTEGER_KEYWORDS)) return 'integer'
  if (includesAny(label, USERS_KEYWORDS)) return 'users'
  if (includesAny(label, USER_KEYWORDS)) return 'user'
  if (includesAny(label, OFFICE_KEYWORDS)) return 'office'
  if (includesAny(label, TEXTAREA_KEYWORDS)) return 'textarea'
  if (includesAny(label, SELECT_KEYWORDS)) return 'select'
  return 'text'
}

const inferStyle = (material = {}) => {
  if (['document-form', 'approval'].includes(material.scene)) {
    return material.scene
  }
  return 'normal'
}

const inferFormMeta = (material = {}, options = {}) => {
  const fieldText = toArray(material.fields).map(field => field.label).join(' ')
  const inferredTitle = material.scene === 'ledger' && includesAny(fieldText, ['设备名称', '设备编号'])
      ? '设备台账'
      : ''
  const sourceText = `${material.title || ''} ${material.source?.name || ''} ${inferredTitle} ${fieldText}`
  const matched = FORM_NAME_MAP.find(item => item.keywords.every(keyword => sourceText.indexOf(keyword) >= 0))
  const title = normalizeText(options.form?.title || material.title || material.source?.name || inferredTitle || matched?.title) || '材料生成表单'
  return {
    name: toSnakeCase(options.form?.name || matched?.name || material.meta?.formName || 'material_generated_form', 'form'),
    title,
    module: toSnakeCase(options.form?.module || 'oas', 'module'),
    tableType: options.form?.tableType || '0',
    pkColumnName: options.form?.pkColumnName || 'id',
  }
}

const normalizeGroupKey = (groupKey = '', index = 0) => {
  const normalized = normalizeText(groupKey)
  if (!normalized || normalized === '基本信息') {
    return 'base'
  }
  if (normalized === '详细信息' || normalized === '审批信息' || normalized === '办理意见') {
    return 'detail'
  }
  return toSnakeCase(normalized, `group_${index + 1}`)
}

const inferGroupKey = (field = {}, type = '', style = 'normal', index = 0) => {
  if (field.groupKey) {
    return normalizeGroupKey(field.groupKey, index)
  }
  const label = normalizeText(field.label)
  if (style === 'document-form' && includesAny(label, ['批示', '批分', '意见', '结果', '附件'])) {
    return 'approval'
  }
  if (FULL_SPAN_TYPES.includes(type) || includesAny(label, TEXTAREA_KEYWORDS)) {
    return 'detail'
  }
  return 'base'
}

const inferSpan = (field = {}, type = '', style = 'normal') => {
  const label = normalizeText(field.label)
  if (style === 'document-form' && includesAny(label, ['标题', '批示', '批分', '意见', '结果', '附件'])) {
    return 24
  }
  if (includesAny(label, TITLE_KEYWORDS) || includesAny(label, TEXTAREA_KEYWORDS) || FULL_SPAN_TYPES.includes(type)) {
    return 24
  }
  const span = Number(field.spanHint)
  if (Number.isInteger(span) && span >= 1 && span <= 24) {
    return span
  }
  return 12
}

const inferRequired = (field = {}, type = '') => {
  if (field.requiredHint === true) return true
  if (field.requiredHint === false) return false
  const label = normalizeText(field.label)
  if (type === 'textarea' && includesAny(label, ['意见', '批示', '批分', '结果'])) {
    return false
  }
  return includesAny(label, REQUIRED_KEYWORDS)
}

const inferQueryType = (type = '') => {
  if (type === 'date') return 'date-range'
  if (type === 'select') return 'select'
  if (['radio', 'checkbox', 'switch'].includes(type)) return 'select'
  if (type === 'user') return 'user-select'
  if (type === 'users') return 'users-select'
  if (type === 'office') return 'office-select'
  if (type === 'area') return 'area'
  if (type === 'tree') return 'tree-select'
  if (type === 'modalSelect') return 'modal-select'
  if (type === 'modalMultiSelect') return 'modal-multi-select'
  if (['integer', 'decimal'].includes(type)) return 'range'
  return 'input'
}

const isListBlocked = (field = {}, style = 'normal') => {
  const label = normalizeText(field.label)
  if (FULL_SPAN_TYPES.includes(field.type) || includesAny(label, TEXTAREA_KEYWORDS)) {
    return true
  }
  return style === 'document-form' && includesAny(label, ['批示', '批分', '意见', '结果', '密级期限'])
}

const isQueryBlocked = (field = {}) => {
  const label = normalizeText(field.label)
  return FULL_SPAN_TYPES.includes(field.type) || includesAny(label, TEXTAREA_KEYWORDS)
}

const inferListPriority = (field = {}, style = 'normal') => {
  if (isListBlocked(field, style)) return 0
  const label = normalizeText(field.label)
  if (includesAny(label, ['名称', '标题', '主题'])) return 95
  if (includesAny(label, ['编号', '文号', '单号', '编码'])) return 90
  if (includesAny(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (includesAny(label, ['日期', '时间'])) return 80
  if (includesAny(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构', '甲方', '乙方'])) return 75
  if (includesAny(label, ['金额', '数量', '费用', '合计', '总额'])) return 70
  if (['select', 'radio', 'checkbox', 'switch', 'date', 'user', 'office'].includes(field.type)) return 65
  return 40
}

const inferQueryPriority = (field = {}) => {
  if (isQueryBlocked(field)) return 0
  const label = normalizeText(field.label)
  if (includesAny(label, ['名称', '标题', '主题', '编号', '文号', '单号', '编码'])) return 95
  if (includesAny(label, ['日期', '时间'])) return 90
  if (includesAny(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (includesAny(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构'])) return 80
  if (includesAny(label, ['金额', '数量', '费用', '合计', '总额'])) return 65
  if (includesAny(field.label, QUERY_KEYWORDS)) return 60
  return 30
}

const scoreListField = (field = {}, style = 'normal', ignoreHint = false) => {
  if (isListBlocked(field, style)) return -1
  let score = toPriority(field.raw?.materialListPriority)
  if (score === null) {
    score = inferListPriority(field, style)
  }
  const hint = toBooleanHint(field.raw?.materialListHint)
  if (!ignoreHint && hint === true) score += 8
  if (!ignoreHint && hint === false) score -= 60
  if (field.required) score += 2
  return Math.max(0, Math.min(100, score))
}

const scoreQueryField = (field = {}, ignoreHint = false) => {
  if (isQueryBlocked(field)) return -1
  let score = toPriority(field.raw?.materialQueryPriority)
  if (score === null) {
    score = inferQueryPriority(field)
  }
  const hint = toBooleanHint(field.raw?.materialQueryHint)
  if (!ignoreHint && hint === true) score += 8
  if (!ignoreHint && hint === false) score -= 60
  if (field.required) score += 2
  return Math.max(0, Math.min(100, score))
}

const selectRecommendedIndexes = ({
  fields = [],
  scorer,
  limit,
  minScore,
  minCount = 0,
}) => {
  const rank = (ignoreHint = false) => fields
      .map((field, index) => ({index, score: scorer(field, ignoreHint)}))
      .filter(item => item.score >= minScore)
      .sort((left, right) => right.score - left.score || left.index - right.index)

  let selected = rank(false).slice(0, limit)
  if (selected.length < minCount) {
    const selectedMap = {}
    selected.forEach(item => {
      selectedMap[item.index] = true
    })
    rank(true)
        .filter(item => !selectedMap[item.index])
        .slice(0, Math.max(0, limit - selected.length))
        .forEach(item => selected.push(item))
  }

  return new Set(selected.map(item => item.index))
}

const inferQueryFieldType = (field = {}) => {
  const mode = normalizeQueryMode(field.raw?.materialQueryMode)
  if (['input', 'select', 'date-range', 'range'].includes(mode)) {
    return mode
  }
  return inferQueryType(field.type)
}

const inferQueryOperator = (field = {}, queryFieldType = '') => {
  const mode = normalizeQueryMode(field.raw?.materialQueryMode)
  if (mode === 'like') return 'like'
  if (mode === 'exact') return '='
  if (mode === 'select') return '='
  if (queryFieldType === 'date-range' || queryFieldType === 'range') return 'between'
  return ''
}

const inferListAlign = (type = '') => {
  if (['integer', 'decimal'].includes(type)) return 'right'
  if (type === 'date') return 'center'
  return 'left'
}

const inferListDataIndex = (name = '', type = '') => {
  return NAME_VALUE_TYPES.includes(type) ? `${name}__name` : name
}

const buildDbConfig = (type = '') => {
  if (type === 'decimal') {
    return {jdbcType: 'decimal', precision: 18, scale: 2}
  }
  if (['textarea', 'richText'].includes(type)) {
    return {jdbcType: 'longtext'}
  }
  if (type === 'integer') {
    return {jdbcType: 'int'}
  }
  if (type === 'date') {
    return {jdbcType: 'datetime'}
  }
  if (type === 'users') {
    return {jdbcType: 'varchar', length: 2000}
  }
  if (['user', 'office', 'area', 'tree', 'modalSelect', 'serialNo'].includes(type)) {
    return {jdbcType: 'varchar', length: 64}
  }
  return {jdbcType: TYPE_CONFIG[type]?.jdbcType || 'varchar', length: 255}
}

const normalizeMaterialFields = (material = {}) => {
  const fields = toArray(material.fields).filter(field => normalizeText(field?.label))
  if (fields.length > 0) {
    return fields.map(field => normalizeMaterialFieldSuggestion(clonePlain(field)))
  }
  const headers = toArray(toArray(material.tables)[0]?.headers).filter(Boolean)
  return headers.map((header, index) => ({
    id: `field_${index + 1}`,
    label: header,
    rawText: header,
    groupKey: 'base',
  })).map(field => normalizeMaterialFieldSuggestion(field))
}

const buildDslField = ({field, index, style, usedNames}) => {
  const label = normalizeText(field.label)
  let type = canonicalType(field.typeHint) || inferType(label)
  if (type === 'text' && includesAny(label, TEXTAREA_KEYWORDS)) {
    type = 'textarea'
  }
  if (style === 'document-form' && type === 'office' && includesAny(label, ['单位'])) {
    type = 'text'
  }
  const config = TYPE_CONFIG[type] || TYPE_CONFIG.text
  const nameHint = isMeaninglessFieldName(field.nameHint) ? '' : field.nameHint
  const name = ensureUniqueFieldName(
      nameHint || `field_${String(index + 1).padStart(2, '0')}`,
      usedNames,
      `field_${String(index + 1).padStart(2, '0')}`,
  )
  const span = inferSpan(field, type, style)
  const required = inferRequired(field, type)
  const isList = false
  const isQuery = false
  const group = inferGroupKey(field, type, style, index)
  const defaultValue = ''
  const dateType = type === 'date' ? inferMaterialDateType(field) : ''

  return {
    name,
    label,
    type,
    group,
    showType: config.showType,
    javaField: '',
    jdbcType: config.jdbcType,
    javaType: config.javaType,
    required,
    readonly: false,
    span,
    isForm: true,
    isList,
    isQuery,
    formSort: (index + 1) * 10,
    listSort: (index + 1) * 10,
    dictType: '',
    dictionaryItems: normalizeDictionaryItems(field.optionItems || field.options || field.dictionaryItems || []),
    dateType,
    db: buildDbConfig(type),
    form: {
      defaultValue,
      readonly: false,
      props: {
        name,
        label,
        required,
      },
      controlProps: {
        placeholder: field.placeholder || `请输入${label}`,
      },
      colProps: {
        span,
      },
    },
    list: {
      show: isList,
      dataIndex: inferListDataIndex(name, type),
      title: label,
      align: inferListAlign(type),
      minWidth: span === 24 ? 180 : 140,
      dict: '',
      queryFieldType: '',
      queryFieldProps: null,
    },
    query: {
      enabled: false,
      type: '',
    },
    raw: {
      materialFieldId: field.id || '',
      materialConfidence: field.confidence ?? '',
      materialRawText: field.rawText || '',
      materialValueExample: field.valueExample || '',
      materialListHint: field.listHint ?? '',
      materialListPriority: field.listPriority ?? '',
      materialListReason: field.listReason || '',
      materialQueryHint: field.queryHint ?? '',
      materialQueryPriority: field.queryPriority ?? '',
      materialQueryMode: field.queryMode || '',
      materialQueryReason: field.queryReason || '',
    },
  }
}

const applyListAndQueryRecommendations = (fields = [], style = 'normal') => {
  const eligibleListCount = fields.filter(field => !isListBlocked(field, style)).length
  const eligibleQueryCount = fields.filter(field => !isQueryBlocked(field)).length
  const listIndexes = selectRecommendedIndexes({
    fields,
    scorer: (field, ignoreHint) => scoreListField(field, style, ignoreHint),
    limit: Math.min(LIST_FIELD_LIMIT, Math.max(1, eligibleListCount)),
    minScore: MIN_LIST_PRIORITY,
    minCount: Math.min(4, eligibleListCount),
  })
  const queryIndexes = selectRecommendedIndexes({
    fields,
    scorer: (field, ignoreHint) => scoreQueryField(field, ignoreHint),
    limit: Math.min(QUERY_FIELD_LIMIT, Math.max(1, eligibleQueryCount)),
    minScore: MIN_QUERY_PRIORITY,
    minCount: Math.min(2, eligibleQueryCount),
  })

  fields.forEach((field, index) => {
    const isList = listIndexes.has(index)
    const isQuery = queryIndexes.has(index)
    const queryFieldType = isQuery ? inferQueryFieldType(field) : ''
    const queryOperator = isQuery ? inferQueryOperator(field, queryFieldType) : ''
    field.isList = isList
    field.isQuery = isQuery
    field.list.show = isList
    field.list.queryFieldType = queryFieldType
    field.list.queryFieldProps = isQuery ? {
      placeholder: field.label,
      formatPatter: field.type === 'date' ? field.dateType : undefined,
    } : null
    field.query = {
      enabled: isQuery,
      type: queryFieldType,
      queryType: queryOperator,
    }
    field.raw.effectiveListPriority = scoreListField(field, style, false)
    field.raw.effectiveQueryPriority = scoreQueryField(field, false)
  })

  return fields
}

const buildGroups = (material = {}, fields = [], style = 'normal') => {
  const materialGroups = toArray(material.groups)
  const groupTitleByKey = {}
  materialGroups.forEach((group, index) => {
    const key = normalizeGroupKey(group.key, index)
    groupTitleByKey[key] = group.title || group.key || key
  })
  const defaultTitleMap = style === 'document-form'
      ? {base: '公文信息', approval: '办理意见', detail: '详细信息'}
      : style === 'approval'
          ? {base: '基本信息', detail: '审批信息'}
          : {base: '基本信息', detail: '详细信息'}
  const used = {}
  return fields
      .map(field => field.group || 'base')
      .filter(key => {
        if (used[key]) return false
        used[key] = true
        return true
      })
      .map(key => ({
        key,
        title: groupTitleByKey[key] || defaultTitleMap[key] || key,
      }))
}

const buildSummary = (dsl = {}, sourceContent = '', schemaIssues = [], materialIssues = []) => {
  const fields = toArray(dsl.fields)
  const groups = toArray(dsl.layout?.groups)
  return {
    title: dsl.form?.title || dsl.form?.name || '材料生成表单',
    style: dsl.layout?.style || '',
    fieldCount: fields.length,
    listCount: fields.filter(field => field.isList).length,
    queryCount: fields.filter(field => field.isQuery).length,
    groupCount: groups.length,
    sourceLength: normalizeText(sourceContent).length,
    schema: summarizeSchemaIssues(schemaIssues),
    material: summarizeFormMaterialIssues(materialIssues),
  }
}

export const convertFormMaterialToDslDraft = (material = {}, options = {}) => {
  const sourceContent = normalizeText(material.rawText || options.sourceContent)
  const materialIssues = toArray(material.issues).concat(validateFormMaterialSchema(material))
  const style = options.style || inferStyle(material)
  const form = inferFormMeta(material, options)
  const materialFields = normalizeMaterialFields(material)
  const usedNames = {}
  const fields = materialFields.map((field, index) => buildDslField({
    field,
    index,
    style,
    usedNames,
  }))
  applyListAndQueryRecommendations(fields, style)
  const generatedAt = new Date().toISOString()

  const dsl = {
    version: DSL_SCHEMA_VERSION,
    generatedAt,
    generator: GENERATOR_NAME,
    form,
    layout: {
      style,
      labelWidth: style === 'document-form' ? 100 : 100,
      groups: buildGroups(material, fields, style),
    },
    fields,
    list: {
      buttons: ['add', 'batch-delete', 'export'],
      rowButtons: ['view', 'edit'],
      queryArea: {
        labelWidth: 80,
      },
    },
    actions: {},
    rules: [],
    raw: {
      sourceType: 'form-material',
      sourceContent,
      materialSourceType: material.source?.type || '',
      materialVersion: material.version || '',
      materialTitle: material.title || '',
      material: clonePlain(material),
    },
  }

  const normalizedDsl = ensureDslDictionarySuggestions(dsl)
  const schemaIssues = validateFormDesignDslSchema(normalizedDsl)
  return {
    id: `material-${Date.now()}`,
    sourceType: 'form-material',
    sourceContent,
    generator: GENERATOR_NAME,
    createdAt: generatedAt,
    dsl: normalizedDsl,
    schemaIssues,
    materialIssues,
    summary: buildSummary(normalizedDsl, sourceContent, schemaIssues, materialIssues),
    material: clonePlain(material),
  }
}

export default convertFormMaterialToDslDraft
