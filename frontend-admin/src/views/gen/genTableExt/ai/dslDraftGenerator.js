import {DSL_SCHEMA_VERSION, validateFormDesignDslSchema, summarizeSchemaIssues} from "@/views/gen/genTableExt/ai/dslSchema";
import {inferFieldNameHint} from "@/views/gen/genTableExt/ai/fieldNameInfer";
import {ensureDslDictionarySuggestions} from "@/views/gen/genTableExt/ai/dictionarySuggestions";

const GENERATOR_NAME = 'local-heuristic'

const FORM_NAME_MAP = [
  {keywords: ['合同', '审批'], name: 'contract_apply', title: '合同审批'},
  {keywords: ['收文'], name: 'oas_receive', title: '收文处理笺'},
  {keywords: ['发文'], name: 'oas_send', title: '发文处理笺'},
  {keywords: ['采购'], name: 'purchase_apply', title: '采购申请'},
  {keywords: ['请假'], name: 'leave_apply', title: '请假申请'},
  {keywords: ['用车'], name: 'car_apply', title: '用车申请'},
  {keywords: ['项目', '立项'], name: 'project_apply', title: '项目立项'},
  {keywords: ['档案'], name: 'archive_register', title: '档案登记'},
]

const SELECT_KEYWORDS = ['状态', '类型', '类别', '级别', '缓急', '密级', '是否']
const DATE_KEYWORDS = ['日期', '时间', '期限', '生日']
const INTEGER_KEYWORDS = ['数量', '人数', '次数', '天数', '年度', '年']
const DECIMAL_KEYWORDS = ['金额', '费用', '价格', '单价', '合计', '总额', '预算']
const UPLOAD_KEYWORDS = ['附件', '材料', '文件', '图片', '影像']
const TEXTAREA_KEYWORDS = ['意见', '说明', '备注', '描述', '内容', '批示', '批分', '结果', '原因', '要求']
const USER_KEYWORDS = ['申请人', '经办人', '负责人', '拟稿人', '核稿人', '审批人', '处理人']
const USERS_KEYWORDS = ['参与人', '协办人', '会签人']
const OFFICE_KEYWORDS = ['部门', '机构']
const TITLE_KEYWORDS = ['标题', '名称', '主题']
const QUERY_KEYWORDS = ['名称', '编号', '文号', '标题', '日期', '状态', '类型', '类别', '密级', '缓急', '申请人', '经办人']
const REQUIRED_KEYWORDS = ['名称', '编号', '标题', '日期', '金额', '申请人', '经办人', '来文单位', '文号']
const NAME_VALUE_TYPES = ['user', 'users', 'office', 'area', 'modalSelect']

const USER_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.User'
const OFFICE_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Office'
const AREA_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.system.Area'
const ZFORM_JAVA_TYPE = 'com.jeestudio.bpm.common.entity.common.Zform'

