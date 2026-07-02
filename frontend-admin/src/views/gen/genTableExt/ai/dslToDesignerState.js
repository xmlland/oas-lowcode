import {
  defaultFieldArray,
  dynamicFormItemType,
  initDynamicFormItemConfig,
  transformGenTableColumnToDynamicFormItemConfig,
} from "@/views/gen/dynamicFormItem";
import {FIELD_TYPE_ALIAS_MAP, validateFormDesignDslSchema, summarizeSchemaIssues} from "@/views/gen/genTableExt/ai/dslSchema";

const TYPE_TO_TEMPLATE_KEY = {
  text: 'textInput',
  textarea: 'textareaInput',
  integer: 'numberInput',
  decimal: 'digitsInput',
  select: 'select',
  radio: 'radio',
  checkbox: 'checkbox',
  switch: 'switch',
  date: 'dateSelect',
  user: 'userSelect',
  users: 'usersSelect',
  office: 'officeSelect',
  area: 'area',
  tree: 'treeSelect',
  modalSelect: 'modalSelect',
  upload: 'upload',
  imageUpload: 'uploadPic',
  onlineFile: 'uploadOnline',
  richText: 'richText',
  serialNo: 'serialNo',
}

const SHOW_TYPE_TO_DSL_TYPE = {
  input: 'text',
  textarea: 'textarea',
  integer: 'integer',
  decimal: 'decimal',
  select: 'select',
  radiobox: 'radio',
  checkbox: 'checkbox',
  switch: 'switch',
  dateselect: 'date',
  treeselectRedio: 'user',
  treeselectCheck: 'users',
  officeselectTree: 'office',
  areaselect: 'area',
  treeselect: 'tree',
  treeSelect: 'tree',
  gridselect: 'modalSelect',
  fileupload: 'upload',
  fileuploadpic: 'imageUpload',
  fileuploadonline: 'onlineFile',
  richText: 'richText',
  serialNo: 'serialNo',
}

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

const TYPE_DB_CONFIG = {
  text: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  textarea: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  integer: {jdbcTypeReplace: 'int', javaType: 'Integer', friendlyJdbcType: '整型'},
  decimal: {jdbcTypeReplace: 'decimal', decimalPrecision: 18, decimalScale: 2, javaType: 'java.math.BigDecimal', friendlyJdbcType: '精确数值'},
  select: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  radio: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  checkbox: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  switch: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: 'String', friendlyJdbcType: '文本'},
  date: {jdbcTypeReplace: 'datetime', javaType: 'Date', friendlyJdbcType: '日期'},
  user: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: USER_JAVA_TYPE, friendlyJdbcType: '文本'},
  users: {jdbcTypeReplace: 'varchar', varcharLength: 2000, javaType: USER_JAVA_TYPE, friendlyJdbcType: '文本'},
  office: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: OFFICE_JAVA_TYPE, friendlyJdbcType: '文本'},
  area: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: AREA_JAVA_TYPE, friendlyJdbcType: '文本'},
  tree: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: ZFORM_JAVA_TYPE, friendlyJdbcType: '文本'},
  modalSelect: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: ZFORM_JAVA_TYPE, friendlyJdbcType: '文本'},
  upload: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  imageUpload: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  onlineFile: {jdbcTypeReplace: 'varchar', varcharLength: 255, javaType: 'String', friendlyJdbcType: '文本'},
  richText: {jdbcTypeReplace: 'varchar', varcharLength: 2000, javaType: 'String', friendlyJdbcType: '文本'},
  serialNo: {jdbcTypeReplace: 'varchar', varcharLength: 64, javaType: 'String', friendlyJdbcType: '文本'},
}

const QUERY_FIELD_TYPE_BY_DSL_TYPE = {
  date: 'date-range',
  select: 'select',
  radio: 'select',
  checkbox: 'select',
  switch: 'select',
  user: 'user-select',
  users: 'users-select',
  office: 'office-select',
  area: 'area',
  tree: 'tree-select',
  modalSelect: 'modal-select',
  modalMultiSelect: 'modal-multi-select',
}

const NAME_VALUE_TYPES = ['user', 'users', 'office', 'area', 'modalSelect']

const SYSTEM_HIDDEN_FIELD_NAMES = [
  'create_by',
  'create_date',
  'update_by',
  'update_date',
  'remarks',
  'owner_code',
]

const SYSTEM_HIDDEN_JAVA_FIELD_TO_NAME = {
  'createby.id': 'create_by',
  createdate: 'create_date',
  'updateby.id': 'update_by',
  updatedate: 'update_date',
  remarks: 'remarks',
  ownercode: 'owner_code',
}

const SYSTEM_HIDDEN_FIELD_NAME_SET = new Set(SYSTEM_HIDDEN_FIELD_NAMES)

const EMPTY_ARRAY = []

const unwrap = (value) => {
  if (value && typeof value === 'object' && 'value' in value) {
    return value.value
  }
  return value
}

const toArray = (value) => {
  const unwrapped = unwrap(value)
  return Array.isArray(unwrapped) ? unwrapped : EMPTY_ARRAY
}

const clone = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (e) {
    return value
  }
}

const normalizeText = (value) => String(value || '').trim()

const normalizeKey = (value) => normalizeText(value).toLowerCase()

const isEmpty = (value) => value === undefined || value === null || normalizeText(value) === ''

const isSystemHiddenFieldName = (value) => SYSTEM_HIDDEN_FIELD_NAME_SET.has(normalizeKey(value))

const getSystemHiddenNameFromJavaField = (value) => SYSTEM_HIDDEN_JAVA_FIELD_TO_NAME[normalizeKey(value)] || ''

