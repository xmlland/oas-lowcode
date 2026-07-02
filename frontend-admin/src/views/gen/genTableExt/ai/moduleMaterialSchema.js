export const MODULE_MATERIAL_SCHEMA_VERSION = '1.0'

export const MODULE_MATERIAL_SOURCE_TYPES = [
  'text',
  'url',
  'excel',
  'word',
  'docx',
  'pdf',
  'image',
  'screenshot',
  'mixed',
]

export const MODULE_MATERIAL_SCENE_OPTIONS = [
  '',
  'normal',
  'document',
  'document-form',
  'approval',
  'ledger',
  'mixed',
]

export const MODULE_MATERIAL_PAGE_TYPE_OPTIONS = [
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

export const MODULE_MATERIAL_RELATION_TYPE_OPTIONS = [
  '',
  'master_detail',
  'reference',
  'workflow',
  'lookup',
  'attachment',
  'statistics',
  'unknown',
]

export const MODULE_MATERIAL_FIELD_TYPE_OPTIONS = [
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

export const MODULE_MATERIAL_QUERY_MODE_OPTIONS = [
  '',
  'input',
  'like',
  'exact',
  'select',
  'date-range',
  'range',
]

const NAME_PATTERN = /^[a-z][a-z0-9_]*$/
const GENERATED_FIELD_NAME_PATTERN = /^field_\d+$/i

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

const isValidNameHint = (value) => {
  const text = normalizeText(value)
  return !text || NAME_PATTERN.test(text)
}

const isGeneratedFieldName = (value) => GENERATED_FIELD_NAME_PATTERN.test(normalizeText(value))

const createIssue = ({
  id,
  level = 'error',
  title,
  menuId = '',
  pageId = '',
  formId = '',
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
  menuId,
  pageId,
  formId,
  fieldId,
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
    metaKey = 'indexes',
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
            [metaKey]: idMap[key],
          },
          ...getExtraIssue(key),
        }))
      })
}

const validateRoot = (material, issues) => {
  if (!isObject(material)) {
    issues.push(createIssue({
      id: 'module-material-root-invalid',
      title: '模块材料结构不合法',
      description: 'ModuleMaterial 必须是一个对象。',
      suggestion: '请生成包含 version、source、module、menus、pages、forms 的材料对象。',
    }))
    return false
  }

  if (material.version !== MODULE_MATERIAL_SCHEMA_VERSION) {
    issues.push(createIssue({
      id: `module-material-version-invalid:${material.version || 'empty'}`,
      title: '模块材料版本不支持',
      description: `当前版本为 ${material.version || '空'}，当前只支持 ${MODULE_MATERIAL_SCHEMA_VERSION}。`,
      suggestion: `请设置 version 为 ${MODULE_MATERIAL_SCHEMA_VERSION}。`,
    }))
  }

  if (!isValidConfidence(material.confidence)) {
    issues.push(createIssue({
      id: `module-material-confidence-invalid:${material.confidence}`,
      level: 'warning',
      title: '模块材料置信度不合法',
      description: 'confidence 必须是 0 到 1 之间的数字。',
      suggestion: '无法判断时可以留空。',
    }))
  }

  return true
}

const validateSource = (material, issues) => {
  if (!isObject(material.source)) {
    issues.push(createIssue({
      id: 'module-material-source-missing',
      title: '模块材料来源缺失',
      description: 'source 对象用于描述模块材料来源类型。',
      suggestion: '请设置 source.type，例如 text、url、excel、word、pdf。',
    }))
    return
  }

  const sourceType = normalizeText(material.source.type)
  if (!MODULE_MATERIAL_SOURCE_TYPES.includes(sourceType)) {
    issues.push(createIssue({
      id: `module-material-source-type-invalid:${sourceType || 'empty'}`,
      title: '模块材料来源类型不支持',
      description: `source.type 当前为 ${sourceType || '空'}。`,
      suggestion: `请使用 ${MODULE_MATERIAL_SOURCE_TYPES.join('、')} 中的一种。`,
    }))
  }

  if (sourceType === 'url' && isEmpty(material.source.url)) {
    issues.push(createIssue({
      id: 'module-material-source-url-missing',
      level: 'warning',
      title: '原型 URL 缺失',
      description: 'source.type 为 url 时，建议保留原型系统地址，便于后续追溯和重新采集。',
      suggestion: '请补充 source.url，或在 meta.captures 中记录页面采集来源。',
    }))
  }

  if (['excel', 'word', 'docx', 'pdf', 'image', 'screenshot'].includes(sourceType)
      && isEmpty(material.source.fileName)
      && isEmpty(material.source.name)) {
    issues.push(createIssue({
      id: 'module-material-source-file-name-missing',
      level: 'warning',
      title: '文件来源名称缺失',
      description: '文件型模块材料建议记录文件名，便于后续质控和追溯。',
      suggestion: '请补充 source.fileName 或 source.name。',
    }))
  }
}

