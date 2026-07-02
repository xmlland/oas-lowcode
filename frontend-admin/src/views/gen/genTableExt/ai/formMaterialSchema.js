export const FORM_MATERIAL_SCHEMA_VERSION = '1.0'

export const FORM_MATERIAL_SOURCE_TYPES = [
  'text',
  'table-text',
  'excel',
  'image',
  'word',
  'pdf',
]

export const FORM_MATERIAL_SCENE_OPTIONS = [
  '',
  'normal',
  'document-form',
  'approval',
  'ledger',
]

export const FORM_MATERIAL_FIELD_TYPE_OPTIONS = [
  'text',
  'textarea',
  'integer',
  'decimal',
  'select',
  'radio',
  'checkbox',
  'switch',
  'date',
  'user',
  'users',
  'office',
  'area',
  'tree',
  'modalSelect',
  'modalMultiSelect',
  'upload',
  'imageUpload',
  'onlineFile',
  'richText',
  'serialNo',
]

export const FORM_MATERIAL_QUERY_MODE_OPTIONS = [
  '',
  'input',
  'like',
  'exact',
  'select',
  'date-range',
  'range',
]

const isObject = (value) => value !== null && typeof value === 'object' && !Array.isArray(value)

const toArray = (value) => Array.isArray(value) ? value : []

const normalizeText = (value) => String(value || '').trim()

const isEmpty = (value) => value === undefined || value === null || normalizeText(value) === ''

