const unwrap = (value) => {
  if (value && typeof value === 'object' && 'value' in value) {
    return value.value
  }
  return value
}

const toArray = (value) => {
  const unwrapped = unwrap(value)
  return Array.isArray(unwrapped) ? unwrapped : []
}

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

const stringifyJson = (value) => JSON.stringify(value || {})

const formatValue = (value) => {
  if (value === undefined || value === null || value === '') return '空'
  if (value === true || value === '1') return '是'
  if (value === false || value === '0') return '否'
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value)
    } catch (e) {
      return String(value)
    }
  }
  return String(value)
}

const findFieldTarget = (context = {}, fieldName = '') => {
  const displayItems = toArray(context.displayFormItemArr)
  const hiddenItems = toArray(context.hideFormItemArr)
  const fixedColumns = toArray(context.fixedArr)
  const extendJsConfig = unwrap(context.extendJsConfig) || {}
  const extendColumns = toArray(extendJsConfig.extendColumns)

  const dynamicItem = displayItems.concat(hiddenItems).find(item => item?.column?.name === fieldName)
  if (dynamicItem) {
    return {
      type: 'dynamic',
      item: dynamicItem,
      column: dynamicItem.column,
    }
  }

  const fixedColumn = fixedColumns.find(column => column?.name === fieldName)
  if (fixedColumn) {
    return {
      type: 'fixed',
      column: fixedColumn,
    }
  }

  const extendColumn = extendColumns.find(column => column?.name === fieldName)
  if (extendColumn) {
    return {
      type: 'extend',
      column: extendColumn,
    }
  }

  return null
}

const createAppliedChange = ({issue, property, before, after, target}) => ({
  issueId: issue.id,
  issueTitle: issue.title,
  fieldName: issue.fix?.fieldName || issue.fieldName,
  fieldLabel: issue.fieldLabel || issue.fieldName,
  target,
  property,
  before,
  after,
  beforeText: formatValue(before),
  afterText: formatValue(after),
})

const applyUpdateColumn = (target, issue) => {
  const changes = []
  const patch = issue.fix?.patch || {}
  Object.keys(patch).forEach(key => {
    const before = target.column[key]
    const after = patch[key]
    if (formatValue(before) !== formatValue(after)) {
      target.column[key] = after
      changes.push(createAppliedChange({
        issue,
        property: key,
        before,
        after,
        target: 'column',
      }))
    }
  })
  return changes
}

const applyUpdateListConfig = (target, issue) => {
  const changes = []
  const patch = issue.fix?.patch || {}
  const listConfig = parseJson(target.column.listConfig)

  Object.keys(patch).forEach(key => {
    const before = listConfig[key]
    const after = patch[key]

    if (key === 'queryFieldProps') {
      const beforeProps = parseJson(before)
      const nextProps = {
        ...beforeProps,
        ...after,
      }
      Object.keys(after || {}).forEach(propKey => {
        if (formatValue(beforeProps[propKey]) !== formatValue(nextProps[propKey])) {
          changes.push(createAppliedChange({
            issue,
            property: `queryFieldProps.${propKey}`,
            before: beforeProps[propKey],
            after: nextProps[propKey],
            target: 'listConfig',
          }))
        }
      })
      listConfig[key] = nextProps
      return
    }

    if (formatValue(before) !== formatValue(after)) {
      listConfig[key] = after
      changes.push(createAppliedChange({
        issue,
        property: key,
        before,
        after,
        target: 'listConfig',
      }))
    }
  })

  target.column.listConfig = stringifyJson(listConfig)
  if (patch.align) {
    target.column.align = patch.align
  }
  return changes
}

const applyUpdateFormColProps = (target, issue) => {
  const changes = []
  const patch = issue.fix?.patch || {}
  if (!target.item) {
    return changes
  }

  if (!target.item.colProps) {
    target.item.colProps = {}
  }

  Object.keys(patch).forEach(key => {
    const before = target.item.colProps[key]
    const after = patch[key]
    if (formatValue(before) !== formatValue(after)) {
      target.item.colProps[key] = after
      changes.push(createAppliedChange({
        issue,
        property: key,
        before,
        after,
        target: 'formColProps',
      }))
    }
  })

  target.item.apply && target.item.apply()
  if (patch.span) {
    target.column.isOneLine = Number(patch.span) === 24 ? '1' : '0'
  }
  return changes
}

export const applyDesignFixes = (context = {}, issues = []) => {
  const selectedIssues = toArray(issues).filter(issue => issue.fixable && issue.fix)
  const appliedChanges = []
  const skippedIssues = []

  selectedIssues.forEach(issue => {
    const target = findFieldTarget(context, issue.fix.fieldName)
    if (!target) {
      skippedIssues.push({
        issue,
        reason: '未找到字段配置',
      })
      return
    }

    let changes = []
    if (issue.fix.type === 'updateColumn') {
      changes = applyUpdateColumn(target, issue)
    } else if (issue.fix.type === 'updateListConfig') {
      changes = applyUpdateListConfig(target, issue)
    } else if (issue.fix.type === 'updateFormColProps') {
      changes = applyUpdateFormColProps(target, issue)
    } else {
      skippedIssues.push({
        issue,
        reason: `暂不支持的修复类型：${issue.fix.type}`,
      })
      return
    }

    appliedChanges.push(...changes)
  })

  return {
    issueCount: selectedIssues.length,
    appliedCount: appliedChanges.length,
    appliedChanges,
    skippedIssues,
  }
}

export default applyDesignFixes