const getDesignerItemName = (item = {}) => normalizeText(item.formItemProps?.name || item.column?.name || item.name)

const getDesignerItemJavaField = (item = {}) => normalizeText(item.column?.javaField || item.javaField)

const getSystemHiddenNameFromDesignerItem = (item = {}) => {
  const name = getDesignerItemName(item)
  if (isSystemHiddenFieldName(name)) {
    return normalizeKey(name)
  }
  return getSystemHiddenNameFromJavaField(getDesignerItemJavaField(item))
}

const getSystemHiddenNameFromField = (field = {}) => {
  const name = normalizeText(field.name)
  if (isSystemHiddenFieldName(name)) {
    return normalizeKey(name)
  }
  return getSystemHiddenNameFromJavaField(field.javaField)
}

const setDesignerItemHidden = (item = {}) => {
  if (item.column) {
    item.column.isForm = '0'
    item.column.isList = item.column.isList || '0'
    item.column.isQuery = item.column.isQuery || '0'
  }
  return item
}

const buildDefaultSystemHiddenItem = (fieldName, context = {}) => {
  const template = defaultFieldArray.find(item => normalizeKey(item.name) === normalizeKey(fieldName))
  if (!template) {
    return null
  }
  const table = unwrap(context.table) || {
    name: context.dsl?.form?.name || 'ai_generated_form',
    tableType: '0',
  }
  return setDesignerItemHidden(transformGenTableColumnToDynamicFormItemConfig(table, clone(template)))
}

const collectSystemHiddenItems = (context = {}) => {
  const byName = new Map()
  toArray(context.hideFormItemArr)
      .concat(toArray(context.displayFormItemArr))
      .forEach(item => {
        const systemName = getSystemHiddenNameFromDesignerItem(item)
        if (systemName && !byName.has(systemName)) {
          byName.set(systemName, setDesignerItemHidden(item))
        }
      })

  return SYSTEM_HIDDEN_FIELD_NAMES
      .map(fieldName => byName.get(fieldName) || buildDefaultSystemHiddenItem(fieldName, context))
      .filter(Boolean)
}

const hasSystemHiddenItem = (items = [], field = {}) => {
  const systemName = getSystemHiddenNameFromField(field)
  if (!systemName) {
    return false
  }
  return items.some(item => getSystemHiddenNameFromDesignerItem(item) === systemName)
}

const normalizeSystemHiddenDslField = (field = {}) => ({
  ...field,
  isForm: false,
  isList: false,
  isQuery: false,
  list: {
    ...(field.list || {}),
    show: false,
  },
  query: {
    ...(field.query || {}),
    enabled: false,
  },
})

const toBoolean = (value, defaultValue = false) => {
  if (value === undefined || value === null || value === '') {
    return defaultValue
  }
  return value === true || value === '1' || value === 1 || value === 'true'
}

const toYesNo = (value, defaultValue = false) => toBoolean(value, defaultValue) ? '1' : '0'

const toFormRequired = (value) => toBoolean(value) ? '0' : '1'

const toNumber = (value, fallback) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
}

const createIssue = ({
  id,
  level = 'warning',
  title,
  fieldName = '',
  fieldLabel = '',
  description = '',
  suggestion = '',
  meta = {},
}) => ({
  id,
  level,
  title,
  fieldName,
  fieldLabel,
  description,
  suggestion,
  meta,
})

const getCanonicalFieldType = (field = {}, issues = []) => {
  const rawType = normalizeText(field.type) || SHOW_TYPE_TO_DSL_TYPE[normalizeText(field.showType)] || 'text'
  const aliasType = FIELD_TYPE_ALIAS_MAP[rawType] || rawType

  if (aliasType === 'modalMultiSelect') {
    issues.push(createIssue({
      id: `dsl-convert-modal-multi-downgrade:${field.name || field.label}`,
      title: '弹出多选暂未直接转换',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: '弹出多选需要关联表、关联字段和中间表配置，当前 DSL 草稿通常缺少这些信息。',
      suggestion: '第一版先降级为单行文本，后续可在设计器中手工改为弹出多选。',
      meta: {
        fromType: rawType,
        toType: 'text',
      },
    }))
    return 'text'
  }

  if (!TYPE_TO_TEMPLATE_KEY[aliasType]) {
    issues.push(createIssue({
      id: `dsl-convert-type-downgrade:${field.name || field.label}:${rawType}`,
      title: '字段类型已降级',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: `当前字段类型 ${rawType || '空'} 暂无稳定设计器模板。`,
      suggestion: '第一版先按单行文本生成，应用后可在设计器里手动调整控件类型。',
      meta: {
        fromType: rawType,
        toType: 'text',
      },
    }))
    return 'text'
  }

  return aliasType
}

const getTemplateByType = (type) => {
  const key = TYPE_TO_TEMPLATE_KEY[type] || TYPE_TO_TEMPLATE_KEY.text
  const template = dynamicFormItemType.find(item => item.key === key)
  return template ? {
    ...template,
    dataJson: clone(template.dataJson),
  } : null
}

const getJdbcTypeText = (column = {}) => {
  if (column.jdbcTypeReplace === 'varchar') {
    return `varchar(${column.varcharLength || 255})`
  }
  if (column.jdbcTypeReplace === 'decimal') {
    return `decimal(${column.decimalPrecision || 18},${column.decimalScale || 2})`
  }
  return column.jdbcTypeReplace || column.jdbcType || ''
}

