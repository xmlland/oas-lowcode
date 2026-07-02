export const DICTIONARY_FIELD_TYPES = ['select', 'radio', 'checkbox']

export const DICTIONARY_CONFIRM_MODES = [
  'create',
  'use-existing',
  'ignore',
]

const DEFAULT_DICT_PARENT_CODE = 'data-params'

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const hasCjkText = (value = '') => /[\u3400-\u9fff]/.test(normalizeText(value))

const isLikelyMachineCode = (value = '') => {
  const text = normalizeText(value)
  return /^[a-z][a-z0-9_]*$/.test(text) && (text.includes('_') || text.length > 24)
}

const clonePlain = (value) => {
  if (value === undefined || value === null) {
    return value
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch (error) {
    return value
  }
}

const toSnakePart = (value, fallback = 'dict') => {
  const text = normalizeText(value)
      .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
      .replace(/[^a-zA-Z0-9_]+/g, '_')
      .replace(/_+/g, '_')
      .replace(/^_+|_+$/g, '')
      .toLowerCase()
  const normalized = text || fallback
  return /^[a-z]/.test(normalized) ? normalized : `${fallback}_${normalized}`.replace(/_+/g, '_')
}

const normalizeMode = (value = '') => {
  const mode = normalizeText(value)
  return DICTIONARY_CONFIRM_MODES.includes(mode) ? mode : 'create'
}

const normalizeDictionaryItem = (item = {}) => {
  if (typeof item === 'string') {
    const line = normalizeText(item)
    if (!line) {
      return null
    }
    if (line.indexOf('=') > 0) {
      const [value, ...textParts] = line.split('=')
      return {
        value: normalizeText(value),
        text: normalizeText(textParts.join('=')) || normalizeText(value),
        textEn: normalizeText(value),
      }
    }
    const parts = line.split(/\s+/).filter(Boolean)
    if (parts.length === 1) {
      return {
        value: parts[0],
        text: parts[0],
        textEn: parts[0],
      }
    }
    return {
      value: parts[0],
      text: parts.slice(1).join(' '),
      textEn: parts[0],
    }
  }

  if (!item || typeof item !== 'object') {
    return null
  }
  const value = normalizeText(item.value || item.code || item.key)
  const text = normalizeText(item.text || item.label || item.name || item.title)
  const textEn = normalizeText(item.textEn || item.nameEn || item.name_en || item.labelEn || item.en || value)
  if (!value && !text) {
    return null
  }
  return {
    value: value || text,
    text: text || value,
    textEn: textEn || value || text,
  }
}

export const normalizeDictionaryItems = (items = []) => {
  const seen = {}
  return toArray(items)
      .map(normalizeDictionaryItem)
      .filter(Boolean)
      .filter(item => {
        const key = `${item.value}::${item.text}`
        if (seen[key]) {
          return false
        }
        seen[key] = true
        return true
      })
}

export const parseDictionaryItemsText = (text = '') => {
  return normalizeDictionaryItems(
      String(text || '')
          .split(/\r?\n/)
          .map(line => normalizeText(line))
          .filter(Boolean),
  )
}

export const dictionaryItemsToText = (items = []) => {
  return normalizeDictionaryItems(items)
      .map(item => `${item.value} ${item.text}`.trim())
      .join('\n')
}

export const isDictionaryField = (field = {}) => {
  return DICTIONARY_FIELD_TYPES.includes(normalizeText(field.type))
      || Boolean(normalizeText(field.dictType))
      || Boolean(normalizeText(field.list?.dict))
}

export const createDictionaryCode = (formName = '', fieldName = '') => {
  const formPart = toSnakePart(formName, 'form')
  const fieldPart = toSnakePart(fieldName, 'field')
  return `${formPart}_${fieldPart}`.replace(/_+/g, '_')
}

export const createDictionaryName = (formTitle = '', fieldLabel = '') => {
  const title = normalizeText(formTitle) || 'AI生成表单'
  const label = normalizeText(fieldLabel) || '字典字段'
  return `${title}-${label}`
}

export const createDictionaryDisplayName = (suggestion = {}) => {
  const code = normalizeText(suggestion.code || suggestion.existingCode || suggestion.dictType)
  const rawName = normalizeText(suggestion.name || suggestion.title)
  if (rawName && (hasCjkText(rawName) || !isLikelyMachineCode(rawName))) {
    return rawName
  }
  const fieldLabel = normalizeText(suggestion.fieldLabel || suggestion.label)
  if (fieldLabel) {
    return createDictionaryName(suggestion.formTitle, fieldLabel)
  }
  return rawName || code
}

export const createDictionaryEnglishName = (suggestion = {}, fallbackCode = '') => {
  const explicitName = normalizeText(suggestion.nameEn || suggestion.name_en || suggestion.englishName || suggestion.titleEn)
  if (explicitName) {
    return explicitName
  }
  const code = normalizeText(fallbackCode || suggestion.code || suggestion.existingCode || suggestion.dictType)
  const rawName = normalizeText(suggestion.name || suggestion.title)
  if (rawName && !hasCjkText(rawName) && !isLikelyMachineCode(rawName)) {
    return rawName
  }
  return code || rawName
}

const getFieldExplicitDictionaryCode = (field = {}) => {
  const fieldName = normalizeText(field.name)
  const code = normalizeText(field.dictType || field.list?.dict || field.form?.controlProps?.dictType)
  if (!code || code === fieldName) {
    return ''
  }
  return code
}

const getFieldDictionaryItems = (field = {}) => {
  return normalizeDictionaryItems(
      field.dictionaryItems
      || field.dictItems
      || field.options
      || field.optionItems
      || field.enumItems
      || field.form?.controlProps?.options
      || [],
  )
}

const normalizeDictionaryCandidate = (candidate = {}, dsl = {}) => {
  const form = dsl.form || {}
  const fieldName = normalizeText(candidate.fieldName || candidate.field || candidate.fieldCode)
  const fieldLabel = normalizeText(candidate.fieldLabel || candidate.label)
  return {
    id: fieldName || normalizeText(candidate.code),
    fieldName,
    fieldLabel,
    fieldType: normalizeText(candidate.fieldType || candidate.type),
    code: normalizeText(candidate.code || candidate.dictType || candidate.existingCode),
    name: normalizeText(candidate.name || candidate.title),
    nameEn: normalizeText(candidate.nameEn || candidate.name_en || candidate.englishName || candidate.titleEn),
    mode: normalizeMode(candidate.mode || candidate.action),
    existingCode: normalizeText(candidate.existingCode),
    items: normalizeDictionaryItems(candidate.items || candidate.options || candidate.dictItems || candidate.children || []),
    note: normalizeText(candidate.note || candidate.reason),
    source: normalizeText(candidate.source || 'ai'),
    created: candidate.created === true,
    sort: candidate.sort || 10,
    formName: normalizeText(form.name),
    formTitle: normalizeText(form.title),
  }
}

const findCandidateForField = (candidates = [], field = {}) => {
  const fieldName = normalizeText(field.name)
  const explicitCode = getFieldExplicitDictionaryCode(field)
  return candidates.find(candidate => {
    return (candidate.fieldName && candidate.fieldName === fieldName)
        || (explicitCode && candidate.code === explicitCode)
  }) || null
}

export const buildDictionarySuggestionsFromDsl = (dsl = {}) => {
  const form = dsl.form || {}
  const formName = normalizeText(form.name)
  const formTitle = normalizeText(form.title)
  const candidates = toArray(dsl.dictionaries).map(candidate => normalizeDictionaryCandidate(candidate, dsl))

  return toArray(dsl.fields)
      .filter(isDictionaryField)
      .map(field => {
        const fieldName = normalizeText(field.name)
        const fieldLabel = normalizeText(field.label)
        const matched = findCandidateForField(candidates, field)
        const code = normalizeText(matched?.code)
            || getFieldExplicitDictionaryCode(field)
            || createDictionaryCode(formName, fieldName)
        const name = createDictionaryDisplayName({
          ...matched,
          code,
          formTitle,
          fieldLabel,
        })
        const nameEn = createDictionaryEnglishName({
          ...matched,
          code,
          name,
        }, code)
        const items = normalizeDictionaryItems(matched?.items?.length ? matched.items : getFieldDictionaryItems(field))
        const matchedMode = normalizeMode(matched?.mode)
        return {
          id: fieldName,
          fieldName,
          fieldLabel,
          fieldType: normalizeText(field.type),
          code,
          name,
          nameEn,
          mode: matchedMode,
          existingCode: normalizeText(matched?.existingCode || (matchedMode === 'use-existing' ? code : '')),
          items,
          itemsText: dictionaryItemsToText(items),
          note: matched?.note || '',
          source: matched?.source || (matched ? 'ai' : 'local'),
          created: matched?.created === true,
          sort: matched?.sort || 10,
          formName,
          formTitle,
        }
      })
}

export const createDictionaryConfirmationMap = (dsl = {}) => {
  return buildDictionarySuggestionsFromDsl(dsl).reduce((map, suggestion) => {
    map[suggestion.fieldName] = {
      ...suggestion,
      itemsText: suggestion.itemsText || dictionaryItemsToText(suggestion.items),
      applied: false,
    }
    return map
  }, {})
}

const normalizeConfirmation = (confirmation = {}, dsl = {}, field = {}) => {
  const base = buildDictionarySuggestionsFromDsl({
    ...dsl,
    fields: [field],
  })[0] || {}
  const merged = {
    ...base,
    ...confirmation,
  }
  const items = confirmation.itemsText !== undefined
      ? parseDictionaryItemsText(confirmation.itemsText)
      : normalizeDictionaryItems(merged.items)
  return {
    ...merged,
    fieldName: normalizeText(merged.fieldName || field.name),
    fieldLabel: normalizeText(merged.fieldLabel || field.label),
    fieldType: normalizeText(merged.fieldType || field.type),
    mode: normalizeMode(merged.mode),
    code: normalizeText(merged.code || base.code),
    name: normalizeText(merged.name || base.name),
    nameEn: normalizeText(merged.nameEn || merged.name_en || base.nameEn),
    existingCode: normalizeText(merged.existingCode),
    items,
    itemsText: dictionaryItemsToText(items),
  }
}

export const applyDictionaryConfirmationsToDsl = (dsl = {}, confirmations = {}) => {
  const nextDsl = clonePlain(dsl) || {}
  const confirmationList = Array.isArray(confirmations)
      ? confirmations
      : Object.values(confirmations || {})
  const confirmationMap = confirmationList.reduce((map, item) => {
    const fieldName = normalizeText(item.fieldName || item.id)
    if (fieldName) {
      map[fieldName] = item
    }
    return map
  }, {})
  const dictionaries = []

  nextDsl.fields = toArray(nextDsl.fields).map(field => {
    if (!isDictionaryField(field)) {
      return field
    }

    const nextField = {
      ...field,
      list: {
        ...(field.list || {}),
      },
    }
    const confirmation = normalizeConfirmation(confirmationMap[normalizeText(field.name)], nextDsl, field)
    const effectiveCode = confirmation.mode === 'use-existing'
        ? normalizeText(confirmation.existingCode)
        : normalizeText(confirmation.code)

    if (confirmation.mode === 'ignore') {
      nextField.dictType = ''
      nextField.list.dict = ''
    } else {
      nextField.dictType = effectiveCode
      nextField.list.dict = effectiveCode
      if (nextField.form?.controlProps) {
        nextField.form.controlProps.dictType = effectiveCode
      }
    }

    dictionaries.push({
      fieldName: confirmation.fieldName,
      fieldLabel: confirmation.fieldLabel,
      fieldType: confirmation.fieldType,
      code: effectiveCode,
      name: confirmation.name,
      nameEn: confirmation.nameEn,
      mode: confirmation.mode,
      existingCode: confirmation.mode === 'use-existing' ? effectiveCode : '',
      items: confirmation.items,
      note: confirmation.note || '',
      source: confirmation.source || 'user-confirmed',
      created: confirmation.created === true,
      sort: confirmation.sort || 10,
    })

    return nextField
  })

  nextDsl.dictionaries = dictionaries
  return nextDsl
}

export const ensureDslDictionarySuggestions = (dsl = {}) => {
  const confirmations = createDictionaryConfirmationMap(dsl)
  return applyDictionaryConfirmationsToDsl(dsl, confirmations)
}

export const buildSysDictionarySaveData = (suggestion = {}) => {
  const code = normalizeText(suggestion.code || suggestion.existingCode)
  const name = createDictionaryDisplayName({
    ...suggestion,
    code,
  })
  const nameEn = createDictionaryEnglishName({
    ...suggestion,
    name,
    code,
  }, code)
  const items = suggestion.itemsText !== undefined
      ? parseDictionaryItemsText(suggestion.itemsText)
      : normalizeDictionaryItems(suggestion.items)

  return {
    parent: {
      id: DEFAULT_DICT_PARENT_CODE,
    },
    parent_code: DEFAULT_DICT_PARENT_CODE,
    view_flag: '1',
    edit_flag: '1',
    code,
    name,
    name_en: nameEn,
    sort: suggestion.sort || 10,
    sys_dictionary: items.map((item, index) => ({
      view_flag: '1',
      edit_flag: '1',
      sort: (index + 1) * 10,
      parent_code: code,
      formNo: 'sys_dictionary',
      code: item.value,
      name: item.text,
      name_en: normalizeText(item.textEn || item.nameEn || item.name_en || item.value || item.text),
    })),
  }
}
