import {
  FORM_MATERIAL_FIELD_TYPE_OPTIONS,
  FORM_MATERIAL_QUERY_MODE_OPTIONS,
} from "@/views/gen/genTableExt/ai/formMaterialSchema";

const normalizeText = (value) => String(value || '').trim()

const includesAny = (value, keywords = []) => {
  const text = normalizeText(value)
  return keywords.some(keyword => text.indexOf(keyword) >= 0)
}

const isEmpty = (value) => value === undefined || value === null || normalizeText(value) === ''

export const MATERIAL_SELECT_KEYWORDS = ['状态', '类型', '类别', '级别', '缓急', '密级', '是否', '性别']
export const MATERIAL_DATE_KEYWORDS = ['日期', '时间', '期限', '生日']
export const MATERIAL_INTEGER_KEYWORDS = ['数量', '人数', '次数', '天数', '年度', '年']
export const MATERIAL_DECIMAL_KEYWORDS = ['金额', '费用', '价格', '单价', '合计', '总额', '预算']
export const MATERIAL_UPLOAD_KEYWORDS = ['附件', '材料', '文件']
export const MATERIAL_IMAGE_KEYWORDS = ['图片', '照片', '影像']
export const MATERIAL_LONG_TEXT_KEYWORDS = ['描述', '说明', '备注', '内容', '意见', '原因', '要求', '批示', '批分', '结果']
export const MATERIAL_USER_KEYWORDS = ['申请人', '经办人', '负责人', '拟稿人', '核稿人', '审批人', '处理人', '联系人']
export const MATERIAL_USERS_KEYWORDS = ['参与人', '协办人', '会签人']
export const MATERIAL_OFFICE_KEYWORDS = ['部门', '机构', '单位']
export const MATERIAL_FULL_SPAN_TYPES = ['textarea', 'richText', 'upload', 'imageUpload', 'onlineFile']
export const MATERIAL_NAME_VALUE_TYPES = ['user', 'users', 'office', 'area', 'modalSelect']

const isSupportedType = (type = '') => FORM_MATERIAL_FIELD_TYPE_OPTIONS.includes(type)

const isSupportedQueryMode = (mode = '') => FORM_MATERIAL_QUERY_MODE_OPTIONS.includes(mode)

export const isMeaninglessMaterialName = (value = '') => {
  const name = normalizeText(value).toLowerCase()
  return /^(field|col|column)_?\d+$/.test(name)
      || ['field', 'column', 'unnamed_field'].includes(name)
}

export const isValidMaterialName = (value = '') => {
  const name = normalizeText(value)
  return /^[a-z][a-z0-9_]*$/.test(name)
}

export const isMaterialLongTextField = (field = {}) => {
  const label = normalizeText(field.label)
  const typeHint = normalizeText(field.typeHint)
  return MATERIAL_FULL_SPAN_TYPES.includes(typeHint)
      || MATERIAL_LONG_TEXT_KEYWORDS.some(keyword => label.indexOf(keyword) >= 0)
}

export const inferMaterialTypeHint = (field = {}) => {
  const currentType = normalizeText(field.typeHint)
  const label = normalizeText(field.label)
  const sample = normalizeText(field.valueExample)

  if (includesAny(label, MATERIAL_IMAGE_KEYWORDS)) return 'imageUpload'
  if (includesAny(label, MATERIAL_UPLOAD_KEYWORDS)) return 'upload'
  if (includesAny(label, MATERIAL_LONG_TEXT_KEYWORDS)) return 'textarea'
  if (includesAny(label, MATERIAL_DATE_KEYWORDS)
      || /^\d{4}[-/.年]\d{1,2}[-/.月]\d{1,2}/.test(sample)
      || /^\d{4}[-/.]\d{1,2}$/.test(sample)) {
    return 'date'
  }
  if (includesAny(label, MATERIAL_DECIMAL_KEYWORDS) || /^-?\d+\.\d+$/.test(sample)) return 'decimal'
  if (includesAny(label, MATERIAL_INTEGER_KEYWORDS) || /^-?\d+$/.test(sample)) return 'integer'
  if (includesAny(label, MATERIAL_USERS_KEYWORDS)) return 'users'
  if (includesAny(label, MATERIAL_USER_KEYWORDS)) return 'user'
  if (includesAny(label, MATERIAL_OFFICE_KEYWORDS)) return 'office'
  if (includesAny(label, MATERIAL_SELECT_KEYWORDS) || ['是', '否', '男', '女'].includes(sample)) return 'select'
  return isSupportedType(currentType) ? currentType : 'text'
}