const getDbConfig = (field = {}, type = 'text') => {
  const config = {
    ...(TYPE_DB_CONFIG[type] || TYPE_DB_CONFIG.text),
  }
  const db = field.db || {}
  const jdbcType = normalizeText(db.jdbcType || field.jdbcType)

  if (jdbcType) {
    if (jdbcType.indexOf('varchar') === 0) {
      config.jdbcTypeReplace = 'varchar'
      const match = jdbcType.match(/\d+/)
      if (match) {
        config.varcharLength = Number(match[0])
      }
    } else if (jdbcType.indexOf('decimal') === 0) {
      config.jdbcTypeReplace = 'decimal'
      const match = jdbcType.match(/decimal\((\d+),(\d+)\)/i)
      if (match) {
        config.decimalPrecision = Number(match[1])
        config.decimalScale = Number(match[2])
      }
    } else {
      config.jdbcTypeReplace = jdbcType
    }
  }

  if (db.length || field.maxLength) {
    config.varcharLength = toNumber(db.length || field.maxLength, config.varcharLength)
  }
  if (db.precision) {
    config.decimalPrecision = toNumber(db.precision, config.decimalPrecision)
  }
  if (db.scale !== undefined) {
    config.decimalScale = toNumber(db.scale, config.decimalScale)
  }
  if (field.javaType || db.javaType) {
    config.javaType = field.javaType || db.javaType
  }
  if (JAVA_TYPE_BY_TYPE[type]) {
    config.javaType = JAVA_TYPE_BY_TYPE[type]
  }

  return config
}

const getQueryFieldType = (field = {}, type = 'text') => {
  const explicit = field.query?.type || field.list?.queryFieldType
  if (explicit) {
    if (explicit === 'range') {
      return 'input'
    }
    return explicit
  }
  if (['integer', 'decimal'].includes(type)) {
    return 'input'
  }
  return QUERY_FIELD_TYPE_BY_DSL_TYPE[type] || 'input'
}

const getQueryType = (field = {}, type = 'text', queryFieldType = '') => {
  if (field.queryType) {
    return field.queryType === 'eq' ? '=' : field.queryType
  }
  if (field.query?.queryType) {
    return field.query.queryType === 'eq' ? '=' : field.query.queryType
  }
  if (queryFieldType === 'date-range' || field.query?.type === 'range') {
    return 'between'
  }
  if (['select', 'radio', 'checkbox', 'switch', 'user', 'users', 'office', 'area', 'tree', 'modalSelect', 'modalMultiSelect'].includes(type)) {
    return '='
  }
  if (['integer', 'decimal'].includes(type)) {
    return '='
  }
  return 'like'
}

const getDefaultAlign = (field = {}, type = 'text') => {
  if (field.list?.align) {
    return field.list.align
  }
  if (['integer', 'decimal'].includes(type)) {
    return 'right'
  }
  if (type === 'date') {
    return 'center'
  }
  return 'left'
}

const isNameValueField = (item = {}) => {
  const javaField = normalizeText(item.column?.javaField)
  return javaField.endsWith('|name') || javaField.indexOf('id|') >= 0
}

const getListDataIndex = (field = {}, item = {}, type = 'text') => {
  const name = normalizeText(field.name)
  const explicitDataIndex = normalizeText(field.list?.dataIndex)
  const isNameValueType = NAME_VALUE_TYPES.includes(type) || isNameValueField(item)
  if (explicitDataIndex) {
    if (isNameValueType && explicitDataIndex === name) {
      return `${name}__name`
    }
    return field.list.dataIndex
  }
  return isNameValueType ? `${name}__name` : name
}

const buildListConfig = ({field, item, type, isList, isQuery}) => {
  const queryFieldType = isQuery ? getQueryFieldType(field, type) : ''
  const queryFieldProps = isQuery
      ? clone(field.list?.queryFieldProps || field.query?.props || {})
      : null
  const align = getDefaultAlign(field, type)
  const dict = field.list?.dict || field.dictType || ''

  return {
    dataIndex: getListDataIndex(field, item, type),
    title: field.list?.title || field.label,
    align,
    minWidth: toNumber(field.list?.minWidth, Number(field.span) === 24 ? 180 : 140),
    sorter: field.list?.sorter || 'false',
    sortColumn: field.list?.sortColumn || '',
    ellipsis: field.list?.ellipsis === undefined ? false : field.list.ellipsis,
    dict,
    dictMultiple: type === 'checkbox' || type === 'users',
    queryColumn: field.list?.queryColumn || '',
    queryDefaultValue: field.list?.queryDefaultValue || '',
    queryFieldType,
    queryFieldProps,
    widthMultiple: field.list?.widthMultiple || 1,
  }
}

const normalizeMatchText = (value = '') => normalizeText(value)
    .toLowerCase()
    .replace(/关联|选择|所属|对应|基本信息|信息|详情|明细|表单|管理|列表/g, '')
    .replace(/[^a-z0-9_\u3400-\u9fff]+/g, '')

const getContextForms = (context = {}) => toArray(context.moduleForms || context.forms || context.moduleDesign?.forms)

const getContextRelations = (context = {}) => toArray(context.moduleRelations || context.relations || context.moduleDesign?.relations)

const getContextCurrentFormId = (context = {}) => normalizeText(context.currentFormId || context.formId || context.form?.id)

const getContextFormId = (form = {}, index = 0) => normalizeText(form.id || form.formId || form.pageId) || `form_${index + 1}`

const getContextFormName = (form = {}) => normalizeText(form.formName || form.name || form.nameHint || form.dsl?.form?.name || form.sourceMaterialForm?.nameHint)

const getContextFormTitle = (form = {}) => normalizeText(form.title || form.formTitle || form.comments || form.dsl?.form?.title || form.sourceMaterialForm?.title)

