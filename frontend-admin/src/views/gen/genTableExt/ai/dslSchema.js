export const DSL_SCHEMA_VERSION = '1.0'

export const FIELD_TYPE_OPTIONS = [
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

export const LAYOUT_STYLE_OPTIONS = [
  'normal',
  'document-form',
  'dense',
  'approval',
]

export const LIST_ALIGN_OPTIONS = [
  '',
  'left',
  'center',
  'right',
]

export const QUERY_TYPE_OPTIONS = [
  '',
  'input',
  'yes-no',
  'select',
  'date',
  'date-range',
  'range',
  'area',
  'modal-select',
  'modal-multi-select',
  'user-select',
  'users-select',
  'office-select',
  'tree-select',
  'cascader-select',
]

export const DICTIONARY_MODE_OPTIONS = [
  '',
  'create',
  'use-existing',
  'ignore',
]

export const FIELD_TYPE_ALIAS_MAP = {
  input: 'text',
  userSelect: 'user',
  usersSelect: 'users',
  officeSelect: 'office',
  treeSelect: 'tree',
  fileupload: 'upload',
  fileuploadpic: 'imageUpload',
  fileuploadonline: 'onlineFile',
  dateselect: 'date',
  treeselectRedio: 'user',
  treeselectCheck: 'users',
  officeselectTree: 'office',
  gridselect: 'modalSelect',
}

const FIELD_NAME_PATTERN = /^[a-z][a-z0-9_]*$/
const FORM_NAME_PATTERN = /^[a-z][a-z0-9_]*$/

const isObject = (value) => value !== null && typeof value === 'object' && !Array.isArray(value)

const isEmpty = (value) => value === undefined || value === null || String(value).trim() === ''

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const isIntegerInRange = (value, min, max) => {
  const numberValue = Number(value)
  return Number.isInteger(numberValue) && numberValue >= min && numberValue <= max
}

const createIssue = ({
  id,
  level = 'error',
  title,
  fieldName = '',
  fieldLabel = '',
  description,
  suggestion,
  fixable = false,
  fix = null,
  meta = {},
}) => ({
  id,
  level,
  title,
  fieldName,
  fieldLabel,
  description,
  suggestion,
  fixable,
  fix,
  meta,
})

const toSnakeCase = (value, fallback = 'field') => {
  const text = normalizeText(value)
      .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
      .replace(/[^a-zA-Z0-9]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_+|_+$/g, '')
      .toLowerCase()
  const normalized = text || fallback
  return /^[a-z]/.test(normalized) ? normalized : `${fallback}_${normalized}`.replace(/_+/g, '_')
}

const isValidName = (name, pattern) => pattern.test(normalizeText(name))

const isKnownFieldType = (type) => {
  return FIELD_TYPE_OPTIONS.includes(type) || Boolean(FIELD_TYPE_ALIAS_MAP[type])
}

const getCanonicalFieldType = (type) => FIELD_TYPE_ALIAS_MAP[type] || type

const validateRoot = (dsl, issues) => {
  if (!isObject(dsl)) {
    issues.push(createIssue({
      id: 'schema-root-invalid',
      title: 'DSL 结构不合法',
      description: 'FormDesignDSL 必须是一个对象。',
      suggestion: '请生成包含 version、form、fields、layout、list 的 JSON 对象。',
    }))
    return false
  }

  if (isEmpty(dsl.version)) {
    issues.push(createIssue({
      id: 'schema-version-missing',
      title: 'DSL 版本缺失',
      description: 'DSL 缺少 version 字段，后续转换器无法判断结构版本。',
      suggestion: `请设置 version 为 ${DSL_SCHEMA_VERSION}。`,
      fixable: true,
      fix: {
        type: 'setDslValue',
        path: 'version',
        value: DSL_SCHEMA_VERSION,
      },
    }))
  } else if (dsl.version !== DSL_SCHEMA_VERSION) {
    issues.push(createIssue({
      id: `schema-version-unsupported:${dsl.version}`,
      title: 'DSL 版本暂不支持',
      description: `当前版本为 ${dsl.version}，本阶段只支持 ${DSL_SCHEMA_VERSION}。`,
      suggestion: `请转换为 FormDesignDSL v${DSL_SCHEMA_VERSION} 后再继续。`,
    }))
  }

  return true
}

const validateForm = (dsl, issues) => {
  const form = dsl.form
  if (!isObject(form)) {
    issues.push(createIssue({
      id: 'schema-form-missing',
      title: '表单信息缺失',
      description: 'DSL 缺少 form 对象。',
      suggestion: '请补充 form.name、form.title、form.module 等基础信息。',
    }))
    return
  }

  if (isEmpty(form.name)) {
    issues.push(createIssue({
      id: 'schema-form-name-missing',
      title: '表名缺失',
      description: 'form.name 是生成表单和接口路径的关键标识。',
      suggestion: '请补充小写下划线格式的表名，例如 contract_apply。',
    }))
  } else if (!isValidName(form.name, FORM_NAME_PATTERN)) {
    const suggestedName = toSnakeCase(form.name, 'form')
    issues.push(createIssue({
      id: `schema-form-name-invalid:${form.name}`,
      title: '表名格式不合法',
      description: 'form.name 只能使用小写字母、数字和下划线，并且必须以字母开头。',
      suggestion: `建议改为 ${suggestedName}。`,
      fixable: true,
      fix: {
        type: 'setDslValue',
        path: 'form.name',
        value: suggestedName,
      },
      meta: {
        suggestedName,
      },
    }))
  }

  if (isEmpty(form.title)) {
    issues.push(createIssue({
      id: 'schema-form-title-missing',
      title: '表单标题缺失',
      description: 'form.title 会作为设计器标题、弹窗标题或页面标题的默认来源。',
      suggestion: '请补充一个面向用户的中文标题，例如 合同审批。',
    }))
  }

  if (isEmpty(form.module)) {
    issues.push(createIssue({
      id: 'schema-form-module-missing',
      title: '模块名缺失',
      description: 'form.module 会影响生成路径、权限编码和菜单归属。',
      suggestion: '请补充模块名，例如 oas。',
    }))
  } else if (!isValidName(form.module, FORM_NAME_PATTERN)) {
    const suggestedName = toSnakeCase(form.module, 'module')
    issues.push(createIssue({
      id: `schema-form-module-invalid:${form.module}`,
      title: '模块名格式不合法',
      description: 'form.module 只能使用小写字母、数字和下划线，并且必须以字母开头。',
      suggestion: `建议改为 ${suggestedName}。`,
      fixable: true,
      fix: {
        type: 'setDslValue',
        path: 'form.module',
        value: suggestedName,
      },
      meta: {
        suggestedName,
      },
    }))
  }
}

const validateLayout = (dsl, issues) => {
  const layout = dsl.layout
  if (layout === undefined || layout === null) {
    return
  }
  if (!isObject(layout)) {
    issues.push(createIssue({
      id: 'schema-layout-invalid',
      title: '布局配置不合法',
      description: 'layout 必须是一个对象。',
      suggestion: '请将 layout 设置为对象，例如 {"style":"normal","labelWidth":100}。',
    }))
    return
  }

  if (!isEmpty(layout.style) && !LAYOUT_STYLE_OPTIONS.includes(layout.style)) {
    issues.push(createIssue({
      id: `schema-layout-style-invalid:${layout.style}`,
      level: 'warning',
      title: '布局风格不在白名单',
      description: `layout.style 当前为 ${layout.style}，暂不属于系统已知风格。`,
      suggestion: `建议使用 ${LAYOUT_STYLE_OPTIONS.join('、')} 中的一种。`,
    }))
  }

  if (!isEmpty(layout.labelWidth) && !isIntegerInRange(layout.labelWidth, 40, 240)) {
    issues.push(createIssue({
      id: `schema-layout-label-width-invalid:${layout.labelWidth}`,
      level: 'warning',
      title: 'Label 宽度不合理',
      description: 'layout.labelWidth 建议在 40 到 240 之间。',
      suggestion: '常规表单可使用 100，公文文单可根据标题长度调整。',
    }))
  }

  if (layout.groups !== undefined && !Array.isArray(layout.groups)) {
    issues.push(createIssue({
      id: 'schema-layout-groups-invalid',
      title: '分组配置不合法',
      description: 'layout.groups 必须是数组。',
      suggestion: '请使用 [{key:"base",title:"基本信息"}] 这样的结构。',
    }))
    return
  }

  toArray(layout.groups).forEach((group, index) => {
    if (!isObject(group)) {
      issues.push(createIssue({
        id: `schema-layout-group-invalid:${index}`,
        title: '分组项不合法',
        description: `第 ${index + 1} 个分组不是对象。`,
        suggestion: '每个分组都需要包含 key 和 title。',
      }))
      return
    }
    if (isEmpty(group.key)) {
      issues.push(createIssue({
        id: `schema-layout-group-key-missing:${index}`,
        title: '分组 key 缺失',
        description: `第 ${index + 1} 个分组缺少 key。`,
        suggestion: '请补充分组 key，例如 base。',
      }))
    } else if (!isValidName(group.key, FIELD_NAME_PATTERN)) {
      const suggestedName = toSnakeCase(group.key, `group_${index + 1}`)
      issues.push(createIssue({
        id: `schema-layout-group-key-invalid:${group.key}`,
        title: '分组 key 格式不合法',
        description: '分组 key 只能使用小写字母、数字和下划线，并且必须以字母开头。',
        suggestion: `建议改为 ${suggestedName}。`,
        meta: {
          suggestedName,
        },
      }))
    }
    if (isEmpty(group.title)) {
      issues.push(createIssue({
        id: `schema-layout-group-title-missing:${group.key || index}`,
        level: 'warning',
        title: '分组标题缺失',
        description: `分组 ${group.key || index + 1} 缺少 title，用户预览时不容易理解分组含义。`,
        suggestion: '请补充分组标题，例如 基本信息。',
      }))
    }
  })
}

const validateFieldList = (field, issues) => {
  if (field.list === undefined || field.list === null) {
    return
  }
  if (!isObject(field.list)) {
    issues.push(createIssue({
      id: `schema-field-list-invalid:${field.name || field.label}`,
      title: '字段列表配置不合法',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: 'field.list 必须是对象。',
      suggestion: '请将 list 设置为对象，例如 {show:true,title:"标题"}。',
    }))
    return
  }

  if (!isEmpty(field.list.align) && !LIST_ALIGN_OPTIONS.includes(field.list.align)) {
    issues.push(createIssue({
      id: `schema-field-list-align-invalid:${field.name}:${field.list.align}`,
      level: 'warning',
      title: '列表对齐方式不合法',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: `字段 ${field.name || field.label} 的 list.align 当前为 ${field.list.align}。`,
      suggestion: '请使用 left、center 或 right。',
    }))
  }

  if (!isEmpty(field.list.minWidth) && Number(field.list.minWidth) < 60) {
    issues.push(createIssue({
      id: `schema-field-list-min-width-invalid:${field.name}:${field.list.minWidth}`,
      level: 'warning',
      title: '列表列宽过小',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: 'list.minWidth 小于 60 时，列表内容容易被挤压。',
      suggestion: '建议设置为 100 到 180 之间。',
    }))
  }
}

const validateFieldQuery = (field, issues) => {
  const query = field.query || field.list?.query
  const queryFieldType = field.list?.queryFieldType
  if (query === undefined && isEmpty(queryFieldType)) {
    return
  }

  if (query !== undefined && query !== null && !isObject(query)) {
    issues.push(createIssue({
      id: `schema-field-query-invalid:${field.name || field.label}`,
      title: '字段查询配置不合法',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: 'field.query 必须是对象。',
      suggestion: '请将 query 设置为对象，例如 {enabled:true,type:"input"}。',
    }))
    return
  }

  const type = query?.type || queryFieldType || ''
  if (!isEmpty(type) && !QUERY_TYPE_OPTIONS.includes(type)) {
    issues.push(createIssue({
      id: `schema-field-query-type-invalid:${field.name}:${type}`,
      level: 'warning',
      title: '查询类型不在白名单',
      fieldName: field.name || '',
      fieldLabel: field.label || '',
      description: `字段 ${field.name || field.label} 的查询类型当前为 ${type}。`,
      suggestion: `建议使用 ${QUERY_TYPE_OPTIONS.filter(Boolean).join('、')} 中的一种。`,
    }))
  }
}

const validateFields = (dsl, issues) => {
  if (!Array.isArray(dsl.fields)) {
    issues.push(createIssue({
      id: 'schema-fields-missing',
      title: '字段列表缺失',
      description: 'DSL 缺少 fields 数组。',
      suggestion: '请至少生成一个字段配置。',
    }))
    return
  }

  if (dsl.fields.length === 0) {
    issues.push(createIssue({
      id: 'schema-fields-empty',
      title: '字段列表为空',
      description: 'fields 为空时无法生成表单。',
      suggestion: '请根据业务需求生成至少一个字段。',
    }))
    return
  }

  const nameMap = {}
  dsl.fields.forEach((field, index) => {
    if (!isObject(field)) {
      issues.push(createIssue({
        id: `schema-field-invalid:${index}`,
        title: '字段项不合法',
        description: `第 ${index + 1} 个字段不是对象。`,
        suggestion: '每个字段都需要包含 name、label、type 等属性。',
      }))
      return
    }

    const fieldName = normalizeText(field.name)
    const fieldLabel = normalizeText(field.label)

    if (isEmpty(fieldName)) {
      issues.push(createIssue({
        id: `schema-field-name-missing:${index}`,
        title: '字段名缺失',
        fieldLabel,
        description: `第 ${index + 1} 个字段缺少 name。`,
        suggestion: `请补充小写下划线格式字段名，例如 ${toSnakeCase(fieldLabel || `field_${index + 1}`)}。`,
      }))
    } else {
      if (!isValidName(fieldName, FIELD_NAME_PATTERN)) {
        const suggestedName = toSnakeCase(fieldName || fieldLabel, `field_${index + 1}`)
        issues.push(createIssue({
          id: `schema-field-name-invalid:${fieldName}`,
          title: '字段名格式不合法',
          fieldName,
          fieldLabel,
          description: '字段名只能使用小写字母、数字和下划线，并且必须以字母开头。',
          suggestion: `建议改为 ${suggestedName}。`,
          fixable: true,
          fix: {
            type: 'setDslFieldValue',
            fieldName,
            path: 'name',
            value: suggestedName,
          },
          meta: {
            suggestedName,
          },
        }))
      }

      if (!nameMap[fieldName]) {
        nameMap[fieldName] = []
      }
      nameMap[fieldName].push({
        index,
        label: fieldLabel,
      })
    }

    if (isEmpty(fieldLabel)) {
      issues.push(createIssue({
        id: `schema-field-label-missing:${fieldName || index}`,
        title: '字段标题缺失',
        fieldName,
        description: `字段 ${fieldName || index + 1} 缺少 label。`,
        suggestion: '请补充面向用户展示的字段标题。',
      }))
    }

    if (isEmpty(field.type)) {
      issues.push(createIssue({
        id: `schema-field-type-missing:${fieldName || index}`,
        title: '字段类型缺失',
        fieldName,
        fieldLabel,
        description: `字段 ${fieldLabel || fieldName || index + 1} 缺少 type。`,
        suggestion: `请设置字段类型，可选值包括 ${FIELD_TYPE_OPTIONS.join('、')}。`,
      }))
    } else if (!isKnownFieldType(field.type)) {
      issues.push(createIssue({
        id: `schema-field-type-invalid:${fieldName}:${field.type}`,
        title: '字段类型不在白名单',
        fieldName,
        fieldLabel,
        description: `字段 ${fieldLabel || fieldName} 的 type 当前为 ${field.type}。`,
        suggestion: `请使用 ${FIELD_TYPE_OPTIONS.join('、')} 中的一种。`,
        meta: {
          validTypes: FIELD_TYPE_OPTIONS,
        },
      }))
    } else if (FIELD_TYPE_ALIAS_MAP[field.type]) {
      issues.push(createIssue({
        id: `schema-field-type-alias:${fieldName}:${field.type}`,
        level: 'suggestion',
        title: '字段类型建议使用 DSL 标准名称',
        fieldName,
        fieldLabel,
        description: `字段 ${fieldLabel || fieldName} 当前使用设计器兼容类型 ${field.type}。`,
        suggestion: `建议在 DSL 草稿中使用 ${getCanonicalFieldType(field.type)}。`,
        fixable: true,
        fix: {
          type: 'setDslFieldValue',
          fieldName,
          path: 'type',
          value: getCanonicalFieldType(field.type),
        },
        meta: {
          aliasType: field.type,
          canonicalType: getCanonicalFieldType(field.type),
        },
      }))
    }

    if (!isEmpty(field.span) && !isIntegerInRange(field.span, 1, 24)) {
      issues.push(createIssue({
        id: `schema-field-span-invalid:${fieldName}:${field.span}`,
        title: '字段宽度不合法',
        fieldName,
        fieldLabel,
        description: '字段 span 必须是 1 到 24 之间的整数。',
        suggestion: '常规半行字段建议设置为 12，整行字段建议设置为 24。',
        fixable: true,
        fix: {
          type: 'setDslFieldValue',
          fieldName,
          path: 'span',
          value: Number(field.span) > 24 ? 24 : 12,
        },
      }))
    }

    if (!isEmpty(field.group) && !isValidName(field.group, FIELD_NAME_PATTERN)) {
      const suggestedName = toSnakeCase(field.group, 'group')
      issues.push(createIssue({
        id: `schema-field-group-invalid:${fieldName}:${field.group}`,
        level: 'warning',
        title: '字段分组 key 不合法',
        fieldName,
        fieldLabel,
        description: '字段 group 只能使用小写字母、数字和下划线，并且必须以字母开头。',
        suggestion: `建议改为 ${suggestedName}。`,
        meta: {
          suggestedName,
        },
      }))
    }

    validateFieldList(field, issues)
    validateFieldQuery(field, issues)
  })

  Object.keys(nameMap)
      .filter(name => nameMap[name].length > 1)
      .forEach(name => {
        issues.push(createIssue({
          id: `schema-field-name-duplicate:${name}`,
          title: '字段名重复',
          fieldName: name,
          fieldLabel: nameMap[name][0].label,
          description: `字段名 ${name} 在草稿中出现了 ${nameMap[name].length} 次。`,
          suggestion: '请为重复字段分配唯一 name，避免保存或渲染时互相覆盖。',
          meta: {
            fields: nameMap[name],
          },
        }))
      })
}

const validateList = (dsl, issues) => {
  const list = dsl.list
  if (list === undefined || list === null) {
    return
  }
  if (!isObject(list)) {
    issues.push(createIssue({
      id: 'schema-list-invalid',
      title: '列表配置不合法',
      description: 'list 必须是一个对象。',
      suggestion: '请将 list 设置为对象，例如 {buttons:[],rowButtons:[],queryArea:{}}。',
    }))
    return
  }

  if (list.buttons !== undefined && !Array.isArray(list.buttons)) {
    issues.push(createIssue({
      id: 'schema-list-buttons-invalid',
      title: '列表按钮配置不合法',
      description: 'list.buttons 必须是数组。',
      suggestion: '请使用 ["add","batch-delete","export"] 这样的结构。',
    }))
  }

  if (list.rowButtons !== undefined && !Array.isArray(list.rowButtons)) {
    issues.push(createIssue({
      id: 'schema-list-row-buttons-invalid',
      title: '行按钮配置不合法',
      description: 'list.rowButtons 必须是数组。',
      suggestion: '请使用 ["view","edit"] 这样的结构。',
    }))
  }

  if (list.queryArea !== undefined && !isObject(list.queryArea)) {
    issues.push(createIssue({
      id: 'schema-list-query-area-invalid',
      title: '查询区配置不合法',
      description: 'list.queryArea 必须是对象。',
      suggestion: '请使用 {labelWidth:80} 这样的结构。',
    }))
  }
}

const validateDictionaries = (dsl, issues) => {
  if (dsl.dictionaries === undefined || dsl.dictionaries === null) {
    return
  }
  if (!Array.isArray(dsl.dictionaries)) {
    issues.push(createIssue({
      id: 'schema-dictionaries-invalid',
      title: '字典建议配置不合法',
      description: 'dictionaries 必须是数组。',
      suggestion: '请使用 [{fieldName:"status",code:"表名_字段名",name:"表单-字段",mode:"create",items:[]}] 这样的结构。',
    }))
    return
  }

  dsl.dictionaries.forEach((dictionary, index) => {
    if (!isObject(dictionary)) {
      issues.push(createIssue({
        id: `schema-dictionary-invalid:${index}`,
        title: '字典建议项不合法',
        description: `第 ${index + 1} 个字典建议不是对象。`,
        suggestion: '每个字典建议至少需要包含 fieldName、code 和 name。',
      }))
      return
    }

    const fieldName = normalizeText(dictionary.fieldName)
    const code = normalizeText(dictionary.code || dictionary.existingCode)
    const mode = normalizeText(dictionary.mode)

    if (isEmpty(fieldName)) {
      issues.push(createIssue({
        id: `schema-dictionary-field-missing:${index}`,
        level: 'warning',
        title: '字典建议缺少字段名',
        description: '字典建议需要通过 fieldName 关联到具体字段。',
        suggestion: '请补充 fieldName，或删除无法关联的字典建议。',
      }))
    } else if (!isValidName(fieldName, FIELD_NAME_PATTERN)) {
      issues.push(createIssue({
        id: `schema-dictionary-field-invalid:${fieldName}`,
        level: 'warning',
        title: '字典建议字段名格式不合法',
        fieldName,
        description: 'dictionary.fieldName 必须是小写下划线格式。',
        suggestion: '请使用与 fields[].name 一致的字段名。',
      }))
    }

    if (mode && !DICTIONARY_MODE_OPTIONS.includes(mode)) {
      issues.push(createIssue({
        id: `schema-dictionary-mode-invalid:${fieldName || index}:${mode}`,
        level: 'warning',
        title: '字典处理方式不支持',
        fieldName,
        description: `mode 当前为 ${mode}。`,
        suggestion: '请使用 create、use-existing 或 ignore。',
      }))
    }

    if (mode !== 'ignore' && isEmpty(code)) {
      issues.push(createIssue({
        id: `schema-dictionary-code-missing:${fieldName || index}`,
        level: 'warning',
        title: '字典编码缺失',
        fieldName,
        description: '新建或使用已有字典时需要明确字典编码。',
        suggestion: '建议使用 表名_字段名 作为默认编码。',
      }))
    }

    if (dictionary.items !== undefined && !Array.isArray(dictionary.items)) {
      issues.push(createIssue({
        id: `schema-dictionary-items-invalid:${fieldName || index}`,
        level: 'warning',
        title: '字典项结构不合法',
        fieldName,
        description: 'dictionary.items 必须是数组。',
        suggestion: '请使用 [{value:"normal",text:"普通"}] 这样的结构。',
      }))
      return
    }

    toArray(dictionary.items).forEach((item, itemIndex) => {
      if (!isObject(item)) {
        issues.push(createIssue({
          id: `schema-dictionary-item-invalid:${fieldName || index}:${itemIndex}`,
          level: 'warning',
          title: '字典项不合法',
          fieldName,
          description: `第 ${itemIndex + 1} 个字典项不是对象。`,
          suggestion: '每个字典项需要包含 value 和 text。',
        }))
        return
      }
      if (isEmpty(item.value) || isEmpty(item.text)) {
        issues.push(createIssue({
          id: `schema-dictionary-item-value-missing:${fieldName || index}:${itemIndex}`,
          level: 'warning',
          title: '字典项缺少编码或名称',
          fieldName,
          description: '字典项需要同时包含 value 和 text。',
          suggestion: '请补齐字典项，或在字典确认区人工补充。',
        }))
      }
    })
  })
}

export const validateFormDesignDslSchema = (dsl = {}) => {
  const issues = []
  if (!validateRoot(dsl, issues)) {
    return issues
  }

  validateForm(dsl, issues)
  validateLayout(dsl, issues)
  validateFields(dsl, issues)
  validateList(dsl, issues)
  validateDictionaries(dsl, issues)

  return issues
}

export const isValidFormDesignDslSchema = (dsl = {}) => {
  return validateFormDesignDslSchema(dsl).every(issue => issue.level !== 'error')
}

export const summarizeSchemaIssues = (issues = []) => {
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

export const suggestDslName = (value, fallback = 'field') => toSnakeCase(value, fallback)

export default validateFormDesignDslSchema