export const inferMaterialDateType = (field = {}) => {
  const label = normalizeText(field.label)
  const rawText = normalizeText(field.rawText)
  const example = normalizeText(field.valueExample)
  const explicit = normalizeText(field.dateType || field.dateFormat || field.formatPatter)
  if (['yyyy', 'yyyy-MM', 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm:ss'].includes(explicit)) {
    return explicit
  }
  const source = `${label} ${rawText} ${example}`
  if (/[（(]?\s*年\s*[\/／-]\s*月\s*[)）]?/.test(source)
      || /年月|月份|月度/.test(source)
      || /^\d{4}[-/.]\d{1,2}$/.test(example)
      || /^\d{4}年\d{1,2}月$/.test(example)) {
    return 'yyyy-MM'
  }
  if (/年度|年份/.test(source) || /^\d{4}$/.test(example)) {
    return 'yyyy'
  }
  if (/时分秒|精确到秒|datetime/i.test(source)) {
    return 'yyyy-MM-dd HH:mm:ss'
  }
  return 'yyyy-MM-dd'
}

export const inferMaterialQueryMode = (field = {}) => {
  const label = normalizeText(field.label)
  const typeHint = normalizeText(field.typeHint) || 'text'
  if (typeHint === 'date' || includesAny(label, ['日期', '时间'])) return 'date-range'
  if (['integer', 'decimal'].includes(typeHint) || includesAny(label, ['金额', '数量', '费用', '合计', '总额'])) return 'range'
  if (['select', 'radio', 'checkbox', 'switch', 'user', 'users', 'office', 'area', 'modalSelect'].includes(typeHint)
      || includesAny(label, ['状态', '类型', '类别', '缓急', '密级', '部门', '单位', '机构', '人员'])) {
    return 'select'
  }
  if (includesAny(label, ['编号', '文号', '单号', '编码'])) return 'exact'
  return 'like'
}

export const inferMaterialListPriority = (field = {}) => {
  const label = normalizeText(field.label)
  const typeHint = normalizeText(field.typeHint) || 'text'
  if (isMaterialLongTextField(field)) return 0
  if (includesAny(label, ['名称', '标题', '主题'])) return 95
  if (includesAny(label, ['编号', '文号', '单号', '编码'])) return 90
  if (includesAny(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (includesAny(label, ['日期', '时间'])) return 80
  if (includesAny(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构'])) return 75
  if (includesAny(label, ['金额', '数量', '费用', '合计', '总额'])) return 70
  if (['select', 'radio', 'checkbox', 'switch', 'date', 'user', 'office'].includes(typeHint)) return 65
  return 40
}

export const inferMaterialQueryPriority = (field = {}) => {
  const label = normalizeText(field.label)
  if (isMaterialLongTextField(field)) return 0
  if (includesAny(label, ['名称', '标题', '主题', '编号', '文号', '单号', '编码'])) return 95
  if (includesAny(label, ['日期', '时间'])) return 90
  if (includesAny(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (includesAny(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构'])) return 80
  if (includesAny(label, ['金额', '数量', '费用', '合计', '总额'])) return 65
  return 30
}

const clearExampleDefaultValue = (field = {}) => {
  const example = normalizeText(field.valueExample)
  if (!example) {
    return false
  }
  let changed = false
  ;['defaultValue', 'defaultHint'].forEach(key => {
    if (normalizeText(field[key]) === example) {
      field[key] = ''
      changed = true
    }
  })
  if (field.form && typeof field.form === 'object' && normalizeText(field.form.defaultValue) === example) {
    field.form.defaultValue = ''
    changed = true
  }
  return changed
}

export const normalizeMaterialFieldSuggestion = (field = {}) => {
  if (!field || typeof field !== 'object') {
    return field
  }

  const inferredType = inferMaterialTypeHint(field)
  if (!isSupportedType(normalizeText(field.typeHint)) || normalizeText(field.typeHint) === 'text' || isMaterialLongTextField(field)) {
    field.typeHint = inferredType
  }

  const isLongText = isMaterialLongTextField(field)
  if (isLongText && normalizeText(field.typeHint) === 'text') {
    field.typeHint = 'textarea'
  }
  if (isLongText || MATERIAL_FULL_SPAN_TYPES.includes(normalizeText(field.typeHint))) {
    field.spanHint = 24
  } else if (![12, 24].includes(Number(field.spanHint))) {
    field.spanHint = 12
  }

  if (normalizeText(field.typeHint) === 'date') {
    field.dateType = inferMaterialDateType(field)
  }

  const listPriority = inferMaterialListPriority(field)
  const queryPriority = inferMaterialQueryPriority(field)
  if (field.listPriority === undefined || field.listPriority === null || field.listPriority === '') {
    field.listPriority = listPriority
  }
  if (field.queryPriority === undefined || field.queryPriority === null || field.queryPriority === '') {
    field.queryPriority = queryPriority
  }
  if (isLongText) {
    field.listHint = false
    field.queryHint = false
    field.listPriority = 0
    field.queryPriority = 0
  } else {
    if (field.listHint === undefined || field.listHint === null || field.listHint === '') {
      field.listHint = Number(field.listPriority || listPriority) >= 60
    }
    if (field.queryHint === undefined || field.queryHint === null || field.queryHint === '') {
      field.queryHint = Number(field.queryPriority || queryPriority) >= 70
    }
  }

  if (field.queryHint && (!isSupportedQueryMode(normalizeText(field.queryMode)) || !normalizeText(field.queryMode))) {
    field.queryMode = inferMaterialQueryMode(field)
  }
  if (!field.listReason) {
    field.listReason = field.listHint ? '适合作为列表摘要字段' : '不适合作为列表摘要字段'
  }
  if (!field.queryReason) {
    field.queryReason = field.queryHint ? '适合作为常用查询条件' : '不适合作为常用查询条件'
  }

  clearExampleDefaultValue(field)
  return field
}

export const normalizeFormMaterialSuggestions = (material = {}) => {
  if (!material || typeof material !== 'object' || !Array.isArray(material.fields)) {
    return material
  }
  material.fields.forEach(field => normalizeMaterialFieldSuggestion(field))
  return material
}

const createIssue = ({
  field = {},
  index = 0,
  code,
  title,
  description,
  level = 'warning',
}) => ({
  id: `material-quality:${code}:${field.id || normalizeText(field.label) || index}`,
  level,
  title,
  fieldId: field.id || '',
  fieldLabel: field.label || '',
  description,
  suggestion: description,
})

export const createMaterialFieldQualityIssues = ({
  field = {},
  index = 0,
  duplicateNameCount = 0,
}) => {
  const issues = []
  const label = normalizeText(field.label)
  const name = normalizeText(field.nameHint)
  const typeHint = normalizeText(field.typeHint) || 'text'
  const spanHint = Number(field.spanHint || 12)
  const inferredType = inferMaterialTypeHint(field)
  const isLongText = isMaterialLongTextField(field)
  const dateType = inferMaterialDateType(field)

  if (!label) {
    issues.push(createIssue({
      field,
      index,
      code: 'label-empty',
      title: '字段标题为空',
      description: '字段标题为空，生成表单后用户无法理解该字段。',
      level: 'error',
    }))
  }
  if (!name) {
    issues.push(createIssue({
      field,
      index,
      code: 'name-empty',
      title: '英文名为空',
      description: '英文名为空，AI 需要重新识别或由你手动确认后才能生成 DSL。',
      level: 'error',
    }))
  } else if (!isValidMaterialName(name)) {
    issues.push(createIssue({
      field,
      index,
      code: 'name-invalid',
      title: '英文名格式不规范',
      description: '英文名建议使用小写 snake_case，并以字母开头。',
    }))
  } else if (isMeaninglessMaterialName(name)) {
    issues.push(createIssue({
      field,
      index,
      code: 'name-meaningless',
      title: '英文名无业务含义',
      description: '英文名类似 field_01，AI 需要重新识别或由你手动确认后才能生成 DSL。',
      level: 'error',
    }))
  } else if (duplicateNameCount > 1) {
    issues.push(createIssue({
      field,
      index,
      code: 'name-duplicate',
      title: '英文名重复',
      description: `英文名 ${name} 出现重复，应用后可能造成字段冲突。`,
      level: 'error',
    }))
  }

  if (!isSupportedType(typeHint)) {
    issues.push(createIssue({
      field,
      index,
      code: 'type-unsupported',
      title: '控件类型不支持',
      description: `控件类型 ${typeHint || '空'} 不在支持范围内，建议重新识别或手工选择。`,
      level: 'error',
    }))
  } else if (label && inferredType !== typeHint && (typeHint === 'text' || ['date', 'user', 'users', 'office', 'textarea'].includes(inferredType))) {
    issues.push(createIssue({
      field,
      index,
      code: 'type-mismatch',
      title: '控件类型待确认',
      description: `从字段标题看更像 ${inferredType} 类型，当前为 ${typeHint}。`,
    }))
  }

  if (isLongText && typeHint === 'text') {
    issues.push(createIssue({
      field,
      index,
      code: 'long-text-type',
      title: '长文本类型待确认',
      description: '描述、说明、备注、意见等字段建议使用多行文本。',
    }))
  }
  if (isLongText && spanHint !== 24) {
    issues.push(createIssue({
      field,
      index,
      code: 'long-text-span',
      title: '长文本建议整行',
      description: '大内容字段半行显示会拥挤，建议设置为整行。',
    }))
  }
  if (isLongText && field.listHint) {
    issues.push(createIssue({
      field,
      index,
      code: 'long-text-list',
      title: '大内容字段进列表',
      description: '描述、说明、备注、意见等字段通常不适合默认显示在列表页。',
    }))
  }
  if (isLongText && field.queryHint) {
    issues.push(createIssue({
      field,
      index,
      code: 'long-text-query',
      title: '大内容字段进查询',
      description: '大内容字段通常不适合作为默认查询条件。',
    }))
  }
  if (typeHint === 'date' && field.dateType && field.dateType !== dateType) {
    issues.push(createIssue({
      field,
      index,
      code: 'date-format-mismatch',
      title: '日期格式待确认',
      description: `从字段标题和样例看更像 ${dateType}，当前为 ${field.dateType}。`,
    }))
  }
  if (field.queryHint && !normalizeText(field.queryMode)) {
    issues.push(createIssue({
      field,
      index,
      code: 'query-mode-empty',
      title: '查询模式为空',
      description: '已勾选查询，建议确认查询模式，例如模糊、精确、日期范围。',
    }))
  }
  if (!isEmpty(field.defaultValue) || !isEmpty(field.defaultHint) || !isEmpty(field.form?.defaultValue)) {
    issues.push(createIssue({
      field,
      index,
      code: 'default-value-present',
      title: '默认值待确认',
      description: '材料中的样例数据通常不应该作为字段默认值，建议确认后再保留。',
    }))
  }

  return issues
}

export default normalizeFormMaterialSuggestions