const getContextCurrentFormEntry = (context = {}) => {
  const currentFormId = getContextCurrentFormId(context)
  const forms = getContextForms(context)
  const currentIndex = forms.findIndex((form, index) => getContextFormId(form, index) === currentFormId)
  if (currentIndex >= 0) {
    return {
      form: forms[currentIndex],
      index: currentIndex,
    }
  }
  return {
    form: context.form || {},
    index: -1,
  }
}

const getContextFormMatchTexts = (form = {}, index = 0) => [
  getContextFormId(form, index),
  getContextFormName(form),
  getContextFormTitle(form),
  form.nameHint,
  form.formName,
  form.formTitle,
  form.comments,
  form.dsl?.form?.name,
  form.dsl?.form?.title,
  form.sourceMaterialForm?.id,
  form.sourceMaterialForm?.nameHint,
  form.sourceMaterialForm?.title,
  form.sourceMaterialForm?.formName,
  form.sourceMaterialForm?.formTitle,
].map(normalizeText).filter(Boolean)

const textMatchesCandidate = (text = '', candidate = '') => {
  const normalizedText = normalizeMatchText(text)
  const normalizedCandidate = normalizeMatchText(candidate)
  if (!normalizedText || !normalizedCandidate) {
    return false
  }
  return normalizedText === normalizedCandidate
      || normalizedText.includes(normalizedCandidate)
      || normalizedCandidate.includes(normalizedText)
}

const textMatchesAnyCandidate = (text = '', candidates = []) => {
  return toArray(candidates).some(candidate => textMatchesCandidate(text, candidate))
}

const relationSourceKeys = [
  'fromFormId',
  'sourceFormId',
  'formId',
  'fromFormName',
  'sourceFormName',
  'fromFormTitle',
  'sourceFormTitle',
  'from',
  'source',
  'sourceName',
  'sourceTitle',
]

const relationTargetKeys = [
  'toFormId',
  'targetFormId',
  'targetId',
  'toFormName',
  'targetFormName',
  'toFormTitle',
  'targetFormTitle',
  'to',
  'target',
  'targetName',
  'targetTitle',
]

const getRelationRoleTexts = (relation = {}, role = 'target') => {
  const keys = role === 'source' ? relationSourceKeys : relationTargetKeys
  return keys.map(key => normalizeText(relation[key])).filter(Boolean)
}

const relationRoleMatchesForm = (relation = {}, form = {}, index = 0, role = 'target') => {
  const roleTexts = getRelationRoleTexts(relation, role)
  if (roleTexts.length === 0) {
    return false
  }
  const formTexts = getContextFormMatchTexts(form, index)
  return roleTexts.some(text => textMatchesAnyCandidate(text, formTexts))
}

const relationDescriptionMentionsForm = (relation = {}, form = {}, index = 0) => {
  const description = normalizeText([
    relation.description,
    relation.name,
    relation.title,
    relation.label,
    relation.sourceRef,
  ].filter(Boolean).join(' '))
  if (!description) {
    return false
  }
  return textMatchesAnyCandidate(description, getContextFormMatchTexts(form, index))
}

const getExplicitModalTargetText = (field = {}, controlProps = {}) => normalizeText(
    controlProps.formNo
    || controlProps.targetFormNo
    || controlProps.targetFormName
    || controlProps.targetTable
    || field.formNo
    || field.tableName
    || field.targetFormNo
    || field.targetFormName
    || field.targetFormTitle
    || field.targetTable
    || field.targetTableName
    || field.relation?.targetFormNo
    || field.relation?.targetFormName
    || field.relation?.targetFormTitle
    || field.relation?.toFormNo
    || field.relation?.toFormId
)

const relationMatchesCurrentAndTarget = (relation = {}, currentFormId = '', currentForm = {}, currentIndex = -1, targetForm = {}, targetIndex = 0) => {
  const targetId = getContextFormId(targetForm, targetIndex)
  const fromFormId = normalizeText(relation.fromFormId || relation.sourceFormId || relation.formId)
  const toFormId = normalizeText(relation.toFormId || relation.targetFormId || relation.targetId)
  if (currentFormId && fromFormId === currentFormId && toFormId === targetId) {
    return true
  }

  const sourceMatchesCurrent = currentFormId
      ? relationRoleMatchesForm(relation, currentForm, currentIndex, 'source')
      : false
  const targetMatchesTarget = relationRoleMatchesForm(relation, targetForm, targetIndex, 'target')
  if (sourceMatchesCurrent && targetMatchesTarget) {
    return true
  }

  const descriptionMatchesCurrent = currentFormId
      ? relationDescriptionMentionsForm(relation, currentForm, currentIndex)
      : false
  const descriptionMatchesTarget = relationDescriptionMentionsForm(relation, targetForm, targetIndex)
  return descriptionMatchesCurrent && descriptionMatchesTarget
}

const scoreModalRelationHint = ({relation, field, currentFormId, currentForm, currentIndex, targetForm, targetIndex}) => {
  if (relationMatchesCurrentAndTarget(relation, currentFormId, currentForm, currentIndex, targetForm, targetIndex)) {
    return 80
  }

  const fieldMatchText = normalizeMatchText(`${field.label || ''} ${field.name || ''}`)
  const targetRoleMatches = relationRoleMatchesForm(relation, targetForm, targetIndex, 'target')
  const relationText = normalizeText([
    relation.description,
    relation.name,
    relation.title,
    relation.label,
  ].filter(Boolean).join(' '))

  if (targetRoleMatches && fieldMatchText && textMatchesCandidate(relationText, fieldMatchText)) {
    return 50
  }
  if (targetRoleMatches) {
    return 30
  }
  if (relationDescriptionMentionsForm(relation, targetForm, targetIndex) && fieldMatchText && textMatchesCandidate(relationText, fieldMatchText)) {
    return 30
  }
  return 0
}

