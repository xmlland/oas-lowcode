import {validateFormDesignDslSchema} from "@/views/gen/genTableExt/ai/dslSchema";

export const MODULE_DESIGN_SCHEMA_VERSION = '1.0'

export const MODULE_DESIGN_SCENE_OPTIONS = [
  '',
  'normal',
  'document',
  'document-form',
  'approval',
  'ledger',
  'mixed',
]

export const MODULE_DESIGN_PAGE_TYPE_OPTIONS = [
  '',
  'list_form',
  'form',
  'list',
  'detail',
  'config',
  'workflow',
  'dashboard',
  'report',
  'external',
  'unknown',
]

export const MODULE_FORM_DRAFT_STATUS_OPTIONS = [
  '',
  'pending',
  'ready',
  'generating',
  'generated',
  'confirmed',
  'failed',
]

export const MODULE_DICTIONARY_MODE_OPTIONS = [
  '',
  'use-existing',
  'create',
  'ignore',
  'need-confirm',
]

export const MODULE_RELATION_TYPE_OPTIONS = [
  '',
  'master_detail',
  'reference',
  'workflow',
  'lookup',
  'attachment',
  'statistics',
  'unknown',
]

export const MODULE_APPLY_STEP_TYPE_OPTIONS = [
  '',
  'create-dictionary',
  'save-form',
  'save-menu',
  'sync-table',
  'mark-draft',
  'manual',
]

export const MODULE_APPLY_STEP_STATUS_OPTIONS = [
  '',
  'pending',
  'ready',
  'blocked',
  'success',
  'failed',
  'skipped',
]

const NAME_PATTERN = /^[a-z][a-z0-9_]*$/

const isObject = (value) => value !== null && typeof value === 'object' && !Array.isArray(value)

const toArray = (value) => Array.isArray(value) ? value : []

const normalizeText = (value) => String(value || '').trim()

const isEmpty = (value) => value === undefined || value === null || normalizeText(value) === ''

const isValidName = (value) => NAME_PATTERN.test(normalizeText(value))