const isValidConfidence = (value) => {
  if (value === undefined || value === null || value === '') {
    return true
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= 0 && numberValue <= 1
}

const isValidPriority = (value) => {
  if (value === undefined || value === null || value === '') {
    return true
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= 0 && numberValue <= 100
}

const createIssue = ({
  id,
  level = 'error',
  title,
  fieldId = '',
  fieldLabel = '',
  description = '',
  suggestion = '',
  fixable = false,
  meta = {},
}) => ({
  id,
  level,
  title,
  fieldId,
  fieldLabel,
  description,
  suggestion,
  fixable,
  meta,
})

const validateRoot = (material, issues) => {
  if (!isObject(material)) {
    issues.push(createIssue({
      id: 'material-root-invalid',
      title: '材料结构不合法',
      description: 'FormMaterial 必须是一个对象。',
      suggestion: '请生成包含 version、source、fields、tables 的材料对象。',
    }))
    return false
  }

  if (material.version !== FORM_MATERIAL_SCHEMA_VERSION) {
    issues.push(createIssue({
      id: `material-version-invalid:${material.version || 'empty'}`,
      title: '材料版本不支持',
      description: `当前版本为 ${material.version || '空'}，当前只支持 ${FORM_MATERIAL_SCHEMA_VERSION}。`,
      suggestion: `请设置 version 为 ${FORM_MATERIAL_SCHEMA_VERSION}。`,
    }))
  }

  if (!isValidConfidence(material.confidence)) {
    issues.push(createIssue({
      id: `material-confidence-invalid:${material.confidence}`,
      level: 'warning',
      title: '材料置信度不合法',
      description: 'confidence 必须是 0 到 1 之间的数字。',
      suggestion: '无法判断时可以留空。',
    }))
  }

  return true
}

const validateSource = (material, issues) => {
  if (!isObject(material.source)) {
    issues.push(createIssue({
      id: 'material-source-missing',
      title: '材料来源缺失',
      description: 'source 对象用于描述材料来源类型。',
      suggestion: '请设置 source.type，例如 table-text、excel、image。',
    }))
    return
  }

  const sourceType = normalizeText(material.source.type)
  if (!FORM_MATERIAL_SOURCE_TYPES.includes(sourceType)) {
    issues.push(createIssue({
      id: `material-source-type-invalid:${sourceType || 'empty'}`,
      title: '材料来源类型不支持',
      description: `source.type 当前为 ${sourceType || '空'}。`,
      suggestion: `请使用 ${FORM_MATERIAL_SOURCE_TYPES.join('、')} 中的一种。`,
    }))
  }
}

const validateScene = (material, issues) => {
  const scene = normalizeText(material.scene)
  if (scene && !FORM_MATERIAL_SCENE_OPTIONS.includes(scene)) {
    issues.push(createIssue({
      id: `material-scene-invalid:${scene}`,
      level: 'warning',
      title: '材料场景不支持',
      description: `scene 当前为 ${scene}。`,
      suggestion: '请使用 normal、document-form、approval 或 ledger。',
    }))
  }
}

const validateFields = (material, issues) => {
  if (!Array.isArray(material.fields)) {
    issues.push(createIssue({
      id: 'material-fields-invalid',
      title: '字段列表不合法',
      description: 'fields 必须是数组。',
      suggestion: '请将识别到的字段放入 fields 数组。',
    }))
    return
  }

  const idMap = {}
  material.fields.forEach((field, index) => {
    if (!isObject(field)) {
      issues.push(createIssue({
        id: `material-field-invalid:${index}`,
        title: '字段项不合法',
        description: `第 ${index + 1} 个字段不是对象。`,
        suggestion: '每个字段至少需要包含 label。',
      }))
      return
    }

    const fieldId = normalizeText(field.id)
    const fieldLabel = normalizeText(field.label)
    if (isEmpty(fieldLabel)) {
      issues.push(createIssue({
        id: `material-field-label-missing:${fieldId || index}`,
        title: '字段标题缺失',
        fieldId,
        description: `第 ${index + 1} 个字段缺少 label。`,
        suggestion: '请补充用户可见字段名。',
      }))
    }

    if (fieldId) {
      if (!idMap[fieldId]) {
        idMap[fieldId] = []
      }
      idMap[fieldId].push(index)
    }

    if (!isEmpty(field.typeHint) && !FORM_MATERIAL_FIELD_TYPE_OPTIONS.includes(field.typeHint)) {
      issues.push(createIssue({
        id: `material-field-type-invalid:${fieldId || fieldLabel}:${field.typeHint}`,
        level: 'warning',
        title: '字段类型建议不支持',
        fieldId,
        fieldLabel,
        description: `typeHint 当前为 ${field.typeHint}。`,
        suggestion: '请使用 FormDesignDSL 支持的字段类型，或留空交给转换器推断。',
      }))
    }

    if (!isValidConfidence(field.confidence)) {
      issues.push(createIssue({
        id: `material-field-confidence-invalid:${fieldId || fieldLabel}`,
        level: 'warning',
        title: '字段置信度不合法',
        fieldId,
        fieldLabel,
        description: '字段 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }

    if (!isValidPriority(field.listPriority)) {
      issues.push(createIssue({
        id: `material-field-list-priority-invalid:${fieldId || fieldLabel}`,
        level: 'warning',
        title: '列表优先级不合法',
        fieldId,
        fieldLabel,
        description: 'listPriority 必须是 0 到 100 之间的数字。',
        suggestion: '无法判断时可以留空，确认页可手动调整列表建议。',
      }))
    }

    if (!isValidPriority(field.queryPriority)) {
      issues.push(createIssue({
        id: `material-field-query-priority-invalid:${fieldId || fieldLabel}`,
        level: 'warning',
        title: '查询优先级不合法',
        fieldId,
        fieldLabel,
        description: 'queryPriority 必须是 0 到 100 之间的数字。',
        suggestion: '无法判断时可以留空，确认页可手动调整查询建议。',
      }))
    }

    const queryMode = normalizeText(field.queryMode)
    if (queryMode && !FORM_MATERIAL_QUERY_MODE_OPTIONS.includes(queryMode)) {
      issues.push(createIssue({
        id: `material-field-query-mode-invalid:${fieldId || fieldLabel}:${queryMode}`,
        level: 'warning',
        title: '查询模式建议不支持',
        fieldId,
        fieldLabel,
        description: `queryMode 当前为 ${queryMode}。`,
        suggestion: `请使用 ${FORM_MATERIAL_QUERY_MODE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }
  })

  Object.keys(idMap)
      .filter(key => idMap[key].length > 1)
      .forEach(key => {
        issues.push(createIssue({
          id: `material-field-id-duplicate:${key}`,
          title: '字段 ID 重复',
          fieldId: key,
          description: `字段 ID ${key} 出现了 ${idMap[key].length} 次。`,
          suggestion: '请为每个识别字段分配唯一 ID。',
          meta: {
            indexes: idMap[key],
          },
        }))
      })
}

const validateTables = (material, issues) => {
  if (!Array.isArray(material.tables)) {
    issues.push(createIssue({
      id: 'material-tables-invalid',
      title: '表格列表不合法',
      description: 'tables 必须是数组。',
      suggestion: '没有表格时请设置为空数组。',
    }))
    return
  }

  material.tables.forEach((table, index) => {
    if (!isObject(table)) {
      issues.push(createIssue({
        id: `material-table-invalid:${index}`,
        title: '表格项不合法',
        description: `第 ${index + 1} 个表格不是对象。`,
        suggestion: '每个表格至少应包含 headers 或 rows。',
      }))
      return
    }

    if (table.headers !== undefined && !Array.isArray(table.headers)) {
      issues.push(createIssue({
        id: `material-table-headers-invalid:${table.id || index}`,
        title: '表头不合法',
        description: 'table.headers 必须是数组。',
        suggestion: '请使用字符串数组保存表头。',
      }))
    }
    if (table.rows !== undefined && !Array.isArray(table.rows)) {
      issues.push(createIssue({
        id: `material-table-rows-invalid:${table.id || index}`,
        title: '表格行不合法',
        description: 'table.rows 必须是二维数组。',
        suggestion: '没有样例数据时可以设置为空数组。',
      }))
    }
  })
}

export const validateFormMaterialSchema = (material = {}) => {
  const issues = []
  if (!validateRoot(material, issues)) {
    return issues
  }

  validateSource(material, issues)
  validateScene(material, issues)
  validateFields(material, issues)
  validateTables(material, issues)

  if (toArray(material.fields).length === 0 && toArray(material.tables).length === 0) {
    issues.push(createIssue({
      id: 'material-content-empty',
      title: '材料内容为空',
      description: 'fields 和 tables 至少需要有一项包含内容。',
      suggestion: '请补充识别字段，或从表格表头生成字段。',
    }))
  }

  return issues
}

export const summarizeFormMaterialIssues = (issues = []) => {
  const summary = {
    total: issues.length,
    error: 0,
    warning: 0,
    suggestion: 0,
    fixable: 0,
  }
  issues.forEach(issue => {
    if (summary[issue.level] !== undefined) {
      summary[issue.level] += 1
    }
    if (issue.fixable) {
      summary.fixable += 1
    }
  })
  return summary
}

export const isValidFormMaterialSchema = (material = {}) => {
  return validateFormMaterialSchema(material).every(issue => issue.level !== 'error')
}

export const createEmptyFormMaterial = (sourceType = 'text') => ({
  version: FORM_MATERIAL_SCHEMA_VERSION,
  source: {
    type: sourceType,
  },
  title: '',
  scene: '',
  language: 'zh-CN',
  fields: [],
  groups: [],
  tables: [],
  rawText: '',
  confidence: 0,
  issues: [],
  meta: {},
})

export default validateFormMaterialSchema