const scoreModalTargetForm = ({field, context, targetForm, targetIndex, explicitTargetText}) => {
  const currentFormId = getContextCurrentFormId(context)
  const currentEntry = getContextCurrentFormEntry(context)
  const targetId = getContextFormId(targetForm, targetIndex)
  if (currentFormId && targetId === currentFormId) {
    return -1000
  }

  const targetName = getContextFormName(targetForm)
  const targetTitle = getContextFormTitle(targetForm)
  const fieldName = normalizeText(field.name)
  const fieldLabel = normalizeText(field.label)
  const fieldMatchText = normalizeMatchText(`${fieldLabel} ${fieldName.replace(/_id$|_ids$/g, '')}`)
  const targetMatchText = normalizeMatchText(`${targetTitle} ${targetName}`)
  let score = 0

  if (explicitTargetText) {
    const explicitText = normalizeMatchText(explicitTargetText)
    if (explicitText && [targetId, targetName, targetTitle].map(normalizeMatchText).some(text => text === explicitText || text.includes(explicitText) || explicitText.includes(text))) {
      score += 100
    }
  }

  if (fieldMatchText && targetMatchText) {
    if (targetMatchText.includes(fieldMatchText) || fieldMatchText.includes(targetMatchText)) {
      score += 40
    }
    fieldMatchText.split(/_+/).filter(text => text.length > 1).forEach(token => {
      if (targetMatchText.includes(token)) {
        score += 12
      }
    })
    Array.from(fieldMatchText.matchAll(/[\u3400-\u9fff]{2,}/g)).forEach(match => {
      if (targetMatchText.includes(match[0])) {
        score += 20
      }
    })
  }

  getContextRelations(context).forEach(relation => {
    score += scoreModalRelationHint({
      relation,
      field,
      currentFormId,
      currentForm: currentEntry.form,
      currentIndex: currentEntry.index,
      targetForm,
      targetIndex,
    })
  })

  return score
}

const resolveModalSelectTargetForm = (field = {}, context = {}, controlProps = {}) => {
  const forms = getContextForms(context)
  if (forms.length === 0) {
    return null
  }
  const explicitTargetText = getExplicitModalTargetText(field, controlProps)
  const scored = forms
      .map((form, index) => ({
        form,
        index,
        score: scoreModalTargetForm({
          field,
          context,
          targetForm: form,
          targetIndex: index,
          explicitTargetText,
        }),
      }))
      .filter(item => item.score > 0)
      .sort((a, b) => b.score - a.score)
  if (scored.length === 0) {
    return null
  }
  return scored[0].form
}

const getTargetFormFields = (form = {}) => toArray(form.dsl?.fields || form.fields || form.sourceMaterialForm?.fields)
    .map(field => ({
      ...field,
      name: normalizeText(field.name || field.nameHint || field.fieldName),
      label: normalizeText(field.label || field.title || field.comments || field.labelHint),
      type: FIELD_TYPE_ALIAS_MAP[field.type || field.typeHint] || field.type || field.typeHint || 'text',
      isList: field.isList ?? field.listHint,
      isQuery: field.isQuery ?? field.queryHint,
    }))
    .filter(field => field.name && field.label && !isSystemHiddenFieldName(field.name))

const scoreModalSelectColumn = (field = {}) => {
  const name = normalizeText(field.name)
  const label = normalizeText(field.label)
  const type = FIELD_TYPE_ALIAS_MAP[field.type] || field.type || 'text'
  let score = 0
  if (toBoolean(field.list?.show) || toBoolean(field.isList)) {
    score += 60
  }
  if (/编号|编码|单号|名称|标题|主题|类型|状态/.test(label)) {
    score += 30
  }
  if (/(^|_)(no|code|name|title|type|status)(_|$)/i.test(name)) {
    score += 24
  }
  if (['text', 'select', 'radio', 'date', 'user', 'office', 'modalSelect'].includes(type)) {
    score += 8
  }
  if (['textarea', 'richText', 'upload', 'imageUpload', 'onlineFile'].includes(type)) {
    score -= 40
  }
  return score
}

const getModalSelectColumnDataIndex = (field = {}) => {
  const type = FIELD_TYPE_ALIAS_MAP[field.type] || field.type || 'text'
  const explicit = normalizeText(field.list?.dataIndex)
  if (explicit) {
    return explicit
  }
  return NAME_VALUE_TYPES.includes(type) ? `${field.name}__name` : field.name
}

const shouldUseModalSelectColumnAsQuery = (field = {}, index = 0) => {
  if (toBoolean(field.query?.enabled) || toBoolean(field.isQuery)) {
    return true
  }
  const text = `${field.name} ${field.label}`
  return index < 2 && /编号|编码|单号|名称|标题|主题|no|code|name|title/i.test(text)
}

const buildModalSelectColumns = (targetForm = {}) => {
  const fields = getTargetFormFields(targetForm)
      .map(field => ({
        field,
        score: scoreModalSelectColumn(field),
      }))
      .filter(item => item.score > -20)
      .sort((a, b) => b.score - a.score)
      .slice(0, 5)
      .map(item => item.field)

  return fields.map((field, index) => ({
    dataIndex: getModalSelectColumnDataIndex(field),
    title: field.label,
    align: field.list?.align || getDefaultAlign(field, FIELD_TYPE_ALIAS_MAP[field.type] || field.type || 'text'),
    isShow: '1',
    isQuery: shouldUseModalSelectColumnAsQuery(field, index) ? '1' : '0',
    queryDataIndex: field.list?.queryColumn || '',
    dict: field.dictType || field.list?.dict || '',
    minWidth: toNumber(field.list?.minWidth, index === 0 ? 160 : 120),
    rowSort: (index + 1) * 10,
  }))
}