const isValidConfidence = (value) => {
  if (value === undefined || value === null || value === '') {
    return true
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= 0 && numberValue <= 1
}

const createIssue = ({
  id,
  level = 'error',
  title,
  menuId = '',
  pageId = '',
  formId = '',
  dictionaryCode = '',
  fieldName = '',
  fieldLabel = '',
  description = '',
  suggestion = '',
  fixable = false,
  meta = {},
}) => ({
  id,
  level,
  title,
  menuId,
  pageId,
  formId,
  dictionaryCode,
  fieldName,
  fieldLabel,
  description,
  suggestion,
  fixable,
  meta,
})

const collectIds = (items = [], key = 'id') => {
  const map = {}
  toArray(items).forEach((item, index) => {
    if (!isObject(item)) {
      return
    }
    const id = normalizeText(item[key])
    if (!id) {
      return
    }
    if (!map[id]) {
      map[id] = []
    }
    map[id].push(index)
  })
  return map
}

const reportDuplicateIds = (idMap = {}, issues, options = {}) => {
  const {
    issuePrefix,
    title,
    descriptionPrefix,
    suggestion,
    getExtraIssue = () => ({}),
  } = options
  Object.keys(idMap)
      .filter(key => idMap[key].length > 1)
      .forEach(key => {
        issues.push(createIssue({
          id: `${issuePrefix}:${key}`,
          title,
          description: `${descriptionPrefix} ${key} 出现了 ${idMap[key].length} 次。`,
          suggestion,
          meta: {
            indexes: idMap[key],
          },
          ...getExtraIssue(key),
        }))
      })
}

const getFormDslFieldNames = (form = {}) => {
  return new Set(toArray(form.dsl?.fields).map(field => normalizeText(field?.name)).filter(Boolean))
}

const validateRoot = (design, issues) => {
  if (!isObject(design)) {
    issues.push(createIssue({
      id: 'module-design-root-invalid',
      title: '模块设计结构不合法',
      description: 'ModuleDesignDSL 必须是一个对象。',
      suggestion: '请生成包含 version、module、menus、pages、forms、dictionaries 的设计对象。',
    }))
    return false
  }

  if (design.version !== MODULE_DESIGN_SCHEMA_VERSION) {
    issues.push(createIssue({
      id: `module-design-version-invalid:${design.version || 'empty'}`,
      title: '模块设计版本不支持',
      description: `当前版本为 ${design.version || '空'}，当前只支持 ${MODULE_DESIGN_SCHEMA_VERSION}。`,
      suggestion: `请设置 version 为 ${MODULE_DESIGN_SCHEMA_VERSION}。`,
    }))
  }

  return true
}

const validateModule = (design, issues) => {
  if (!isObject(design.module)) {
    issues.push(createIssue({
      id: 'module-design-module-missing',
      title: '模块信息缺失',
      description: 'module 对象用于描述模块编码、标题、场景和说明。',
      suggestion: '请补充 module.name、module.title。',
    }))
    return
  }

  const name = normalizeText(design.module.name)
  const title = normalizeText(design.module.title)
  const scene = normalizeText(design.module.scene)

  if (!name) {
    issues.push(createIssue({
      id: 'module-design-module-name-missing',
      title: '模块编码缺失',
      description: 'module.name 会作为批量表单、菜单和权限规划的重要上下文。',
      suggestion: '请补充小写下划线格式的模块编码，例如 oa_receive。',
    }))
  } else if (!isValidName(name)) {
    issues.push(createIssue({
      id: `module-design-module-name-invalid:${name}`,
      title: '模块编码格式不合法',
      description: 'module.name 只能使用小写字母、数字和下划线，并且必须以字母开头。',
      suggestion: '请改为有业务含义的 snake_case 模块编码。',
    }))
  }

  if (!title) {
    issues.push(createIssue({
      id: 'module-design-module-title-missing',
      title: '模块标题缺失',
      description: 'module.title 会作为模块蓝图和批量保存报告中的主要展示名称。',
      suggestion: '请补充面向用户的中文模块标题。',
    }))
  }

  if (scene && !MODULE_DESIGN_SCENE_OPTIONS.includes(scene)) {
    issues.push(createIssue({
      id: `module-design-module-scene-invalid:${scene}`,
      level: 'warning',
      title: '模块场景不支持',
      description: `module.scene 当前为 ${scene}。`,
      suggestion: `请使用 ${MODULE_DESIGN_SCENE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
    }))
  }
}

const validateMenus = (design, issues) => {
  if (!Array.isArray(design.menus)) {
    issues.push(createIssue({
      id: 'module-design-menus-invalid',
      title: '菜单列表不合法',
      description: 'menus 必须是数组。没有菜单规划时请设置为空数组。',
      suggestion: '请将模块菜单放入 menus 数组。',
    }))
    return {}
  }

  const idMap = collectIds(design.menus)
  const ids = new Set(Object.keys(idMap))

  design.menus.forEach((menu, index) => {
    if (!isObject(menu)) {
      issues.push(createIssue({
        id: `module-design-menu-invalid:${index}`,
        title: '菜单项不合法',
        description: `第 ${index + 1} 个菜单不是对象。`,
        suggestion: '每个菜单至少需要包含 id 和 title。',
      }))
      return
    }

    const menuId = normalizeText(menu.id)
    if (!menuId) {
      issues.push(createIssue({
        id: `module-design-menu-id-missing:${index}`,
        title: '菜单 ID 缺失',
        description: `第 ${index + 1} 个菜单缺少 id，后续页面无法稳定关联菜单。`,
        suggestion: '请为菜单分配稳定临时 ID，例如 menu_01。',
      }))
    }

    if (isEmpty(menu.title)) {
      issues.push(createIssue({
        id: `module-design-menu-title-missing:${menuId || index}`,
        title: '菜单标题缺失',
        menuId,
        description: '菜单需要有用户可见标题。',
        suggestion: '请补充菜单标题。',
      }))
    }

    const parentId = normalizeText(menu.parentId)
    if (parentId && !ids.has(parentId)) {
      issues.push(createIssue({
        id: `module-design-menu-parent-missing:${menuId || index}:${parentId}`,
        level: 'warning',
        title: '菜单父级不存在',
        menuId,
        description: `菜单父级 ${parentId} 没有出现在当前 menus 中。`,
        suggestion: '请确认菜单层级，或删除无效 parentId。',
      }))
    }
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-design-menu-id-duplicate',
    title: '菜单 ID 重复',
    descriptionPrefix: '菜单 ID',
    suggestion: '请为每个菜单分配唯一 ID。',
    getExtraIssue: (menuId) => ({menuId}),
  })

  return {
    ids,
    idMap,
  }
}

const validatePages = (design, issues, menuContext = {}, formIds = new Set()) => {
  if (!Array.isArray(design.pages)) {
    issues.push(createIssue({
      id: 'module-design-pages-invalid',
      title: '页面列表不合法',
      description: 'pages 必须是数组。',
      suggestion: '请将模块页面放入 pages 数组。',
    }))
    return {}
  }

  const idMap = collectIds(design.pages)
  const ids = new Set(Object.keys(idMap))
  const menuIds = menuContext.ids || new Set()

  design.pages.forEach((page, index) => {
    if (!isObject(page)) {
      issues.push(createIssue({
        id: `module-design-page-invalid:${index}`,
        title: '页面项不合法',
        description: `第 ${index + 1} 个页面不是对象。`,
        suggestion: '每个页面至少需要包含 id、title 和 pageType。',
      }))
      return
    }

    const pageId = normalizeText(page.id)
    const pageType = normalizeText(page.pageType)
    const menuId = normalizeText(page.menuId)
    const formId = normalizeText(page.formId)

    if (!pageId) {
      issues.push(createIssue({
        id: `module-design-page-id-missing:${index}`,
        title: '页面 ID 缺失',
        description: `第 ${index + 1} 个页面缺少 id。`,
        suggestion: '请为页面分配稳定临时 ID，例如 page_01。',
      }))
    }

    if (isEmpty(page.title)) {
      issues.push(createIssue({
        id: `module-design-page-title-missing:${pageId || index}`,
        title: '页面标题缺失',
        pageId,
        description: '页面需要有用户可见标题。',
        suggestion: '请补充页面标题。',
      }))
    }

    if (pageType && !MODULE_DESIGN_PAGE_TYPE_OPTIONS.includes(pageType)) {
      issues.push(createIssue({
        id: `module-design-page-type-invalid:${pageId || index}:${pageType}`,
        level: 'warning',
        title: '页面类型不支持',
        pageId,
        description: `pageType 当前为 ${pageType}。`,
        suggestion: `请使用 ${MODULE_DESIGN_PAGE_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (menuId && !menuIds.has(menuId)) {
      issues.push(createIssue({
        id: `module-design-page-menu-missing:${pageId || index}:${menuId}`,
        level: 'warning',
        title: '页面关联菜单不存在',
        pageId,
        menuId,
        description: `页面关联的菜单 ${menuId} 没有出现在当前 menus 中。`,
        suggestion: '请确认页面归属菜单，或清空 menuId 后在蓝图确认页重新选择。',
      }))
    }

    if (formId && !formIds.has(formId)) {
      issues.push(createIssue({
        id: `module-design-page-form-missing:${pageId || index}:${formId}`,
        level: 'warning',
        title: '页面关联表单不存在',
        pageId,
        formId,
        description: `页面关联的表单 ${formId} 没有出现在当前 forms 中。`,
        suggestion: '请确认页面绑定的表单，或清空 formId 后重新选择。',
      }))
    }
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-design-page-id-duplicate',
    title: '页面 ID 重复',
    descriptionPrefix: '页面 ID',
    suggestion: '请为每个页面分配唯一 ID。',
    getExtraIssue: (pageId) => ({pageId}),
  })

  return {
    ids,
    idMap,
  }
}

const validateForms = (design, issues) => {
  if (!Array.isArray(design.forms)) {
    issues.push(createIssue({
      id: 'module-design-forms-invalid',
      title: '表单列表不合法',
      description: 'forms 必须是数组。模块设计至少应包含一个可生成表单。',
      suggestion: '请将生成后的 FormDesignDSL 放入 forms 数组。',
    }))
    return {}
  }

  const idMap = collectIds(design.forms)
  const ids = new Set(Object.keys(idMap))
  const formNameMap = {}
  const dslFormNameMap = {}
  const formFieldNameMap = {}

  design.forms.forEach((form, index) => {
    if (!isObject(form)) {
      issues.push(createIssue({
        id: `module-design-form-invalid:${index}`,
        title: '表单项不合法',
        description: `第 ${index + 1} 个表单不是对象。`,
        suggestion: '每个表单至少需要包含 id、title、formName 和 dsl。',
      }))
      return
    }

    const formId = normalizeText(form.id)
    const title = normalizeText(form.title)
    const formName = normalizeText(form.formName || form.name)
    const status = normalizeText(form.status)

    if (!formId) {
      issues.push(createIssue({
        id: `module-design-form-id-missing:${index}`,
        title: '表单 ID 缺失',
        description: `第 ${index + 1} 个表单缺少 id。`,
        suggestion: '请为表单分配稳定临时 ID，例如 form_01。',
      }))
    }

    if (!title) {
      issues.push(createIssue({
        id: `module-design-form-title-missing:${formId || index}`,
        title: '表单标题缺失',
        formId,
        description: '表单需要有用户可见标题。',
        suggestion: '请补充表单标题。',
      }))
    }

    if (!formName) {
      issues.push(createIssue({
        id: `module-design-form-name-missing:${formId || index}`,
        title: '表名缺失',
        formId,
        description: 'formName 是批量保存和正式化预览的关键标识。',
        suggestion: '请补充小写下划线格式的表名。',
      }))
    } else {
      if (!isValidName(formName)) {
        issues.push(createIssue({
          id: `module-design-form-name-invalid:${formId || index}:${formName}`,
          title: '表名格式不合法',
          formId,
          description: 'formName 只能使用小写字母、数字和下划线，并且必须以字母开头。',
          suggestion: '请改为有业务含义的 snake_case 表名。',
        }))
      }
      if (!formNameMap[formName]) {
        formNameMap[formName] = []
      }
      formNameMap[formName].push(formId || index)
    }

    if (status && !MODULE_FORM_DRAFT_STATUS_OPTIONS.includes(status)) {
      issues.push(createIssue({
        id: `module-design-form-status-invalid:${formId || index}:${status}`,
        level: 'warning',
        title: '表单草稿状态不支持',
        formId,
        description: `forms[].status 当前为 ${status}。`,
        suggestion: `请使用 ${MODULE_FORM_DRAFT_STATUS_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (!isObject(form.dsl)) {
      const isGeneratedStatus = ['generated', 'confirmed'].includes(status)
      issues.push(createIssue({
        id: `module-design-form-dsl-missing:${formId || index}`,
        level: isGeneratedStatus ? 'error' : 'suggestion',
        title: isGeneratedStatus ? '表单 DSL 缺失' : '表单草稿尚未生成 DSL',
        formId,
        description: isGeneratedStatus
            ? '该表单已标记为 generated，但 forms[].dsl 不是合法的 FormDesignDSL v1 对象。'
            : '该表单仍处于逐个生成前的中间态，允许 dsl 暂时为空。',
        suggestion: '请先为该表单生成单表单 DSL，再进入模块级正式化预览。',
      }))
      return
    }

    const dslFormName = normalizeText(form.dsl.form?.name)
    const dslFormTitle = normalizeText(form.dsl.form?.title)
    if (dslFormName) {
      if (!dslFormNameMap[dslFormName]) {
        dslFormNameMap[dslFormName] = []
      }
      dslFormNameMap[dslFormName].push(formId || index)
    }

    if (formName && dslFormName && formName !== dslFormName) {
      issues.push(createIssue({
        id: `module-design-form-name-mismatch:${formId || index}`,
        level: 'warning',
        title: '表名与 DSL 不一致',
        formId,
        description: `forms[].formName 为 ${formName}，但 dsl.form.name 为 ${dslFormName}。`,
        suggestion: '请保持模块表单清单和单表单 DSL 中的表名一致。',
      }))
    }

    if (title && dslFormTitle && title !== dslFormTitle) {
      issues.push(createIssue({
        id: `module-design-form-title-mismatch:${formId || index}`,
        level: 'warning',
        title: '表单标题与 DSL 不一致',
        formId,
        description: `forms[].title 为 ${title}，但 dsl.form.title 为 ${dslFormTitle}。`,
        suggestion: '请确认是否需要统一模块清单和单表单 DSL 的标题。',
      }))
    }

    formFieldNameMap[formId] = getFormDslFieldNames(form)

    const dslIssues = validateFormDesignDslSchema(form.dsl)
    dslIssues.forEach(dslIssue => {
      issues.push(createIssue({
        id: `module-design-form-dsl:${formId || index}:${dslIssue.id}`,
        level: dslIssue.level,
        title: `表单 DSL：${dslIssue.title}`,
        formId,
        fieldName: dslIssue.fieldName || '',
        fieldLabel: dslIssue.fieldLabel || '',
        description: dslIssue.description || '',
        suggestion: dslIssue.suggestion || '',
        fixable: Boolean(dslIssue.fixable),
        meta: {
          sourceIssue: dslIssue,
        },
      }))
    })
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-design-form-id-duplicate',
    title: '表单 ID 重复',
    descriptionPrefix: '表单 ID',
    suggestion: '请为每个表单分配唯一 ID。',
    getExtraIssue: (formId) => ({formId}),
  })

  Object.keys(formNameMap)
      .filter(key => formNameMap[key].length > 1)
      .forEach(key => {
        issues.push(createIssue({
          id: `module-design-form-name-duplicate:${key}`,
          title: '表名重复',
          description: `表名 ${key} 被 ${formNameMap[key].length} 个表单使用。`,
          suggestion: '批量保存前必须保证每个表单表名唯一。',
          meta: {
            formRefs: formNameMap[key],
          },
        }))
      })

  Object.keys(dslFormNameMap)
      .filter(key => dslFormNameMap[key].length > 1)
      .forEach(key => {
        issues.push(createIssue({
          id: `module-design-dsl-form-name-duplicate:${key}`,
          title: 'DSL 表名重复',
          description: `dsl.form.name ${key} 被 ${dslFormNameMap[key].length} 个表单使用。`,
          suggestion: '请调整重复表名，否则后续正式化保存会冲突。',
          meta: {
            formRefs: dslFormNameMap[key],
          },
        }))
      })

  return {
    ids,
    idMap,
    formNameMap,
    dslFormNameMap,
    formFieldNameMap,
  }
}

const validateDictionaryFieldRefs = (dictionary, issues, context = {}) => {
  const {index = 0, formContext = {}} = context
  const fieldRefs = toArray(dictionary.fieldRefs)
  const mode = normalizeText(dictionary.mode)
  const code = normalizeText(dictionary.code || dictionary.existingCode)
  const dictionaryCode = code || normalizeText(dictionary.name) || String(index)
  const formIds = formContext.ids || new Set()
  const formFieldNameMap = formContext.formFieldNameMap || {}

  if (dictionary.fieldRefs !== undefined && !Array.isArray(dictionary.fieldRefs)) {
    issues.push(createIssue({
      id: `module-design-dictionary-field-refs-invalid:${dictionaryCode}`,
      level: 'warning',
      title: '字典关联字段不合法',
      dictionaryCode,
      description: 'dictionary.fieldRefs 必须是数组。',
      suggestion: '请使用 [{formId, fieldName, fieldLabel}] 结构关联字段。',
    }))
    return
  }

  if (mode !== 'ignore' && fieldRefs.length === 0) {
    issues.push(createIssue({
      id: `module-design-dictionary-field-refs-empty:${dictionaryCode}`,
      level: 'warning',
      title: '字典缺少关联字段',
      dictionaryCode,
      description: '模块级字典建议需要说明会应用到哪些表单字段。',
      suggestion: '请补充 fieldRefs，或将该字典标记为暂不处理。',
    }))
  }

  fieldRefs.forEach((fieldRef, refIndex) => {
    if (!isObject(fieldRef)) {
      issues.push(createIssue({
        id: `module-design-dictionary-field-ref-invalid:${dictionaryCode}:${refIndex}`,
        level: 'warning',
        title: '字典关联字段项不合法',
        dictionaryCode,
        description: `第 ${refIndex + 1} 个 fieldRef 不是对象。`,
        suggestion: '每个 fieldRef 至少需要包含 formId 和 fieldName。',
      }))
      return
    }

    const formId = normalizeText(fieldRef.formId)
    const fieldName = normalizeText(fieldRef.fieldName)
    if (!formId) {
      issues.push(createIssue({
        id: `module-design-dictionary-field-ref-form-missing:${dictionaryCode}:${refIndex}`,
        level: 'warning',
        title: '字典关联表单缺失',
        dictionaryCode,
        fieldName,
        description: 'fieldRef.formId 不能为空。',
        suggestion: '请补充关联表单 ID。',
      }))
    } else if (!formIds.has(formId)) {
      issues.push(createIssue({
        id: `module-design-dictionary-field-ref-form-not-found:${dictionaryCode}:${formId}`,
        level: 'warning',
        title: '字典关联表单不存在',
        dictionaryCode,
        formId,
        fieldName,
        description: `fieldRef.formId ${formId} 没有出现在当前 forms 中。`,
        suggestion: '请确认字典关联字段，或删除无效关联。',
      }))
    }

    if (!fieldName) {
      issues.push(createIssue({
        id: `module-design-dictionary-field-ref-name-missing:${dictionaryCode}:${refIndex}`,
        level: 'warning',
        title: '字典关联字段名缺失',
        dictionaryCode,
        formId,
        description: 'fieldRef.fieldName 不能为空。',
        suggestion: '请补充与 FormDesignDSL fields[].name 一致的字段名。',
      }))
    } else if (formId && formFieldNameMap[formId] && !formFieldNameMap[formId].has(fieldName)) {
      issues.push(createIssue({
        id: `module-design-dictionary-field-ref-name-not-found:${dictionaryCode}:${formId}:${fieldName}`,
        level: 'warning',
        title: '字典关联字段不存在',
        dictionaryCode,
        formId,
        fieldName,
        description: `字段 ${fieldName} 没有出现在表单 ${formId} 的 DSL 字段列表中。`,
        suggestion: '请确认字段名，或先重新生成对应表单 DSL。',
      }))
    }
  })
}

const validateDictionaries = (design, issues, formContext = {}) => {
  if (design.dictionaries === undefined) {
    return
  }
  if (!Array.isArray(design.dictionaries)) {
    issues.push(createIssue({
      id: 'module-design-dictionaries-invalid',
      title: '模块字典计划不合法',
      description: 'dictionaries 必须是数组。',
      suggestion: '没有字典计划时请设置为空数组。',
    }))
    return
  }

  const codeMap = {}
  design.dictionaries.forEach((dictionary, index) => {
    if (!isObject(dictionary)) {
      issues.push(createIssue({
        id: `module-design-dictionary-invalid:${index}`,
        title: '模块字典项不合法',
        description: `第 ${index + 1} 个字典计划不是对象。`,
        suggestion: '每个字典计划至少需要包含 mode 和 fieldRefs。',
      }))
      return
    }

    const mode = normalizeText(dictionary.mode)
    const existingCode = normalizeText(dictionary.existingCode)
    const code = normalizeText(dictionary.code)
    const dictionaryCode = existingCode || code || normalizeText(dictionary.name) || String(index)

    if (mode && !MODULE_DICTIONARY_MODE_OPTIONS.includes(mode)) {
      issues.push(createIssue({
        id: `module-design-dictionary-mode-invalid:${dictionaryCode}:${mode}`,
        level: 'warning',
        title: '字典处理方式不支持',
        dictionaryCode,
        description: `mode 当前为 ${mode}。`,
        suggestion: '请使用 use-existing、create、ignore 或 need-confirm。',
      }))
    }

    if (mode === 'use-existing' && !existingCode) {
      issues.push(createIssue({
        id: `module-design-dictionary-existing-code-missing:${dictionaryCode}`,
        level: 'warning',
        title: '已有字典编码缺失',
        dictionaryCode,
        description: '使用已有字典时必须明确 existingCode。',
        suggestion: '请在字典确认区选择系统字典。',
      }))
    }

    if (mode === 'create') {
      if (!code) {
        issues.push(createIssue({
          id: `module-design-dictionary-code-missing:${dictionaryCode}`,
          level: 'warning',
          title: '新建字典编码缺失',
          dictionaryCode,
          description: '新建系统字典时必须明确 code。',
          suggestion: '建议使用模块名或表名加字段名，例如 oa_receive_status。',
        }))
      } else {
        if (!isValidName(code)) {
          issues.push(createIssue({
            id: `module-design-dictionary-code-invalid:${code}`,
            level: 'warning',
            title: '字典编码格式不合法',
            dictionaryCode: code,
            description: '字典编码建议使用小写字母、数字和下划线，并以字母开头。',
            suggestion: '请改为有业务含义的 snake_case 字典编码。',
          }))
        }
        if (!codeMap[code]) {
          codeMap[code] = []
        }
        codeMap[code].push(index)
      }

      if (isEmpty(dictionary.name)) {
        issues.push(createIssue({
          id: `module-design-dictionary-name-missing:${dictionaryCode}`,
          level: 'warning',
          title: '新建字典名称缺失',
          dictionaryCode,
          description: '新建系统字典时建议提供中文名称。',
          suggestion: '请补充字典名称，例如 收文状态。',
        }))
      }

      if (dictionary.items !== undefined && !Array.isArray(dictionary.items)) {
        issues.push(createIssue({
          id: `module-design-dictionary-items-invalid:${dictionaryCode}`,
          level: 'warning',
          title: '字典项结构不合法',
          dictionaryCode,
          description: 'dictionary.items 必须是数组。',
          suggestion: '请使用 [{value:"draft",text:"草稿"}] 这样的结构。',
        }))
      } else if (toArray(dictionary.items).length === 0) {
        issues.push(createIssue({
          id: `module-design-dictionary-items-empty:${dictionaryCode}`,
          level: 'warning',
          title: '新建字典缺少字典项',
          dictionaryCode,
          description: '新建系统字典没有字典项，批量创建前需要人工补充或改为使用已有字典。',
          suggestion: '请补充字典项，或将该字典标记为待确认。',
        }))
      }
    }

    toArray(dictionary.items).forEach((item, itemIndex) => {
      if (!isObject(item)) {
        issues.push(createIssue({
          id: `module-design-dictionary-item-invalid:${dictionaryCode}:${itemIndex}`,
          level: 'warning',
          title: '字典项不合法',
          dictionaryCode,
          description: `第 ${itemIndex + 1} 个字典项不是对象。`,
          suggestion: '每个字典项需要包含 value 和 text。',
        }))
        return
      }
      if (isEmpty(item.value) || isEmpty(item.text)) {
        issues.push(createIssue({
          id: `module-design-dictionary-item-value-missing:${dictionaryCode}:${itemIndex}`,
          level: 'warning',
          title: '字典项缺少编码或名称',
          dictionaryCode,
          description: '字典项需要同时包含 value 和 text。',
          suggestion: '请补齐字典项，或在字典确认区人工补充。',
        }))
      }
    })

    if (!isValidConfidence(dictionary.confidence)) {
      issues.push(createIssue({
        id: `module-design-dictionary-confidence-invalid:${dictionaryCode}`,
        level: 'warning',
        title: '字典建议置信度不合法',
        dictionaryCode,
        description: '字典 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }

    validateDictionaryFieldRefs(dictionary, issues, {
      index,
      formContext,
    })
  })

  Object.keys(codeMap)
      .filter(key => codeMap[key].length > 1)
      .forEach(key => {
        issues.push(createIssue({
          id: `module-design-dictionary-code-duplicate:${key}`,
          title: '新建字典编码重复',
          dictionaryCode: key,
          description: `字典编码 ${key} 在模块字典计划中出现了 ${codeMap[key].length} 次。`,
          suggestion: '请合并同义字典，或为不同语义字典设置不同编码。',
          meta: {
            indexes: codeMap[key],
          },
        }))
      })
}

const validateRelations = (design, issues, formContext = {}) => {
  if (design.relations === undefined) {
    return
  }
  if (!Array.isArray(design.relations)) {
    issues.push(createIssue({
      id: 'module-design-relations-invalid',
      level: 'warning',
      title: '表单关系列表不合法',
      description: 'relations 必须是数组。',
      suggestion: '没有表单关系时请设置为空数组。',
    }))
    return
  }

  const formIds = formContext.ids || new Set()
  design.relations.forEach((relation, index) => {
    if (!isObject(relation)) {
      issues.push(createIssue({
        id: `module-design-relation-invalid:${index}`,
        level: 'warning',
        title: '表单关系项不合法',
        description: `第 ${index + 1} 个关系不是对象。`,
        suggestion: '每个关系至少需要包含 type、fromFormId、toFormId。',
      }))
      return
    }

    const type = normalizeText(relation.type)
    const fromFormId = normalizeText(relation.fromFormId)
    const toFormId = normalizeText(relation.toFormId)

    if (type && !MODULE_RELATION_TYPE_OPTIONS.includes(type)) {
      issues.push(createIssue({
        id: `module-design-relation-type-invalid:${index}:${type}`,
        level: 'warning',
        title: '表单关系类型不支持',
        description: `relation.type 当前为 ${type}。`,
        suggestion: `请使用 ${MODULE_RELATION_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (fromFormId && !formIds.has(fromFormId)) {
      issues.push(createIssue({
        id: `module-design-relation-from-missing:${index}:${fromFormId}`,
        level: 'warning',
        title: '关系来源表单不存在',
        formId: fromFormId,
        description: `fromFormId ${fromFormId} 没有出现在当前 forms 中。`,
        suggestion: '请确认表单关系，或删除无法关联的关系。',
      }))
    }

    if (toFormId && !formIds.has(toFormId)) {
      issues.push(createIssue({
        id: `module-design-relation-to-missing:${index}:${toFormId}`,
        level: 'warning',
        title: '关系目标表单不存在',
        formId: toFormId,
        description: `toFormId ${toFormId} 没有出现在当前 forms 中。`,
        suggestion: '请确认表单关系，或删除无法关联的关系。',
      }))
    }
  })
}

const validateApplyPlan = (design, issues, formContext = {}) => {
  if (design.applyPlan === undefined) {
    return
  }
  if (!isObject(design.applyPlan)) {
    issues.push(createIssue({
      id: 'module-design-apply-plan-invalid',
      level: 'warning',
      title: '批量保存计划不合法',
      description: 'applyPlan 必须是对象。',
      suggestion: '请使用 {steps,warnings,blockingIssues} 结构。',
    }))
    return
  }

  if (design.applyPlan.steps !== undefined && !Array.isArray(design.applyPlan.steps)) {
    issues.push(createIssue({
      id: 'module-design-apply-plan-steps-invalid',
      level: 'warning',
      title: '批量保存步骤不合法',
      description: 'applyPlan.steps 必须是数组。',
      suggestion: '没有保存步骤时请设置为空数组。',
    }))
    return
  }

  const formIds = formContext.ids || new Set()
  toArray(design.applyPlan.steps).forEach((step, index) => {
    if (!isObject(step)) {
      issues.push(createIssue({
        id: `module-design-apply-step-invalid:${index}`,
        level: 'warning',
        title: '批量保存步骤项不合法',
        description: `第 ${index + 1} 个保存步骤不是对象。`,
        suggestion: '每个步骤建议包含 type、targetId、status。',
      }))
      return
    }

    const type = normalizeText(step.type)
    const status = normalizeText(step.status)
    const targetId = normalizeText(step.targetId || step.formId)

    if (type && !MODULE_APPLY_STEP_TYPE_OPTIONS.includes(type)) {
      issues.push(createIssue({
        id: `module-design-apply-step-type-invalid:${index}:${type}`,
        level: 'warning',
        title: '批量保存步骤类型不支持',
        description: `step.type 当前为 ${type}。`,
        suggestion: `请使用 ${MODULE_APPLY_STEP_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (status && !MODULE_APPLY_STEP_STATUS_OPTIONS.includes(status)) {
      issues.push(createIssue({
        id: `module-design-apply-step-status-invalid:${index}:${status}`,
        level: 'warning',
        title: '批量保存步骤状态不支持',
        description: `step.status 当前为 ${status}。`,
        suggestion: `请使用 ${MODULE_APPLY_STEP_STATUS_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (type === 'save-form' && targetId && !formIds.has(targetId)) {
      issues.push(createIssue({
        id: `module-design-apply-step-form-not-found:${index}:${targetId}`,
        level: 'warning',
        title: '保存步骤关联表单不存在',
        formId: targetId,
        description: `保存步骤关联的表单 ${targetId} 没有出现在当前 forms 中。`,
        suggestion: '请确认保存计划，或重新生成批量正式化预览。',
      }))
    }
  })

  if (design.applyPlan.warnings !== undefined && !Array.isArray(design.applyPlan.warnings)) {
    issues.push(createIssue({
      id: 'module-design-apply-plan-warnings-invalid',
      level: 'warning',
      title: '保存计划警告列表不合法',
      description: 'applyPlan.warnings 必须是数组。',
      suggestion: '没有警告时请设置为空数组。',
    }))
  }

  if (design.applyPlan.blockingIssues !== undefined && !Array.isArray(design.applyPlan.blockingIssues)) {
    issues.push(createIssue({
      id: 'module-design-apply-plan-blocking-invalid',
      level: 'warning',
      title: '保存计划阻断问题列表不合法',
      description: 'applyPlan.blockingIssues 必须是数组。',
      suggestion: '没有阻断问题时请设置为空数组。',
    }))
  }
}

export const validateModuleDesignSchema = (design = {}) => {
  const issues = []
  if (!validateRoot(design, issues)) {
    return issues
  }

  validateModule(design, issues)
  const formContext = validateForms(design, issues)
  const menuContext = validateMenus(design, issues)
  validatePages(design, issues, menuContext, formContext.ids || new Set())
  validateDictionaries(design, issues, formContext)
  validateRelations(design, issues, formContext)
  validateApplyPlan(design, issues, formContext)

  if (toArray(design.forms).length === 0) {
    issues.push(createIssue({
      id: 'module-design-forms-empty',
      title: '模块没有可生成表单',
      description: 'ModuleDesignDSL 至少应包含一个表单草稿。',
      suggestion: '请先从模块蓝图中选择需要生成的页面，并生成对应 FormDesignDSL。',
    }))
  }

  return issues
}

export const summarizeModuleDesignIssues = (issues = []) => {
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

export const isValidModuleDesignSchema = (design = {}) => {
  return validateModuleDesignSchema(design).every(issue => issue.level !== 'error')
}

export const createEmptyModuleDesign = () => ({
  version: MODULE_DESIGN_SCHEMA_VERSION,
  exportedAt: '',
  module: {
    name: '',
    title: '',
    description: '',
    scene: '',
  },
  menus: [],
  pages: [],
  forms: [],
  dictionaries: [],
  relations: [],
  checks: [],
  applyPlan: {
    steps: [],
    warnings: [],
    blockingIssues: [],
  },
  meta: {},
})

export default validateModuleDesignSchema
