const toArray = (value) => Array.isArray(value) ? value : []

const isEmpty = (value) => value === undefined || value === null || value === ''

const formatValue = (value) => {
  if (value === undefined || value === null || value === '') {
    return '空'
  }
  if (value === true) return '是'
  if (value === false) return '否'
  if (value === '1') return '是'
  if (value === '0') return '否'
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value)
    } catch (e) {
      return String(value)
    }
  }
  return String(value)
}

const findField = (dsl = {}, fieldName = '') => {
  return toArray(dsl.fields).find(field => field.name === fieldName) || null
}

const findListColumn = (dsl = {}, fieldName = '') => {
  return toArray(dsl.list?.columns).find(column => column.name === fieldName) || null
}

const getListCurrentValue = (field = {}, listColumn = {}, key) => {
  if (key === 'queryFieldProps') {
    return listColumn?.queryFieldProps || field?.list?.queryFieldProps || {}
  }
  return listColumn?.[key] ?? field?.list?.[key]
}

const getColumnCurrentValue = (field = {}, listColumn = {}, key) => {
  if (key === 'isList') {
    return listColumn?.isList ?? field?.isList
  }
  if (key === 'isQuery') {
    return listColumn?.isQuery ?? field?.isQuery
  }
  return field?.raw?.column?.[key] ?? field?.[key]
}

const labelMap = {
  isList: '列表显示',
  isQuery: '查询显示',
  dataIndex: '列表取值字段',
  title: '列表标题',
  align: '列表对齐',
  dict: '列表字典',
  queryFieldType: '查询组件',
  queryFieldProps: '查询组件属性',
  placeholder: '查询提示',
  formatPatter: '日期格式',
  span: '表单宽度',
}

const createChange = ({issue, field, property, before, after, target, path}) => ({
  id: `${issue.id}:${path || property}`,
  issueId: issue.id,
  issueTitle: issue.title,
  fieldName: issue.fieldName,
  fieldLabel: issue.fieldLabel || field?.label || issue.fieldName,
  target,
  property,
  path: path || property,
  label: labelMap[property] || property,
  before,
  after,
  beforeText: formatValue(before),
  afterText: formatValue(after),
})

const buildObjectChanges = ({issue, field, target, parentProperty, before = {}, patch = {}}) => {
  return Object.keys(patch).map(key => createChange({
    issue,
    field,
    target,
    property: key,
    path: `${parentProperty}.${key}`,
    before: before?.[key],
    after: patch[key],
  }))
}

const previewUpdateListConfig = (dsl, issue) => {
  const field = findField(dsl, issue.fix?.fieldName)
  const listColumn = findListColumn(dsl, issue.fix?.fieldName)
  const patch = issue.fix?.patch || {}

  return Object.keys(patch).flatMap(key => {
    const before = getListCurrentValue(field, listColumn, key)
    const after = patch[key]
    if (key === 'queryFieldProps') {
      return buildObjectChanges({
        issue,
        field,
        target: 'listConfig',
        parentProperty: key,
        before,
        patch: after,
      })
    }
    return [createChange({
      issue,
      field,
      target: 'listConfig',
      property: key,
      before,
      after,
    })]
  })
}

const previewUpdateColumn = (dsl, issue) => {
  const field = findField(dsl, issue.fix?.fieldName)
  const listColumn = findListColumn(dsl, issue.fix?.fieldName)
  const patch = issue.fix?.patch || {}

  return Object.keys(patch).map(key => createChange({
    issue,
    field,
    target: 'column',
    property: key,
    before: getColumnCurrentValue(field, listColumn, key),
    after: patch[key],
  }))
}

const previewUpdateFormColProps = (dsl, issue) => {
  const field = findField(dsl, issue.fix?.fieldName)
  const patch = issue.fix?.patch || {}

  return Object.keys(patch).map(key => createChange({
    issue,
    field,
    target: 'formColProps',
    property: key,
    before: key === 'span' ? field?.span : field?.form?.colProps?.[key],
    after: patch[key],
  }))
}

const removeNoopChanges = (changes) => {
  return changes.filter(change => formatValue(change.before) !== formatValue(change.after))
}

export const generateFixPreview = (dsl = {}, issues = []) => {
  const selectedIssues = toArray(issues).filter(issue => issue.fixable && issue.fix)
  const changes = selectedIssues.flatMap(issue => {
    if (issue.fix.type === 'updateListConfig') {
      return previewUpdateListConfig(dsl, issue)
    }
    if (issue.fix.type === 'updateColumn') {
      return previewUpdateColumn(dsl, issue)
    }
    if (issue.fix.type === 'updateFormColProps') {
      return previewUpdateFormColProps(dsl, issue)
    }
    return []
  })

  return {
    issueCount: selectedIssues.length,
    changes: removeNoopChanges(changes),
    unsupportedIssues: selectedIssues.filter(issue => {
      return !['updateListConfig', 'updateColumn', 'updateFormColProps'].includes(issue.fix.type)
    }),
  }
}

export const hasPreviewableFix = (issue = {}) => {
  return Boolean(issue.fixable && issue.fix && !isEmpty(issue.fix.type))
}

export default generateFixPreview