const getModalSelectNameDataIndex = (columns = []) => {
  const nameColumn = columns.find(column => /名称|标题|主题|name|title/i.test(`${column.title} ${column.dataIndex}`))
  return (nameColumn || columns[0] || {}).dataIndex || ''
}

const normalizeModalSelectColumns = (columns = []) => toArray(columns)
    .map((rawColumn, index) => {
      const column = rawColumn || {}
      const dataIndex = normalizeText(column.dataIndex || column.key || column.fieldName || column.name || column.queryDataIndex)
      const title = normalizeText(column.title || column.label || column.comments || dataIndex)
      if (!dataIndex || !title) {
        return null
      }
      return {
        ...column,
        dataIndex,
        title,
        align: column.align || 'left',
        isShow: column.isShow === undefined || column.isShow === '' ? '1' : toYesNo(column.isShow, true),
        isQuery: column.isQuery === undefined || column.isQuery === '' ? (index === 0 ? '1' : '0') : toYesNo(column.isQuery),
        queryDataIndex: normalizeText(column.queryDataIndex || ''),
        dict: normalizeText(column.dict || column.dictType || ''),
        minWidth: toNumber(column.minWidth, index === 0 ? 160 : 120),
        rowSort: toNumber(column.rowSort, (index + 1) * 10),
      }
    })
    .filter(Boolean)

const normalizeModalSelectSearchArray = (value) => {
  if (Array.isArray(value)) {
    return value.map(item => normalizeText(item)).filter(Boolean)
  }
  return normalizeText(value)
      .split(',')
      .map(item => normalizeText(item))
      .filter(Boolean)
}

const syncModalSelectColumnConfig = (item = {}) => {
  const controlProps = item.formControlProps || {}
  const column = item.column || {}
  const formNo = normalizeText(controlProps.formNo || column.tableName || column.formNo)
  if (formNo) {
    controlProps.formNo = formNo
    column.tableName = formNo
    column.formNo = formNo
  }

  const allColumns = normalizeModalSelectColumns(controlProps.allColumns)
  if (allColumns.length === 0) {
    return
  }

  controlProps.allColumns = allColumns
  const visibleColumns = allColumns.filter(column => column.isShow === '1')
  const exportColumns = visibleColumns.length > 0 ? visibleColumns : allColumns
  column.fieldLabels = exportColumns.map(column => column.title).join(',')
  column.fieldKeys = exportColumns.map(column => column.dataIndex).join(',')

  const queryColumns = allColumns.filter(column => column.isQuery === '1')
  const effectiveQueryColumns = queryColumns.length > 0 ? queryColumns : allColumns.slice(0, 1)
  const searchKeys = effectiveQueryColumns.map(column => column.queryDataIndex || column.dataIndex).filter(Boolean)
  const searchLabels = effectiveQueryColumns.map(column => column.title).filter(Boolean)
  if (searchKeys.length > 0) {
    column.searchKey = searchKeys[0]
    column.searchLabel = searchLabels[0] || ''
  }
  if (normalizeModalSelectSearchArray(controlProps.searchKey).length === 0) {
    controlProps.searchKey = searchKeys
  }
  if (normalizeModalSelectSearchArray(controlProps.searchLabel).length === 0) {
    controlProps.searchLabel = searchLabels
  }
  if (!controlProps.nameDataIndex) {
    controlProps.nameDataIndex = getModalSelectNameDataIndex(allColumns)
  }
}

const patchModalSelectProps = (item, field, context = {}) => {
  const controlProps = item.formControlProps || {}
  if (controlProps.formNo && toArray(controlProps.allColumns).length > 0) {
    syncModalSelectColumnConfig(item)
    return
  }
  const targetForm = resolveModalSelectTargetForm(field, context, controlProps)
  if (!targetForm) {
    syncModalSelectColumnConfig(item)
    return
  }
  const targetFormName = getContextFormName(targetForm)
  if (!controlProps.formNo && targetFormName) {
    controlProps.formNo = targetFormName
  }
  if (toArray(controlProps.allColumns).length === 0) {
    controlProps.allColumns = buildModalSelectColumns(targetForm)
  }
  if (!controlProps.nameDataIndex) {
    controlProps.nameDataIndex = getModalSelectNameDataIndex(controlProps.allColumns)
  }
  syncModalSelectColumnConfig(item)
  if (!controlProps.modalTitle) {
    controlProps.modalTitle = `请选择${field.label || getContextFormTitle(targetForm) || ''}`
  }
}

export const enrichDslModalSelectFields = (dsl = {}, context = {}) => {
  const nextDsl = clone(dsl) || {}
  if (!Array.isArray(nextDsl.fields)) {
    return nextDsl
  }
  nextDsl.fields = nextDsl.fields.map(field => {
    const type = getCanonicalFieldType(field || {}, [])
    if (type !== 'modalSelect') {
      return field
    }

    const nextField = {
      ...(field || {}),
    }
    const formConfig = {
      ...(nextField.form || {}),
    }
    const controlProps = {
      ...(formConfig.controlProps || {}),
      ...(nextField.controlProps || {}),
    }
    const targetForm = resolveModalSelectTargetForm(nextField, context, controlProps)
    if (targetForm) {
      const targetFormName = getContextFormName(targetForm)
      if (!controlProps.formNo && targetFormName) {
        controlProps.formNo = targetFormName
      }
      if (toArray(controlProps.allColumns).length === 0) {
        controlProps.allColumns = buildModalSelectColumns(targetForm)
      }
      if (!controlProps.nameDataIndex) {
        controlProps.nameDataIndex = getModalSelectNameDataIndex(controlProps.allColumns)
      }
      if (!controlProps.modalTitle) {
        controlProps.modalTitle = `请选择${nextField.label || getContextFormTitle(targetForm) || ''}`
      }
    }

    const bridgeItem = {
      formControlProps: controlProps,
      column: {},
    }
    syncModalSelectColumnConfig(bridgeItem)
    formConfig.controlProps = bridgeItem.formControlProps
    nextField.form = formConfig
    return nextField
  })
  return nextDsl
}

