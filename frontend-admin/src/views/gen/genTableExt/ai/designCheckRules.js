const LEVEL_ERROR = 'error'
const LEVEL_WARNING = 'warning'
const LEVEL_SUGGESTION = 'suggestion'

const SELECT_SHOW_TYPES = ['select', 'radiobox', 'checkbox']
const NAME_VALUE_SHOW_TYPES = ['treeselectRedio', 'treeselectCheck', 'officeselectTree', 'areaselect', 'gridselect', 'parentId']
const HEAVY_LIST_SHOW_TYPES = ['fileupload', 'fileuploadpic', 'fileuploadonline', 'richText', 'textarea']
const TITLE_LABEL_KEYWORDS = ['标题', '名称', '主题']
const TITLE_FIELD_NAMES = ['title', 'name', 'subject']
const NUMBER_FIELD_KEYWORDS = ['金额', '数量', '分数', '价格', '单价', '合计', '总额', 'amount', 'price', 'score', 'num', 'count', 'qty']

const isEmpty = (value) => value === undefined || value === null || value === ''

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const parseJson = (value, fallback = {}) => {
  if (!value) {
    return fallback
  }
  if (typeof value === 'object') {
    return value
  }
  try {
    return JSON.parse(value)
  } catch (e) {
    return fallback
  }
}

const isTruthy = (value) => value === true || value === '1' || value === 1

const includesAny = (value, keywords) => {
  const text = normalizeText(value).toLowerCase()
  return keywords.some(keyword => text.indexOf(keyword.toLowerCase()) >= 0)
}

const isTitleLikeField = (field = {}) => {
  const label = normalizeText(field.label)
  const name = normalizeText(field.name).toLowerCase()
  return includesAny(label, TITLE_LABEL_KEYWORDS) || TITLE_FIELD_NAMES.includes(name)
}

const createIssue = ({
  id,
  level,
  title,
  field,
  fieldName,
  description,
  suggestion,
  fixable = false,
  fix = null,
  meta = {},
}) => ({
  id,
  level,
  title,
  fieldName: fieldName || field?.name || '',
  fieldLabel: field?.label || '',
  description,
  suggestion,
  fixable,
  fix,
  meta,
})

const groupByValue = (items, getter) => {
  const map = {}
  items.forEach(item => {
    const value = normalizeText(getter(item))
    if (!value) {
      return
    }
    if (!map[value]) {
      map[value] = []
    }
    map[value].push(item)
  })
  return Object.keys(map)
      .filter(key => map[key].length > 1)
      .map(key => ({key, items: map[key]}))
}

const getListConfig = (field = {}) => {
  return parseJson(field.raw?.column?.listConfig)
}

const getQueryFieldProps = (field = {}, listColumn = {}) => {
  return parseJson(listColumn?.queryFieldProps || field.list?.queryFieldProps, {})
}

const getFieldListColumn = (field = {}, listColumns = []) => {
  return listColumns.find(item => item.name === field.name) || null
}

const getEffectiveList = (field = {}, listColumn = {}) => ({
  isList: isTruthy(listColumn?.isList) || field.isList === true,
  isQuery: isTruthy(listColumn?.isQuery) || field.isQuery === true,
  dataIndex: listColumn?.dataIndex || field.list?.dataIndex || field.name || '',
  title: listColumn?.title || field.list?.title || field.label || '',
  align: listColumn?.align || field.list?.align || '',
  dict: listColumn?.dict || field.list?.dict || '',
  queryColumn: listColumn?.queryColumn || field.list?.queryColumn || '',
  queryFieldType: listColumn?.queryFieldType || field.list?.queryFieldType || '',
  queryFieldProps: getQueryFieldProps(field, listColumn),
  explicitListConfig: getListConfig(field),
})

const isRelationSelect = (field = {}) => {
  const column = field.raw?.column || {}
  const controlProps = field.form?.controlProps || {}
  return column.selectSimple === '0' || Boolean(controlProps.dataUrl || controlProps.valueField || controlProps.textField)
}

