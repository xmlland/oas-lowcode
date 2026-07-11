/**
 * AI 草稿 → 设计器：增量补充字段。
 * - 按 column.name 匹配；同名一律跳过（保留已有配置）
 * - 不改隐藏区 / 表头 / formProps
 * - 仅追加 DSL 中新增的业务字段到显示区末尾
 */
import {convertDslFieldToDesignerItem} from '@/views/gen/genTableExt/ai/dslToDesignerState'
import {validateFormDesignDslSchema} from '@/views/gen/genTableExt/ai/dslSchema'

const SYSTEM_HIDDEN_FIELD_NAMES = new Set([
  'create_by',
  'create_date',
  'update_by',
  'update_date',
  'remarks',
  'owner_code',
  'del_flag',
  'id',
])

const toArray = (value) => (Array.isArray(value) ? value : [])

const clonePlain = (value) => {
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

const getItemName = (item = {}) =>
  normalizeText(item?.formItemProps?.name || item?.column?.name || item?.name)

const isSystemHiddenName = (name) => SYSTEM_HIDDEN_FIELD_NAMES.has(normalizeKey(name))

const countListFields = (items = []) => items.filter(item => item?.column?.isList === '1').length

const countQueryFields = (items = []) => items.filter(item => item?.column?.isQuery === '1').length

/**
 * 已有业务显示字段数 > 0 → 默认增量；否则全量。
 */
export const resolveDefaultApplyMode = (context = {}) => {
  const display = toArray(context.displayFormItemArr)
  const businessDisplayCount = display.filter(item => {
    const name = getItemName(item)
    return name && !isSystemHiddenName(name)
  }).length
  return businessDisplayCount > 0 ? 'incremental' : 'replace'
}

export const buildExistingFieldNameSet = (context = {}) => {
  const names = new Set()
  toArray(context.displayFormItemArr).forEach(item => {
    const name = getItemName(item)
    if (name) {
      names.add(normalizeKey(name))
    }
  })
  toArray(context.hideFormItemArr).forEach(item => {
    const name = getItemName(item)
    if (name) {
      names.add(normalizeKey(name))
    }
  })
  return names
}

/**
 * @param {object} dsl
 * @param {object} context  当前设计器 context（含 display/hide）
 * @param {object} options
 * @returns {object} 兼容 applyDraftStatePatch 的 patch + 增量预览信息
 */
export const buildIncrementalFieldPatch = (dsl = {}, context = {}, options = {}) => {
  const schemaIssues = validateFormDesignDslSchema(dsl).map(issue => ({
    ...issue,
    source: 'schema',
  }))
  const issues = [...schemaIssues]

  const existingDisplay = clonePlain(toArray(context.displayFormItemArr))
  const existingHide = clonePlain(toArray(context.hideFormItemArr))
  const existingNames = buildExistingFieldNameSet(context)

  const toAdd = []
  const skipped = []
  const ignoredHidden = []
  const typeCounter = {}

  const fields = toArray(dsl.fields)
  fields.forEach((field, index) => {
    const name = normalizeText(field?.name)
    const label = normalizeText(field?.label) || name
    if (!name) {
      issues.push({
        id: `incremental-empty-name:${index}`,
        level: 'error',
        title: '字段名为空',
        fieldName: name,
        fieldLabel: label,
        description: '增量转换时字段 name 不能为空。',
        suggestion: '请补齐 DSL 字段名。',
        source: 'incremental',
      })
      return
    }

    if (isSystemHiddenName(name)) {
      ignoredHidden.push({
        name,
        label,
        reason: '系统/隐藏字段，增量模式不处理',
      })
      return
    }

    if (existingNames.has(normalizeKey(name))) {
      skipped.push({
        name,
        label,
        reason: '设计器已存在同名字段，保留原配置',
      })
      return
    }

    const result = convertDslFieldToDesignerItem(field, index, {
      ...context,
      dsl,
      displayFormItemArr: existingDisplay,
      hideFormItemArr: existingHide,
    }, {
      reserveExistingFields: true,
      generatedDisplay: toAdd,
      generatedHidden: [],
    })

    issues.push(...result.issues.map(issue => ({
      ...issue,
      source: issue.source || 'converter',
    })))

    if (!result.item) {
      return
    }

    // 增量只追加到显示区；DSL 标成隐藏的业务字段也放到显示区末尾，避免动 hide
    typeCounter[result.type] = (typeCounter[result.type] || 0) + 1
    toAdd.push(result.item)
    existingNames.add(normalizeKey(name))
  })

  const displayFormItemArr = existingDisplay.concat(toAdd)
  const hideFormItemArr = existingHide
  const errors = issues.filter(issue => issue.level === 'error')
  const warnings = issues.filter(issue => issue.level === 'warning')

  if (toAdd.length === 0) {
    warnings.push({
      id: 'incremental-nothing-to-add',
      level: 'warning',
      title: '没有可新增的字段',
      description: skipped.length > 0
        ? '草稿中的业务字段均已存在于设计器，本次不会改动画布。'
        : '草稿中没有可增量追加的业务字段。',
      suggestion: '请在材料中只写尚未存在的字段名，或改用全量替换（会覆盖已有配置）。',
      source: 'incremental',
    })
  }

  const allIssues = issues.concat(warnings.filter(w => !issues.some(i => i.id === w.id)))
  const allWarnings = allIssues.filter(issue => issue.level === 'warning')
  const allErrors = allIssues.filter(issue => issue.level === 'error')

  const summary = {
    title: dsl.form?.title || '',
    formName: dsl.form?.name || '',
    fieldCount: fields.length,
    displayCount: displayFormItemArr.length,
    hiddenCount: hideFormItemArr.length,
    addCount: toAdd.length,
    skipCount: skipped.length,
    ignoredHiddenCount: ignoredHidden.length,
    existingDisplayCount: existingDisplay.length,
    errorCount: allErrors.length,
    warningCount: allWarnings.length,
    typeCounter,
    mode: 'incremental',
  }

  return {
    version: dsl.version || '',
    mode: 'incremental',
    formPatch: {},
    formPropsPatch: {},
    displayFormItemArr,
    hideFormItemArr,
    fixedArr: toArray(context.fixedArr),
    toAdd,
    skipped,
    ignoredHidden,
    issues: allIssues,
    errors: allErrors,
    warnings: allWarnings,
    summary,
    canApply: allErrors.length === 0 && toAdd.length > 0,
  }
}

export const summarizeIncrementalPreview = (patch = {}) => ({
  addCount: patch.summary?.addCount ?? toArray(patch.toAdd).length,
  skipCount: patch.summary?.skipCount ?? toArray(patch.skipped).length,
  ignoredHiddenCount: patch.summary?.ignoredHiddenCount ?? toArray(patch.ignoredHidden).length,
  listFieldCount: countListFields(toArray(patch.toAdd)),
  queryFieldCount: countQueryFields(toArray(patch.toAdd)),
})

export default buildIncrementalFieldPatch