const patchColumnDbConfig = (column, field, type) => {
  const dbConfig = getDbConfig(field, type)
  column.jdbcTypeReplace = dbConfig.jdbcTypeReplace
  column.jdbcType = getJdbcTypeText(dbConfig)
  column.javaType = dbConfig.javaType
  column.friendlyJdbcType = dbConfig.friendlyJdbcType

  if (dbConfig.jdbcTypeReplace === 'varchar') {
    column.varcharLength = dbConfig.varcharLength || 255
    column.maxLength = field.maxLength || String(column.varcharLength)
  }
  if (dbConfig.jdbcTypeReplace === 'decimal') {
    column.decimalPrecision = dbConfig.decimalPrecision || 18
    column.decimalScale = dbConfig.decimalScale || 2
  }
}

const syncControlMaxlengthWithColumn = (item) => {
  const column = item.column || {}
  if (column.jdbcTypeReplace !== 'varchar') {
    return
  }
  if (!['input', 'textarea'].includes(item.type)) {
    return
  }
  const varcharLength = Number(column.varcharLength || 0)
  const maxlength = Number(item.formControlProps?.maxlength || 0)
  if (varcharLength > 0 && (!maxlength || maxlength < varcharLength)) {
    item.formControlProps.maxlength = varcharLength
  }
}

const patchControlProps = (item, field, type, context = {}) => {
  const controlProps = field.form?.controlProps || field.controlProps || {}
  Object.assign(item.formControlProps, clone(controlProps))

  if (field.defaultValue !== undefined) {
    item.formControlProps.defaultValue = field.defaultValue
  }
  if (field.form?.defaultValue !== undefined) {
    item.formControlProps.defaultValue = field.form.defaultValue
  }
  item.formControlProps.disabled = toYesNo(field.readonly || field.form?.readonly)

  if (type === 'date') {
    item.formControlProps.formatPatter = field.dateType || field.form?.controlProps?.formatPatter || 'yyyy-MM-dd'
  }
  if (type === 'select' || type === 'radio' || type === 'checkbox') {
    item.formControlProps.dictType = field.dictType || field.list?.dict || ''
  }
  if (type === 'checkbox') {
    item.formControlProps.multiple = true
  }
  if (type === 'modalSelect') {
    patchModalSelectProps(item, field, context)
  }
}

const patchDesignerItem = ({item, field, type, index, context = {}}) => {
  const span = toNumber(field.form?.colProps?.span || field.span, 12)
  const isList = field.list?.show !== undefined ? toBoolean(field.list.show) : toBoolean(field.isList)
  const isQuery = field.query?.enabled !== undefined ? toBoolean(field.query.enabled) : toBoolean(field.isQuery)
  const isForm = field.isForm !== undefined ? toBoolean(field.isForm, true) : true
  const formSort = toNumber(field.formSort, (index + 1) * 10)
  const listSort = toNumber(field.listSort, (index + 1) * 10)
  const searchSort = toNumber(field.searchSort, (index + 1) * 10)
  const queryFieldType = isQuery ? getQueryFieldType(field, type) : ''

  item.colProps.span = span
  item.formItemProps.name = field.name
  item.formItemProps.label = field.label
  item.formItemProps.required = toYesNo(field.required)
  item.formItemProps.validateType = field.validateType || item.formItemProps.validateType || ''
  item.formItemProps.unique = toYesNo(field.unique)
  item.formItemProps.encryptType = field.encryptType || ''

  patchControlProps(item, field, type, context)

  const column = item.column
  column.name = field.name
  column.comments = field.label
  column.comments_EN = field.comments_EN || field.label
  column.isNull = toFormRequired(field.required)
  column.isReadonly = toYesNo(field.readonly)
  column.defaultValue = item.formControlProps.defaultValue || ''
  column.isOneLine = Number(span) === 24 ? '1' : '0'
  column.isForm = isForm ? '1' : '0'
  column.isList = isList ? '1' : '0'
  column.isQuery = isQuery ? '1' : '0'
  column.formSort = formSort
  column.listSort = listSort
  column.searchSort = searchSort
  column.queryType = isQuery ? getQueryType(field, type, queryFieldType) : (column.queryType || '')
  column.dictType = field.dictType || field.list?.dict || column.dictType || ''
  column.dateType = field.dateType || column.dateType || ''
  column.remarks = field.remarks || column.remarks || ''
  column.blockChainParam1 = column.blockChainParam1 || '0'
  column.blockChainParam2 = column.blockChainParam2 || '0'
  column.blockChainParam3 = column.blockChainParam3 || '0'

  patchColumnDbConfig(column, field, type)
  syncControlMaxlengthWithColumn(item)
  if (type === 'modalSelect') {
    syncModalSelectColumnConfig(item)
  }

  if (['select', 'radio', 'checkbox'].includes(type)) {
    column.dictType = field.dictType || field.list?.dict || ''
    column.selectSimple = column.selectSimple || '1'
  }

  column.listConfig = JSON.stringify(buildListConfig({
    field,
    item,
    type,
    isList,
    isQuery,
  }))

  item.apply && item.apply()
  return item
}