const validateModuleInfo = (material, issues) => {
  if (!isObject(material.module)) {
    issues.push(createIssue({
      id: 'module-material-module-missing',
      level: 'warning',
      title: '模块信息缺失',
      description: 'module 对象用于承载模块名称、标题、场景和说明。',
      suggestion: '请补充 module.title，无法确定模块编码时 nameHint 可以先留空。',
    }))
    return
  }

  const nameHint = normalizeText(material.module.nameHint || material.module.name)
  const title = normalizeText(material.module.title)
  const scene = normalizeText(material.module.scene || material.scene)

  if (!title) {
    issues.push(createIssue({
      id: 'module-material-module-title-missing',
      level: 'warning',
      title: '模块标题缺失',
      description: '模块标题会作为蓝图确认页和后续草稿的主要识别信息。',
      suggestion: '请补充面向用户的中文模块标题。',
    }))
  }

  if (!isValidNameHint(nameHint)) {
    issues.push(createIssue({
      id: `module-material-module-name-invalid:${nameHint}`,
      level: 'warning',
      title: '模块编码建议格式不合法',
      description: 'module.nameHint 建议使用小写字母、数字和下划线，并以字母开头。',
      suggestion: '请让 AI 重新给出有业务含义的 snake_case 模块编码，或在确认页手动修改。',
    }))
  }

  if (scene && !MODULE_MATERIAL_SCENE_OPTIONS.includes(scene)) {
    issues.push(createIssue({
      id: `module-material-module-scene-invalid:${scene}`,
      level: 'warning',
      title: '模块场景不支持',
      description: `模块场景当前为 ${scene}。`,
      suggestion: `请使用 ${MODULE_MATERIAL_SCENE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
    }))
  }
}

const validateMenus = (material, issues) => {
  if (!Array.isArray(material.menus)) {
    issues.push(createIssue({
      id: 'module-material-menus-invalid',
      title: '菜单列表不合法',
      description: 'menus 必须是数组。没有菜单时请设置为空数组。',
      suggestion: '请将识别到的菜单放入 menus 数组。',
    }))
    return {}
  }

  const idMap = collectIds(material.menus)
  const menuIds = new Set(Object.keys(idMap))

  material.menus.forEach((menu, index) => {
    if (!isObject(menu)) {
      issues.push(createIssue({
        id: `module-material-menu-invalid:${index}`,
        title: '菜单项不合法',
        description: `第 ${index + 1} 个菜单不是对象。`,
        suggestion: '每个菜单至少需要包含 id 和 title。',
      }))
      return
    }

    const menuId = normalizeText(menu.id)
    if (!menuId) {
      issues.push(createIssue({
        id: `module-material-menu-id-missing:${index}`,
        title: '菜单 ID 缺失',
        description: `第 ${index + 1} 个菜单缺少 id，后续页面无法稳定关联菜单。`,
        suggestion: '请为菜单分配稳定临时 ID，例如 menu_01。',
      }))
    }

    if (isEmpty(menu.title)) {
      issues.push(createIssue({
        id: `module-material-menu-title-missing:${menuId || index}`,
        title: '菜单标题缺失',
        menuId,
        description: '菜单需要有用户可见标题。',
        suggestion: '请补充菜单标题。',
      }))
    }

    const parentId = normalizeText(menu.parentId)
    if (parentId && !menuIds.has(parentId)) {
      issues.push(createIssue({
        id: `module-material-menu-parent-missing:${menuId || index}:${parentId}`,
        level: 'warning',
        title: '菜单父级不存在',
        menuId,
        description: `菜单父级 ${parentId} 没有出现在当前 menus 中。`,
        suggestion: '请确认菜单层级，或删除无效 parentId。',
      }))
    }

    if (!isValidConfidence(menu.confidence)) {
      issues.push(createIssue({
        id: `module-material-menu-confidence-invalid:${menuId || index}`,
        level: 'warning',
        title: '菜单置信度不合法',
        menuId,
        description: '菜单 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-material-menu-id-duplicate',
    title: '菜单 ID 重复',
    descriptionPrefix: '菜单 ID',
    suggestion: '请为每个菜单分配唯一 ID。',
    getExtraIssue: (menuId) => ({menuId}),
  })

  return {
    idMap,
    ids: menuIds,
  }
}

const validatePages = (material, issues, menuContext = {}) => {
  if (!Array.isArray(material.pages)) {
    issues.push(createIssue({
      id: 'module-material-pages-invalid',
      title: '页面列表不合法',
      description: 'pages 必须是数组。模块级材料至少应识别菜单、页面或表单之一。',
      suggestion: '请将识别到的页面放入 pages 数组。',
    }))
    return {}
  }

  const idMap = collectIds(material.pages)
  const pageIds = new Set(Object.keys(idMap))
  const menuIds = menuContext.ids || new Set()

  material.pages.forEach((page, index) => {
    if (!isObject(page)) {
      issues.push(createIssue({
        id: `module-material-page-invalid:${index}`,
        title: '页面项不合法',
        description: `第 ${index + 1} 个页面不是对象。`,
        suggestion: '每个页面至少需要包含 id、title 和 pageType。',
      }))
      return
    }

    const pageId = normalizeText(page.id)
    if (!pageId) {
      issues.push(createIssue({
        id: `module-material-page-id-missing:${index}`,
        title: '页面 ID 缺失',
        description: `第 ${index + 1} 个页面缺少 id，后续表单无法稳定关联页面。`,
        suggestion: '请为页面分配稳定临时 ID，例如 page_01。',
      }))
    }

    if (isEmpty(page.title)) {
      issues.push(createIssue({
        id: `module-material-page-title-missing:${pageId || index}`,
        title: '页面标题缺失',
        pageId,
        description: '页面需要有用户可见标题。',
        suggestion: '请补充页面标题。',
      }))
    }

    const pageType = normalizeText(page.pageType)
    if (pageType && !MODULE_MATERIAL_PAGE_TYPE_OPTIONS.includes(pageType)) {
      issues.push(createIssue({
        id: `module-material-page-type-invalid:${pageId || index}:${pageType}`,
        level: 'warning',
        title: '页面类型不支持',
        pageId,
        description: `pageType 当前为 ${pageType}。`,
        suggestion: `请使用 ${MODULE_MATERIAL_PAGE_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    const menuId = normalizeText(page.menuId)
    if (menuId && !menuIds.has(menuId)) {
      issues.push(createIssue({
        id: `module-material-page-menu-missing:${pageId || index}:${menuId}`,
        level: 'warning',
        title: '页面关联菜单不存在',
        pageId,
        menuId,
        description: `页面关联的菜单 ${menuId} 没有出现在当前 menus 中。`,
        suggestion: '请确认页面归属菜单，或清空 menuId 后在蓝图确认页重新选择。',
      }))
    }

    if (page.sourceRefs !== undefined && !Array.isArray(page.sourceRefs)) {
      issues.push(createIssue({
        id: `module-material-page-source-refs-invalid:${pageId || index}`,
        level: 'warning',
        title: '页面来源引用不合法',
        pageId,
        description: 'page.sourceRefs 必须是数组。',
        suggestion: '请使用数组记录页面来自哪些材料片段、截图或 URL。',
      }))
    }

    if (!isValidConfidence(page.confidence)) {
      issues.push(createIssue({
        id: `module-material-page-confidence-invalid:${pageId || index}`,
        level: 'warning',
        title: '页面置信度不合法',
        pageId,
        description: '页面 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-material-page-id-duplicate',
    title: '页面 ID 重复',
    descriptionPrefix: '页面 ID',
    suggestion: '请为每个页面分配唯一 ID。',
    getExtraIssue: (pageId) => ({pageId}),
  })

  return {
    idMap,
    ids: pageIds,
  }
}

const validateField = (field, issues, context = {}) => {
  const {formId = '', index = 0, fieldIdMap = {}} = context
  if (!isObject(field)) {
    issues.push(createIssue({
      id: `module-material-field-invalid:${formId || 'form'}:${index}`,
      title: '字段项不合法',
      formId,
      description: `第 ${index + 1} 个字段不是对象。`,
      suggestion: '每个字段至少需要包含 label。',
    }))
    return
  }

  const fieldId = normalizeText(field.id)
  const fieldLabel = normalizeText(field.label)
  const nameHint = normalizeText(field.nameHint)

  if (fieldId) {
    if (!fieldIdMap[fieldId]) {
      fieldIdMap[fieldId] = []
    }
    fieldIdMap[fieldId].push(index)
  }

  if (isEmpty(fieldLabel)) {
    issues.push(createIssue({
      id: `module-material-field-label-missing:${formId || 'form'}:${fieldId || index}`,
      title: '字段标题缺失',
      formId,
      fieldId,
      description: `第 ${index + 1} 个字段缺少 label。`,
      suggestion: '请补充用户可见字段名。',
    }))
  }

  if (!isValidNameHint(nameHint)) {
    issues.push(createIssue({
      id: `module-material-field-name-invalid:${formId || 'form'}:${fieldId || fieldLabel}:${nameHint}`,
      level: 'warning',
      title: '字段英文名建议格式不合法',
      formId,
      fieldId,
      fieldLabel,
      description: 'field.nameHint 建议使用小写字母、数字和下划线，并以字母开头。',
      suggestion: '请让 AI 重新给出有业务含义的 snake_case 字段名，或在确认页手动修改。',
    }))
  } else if (isGeneratedFieldName(nameHint)) {
    issues.push(createIssue({
      id: `module-material-field-name-low-quality:${formId || 'form'}:${fieldId || fieldLabel}:${nameHint}`,
      level: 'warning',
      title: '字段英文名缺少业务含义',
      formId,
      fieldId,
      fieldLabel,
      description: `field.nameHint 当前为 ${nameHint}，这类 field_01 名称不能支撑后续落库和代码生成。`,
      suggestion: '请重新调用 AI 识别或在确认页手动改为有业务含义的英文名。',
    }))
  }

  if (!isEmpty(field.typeHint) && !MODULE_MATERIAL_FIELD_TYPE_OPTIONS.includes(field.typeHint)) {
    issues.push(createIssue({
      id: `module-material-field-type-invalid:${formId || 'form'}:${fieldId || fieldLabel}:${field.typeHint}`,
      level: 'warning',
      title: '字段类型建议不支持',
      formId,
      fieldId,
      fieldLabel,
      description: `typeHint 当前为 ${field.typeHint}。`,
      suggestion: '请使用 FormDesignDSL 支持的字段类型，或留空交给后续确认。',
    }))
  }

  const queryMode = normalizeText(field.queryMode)
  if (queryMode && !MODULE_MATERIAL_QUERY_MODE_OPTIONS.includes(queryMode)) {
    issues.push(createIssue({
      id: `module-material-field-query-mode-invalid:${formId || 'form'}:${fieldId || fieldLabel}:${queryMode}`,
      level: 'warning',
      title: '查询模式建议不支持',
      formId,
      fieldId,
      fieldLabel,
      description: `queryMode 当前为 ${queryMode}。`,
      suggestion: `请使用 ${MODULE_MATERIAL_QUERY_MODE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
    }))
  }

  if (!isValidPriority(field.listPriority)) {
    issues.push(createIssue({
      id: `module-material-field-list-priority-invalid:${formId || 'form'}:${fieldId || fieldLabel}`,
      level: 'warning',
      title: '列表优先级不合法',
      formId,
      fieldId,
      fieldLabel,
      description: 'listPriority 必须是 0 到 100 之间的数字。',
      suggestion: '无法判断时可以留空，确认页可手动调整列表建议。',
    }))
  }

  if (!isValidPriority(field.queryPriority)) {
    issues.push(createIssue({
      id: `module-material-field-query-priority-invalid:${formId || 'form'}:${fieldId || fieldLabel}`,
      level: 'warning',
      title: '查询优先级不合法',
      formId,
      fieldId,
      fieldLabel,
      description: 'queryPriority 必须是 0 到 100 之间的数字。',
      suggestion: '无法判断时可以留空，确认页可手动调整查询建议。',
    }))
  }

  if (!isValidConfidence(field.confidence)) {
    issues.push(createIssue({
      id: `module-material-field-confidence-invalid:${formId || 'form'}:${fieldId || fieldLabel}`,
      level: 'warning',
      title: '字段置信度不合法',
      formId,
      fieldId,
      fieldLabel,
      description: '字段 confidence 必须是 0 到 1 之间的数字。',
      suggestion: '无法判断时可以留空。',
    }))
  }
}

const validateForms = (material, issues, pageContext = {}) => {
  if (!Array.isArray(material.forms)) {
    issues.push(createIssue({
      id: 'module-material-forms-invalid',
      title: '表单列表不合法',
      description: 'forms 必须是数组。没有表单边界时请设置为空数组。',
      suggestion: '请将识别到的表单放入 forms 数组。',
    }))
    return {}
  }

  const idMap = collectIds(material.forms)
  const formIds = new Set(Object.keys(idMap))
  const pageIds = pageContext.ids || new Set()

  material.forms.forEach((form, index) => {
    if (!isObject(form)) {
      issues.push(createIssue({
        id: `module-material-form-invalid:${index}`,
        title: '表单项不合法',
        description: `第 ${index + 1} 个表单不是对象。`,
        suggestion: '每个表单至少需要包含 id、title 和 fields。',
      }))
      return
    }

    const formId = normalizeText(form.id)
    if (!formId) {
      issues.push(createIssue({
        id: `module-material-form-id-missing:${index}`,
        title: '表单 ID 缺失',
        description: `第 ${index + 1} 个表单缺少 id，后续无法稳定生成单表单 DSL。`,
        suggestion: '请为表单分配稳定临时 ID，例如 form_01。',
      }))
    }

    if (isEmpty(form.title)) {
      issues.push(createIssue({
        id: `module-material-form-title-missing:${formId || index}`,
        title: '表单标题缺失',
        formId,
        description: '表单需要有用户可见标题。',
        suggestion: '请补充表单标题。',
      }))
    }

    const nameHint = normalizeText(form.nameHint || form.formName)
    if (!isValidNameHint(nameHint)) {
      issues.push(createIssue({
        id: `module-material-form-name-invalid:${formId || index}:${nameHint}`,
        level: 'warning',
        title: '表名建议格式不合法',
        formId,
        description: 'form.nameHint 建议使用小写字母、数字和下划线，并以字母开头。',
        suggestion: '请让 AI 重新给出有业务含义的 snake_case 表名，或在确认页手动修改。',
      }))
    }

    const pageId = normalizeText(form.pageId)
    if (pageId && !pageIds.has(pageId)) {
      issues.push(createIssue({
        id: `module-material-form-page-missing:${formId || index}:${pageId}`,
        level: 'warning',
        title: '表单关联页面不存在',
        formId,
        pageId,
        description: `表单关联的页面 ${pageId} 没有出现在当前 pages 中。`,
        suggestion: '请确认表单归属页面，或清空 pageId 后在蓝图确认页重新选择。',
      }))
    }

    if (form.fields !== undefined && !Array.isArray(form.fields)) {
      issues.push(createIssue({
        id: `module-material-form-fields-invalid:${formId || index}`,
        title: '表单字段列表不合法',
        formId,
        description: 'form.fields 必须是数组。',
        suggestion: '没有识别到字段时请设置为空数组。',
      }))
    } else {
      const fieldIdMap = {}
      toArray(form.fields).forEach((field, fieldIndex) => {
        validateField(field, issues, {
          formId,
          index: fieldIndex,
          fieldIdMap,
        })
      })

      reportDuplicateIds(fieldIdMap, issues, {
        issuePrefix: `module-material-field-id-duplicate:${formId || index}`,
        title: '字段 ID 重复',
        descriptionPrefix: '字段 ID',
        suggestion: '请为同一个表单内的每个字段分配唯一 ID。',
        getExtraIssue: (fieldId) => ({formId, fieldId}),
      })
    }

    if (form.groups !== undefined && !Array.isArray(form.groups)) {
      issues.push(createIssue({
        id: `module-material-form-groups-invalid:${formId || index}`,
        level: 'warning',
        title: '表单分组列表不合法',
        formId,
        description: 'form.groups 必须是数组。',
        suggestion: '没有分组时请设置为空数组。',
      }))
    }

    if (form.tables !== undefined && !Array.isArray(form.tables)) {
      issues.push(createIssue({
        id: `module-material-form-tables-invalid:${formId || index}`,
        level: 'warning',
        title: '表单表格材料不合法',
        formId,
        description: 'form.tables 必须是数组。',
        suggestion: '没有表格材料时请设置为空数组。',
      }))
    }

    if (!isValidConfidence(form.confidence)) {
      issues.push(createIssue({
        id: `module-material-form-confidence-invalid:${formId || index}`,
        level: 'warning',
        title: '表单置信度不合法',
        formId,
        description: '表单 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }
  })

  reportDuplicateIds(idMap, issues, {
    issuePrefix: 'module-material-form-id-duplicate',
    title: '表单 ID 重复',
    descriptionPrefix: '表单 ID',
    suggestion: '请为每个表单分配唯一 ID。',
    getExtraIssue: (formId) => ({formId}),
  })

  return {
    idMap,
    ids: formIds,
  }
}

const validateRelations = (material, issues, formContext = {}) => {
  if (material.relations === undefined) {
    return
  }
  if (!Array.isArray(material.relations)) {
    issues.push(createIssue({
      id: 'module-material-relations-invalid',
      level: 'warning',
      title: '表单关系列表不合法',
      description: 'relations 必须是数组。',
      suggestion: '没有表单关系时请设置为空数组。',
    }))
    return
  }

  const formIds = formContext.ids || new Set()
  material.relations.forEach((relation, index) => {
    if (!isObject(relation)) {
      issues.push(createIssue({
        id: `module-material-relation-invalid:${index}`,
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

    if (type && !MODULE_MATERIAL_RELATION_TYPE_OPTIONS.includes(type)) {
      issues.push(createIssue({
        id: `module-material-relation-type-invalid:${index}:${type}`,
        level: 'warning',
        title: '表单关系类型不支持',
        description: `relation.type 当前为 ${type}。`,
        suggestion: `请使用 ${MODULE_MATERIAL_RELATION_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
      }))
    }

    if (fromFormId && !formIds.has(fromFormId)) {
      issues.push(createIssue({
        id: `module-material-relation-from-missing:${index}:${fromFormId}`,
        level: 'warning',
        title: '关系来源表单不存在',
        formId: fromFormId,
        description: `fromFormId ${fromFormId} 没有出现在当前 forms 中。`,
        suggestion: '请确认表单关系，或删除无法关联的关系。',
      }))
    }

    if (toFormId && !formIds.has(toFormId)) {
      issues.push(createIssue({
        id: `module-material-relation-to-missing:${index}:${toFormId}`,
        level: 'warning',
        title: '关系目标表单不存在',
        formId: toFormId,
        description: `toFormId ${toFormId} 没有出现在当前 forms 中。`,
        suggestion: '请确认表单关系，或删除无法关联的关系。',
      }))
    }

    if (!isValidConfidence(relation.confidence)) {
      issues.push(createIssue({
        id: `module-material-relation-confidence-invalid:${index}`,
        level: 'warning',
        title: '表单关系置信度不合法',
        description: '关系 confidence 必须是 0 到 1 之间的数字。',
        suggestion: '无法判断时可以留空。',
      }))
    }
  })
}

const validateSourceTables = (material, issues) => {
  const tables = material.source?.tables ?? material.tables
  if (tables === undefined) {
    return
  }
  if (!Array.isArray(tables)) {
    issues.push(createIssue({
      id: 'module-material-source-tables-invalid',
      level: 'warning',
      title: '来源表格列表不合法',
      description: 'source.tables 或 tables 必须是数组。',
      suggestion: '没有来源表格时请设置为空数组。',
    }))
    return
  }

  tables.forEach((table, index) => {
    if (!isObject(table)) {
      issues.push(createIssue({
        id: `module-material-source-table-invalid:${index}`,
        level: 'warning',
        title: '来源表格项不合法',
        description: `第 ${index + 1} 个来源表格不是对象。`,
        suggestion: '每个来源表格至少应包含 headers 或 rows。',
      }))
      return
    }
    if (table.headers !== undefined && !Array.isArray(table.headers)) {
      issues.push(createIssue({
        id: `module-material-source-table-headers-invalid:${table.id || index}`,
        level: 'warning',
        title: '来源表头不合法',
        description: 'table.headers 必须是数组。',
        suggestion: '请使用字符串数组保存表头。',
      }))
    }
    if (table.rows !== undefined && !Array.isArray(table.rows)) {
      issues.push(createIssue({
        id: `module-material-source-table-rows-invalid:${table.id || index}`,
        level: 'warning',
        title: '来源表格行不合法',
        description: 'table.rows 必须是二维数组。',
        suggestion: '没有样例数据时可以设置为空数组。',
      }))
    }
  })
}

export const validateModuleMaterialSchema = (material = {}) => {
  const issues = []
  if (!validateRoot(material, issues)) {
    return issues
  }

  validateSource(material, issues)
  validateModuleInfo(material, issues)
  const menuContext = validateMenus(material, issues)
  const pageContext = validatePages(material, issues, menuContext)
  const formContext = validateForms(material, issues, pageContext)
  validateRelations(material, issues, formContext)
  validateSourceTables(material, issues)

  if (toArray(material.menus).length === 0
      && toArray(material.pages).length === 0
      && toArray(material.forms).length === 0) {
    issues.push(createIssue({
      id: 'module-material-content-empty',
      title: '模块材料内容为空',
      description: 'menus、pages、forms 至少需要有一项包含内容。',
      suggestion: '请补充识别到的菜单、页面或表单边界。',
    }))
  }

  if (toArray(material.pages).length > 0 && toArray(material.forms).length === 0) {
    issues.push(createIssue({
      id: 'module-material-forms-empty',
      level: 'suggestion',
      title: '尚未识别到表单边界',
      description: '当前材料识别到了页面，但没有识别到可生成的表单。',
      suggestion: '可以先在模块蓝图确认页标记哪些页面需要生成表单，再逐页补充表单字段。',
    }))
  }

  return issues
}

export const summarizeModuleMaterialIssues = (issues = []) => {
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

export const isValidModuleMaterialSchema = (material = {}) => {
  return validateModuleMaterialSchema(material).every(issue => issue.level !== 'error')
}

export const createEmptyModuleMaterial = (sourceType = 'text') => ({
  version: MODULE_MATERIAL_SCHEMA_VERSION,
  source: {
    type: sourceType,
    name: '',
    fileName: '',
    url: '',
    rawText: '',
    tables: [],
    screenshots: [],
    captures: [],
    meta: {},
  },
  module: {
    nameHint: '',
    title: '',
    description: '',
    scene: '',
  },
  language: 'zh-CN',
  menus: [],
  pages: [],
  forms: [],
  relations: [],
  confidence: 0,
  issues: [],
  meta: {},
})

export default validateModuleMaterialSchema