const checkDuplicateFieldName = (fields) => {
  const realFields = fields.filter(field => field.source !== 'extend')
  return groupByValue(realFields, field => field.name).map(({key, items}) => createIssue({
    id: `duplicate-field-name:${key}`,
    level: LEVEL_ERROR,
    title: '字段名重复',
    field: items[0],
    description: `字段名 ${key} 出现了 ${items.length} 次，保存或渲染时可能互相覆盖。`,
    suggestion: '请手工重命名其中一个字段，确保每个字段名唯一。',
    fixable: false,
    meta: {
      fields: items.map(item => ({
        name: item.name,
        label: item.label,
        source: item.source,
        javaField: item.javaField,
        isForm: item.isForm,
        isList: item.isList,
      })),
    },
  }))
}

const checkDuplicateJavaField = (fields) => {
  const realFields = fields.filter(field => !field.isExtend && field.source !== 'extend' && field.type !== 'modalMultiSelect')
  return groupByValue(realFields, field => field.javaField).map(({key, items}) => createIssue({
    id: `duplicate-java-field:${key}`,
    level: LEVEL_ERROR,
    title: '底层字段重复',
    field: items[0],
    description: `底层 javaField ${key} 被多个字段共用，可能造成数据库字段冲突。`,
    suggestion: '请检查字段复制或手工重新分配底层字段。',
    fixable: false,
    meta: {
      fieldNames: items.map(item => item.name),
    },
  }))
}

const checkMissingRequiredProperties = (fields) => {
  const issues = []
  fields.forEach(field => {
    const missing = []
    if (!field.name) missing.push('name')
    if (!field.label) missing.push('label')
    if (field.source !== 'fixed' && field.type !== 'modalMultiSelect' && !field.showType) missing.push('showType')
    if (!field.isExtend && field.source !== 'extend' && field.type !== 'modalMultiSelect' && !field.jdbcType) missing.push('jdbcType')

    if (missing.length > 0) {
      issues.push(createIssue({
        id: `missing-required-properties:${field.name || field.id || issues.length}`,
        level: LEVEL_ERROR,
        title: '必要属性缺失',
        field,
        description: `字段缺少必要属性：${missing.join('、')}。`,
        suggestion: '请补齐字段名、说明、控件类型或物理类型后再继续生成。',
        fixable: false,
        meta: {
          missing,
        },
      }))
    }
  })
  return issues
}

const checkSelectMissingDict = (fields) => {
  return fields
      .filter(field => SELECT_SHOW_TYPES.includes(field.showType))
      .filter(field => !isRelationSelect(field))
      .filter(field => isEmpty(field.dictType))
      .map(field => createIssue({
        id: `select-missing-dict:${field.name}`,
        level: LEVEL_WARNING,
        title: '选择字段缺少字典',
        field,
        description: '当前字段是选择类控件，但没有配置字典类型。',
        suggestion: `建议创建或选择字典编码，例如 ${normalizeText(field.raw?.column?.tableName || field.formNo || 'form')}_${field.name}。`,
        fixable: false,
      }))
}

const checkDateQueryFieldType = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, listColumn, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => field.showType === 'dateselect' && list.isQuery)
      .filter(({list}) => list.queryFieldType !== 'date-range')
      .map(({field, list}) => createIssue({
        id: `date-query-field-type:${field.name}`,
        level: LEVEL_WARNING,
        title: '日期字段建议使用日期范围查询',
        field,
        description: '当前字段已作为查询条件，但查询组件不是 date-range。',
        suggestion: '建议将查询组件改为 date-range，并沿用字段日期格式。',
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            queryFieldType: 'date-range',
            queryFieldProps: {
              ...list.queryFieldProps,
              placeholder: list.queryFieldProps.placeholder || field.label,
              formatPatter: list.queryFieldProps.formatPatter || field.dateType || 'yyyy-MM-dd',
            },
          },
        },
      }))
}

const checkNameValueListDataIndex = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => NAME_VALUE_SHOW_TYPES.includes(field.showType) && list.isList)
      .filter(({list}) => !normalizeText(list.dataIndex).endsWith('__name'))
      .map(({field, list}) => createIssue({
        id: `name-value-list-data-index:${field.name}`,
        level: LEVEL_WARNING,
        title: '列表显示值建议使用名称字段',
        field,
        description: `当前列表 dataIndex 为 ${list.dataIndex || '空'}，这类字段直接显示 id 通常不便阅读。`,
        suggestion: `建议将列表 dataIndex 改为 ${field.name}__name。`,
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            dataIndex: `${field.name}__name`,
          },
        },
      }))
}

