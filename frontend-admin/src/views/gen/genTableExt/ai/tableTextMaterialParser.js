import {
  createEmptyFormMaterial,
  summarizeFormMaterialIssues,
  validateFormMaterialSchema,
} from "@/views/gen/genTableExt/ai/formMaterialSchema";
import {inferFieldNameHint} from "@/views/gen/genTableExt/ai/fieldNameInfer";

const normalizeText = (value) => String(value || '').trim()

const normalizeCell = (value) => normalizeText(value)
    .replace(/\u00a0/g, ' ')
    .replace(/^["“”'‘’]+|["“”'‘’]+$/g, '')
    .trim()

const createIssue = ({
  id,
  code = id,
  level = 'warning',
  title,
  description = '',
  fieldId = '',
  fieldLabel = '',
  fixable = false,
  meta = {},
}) => ({
  id,
  code,
  level,
  title,
  description,
  fieldId,
  fieldLabel,
  fixable,
  meta,
})

const hasAnyKeyword = (value, keywords = []) => {
  const text = normalizeText(value)
  return keywords.some(keyword => text.indexOf(keyword) >= 0)
}

const SELECT_KEYWORDS = ['状态', '类型', '类别', '级别', '缓急', '密级', '是否', '性别']
const DATE_KEYWORDS = ['日期', '时间', '期限', '生日']
const INTEGER_KEYWORDS = ['数量', '人数', '次数', '天数', '年度', '年']
const DECIMAL_KEYWORDS = ['金额', '费用', '价格', '单价', '合计', '总额', '预算']
const UPLOAD_KEYWORDS = ['附件', '材料', '文件']
const IMAGE_KEYWORDS = ['图片', '照片', '影像']
const TEXTAREA_KEYWORDS = ['意见', '说明', '备注', '描述', '内容', '批示', '批分', '结果', '原因', '要求']
const USER_KEYWORDS = ['申请人', '经办人', '负责人', '拟稿人', '核稿人', '审批人', '处理人', '联系人']
const USERS_KEYWORDS = ['参与人', '协办人', '会签人']
const OFFICE_KEYWORDS = ['部门', '机构', '单位']
const QUERY_KEYWORDS = ['名称', '编号', '文号', '标题', '日期', '状态', '类型', '类别', '密级', '缓急', '申请人', '经办人']
const REQUIRED_KEYWORDS = ['名称', '编号', '标题', '日期', '金额', '申请人', '经办人', '来文单位', '文号']
const FULL_SPAN_TYPES = ['textarea', 'richText', 'upload', 'imageUpload', 'onlineFile']

const FIELD_LABEL_HEADERS = [
  '字段',
  '字段名',
  '字段名称',
  '字段标题',
  '显示名称',
  '表单字段',
  'field',
  'label',
]

const TYPE_VALUE_MAP = [
  {keywords: ['多行', '大文本', '文本域', 'textarea', 'longtext'], type: 'textarea'},
  {keywords: ['整数', 'int', 'integer'], type: 'integer'},
  {keywords: ['小数', '数字', 'decimal', 'number'], type: 'decimal'},
  {keywords: ['日期', '时间', 'date', 'datetime'], type: 'date'},
  {keywords: ['下拉', '枚举', '选择', 'select'], type: 'select'},
  {keywords: ['单选', 'radio'], type: 'radio'},
  {keywords: ['多选', 'checkbox'], type: 'checkbox'},
  {keywords: ['开关', '是否', 'switch', 'boolean'], type: 'switch'},
  {keywords: ['人员多选', '多人'], type: 'users'},
  {keywords: ['人员', '用户', 'user'], type: 'user'},
  {keywords: ['部门', '机构', 'office'], type: 'office'},
  {keywords: ['附件', '文件', 'upload'], type: 'upload'},
  {keywords: ['图片', '照片', 'image'], type: 'imageUpload'},
  {keywords: ['富文本', 'rich'], type: 'richText'},
  {keywords: ['流水号', '编号生成', 'serial'], type: 'serialNo'},
  {keywords: ['单行', '文本', 'varchar', 'string', 'input'], type: 'text'},
]

const truthyText = (value) => {
  const text = normalizeText(value).toLowerCase()
  if (!text) {
    return false
  }
  return ['1', 'y', 'yes', 'true', '是', '有', '必填', '需要', '显示', '查询', '列表'].includes(text)
      || text.indexOf('是') >= 0
      || text.indexOf('必') >= 0
}

const inferFieldType = (label, example = '', typeText = '') => {
  const explicitType = normalizeText(typeText)
  if (explicitType) {
    const matched = TYPE_VALUE_MAP.find(item => hasAnyKeyword(explicitType, item.keywords))
    if (matched) {
      return matched.type
    }
  }

  const sample = normalizeText(example)
  if (hasAnyKeyword(label, IMAGE_KEYWORDS)) return 'imageUpload'
  if (hasAnyKeyword(label, UPLOAD_KEYWORDS)) return 'upload'
  if (hasAnyKeyword(label, TEXTAREA_KEYWORDS)) return 'textarea'
  if (hasAnyKeyword(label, DATE_KEYWORDS) || /^\d{4}[-/.年]\d{1,2}[-/.月]\d{1,2}/.test(sample)) return 'date'
  if (hasAnyKeyword(label, INTEGER_KEYWORDS) || /^-?\d+$/.test(sample)) return 'integer'
  if (hasAnyKeyword(label, DECIMAL_KEYWORDS) || /^-?\d+\.\d+$/.test(sample)) return 'decimal'
  if (hasAnyKeyword(label, USERS_KEYWORDS)) return 'users'
  if (hasAnyKeyword(label, USER_KEYWORDS)) return 'user'
  if (hasAnyKeyword(label, OFFICE_KEYWORDS)) return 'office'
  if (hasAnyKeyword(label, SELECT_KEYWORDS) || ['是', '否', '男', '女'].includes(sample)) return 'select'
  return 'text'
}

const inferListPriority = (label = '', typeHint = '') => {
  if (FULL_SPAN_TYPES.includes(typeHint) || hasAnyKeyword(label, TEXTAREA_KEYWORDS)) return 0
  if (hasAnyKeyword(label, ['名称', '标题', '主题'])) return 95
  if (hasAnyKeyword(label, ['编号', '文号', '单号', '编码'])) return 90
  if (hasAnyKeyword(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (hasAnyKeyword(label, ['日期', '时间'])) return 80
  if (hasAnyKeyword(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构'])) return 75
  if (hasAnyKeyword(label, ['金额', '数量', '费用', '合计', '总额'])) return 70
  if (['select', 'radio', 'checkbox', 'switch', 'date', 'user', 'office'].includes(typeHint)) return 65
  return 40
}

const inferQueryPriority = (label = '', typeHint = '') => {
  if (FULL_SPAN_TYPES.includes(typeHint) || hasAnyKeyword(label, TEXTAREA_KEYWORDS)) return 0
  if (hasAnyKeyword(label, ['名称', '标题', '主题', '编号', '文号', '单号', '编码'])) return 95
  if (hasAnyKeyword(label, ['日期', '时间'])) return 90
  if (hasAnyKeyword(label, ['状态', '类型', '类别', '缓急', '密级'])) return 85
  if (hasAnyKeyword(label, ['申请人', '经办人', '负责人', '部门', '单位', '机构'])) return 80
  if (hasAnyKeyword(label, ['金额', '数量', '费用', '合计', '总额'])) return 65
  if (hasAnyKeyword(label, QUERY_KEYWORDS)) return 60
  return 30
}

const inferQueryMode = (label = '', typeHint = '') => {
  if (typeHint === 'date' || hasAnyKeyword(label, ['日期', '时间'])) return 'date-range'
  if (['integer', 'decimal'].includes(typeHint) || hasAnyKeyword(label, ['金额', '数量', '费用', '合计', '总额'])) return 'range'
  if (['select', 'radio', 'checkbox', 'switch'].includes(typeHint) || hasAnyKeyword(label, ['状态', '类型', '类别', '缓急', '密级'])) return 'select'
  if (hasAnyKeyword(label, ['编号', '文号', '单号', '编码'])) return 'exact'
  return 'like'
}

const parseCsvLine = (line = '') => {
  const cells = []
  let current = ''
  let quoted = false
  for (let index = 0; index < line.length; index += 1) {
    const char = line[index]
    const nextChar = line[index + 1]
    if (char === '"' && nextChar === '"') {
      current += '"'
      index += 1
      continue
    }
    if (char === '"') {
      quoted = !quoted
      continue
    }
    if (char === ',' && !quoted) {
      cells.push(normalizeCell(current))
      current = ''
      continue
    }
    current += char
  }
  cells.push(normalizeCell(current))
  return cells
}

const isMarkdownSeparatorRow = (cells = []) => {
  return cells.length > 0 && cells.every(cell => /^:?-{3,}:?$/.test(normalizeText(cell)))
}

const splitTableLine = (line = '') => {
  const source = String(line || '').trim()
  if (!source) {
    return []
  }
  if (source.includes('\t')) {
    return source.split('\t').map(normalizeCell)
  }
  if (source.includes('|')) {
    const cells = source.split('|').map(normalizeCell)
    if (!cells[0]) cells.shift()
    if (!cells[cells.length - 1]) cells.pop()
    return cells
  }
  if (source.includes(',')) {
    return parseCsvLine(source)
  }
  if (source.includes('，')) {
    return source.split('，').map(normalizeCell)
  }
  if (/\s{2,}/.test(source)) {
    return source.split(/\s{2,}/).map(normalizeCell)
  }
  return [normalizeCell(source)]
}

const normalizeRows = (text = '') => {
  return String(text || '')
      .replace(/\r\n/g, '\n')
      .replace(/\r/g, '\n')
      .split('\n')
      .map(line => splitTableLine(line))
      .filter(cells => cells.some(cell => normalizeText(cell)))
      .filter(cells => !isMarkdownSeparatorRow(cells))
}

const findHeaderRowIndex = (rows = []) => {
  const index = rows.findIndex(cells => cells.filter(Boolean).length >= 2)
  if (index >= 0) {
    return index
  }
  return rows.length > 0 ? 0 : -1
}

const normalizeFieldLabel = (value = '') => {
  return normalizeText(value)
      .replace(/[＊*]/g, '')
      .replace(/^(字段|名称|标题)[：:]/, '')
      .replace(/[：:。；;，,、\s]+$/g, '')
      .trim()
}

const detectHeaderRole = (header = '') => {
  const text = normalizeText(header).toLowerCase()
  if (FIELD_LABEL_HEADERS.includes(text) || FIELD_LABEL_HEADERS.some(item => normalizeText(item) === text)) return 'label'
  if (hasAnyKeyword(header, ['字段名称', '字段标题', '显示名称', '字段名', '表单字段'])) return 'label'
  if (hasAnyKeyword(header, ['控件', '类型', '字段类型', '数据类型', '物理类型'])) return 'type'
  if (hasAnyKeyword(header, ['必填', '必录', '是否必填', 'required'])) return 'required'
  if (hasAnyKeyword(header, ['列表', '列表显示', '是否列表'])) return 'list'
  if (hasAnyKeyword(header, ['查询', '查询条件', '是否查询'])) return 'query'
  if (hasAnyKeyword(header, ['分组', '区域'])) return 'group'
  if (hasAnyKeyword(header, ['样例', '示例', '默认值', '示例值'])) return 'example'
  if (hasAnyKeyword(header, ['说明', '备注', '描述'])) return 'description'
  return ''
}

const buildHeaderRoleMap = (headers = []) => {
  const roles = headers.map(detectHeaderRole)
  const pick = (role) => roles.indexOf(role)
  return {
    roles,
    label: pick('label'),
    type: pick('type'),
    required: pick('required'),
    list: pick('list'),
    query: pick('query'),
    group: pick('group'),
    example: pick('example'),
    description: pick('description'),
  }
}

const isDefinitionTable = (headers = []) => {
  const roleMap = buildHeaderRoleMap(headers)
  const metadataRoles = ['type', 'required', 'list', 'query', 'group', 'example', 'description']
  return roleMap.label >= 0 && metadataRoles.some(role => roleMap[role] >= 0)
}

const createFieldId = (index) => `field_${index + 1}`

const createMaterialField = ({
  index,
  label,
  rawText,
  rowIndex = 0,
  columnIndex = 0,
  example = '',
  typeText = '',
  groupKey = 'base',
  requiredText = '',
  listText = '',
  queryText = '',
  description = '',
}) => {
  const normalizedLabel = normalizeFieldLabel(label)
  const requiredHint = truthyText(requiredText) || /[＊*]/.test(rawText || label) || hasAnyKeyword(normalizedLabel, REQUIRED_KEYWORDS)
  const typeHint = inferFieldType(normalizedLabel, example, typeText)
  const listPriority = inferListPriority(normalizedLabel, typeHint)
  const queryPriority = inferQueryPriority(normalizedLabel, typeHint)
  const listHint = listText ? truthyText(listText) : listPriority >= 60
  const queryHint = queryText ? truthyText(queryText) : queryPriority >= 70
  return {
    id: createFieldId(index),
    label: normalizedLabel,
    nameHint: inferFieldNameHint(normalizedLabel),
    typeHint,
    groupKey: normalizeText(groupKey) || 'base',
    requiredHint,
    listHint,
    listPriority,
    listReason: listHint ? '适合作为列表摘要字段' : '不适合作为列表摘要字段',
    queryHint,
    queryPriority,
    queryMode: queryHint ? inferQueryMode(normalizedLabel, typeHint) : '',
    queryReason: queryHint ? '适合作为常用查询条件' : '不适合作为常用查询条件',
    spanHint: FULL_SPAN_TYPES.includes(typeHint) ? 24 : 12,
    valueExample: normalizeText(example),
    placeholder: description ? `请输入${normalizedLabel}` : '',
    position: {
      page: 0,
      row: rowIndex,
      column: columnIndex,
    },
    confidence: typeHint === 'text' && !example ? 0.78 : 0.86,
    rawText: normalizeText(rawText || label),
    issues: [],
  }
}

const buildFieldsFromHeaders = (headers = [], sampleRows = [], issues = [], headerRowIndex = 0) => {
  const fields = []
  const seenLabels = {}
  headers.forEach((header, columnIndex) => {
    const label = normalizeFieldLabel(header)
    if (!label) {
      issues.push(createIssue({
        id: `table-header-empty:${columnIndex}`,
        code: 'table-header-empty',
        title: '表头为空',
        description: `第 ${columnIndex + 1} 列表头为空，已跳过。`,
        meta: {columnIndex},
      }))
      return
    }
    seenLabels[label] = (seenLabels[label] || 0) + 1
    if (seenLabels[label] > 1) {
      issues.push(createIssue({
        id: `table-header-duplicate:${label}:${columnIndex}`,
        code: 'table-header-duplicate',
        title: '表头重复',
        description: `字段“${label}”出现了 ${seenLabels[label]} 次，请后续确认是否需要拆分。`,
        fieldLabel: label,
        meta: {columnIndex},
      }))
    }
    fields.push(createMaterialField({
      index: fields.length,
      label,
      rawText: header,
      rowIndex: headerRowIndex,
      columnIndex,
      example: sampleRows.find(row => normalizeText(row[columnIndex]))?.[columnIndex] || '',
    }))
  })
  return fields
}

const buildFieldsFromDefinitionRows = (headers = [], rows = [], issues = [], headerRowIndex = 0) => {
  const roleMap = buildHeaderRoleMap(headers)
  const fields = []
  rows.forEach((row, rowOffset) => {
    const label = normalizeFieldLabel(row[roleMap.label])
    if (!label) {
      issues.push(createIssue({
        id: `table-definition-label-empty:${rowOffset}`,
        code: 'table-definition-label-empty',
        title: '字段定义行缺少字段名',
        description: `第 ${headerRowIndex + rowOffset + 2} 行没有字段名称，已跳过。`,
        meta: {rowIndex: headerRowIndex + rowOffset + 1},
      }))
      return
    }
    fields.push(createMaterialField({
      index: fields.length,
      label,
      rawText: row[roleMap.label],
      rowIndex: headerRowIndex + rowOffset + 1,
      columnIndex: roleMap.label,
      example: roleMap.example >= 0 ? row[roleMap.example] : '',
      typeText: roleMap.type >= 0 ? row[roleMap.type] : '',
      groupKey: roleMap.group >= 0 ? row[roleMap.group] : 'base',
      requiredText: roleMap.required >= 0 ? row[roleMap.required] : '',
      listText: roleMap.list >= 0 ? row[roleMap.list] : '',
      queryText: roleMap.query >= 0 ? row[roleMap.query] : '',
      description: roleMap.description >= 0 ? row[roleMap.description] : '',
    }))
  })
  return fields
}

const inferTitle = (rows = [], headerRowIndex = 0, fields = []) => {
  const beforeHeader = rows.slice(0, headerRowIndex)
      .map(row => row.filter(Boolean).join(' '))
      .map(normalizeText)
      .filter(Boolean)
  if (beforeHeader.length > 0) {
    return beforeHeader[beforeHeader.length - 1]
  }
  const titleField = fields.find(field => ['标题', '名称', '主题'].includes(field.label))
  if (titleField?.valueExample) {
    return titleField.valueExample
  }
  return ''
}

const inferScene = (title = '', fields = [], options = {}) => {
  const text = `${title} ${fields.map(field => field.label).join(' ')}`
  if (hasAnyKeyword(text, ['收文', '发文', '文号', '来文单位', '主送单位', '抄送单位'])) {
    return 'document-form'
  }
  if (hasAnyKeyword(text, ['审批', '意见', '申请', '审核'])) {
    return 'approval'
  }
  if (options.hasSampleRows || hasAnyKeyword(text, ['台账', '清单', '登记', '明细'])) {
    return 'ledger'
  }
  return 'normal'
}

const uniqueGroups = (fields = []) => {
  const groups = {}
  fields.forEach(field => {
    const key = normalizeText(field.groupKey) || 'base'
    groups[key] = {
      key,
      title: key === 'base' ? '基本信息' : key,
      position: {},
      confidence: 0.8,
    }
  })
  return Object.values(groups)
}

export const parseTableTextToFormMaterial = (text = '', options = {}) => {
  const rawText = String(text || '')
  const parseIssues = []
  const rows = normalizeRows(rawText)
  const headerRowIndex = findHeaderRowIndex(rows)
  const material = createEmptyFormMaterial('table-text')
  material.source = {
    type: 'table-text',
    name: options.name || '',
    capturedAt: new Date().toISOString(),
  }
  material.rawText = rawText
  material.meta = {
    parser: 'table-text-mvp',
    rowCount: rows.length,
    headerRowIndex,
  }

  if (!rawText.trim()) {
    parseIssues.push(createIssue({
      id: 'table-text-empty',
      code: 'table-text-empty',
      level: 'error',
      title: '粘贴内容为空',
      description: '请先粘贴 Excel、WPS、网页表格或 Markdown 表格文本。',
    }))
    material.issues = parseIssues
    return {
      material,
      parseIssues,
      schemaIssues: validateFormMaterialSchema(material),
      summary: summarizeFormMaterialIssues(parseIssues),
    }
  }

  if (headerRowIndex < 0) {
    parseIssues.push(createIssue({
      id: 'table-header-not-found',
      code: 'table-header-not-found',
      level: 'error',
      title: '没有识别到表头',
      description: '请确认粘贴内容包含至少一行字段标题。',
    }))
    material.issues = parseIssues
    return {
      material,
      parseIssues,
      schemaIssues: validateFormMaterialSchema(material),
      summary: summarizeFormMaterialIssues(parseIssues),
    }
  }

  const headers = rows[headerRowIndex] || []
  const dataRows = rows.slice(headerRowIndex + 1)
  const definitionTable = isDefinitionTable(headers)
  const fields = definitionTable
      ? buildFieldsFromDefinitionRows(headers, dataRows, parseIssues, headerRowIndex)
      : buildFieldsFromHeaders(headers, dataRows, parseIssues, headerRowIndex)

  material.fields = fields
  material.groups = uniqueGroups(fields)
  material.tables = [{
    id: 'table_1',
    title: '',
    headers,
    rows: dataRows,
    position: {
      page: 0,
      row: headerRowIndex,
      column: 0,
    },
    confidence: definitionTable ? 0.82 : 0.86,
  }]
  material.title = options.title || inferTitle(rows, headerRowIndex, fields)
  material.scene = options.scene || inferScene(material.title, fields, {hasSampleRows: dataRows.length > 0})
  material.confidence = fields.length > 0 ? Math.min(0.95, 0.72 + Math.min(fields.length, 10) * 0.02) : 0.35

  if (definitionTable) {
    parseIssues.push(createIssue({
      id: 'table-definition-mode',
      code: 'table-definition-mode',
      level: 'suggestion',
      title: '已按字段定义表解析',
      description: '识别到“字段/类型/必填”等表头，已按每一行生成一个字段。',
      fixable: false,
    }))
  }

  if (fields.length === 0) {
    parseIssues.push(createIssue({
      id: 'table-fields-empty',
      code: 'table-fields-empty',
      level: 'error',
      title: '没有生成字段',
      description: '未能从表头或字段定义行中识别字段，请检查表格格式。',
    }))
  }

  material.issues = parseIssues
  const schemaIssues = validateFormMaterialSchema(material)
  return {
    material,
    parseIssues,
    schemaIssues,
    summary: summarizeFormMaterialIssues(parseIssues.concat(schemaIssues)),
  }
}

export default parseTableTextToFormMaterial