const getAllocationArrays = (context, generatedDisplay, generatedHidden, options = {}) => {
  if (!options.reserveExistingFields) {
    return {
      display: generatedDisplay,
      hidden: generatedHidden,
    }
  }
  return {
    display: toArray(context.displayFormItemArr).concat(generatedDisplay),
    hidden: toArray(context.hideFormItemArr).concat(generatedHidden),
  }
}

export const convertDslFieldToDesignerItem = (field = {}, index = 0, context = {}, options = {}) => {
  const issues = []
  const name = normalizeText(field.name)
  const label = normalizeText(field.label)

  if (isEmpty(name) || isEmpty(label)) {
    return {
      item: null,
      target: 'display',
      type: '',
      issues: [createIssue({
        id: `dsl-convert-field-invalid:${index}`,
        level: 'error',
        title: '字段缺少必要信息',
        fieldName: name,
        fieldLabel: label,
        description: '转换到设计器字段时，name 和 label 都是必需的。',
        suggestion: '请先补齐 DSL 字段名和字段标题。',
      })],
    }
  }

  const normalizedField = {
    ...field,
    name,
    label,
  }
  const type = getCanonicalFieldType(normalizedField, issues)
  const template = getTemplateByType(type)

  if (!template) {
    return {
      item: null,
      target: 'display',
      type,
      issues: issues.concat(createIssue({
        id: `dsl-convert-template-missing:${name}:${type}`,
        level: 'error',
        title: '设计器模板不存在',
        fieldName: name,
        fieldLabel: label,
        description: `未找到 ${type} 对应的设计器字段模板。`,
        suggestion: '请检查 dynamicFormItemType 模板配置。',
      })),
    }
  }

  const table = unwrap(context.table) || {
    name: context.dsl?.form?.name || 'ai_generated_form',
    tableType: '0',
  }
  const allocation = getAllocationArrays(context, options.generatedDisplay || [], options.generatedHidden || [], options)
  const item = initDynamicFormItemConfig(table, template, allocation.display, allocation.hidden)
  const target = toBoolean(normalizedField.isForm, true) ? 'display' : 'hidden'

  return {
    item: patchDesignerItem({
      item,
      field: normalizedField,
      type,
      index,
      context,
    }),
    target,
    type,
    issues,
  }
}

const buildFormPatch = (dsl = {}) => {
  const form = dsl.form || {}
  const patch = {}
  if (!isEmpty(form.name)) {
    patch.name = form.name
  }
  if (!isEmpty(form.title)) {
    patch.comments = form.title
    patch.comments_EN = form.title
  }
  if (!isEmpty(form.module)) {
    patch.module = form.module
  }
  patch.tableType = form.tableType || '0'
  patch.pkColumnName = form.pkColumnName || 'id'
  return patch
}

const buildFormPropsPatch = (dsl = {}) => {
  const layout = dsl.layout || {}
  const form = dsl.form || {}
  const patch = {}
  if (!isEmpty(layout.labelWidth)) {
    patch.labelWidth = Number(layout.labelWidth)
  }
  if (!isEmpty(form.title)) {
    patch.mainTableTitle = form.title
  }
  if (layout.style === 'document-form') {
    patch.modal__Width = 1200
  }
  return patch
}

const buildSummary = ({dsl, displayItems, hiddenItems, issues, schemaIssues, typeCounter}) => ({
  title: dsl.form?.title || '',
  formName: dsl.form?.name || '',
  fieldCount: toArray(dsl.fields).length,
  displayCount: displayItems.length,
  hiddenCount: hiddenItems.length,
  errorCount: issues.filter(issue => issue.level === 'error').length,
  warningCount: issues.filter(issue => issue.level === 'warning').length,
  schema: summarizeSchemaIssues(schemaIssues),
  typeCounter,
})

export const convertDslToDesignerStatePatch = (dsl = {}, context = {}, options = {}) => {
  const schemaIssues = validateFormDesignDslSchema(dsl)
  const issues = schemaIssues.map(issue => ({
    ...issue,
    source: 'schema',
  }))
  const displayFormItemArr = []
  const hideFormItemArr = collectSystemHiddenItems({
    ...context,
    dsl,
  })
  const typeCounter = {}

  toArray(dsl.fields).forEach((field, index) => {
    const systemHiddenName = getSystemHiddenNameFromField(field)
    if (systemHiddenName && hasSystemHiddenItem(hideFormItemArr, field)) {
      return
    }

    const normalizedField = systemHiddenName ? normalizeSystemHiddenDslField(field) : field
    const result = convertDslFieldToDesignerItem(normalizedField, index, {
      ...context,
      dsl,
    }, {
      ...options,
      generatedDisplay: displayFormItemArr,
      generatedHidden: hideFormItemArr,
    })

    issues.push(...result.issues.map(issue => ({
      ...issue,
      source: 'converter',
    })))

    if (!result.item) {
      return
    }

    typeCounter[result.type] = (typeCounter[result.type] || 0) + 1
    if (result.target === 'hidden') {
      hideFormItemArr.push(result.item)
    } else {
      displayFormItemArr.push(result.item)
    }
  })

  const summary = buildSummary({
    dsl,
    displayItems: displayFormItemArr,
    hiddenItems: hideFormItemArr,
    issues,
    schemaIssues,
    typeCounter,
  })

  return {
    version: dsl.version || '',
    formPatch: buildFormPatch(dsl),
    formPropsPatch: buildFormPropsPatch(dsl),
    displayFormItemArr,
    hideFormItemArr,
    fixedArr: toArray(context.fixedArr),
    issues,
    errors: issues.filter(issue => issue.level === 'error'),
    warnings: issues.filter(issue => issue.level === 'warning'),
    summary,
    canApply: issues.every(issue => issue.level !== 'error'),
  }
}

export default convertDslToDesignerStatePatch