const checkListDictMissing = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => list.isList && !isEmpty(field.dictType))
      .filter(({list}) => isEmpty(list.dict) && isEmpty(list.explicitListConfig.dict))
      .map(({field}) => createIssue({
        id: `list-dict-missing:${field.name}`,
        level: LEVEL_WARNING,
        title: '字典字段列表缺少 dict 配置',
        field,
        description: '字段配置了字典类型，但列表列没有显式配置 dict，可能影响列表翻译显示。',
        suggestion: `建议将列表 dict 设置为 ${field.dictType}。`,
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            dict: field.dictType,
          },
        },
      }))
}

const checkHeavyFieldShownInList = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => HEAVY_LIST_SHOW_TYPES.includes(field.showType) && list.isList)
      .map(({field}) => createIssue({
        id: `heavy-field-shown-in-list:${field.name}`,
        level: LEVEL_WARNING,
        title: '大内容字段不建议显示在列表',
        field,
        description: '附件、富文本或大文本字段显示在列表中，容易造成列表拥挤或加载变慢。',
        suggestion: '建议从列表显示字段中移除，保留在详情或表单内查看。',
        fixable: true,
        fix: {
          type: 'updateColumn',
          fieldName: field.name,
          patch: {
            isList: '0',
          },
        },
      }))
}

const checkTitleFieldSpan = (fields) => {
  return fields
      .filter(field => Number(field.span) === 12)
      .filter(field => isTitleLikeField(field))
      .map(field => createIssue({
        id: `title-field-span:${field.name}`,
        level: LEVEL_SUGGESTION,
        title: '标题类字段建议占整行',
        field,
        description: '标题、名称、主题类字段通常信息较长，半行展示容易拥挤。',
        suggestion: '建议将字段宽度调整为整行。',
        fixable: true,
        fix: {
          type: 'updateFormColProps',
          fieldName: field.name,
          patch: {
            span: 24,
          },
        },
      }))
}

const checkNumberListAlign = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => list.isList && (['integer', 'decimal', 'double', 'float', 'number', 'bigdecimal', 'long'].includes(normalizeText(field.javaType).toLowerCase()) || includesAny(`${field.name} ${field.label}`, NUMBER_FIELD_KEYWORDS)))
      .filter(({list}) => isEmpty(list.align) || list.align === 'left')
      .map(({field}) => createIssue({
        id: `number-list-align:${field.name}`,
        level: LEVEL_SUGGESTION,
        title: '数值字段建议右对齐',
        field,
        description: '金额、数量、分数等数值字段右对齐后更便于比较。',
        suggestion: '建议将列表对齐方式设置为 right。',
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            align: 'right',
          },
        },
      }))
}

const checkDateListAlign = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({field, list}) => field.showType === 'dateselect' && list.isList)
      .filter(({list}) => isEmpty(list.align) || list.align === 'left')
      .map(({field}) => createIssue({
        id: `date-list-align:${field.name}`,
        level: LEVEL_SUGGESTION,
        title: '日期字段建议居中显示',
        field,
        description: '日期字段居中后列表更规整，也方便扫描。',
        suggestion: '建议将列表对齐方式设置为 center。',
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            align: 'center',
          },
        },
      }))
}

const checkQueryPlaceholderMissing = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({list}) => list.isQuery)
      .filter(({list}) => isEmpty(list.queryFieldProps.placeholder))
      .map(({field}) => createIssue({
        id: `query-placeholder-missing:${field.name}`,
        level: LEVEL_SUGGESTION,
        title: '查询字段缺少 placeholder',
        field,
        description: '查询条件没有 placeholder，使用时不容易判断该输入什么。',
        suggestion: `建议将 placeholder 设置为 ${field.label || field.name}。`,
        fixable: true,
        fix: {
          type: 'updateListConfig',
          fieldName: field.name,
          patch: {
            queryFieldProps: {
              placeholder: field.label || field.name,
            },
          },
        },
      }))
}

const isSelectLikeQueryField = (field = {}, list = {}) => {
  const queryFieldType = normalizeText(list.queryFieldType)
  const showType = normalizeText(field.showType || field.type)
  return queryFieldType === 'select'
      || ['select', 'radio', 'checkbox', 'radiobox'].includes(showType)
}

