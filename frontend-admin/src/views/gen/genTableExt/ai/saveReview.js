const LEVEL_ERROR = 'error'
const LEVEL_WARNING = 'warning'
const LEVEL_SUGGESTION = 'suggestion'

const toArray = (value) => Array.isArray(value) ? value : []

const normalizeText = (value) => String(value || '').trim()

const countBy = (items, predicate) => toArray(items).filter(predicate).length

const createReviewIssue = ({
  id,
  level,
  title,
  description,
  suggestion = '',
  source = 'save-review',
}) => ({
  id,
  level,
  title,
  fieldName: '',
  fieldLabel: '',
  description,
  suggestion,
  source,
  fixable: false,
})

const getBusinessFields = (fields = []) => {
  return toArray(fields).filter(field => !['fixed', 'extend'].includes(field.source))
}

const getAppliedAiSummary = (aiApplyState = {}) => {
  const parts = []
  if (aiApplyState.draftApplied) {
    parts.push(`AI草稿 ${aiApplyState.draftFieldCount || 0} 个字段`)
  }
  if (aiApplyState.fixAppliedCount > 0) {
    parts.push(`AI修复 ${aiApplyState.fixAppliedCount} 项`)
  }
  return parts.join('，')
}

const getAiFormalizeSourceSummary = (aiFormalizeContext = {}) => {
  const source = aiFormalizeContext?.source || {}
  if (!source.type) {
    return ''
  }
  const typeLabelMap = {
    'server-draft': '服务器草稿',
    'local-draft': '本地草稿',
    'ai-draft': 'AI新生成草稿',
    'form-material': '材料转换草稿',
    'current-designer': '当前设计器',
  }
  const parts = [typeLabelMap[source.type] || source.type]
  if (source.draftTitle) {
    parts.push(`标题：${source.draftTitle}`)
  }
  if (source.draftId) {
    parts.push(`草稿ID：${source.draftId}`)
  }
  if (source.versionId) {
    parts.push(`版本ID：${source.versionId}`)
  }
  return parts.join('，')
}

export const createSaveReviewContextIssues = (dsl = {}, options = {}) => {
  const fields = toArray(dsl.fields)
  const businessFields = getBusinessFields(fields)
  const displayCount = countBy(businessFields, field => field.source === 'display' || field.isForm)
  const listCount = countBy(fields, field => field.isList)
  const queryCount = countBy(fields, field => field.isQuery)
  const issues = []

  if (businessFields.length === 0) {
    issues.push(createReviewIssue({
      id: 'save-review:no-business-fields',
      level: LEVEL_WARNING,
      title: '未发现业务字段',
      description: '当前表单只有系统字段或扩展字段，保存后可能不是一个可用的业务表单。',
      suggestion: '如果这是新建表单，建议先补充业务字段；如果只是配置空壳表，可以继续保存。',
    }))
  }

  if (displayCount === 0) {
    issues.push(createReviewIssue({
      id: 'save-review:no-display-fields',
      level: LEVEL_WARNING,
      title: '表单显示区没有业务字段',
      description: '用户打开表单时可能看不到可填写的业务内容。',
      suggestion: '建议至少保留一个显示区字段，或确认这是纯隐藏/系统配置表。',
    }))
  }

  if (listCount === 0) {
    issues.push(createReviewIssue({
      id: 'save-review:no-list-fields',
      level: LEVEL_SUGGESTION,
      title: '列表字段为空',
      description: '列表页可能只能看到操作列或系统默认列，不利于查看数据。',
      suggestion: '建议选择几个关键字段展示到列表。',
    }))
  }

  if (queryCount === 0) {
    issues.push(createReviewIssue({
      id: 'save-review:no-query-fields',
      level: LEVEL_SUGGESTION,
      title: '查询条件为空',
      description: '数据量变大后，列表页缺少检索入口。',
      suggestion: '建议为编号、标题、日期、状态等字段配置查询条件。',
    }))
  }

  const aiAppliedSummary = getAppliedAiSummary(options.aiApplyState)
  if (options.hasAiRollbackSnapshot || aiAppliedSummary) {
    issues.push(createReviewIssue({
      id: 'save-review:ai-applied-notice',
      level: LEVEL_WARNING,
      title: '检测到 AI 修改尚未最终确认',
      description: aiAppliedSummary
          ? `本次页面已应用 ${aiAppliedSummary}，正式保存后这些页面内存变更会进入保存流程。`
          : '本次页面应用过 AI 草稿，正式保存后这些页面内存变更会进入保存流程。',
      suggestion: '建议先预览或人工检查字段、列表和查询配置；确认无误后再继续保存。',
    }))
  }

  const aiFormalizeSourceSummary = getAiFormalizeSourceSummary(options.aiFormalizeContext)
  if (aiFormalizeSourceSummary) {
    issues.push(createReviewIssue({
      id: 'save-review:ai-formalize-source',
      level: LEVEL_SUGGESTION,
      title: '已记录 AI 草稿来源',
      description: `本次保存前审查已关联来源：${aiFormalizeSourceSummary}。`,
      suggestion: '该记录只用于保存前追踪和人工确认，不会自动建表、同步表结构或绕过原保存流程。',
    }))
  }

  return issues
}

export const buildSaveReviewResult = (dsl = {}, designIssues = [], options = {}) => {
  const issues = toArray(designIssues).concat(createSaveReviewContextIssues(dsl, options))
  const summary = {
    total: issues.length,
    error: 0,
    warning: 0,
    suggestion: 0,
    fixable: 0,
    fieldCount: toArray(dsl.fields).length,
    displayCount: countBy(dsl.fields, field => field.source === 'display' || field.isForm),
    hiddenCount: countBy(dsl.fields, field => field.source === 'hidden'),
    listCount: countBy(dsl.fields, field => field.isList),
    queryCount: countBy(dsl.fields, field => field.isQuery),
    formName: normalizeText(dsl.form?.name),
    formTitle: normalizeText(dsl.form?.title),
    module: normalizeText(dsl.form?.module),
  }

  issues.forEach(issue => {
    if (summary[issue.level] !== undefined) {
      summary[issue.level] += 1
    }
    if (issue.fixable) {
      summary.fixable += 1
    }
  })

  return {
    dsl,
    issues,
    summary,
    aiFormalizeContext: options.aiFormalizeContext || null,
    canSave: summary.error === 0,
    needConfirm: summary.error === 0 && summary.total > 0,
    topIssues: issues.slice(0, 8),
  }
}

export default buildSaveReviewResult