const TYPE_CONFIG = {
  text: {showType: 'input', jdbcType: 'varchar', javaType: 'String'},
  textarea: {showType: 'textarea', jdbcType: 'longtext', javaType: 'String'},
  integer: {showType: 'integer', jdbcType: 'integer', javaType: 'Integer'},
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

const includesAny = (value, keywords) => {
  const text = normalizeText(value)
  return keywords.some(keyword => text.indexOf(keyword) >= 0)
}

const uniq = (items) => {
  const seen = {}
  return items.filter(item => {
    const key = normalizeText(item)
    if (!key || seen[key]) {
      return false
    }
    seen[key] = true
    return true
  })
}

const normalizeFieldLabel = (value) => {
  return normalizeText(value)
      .replace(/^(以及|还有|和|及|与)/, '')
      .replace(/(等字段|等信息|等内容|字段|信息)$/g, '')
      .replace(/[：:。；;，,、\s]+$/g, '')
      .trim()
}

const splitFieldText = (text) => {
  const source = normalizeText(text)
      .replace(/\r?\n/g, '、')
      .replace(/以及/g, '、')
      .replace(/还有/g, '、')
      .replace(/和/g, '、')
      .replace(/及/g, '、')
      .replace(/与/g, '、')
  return uniq(source.split(/[、，,；;。]/).map(normalizeFieldLabel))
}

const extractRequirementBody = (text) => {
  const source = normalizeText(text)
  const markers = ['需要', '包含', '包括', '字段有', '字段包括', '字段包含', '录入']
  const marker = markers.find(item => source.indexOf(item) >= 0)
  if (marker) {
    return source.slice(source.indexOf(marker) + marker.length)
  }
  const commaIndex = source.search(/[，,:：]/)
  return commaIndex >= 0 ? source.slice(commaIndex + 1) : source
}

const inferLayoutStyle = (text, explicitStyle = '') => {
  if (explicitStyle) {
    return explicitStyle
  }
  if (includesAny(text, ['收文', '发文', '文号', '密级', '缓急', '批示', '处理笺'])) {
    return 'document-form'
  }
  if (includesAny(text, ['审批', '申请', '意见', '流程'])) {
    return 'approval'
  }
  if (includesAny(text, ['台账', '登记', '管理'])) {
    return 'normal'
  }
  return 'normal'
}

const inferFormMeta = (text, options = {}) => {
  const source = normalizeText(text)
  const matched = FORM_NAME_MAP.find(item => item.keywords.every(keyword => source.indexOf(keyword) >= 0))
  const title = options.title || matched?.title || inferTitle(source)
  return {
    name: options.name || matched?.name || 'ai_generated_form',
    title,
    module: options.module || 'oas',
    tableType: options.tableType || '0',
    pkColumnName: options.pkColumnName || 'id',
  }
}

const inferTitle = (text) => {
  const beforeMarker = normalizeText(text).split(/需要|包含|包括|字段有|字段包括|字段包含/)[0]
  const title = beforeMarker
      .replace(/^(帮我|请|生成|创建|做一个|做|新建|设计一个|设计)/, '')
      .replace(/表单|表|页面/g, '')
      .replace(/[，,：:\s]+$/g, '')
      .trim()
  return title || 'AI生成表单'
}

const inferFieldName = (label, index, usedNames) => {
  let name = inferFieldNameHint(label, `field_${String(index + 1).padStart(2, '0')}`)
  if (!usedNames[name]) {
    usedNames[name] = 1
    return name
  }
  usedNames[name] += 1
  return `${name}_${usedNames[name]}`
}

const inferFieldType = (label, style) => {
  if (includesAny(label, UPLOAD_KEYWORDS)) {
    return label.indexOf('图片') >= 0 ? 'imageUpload' : 'upload'
  }
  if (includesAny(label, DATE_KEYWORDS)) {
    return 'date'
  }
  if (includesAny(label, DECIMAL_KEYWORDS)) {
    return 'decimal'
  }
  if (includesAny(label, INTEGER_KEYWORDS)) {
    return 'integer'
  }
  if (includesAny(label, USERS_KEYWORDS)) {
    return 'users'
  }
  if (includesAny(label, USER_KEYWORDS)) {
    return 'user'
  }
  if (includesAny(label, TEXTAREA_KEYWORDS)) {
    return 'textarea'
  }
  if (includesAny(label, OFFICE_KEYWORDS) && style !== 'document-form') {
    return 'office'
  }
  if (includesAny(label, SELECT_KEYWORDS)) {
    return 'select'
  }
  return 'text'
}

const inferSpan = (label, type, style) => {
  if (style === 'document-form' && includesAny(label, ['标题', '批示', '批分', '意见', '结果', '附件'])) {
    return 24
  }
  if (includesAny(label, TITLE_KEYWORDS) || ['textarea', 'upload', 'imageUpload', 'onlineFile', 'richText'].includes(type)) {
    return 24
  }
  return 12
}

const inferRequired = (label, type) => {
  if (type === 'textarea' && includesAny(label, ['意见', '批示', '批分', '结果'])) {
    return false
  }
  return includesAny(label, REQUIRED_KEYWORDS)
}

const inferListShow = (label, type, style) => {
  if (['textarea', 'upload', 'imageUpload', 'onlineFile', 'richText'].includes(type)) {
    return false
  }
  if (style === 'document-form' && includesAny(label, ['批示', '批分', '意见', '结果', '密级期限'])) {
    return false
  }
  return includesAny(label, ['名称', '编号', '文号', '标题', '日期', '金额', '状态', '类型', '缓急', '密级', '申请人', '经办人', '来文单位', '甲方', '乙方'])
}

const inferQueryEnabled = (label, type) => {
  if (['textarea', 'upload', 'imageUpload', 'onlineFile', 'richText'].includes(type)) {
    return false
  }
  return includesAny(label, QUERY_KEYWORDS)
}

const inferQueryType = (type) => {
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

const inferListAlign = (type) => {
  if (['integer', 'decimal'].includes(type)) return 'right'
  if (type === 'date') return 'center'
  return 'left'
}

const inferListDataIndex = (name, type) => {
  return NAME_VALUE_TYPES.includes(type) ? `${name}__name` : name
}

const getGroupKey = (label, type, style) => {
  if (style === 'document-form') {
    if (includesAny(label, ['批示', '批分', '意见', '结果', '附件'])) {
      return 'approval'
    }
    return 'base'
  }
  if (['textarea', 'upload', 'imageUpload', 'onlineFile', 'richText'].includes(type) || includesAny(label, TEXTAREA_KEYWORDS)) {
    return 'detail'
  }
  return 'base'
}

const buildGroups = (style, fields) => {
  const groupTitleMap = style === 'document-form'
      ? {base: '收文信息', approval: '办理意见', detail: '详细信息'}
      : style === 'approval'
          ? {base: '基本信息', detail: '审批信息'}
          : {base: '基本信息', detail: '详细信息'}
  const keys = uniq(fields.map(field => field.group))
  return keys.map(key => ({
    key,
    title: groupTitleMap[key] || key,
  }))
}

const buildField = ({label, index, style, usedNames}) => {
  const type = inferFieldType(label, style)
  const config = TYPE_CONFIG[type] || TYPE_CONFIG.text
  const name = inferFieldName(label, index, usedNames)
  const span = inferSpan(label, type, style)
  const isList = inferListShow(label, type, style)
  const isQuery = inferQueryEnabled(label, type)
  const group = getGroupKey(label, type, style)

  return {
    name,
    label,
    type,
    group,
    showType: config.showType,
    javaField: '',
    jdbcType: config.jdbcType,
    javaType: config.javaType,
    required: inferRequired(label, type),
    readonly: false,
    span,
    isForm: true,
    isList,
    isQuery,
    formSort: (index + 1) * 10,
    listSort: (index + 1) * 10,
    dictType: '',
    dateType: type === 'date' ? 'yyyy-MM-dd' : '',
    db: {
      jdbcType: config.jdbcType,
      length: type === 'text' || type === 'select' ? 255 : undefined,
      precision: type === 'decimal' ? 18 : undefined,
      scale: type === 'decimal' ? 2 : undefined,
    },
    form: {
      defaultValue: '',
      readonly: false,
      props: {
        name,
        label,
        required: inferRequired(label, type),
      },
      controlProps: {},
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
      queryFieldType: isQuery ? inferQueryType(type) : '',
      // dictType 在后续绑定字典后由 dslToDesignerState/buildListConfig 补齐
      queryFieldProps: isQuery ? {
        placeholder: label,
        formatPatter: type === 'date' ? 'yyyy-MM-dd' : undefined,
      } : null,
    },
    query: {
      enabled: isQuery,
      type: isQuery ? inferQueryType(type) : '',
    },
  }
}

const createDefaultLabels = (style) => {
  if (style === 'document-form') {
    return ['来文单位', '文号', '标题', '收文日期', '缓急', '局领导批示', '办公室批分', '附件']
  }
  if (style === 'approval') {
    return ['名称', '申请人', '申请部门', '申请日期', '说明', '部门意见', '领导意见', '附件']
  }
  return ['名称', '编号', '日期', '状态', '备注']
}

const buildSummary = (dsl, sourceContent) => {
  const fields = dsl.fields || []
  return {
    title: dsl.form.title,
    style: dsl.layout.style,
    fieldCount: fields.length,
    listCount: fields.filter(field => field.isList).length,
    queryCount: fields.filter(field => field.isQuery).length,
    groupCount: dsl.layout.groups.length,
    sourceLength: normalizeText(sourceContent).length,
  }
}

export const parseFieldLabelsFromText = (text, options = {}) => {
  const style = inferLayoutStyle(text, options.style)
  const labels = splitFieldText(extractRequirementBody(text))
  return labels.length > 0 ? labels : createDefaultLabels(style)
}

export const generateFormDesignDslDraft = (text = '', options = {}) => {
  const sourceContent = normalizeText(text)
  const style = inferLayoutStyle(sourceContent, options.style)
  const form = inferFormMeta(sourceContent, options.form || {})
  const labels = parseFieldLabelsFromText(sourceContent, {style})
  const usedNames = {}
  const fields = labels.map((label, index) => buildField({
    label,
    index,
    style,
    usedNames,
  }))
  const groups = buildGroups(style, fields)

  const dsl = {
    version: DSL_SCHEMA_VERSION,
    generatedAt: new Date().toISOString(),
    generator: GENERATOR_NAME,
    form,
    layout: {
      style,
      labelWidth: style === 'document-form' ? 100 : 100,
      groups,
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
      sourceType: 'natural-language',
      sourceContent,
    },
  }

  const normalizedDsl = ensureDslDictionarySuggestions(dsl)
  const schemaIssues = validateFormDesignDslSchema(normalizedDsl)
  return {
    id: `local-${Date.now()}`,
    sourceType: 'natural-language',
    sourceContent,
    generator: GENERATOR_NAME,
    createdAt: normalizedDsl.generatedAt,
    dsl: normalizedDsl,
    schemaIssues,
    summary: {
      ...buildSummary(normalizedDsl, sourceContent),
      schema: summarizeSchemaIssues(schemaIssues),
    },
  }
}

export default generateFormDesignDslDraft