const checkQuerySelectDictMissing = (fields, listColumns) => {
  return fields
      .map(field => ({field, listColumn: getFieldListColumn(field, listColumns)}))
      .map(({field, listColumn}) => ({field, list: getEffectiveList(field, listColumn)}))
      .filter(({list}) => list.isQuery)
      .filter(({field, list}) => isSelectLikeQueryField(field, list))
      .filter(({field}) => !isRelationSelect(field))
      // 关键：queryFieldProps.dictType 为空就会导致查询下拉无数据
      .filter(({list}) => isEmpty(list.queryFieldProps?.dictType))
      .map(({field, list}) => {
        const dictType = normalizeText(field.dictType || list.dict || field.list?.dict || '')
        return createIssue({
          id: `query-select-dict-missing:${field.name}`,
          level: LEVEL_WARNING,
          title: '查询下拉缺少 dictType',
          field,
          description: '列表查询使用了 select，但 queryFieldProps.dictType 为空。DynamicQueryField 优先读 queryFieldProps，不会回退到列 dictType，导致查询下拉无数据。',
          suggestion: dictType
              ? `建议将 queryFieldProps.dictType 设置为 ${dictType}。`
              : '建议为该查询下拉补充字典编码，并写入 queryFieldProps.dictType。',
          fixable: Boolean(dictType),
          fix: dictType ? {
            type: 'updateListConfig',
            fieldName: field.name,
            patch: {
              dict: dictType,
              queryFieldProps: {
                placeholder: list.queryFieldProps?.placeholder || field.label || field.name,
                dictType,
              },
            },
          } : null,
        })
      })
}

const FIXED_JAVA_FIELD_BY_NAME = {
  id: 'id',
  status: 'status',
  remarks: 'remarks',
  owner_code: 'ownerCode',
  del_flag: 'delFlag',
  create_by: 'createBy.id',
  create_date: 'createDate',
  update_by: 'updateBy.id',
  update_date: 'updateDate',
  sort: 'sort',
}

const checkFixedJavaFieldMismatch = (fields) => {
  return fields
      .filter(field => FIXED_JAVA_FIELD_BY_NAME[normalizeText(field.name)])
      .filter(field => {
        const expected = FIXED_JAVA_FIELD_BY_NAME[normalizeText(field.name)]
        const actual = normalizeText(field.javaField || field.raw?.column?.javaField || '')
        return actual && actual !== expected
      })
      .map(field => createIssue({
        id: `fixed-java-field-mismatch:${field.name}`,
        level: LEVEL_WARNING,
        title: '固定字段 javaField 不正确',
        field,
        description: `字段 ${field.name} 在 Zform 实体上有固定属性，javaField 应为 ${FIXED_JAVA_FIELD_BY_NAME[normalizeText(field.name)]}，当前为 ${field.javaField || field.raw?.column?.javaField || '空'}。`,
        suggestion: `请将属性名称改为 ${FIXED_JAVA_FIELD_BY_NAME[normalizeText(field.name)]}，不要使用 s0x 槽位。`,
        fixable: false,
      }))
}

export const checkFormDesignDsl = (dsl = {}) => {
  const fields = toArray(dsl.fields)
  const listColumns = toArray(dsl.list?.columns)

  return []
      .concat(checkDuplicateFieldName(fields))
      .concat(checkDuplicateJavaField(fields))
      .concat(checkMissingRequiredProperties(fields))
      .concat(checkSelectMissingDict(fields))
      .concat(checkDateQueryFieldType(fields, listColumns))
      .concat(checkNameValueListDataIndex(fields, listColumns))
      .concat(checkListDictMissing(fields, listColumns))
      .concat(checkHeavyFieldShownInList(fields, listColumns))
      .concat(checkTitleFieldSpan(fields))
      .concat(checkNumberListAlign(fields, listColumns))
      .concat(checkDateListAlign(fields, listColumns))
      .concat(checkQueryPlaceholderMissing(fields, listColumns))
      .concat(checkQuerySelectDictMissing(fields, listColumns))
      .concat(checkFixedJavaFieldMismatch(fields))
}

export const summarizeDesignIssues = (issues = []) => {
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

export default checkFormDesignDsl
