import {listDataAction} from "@/api/api";

const DEFAULT_DICT_PARENT_CODE = 'data-params'

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

let cachedDictionaryCodeMap = {}

const normalizeCodeList = (codes = []) => Array.from(new Set(
  toArray(codes)
      .map(code => normalizeText(code))
      .filter(Boolean),
))

const pickCodeMap = (codeMap = {}, codes = []) => {
  const normalizedCodes = normalizeCodeList(codes)
  if (normalizedCodes.length === 0) {
    return {...codeMap}
  }
  return normalizedCodes.reduce((map, code) => {
    if (codeMap[code]) {
      map[code] = codeMap[code]
    }
    return map
  }, {})
}

const extractRows = (response = {}) => {
  if (!response) {
    return []
  }
  if (Array.isArray(response)) {
    return response
  }
  if (Array.isArray(response.rows)) {
    return response.rows
  }
  if (Array.isArray(response.data?.rows)) {
    return response.data.rows
  }
  if (Array.isArray(response.data?.data)) {
    return response.data.data
  }
  if (Array.isArray(response.data?.list)) {
    return response.data.list
  }
  if (Array.isArray(response.data?.records)) {
    return response.data.records
  }
  if (Array.isArray(response.data?.data?.rows)) {
    return response.data.data.rows
  }
  if (Array.isArray(response.data?.data?.list)) {
    return response.data.data.list
  }
  if (Array.isArray(response.data?.data?.records)) {
    return response.data.data.records
  }
  return []
}

const isDictionaryCategoryRow = (row = {}) => {
  const parentCode = normalizeText(row.parent_code || row.parentCode || row.parent?.code || row.parent?.id)
  return !parentCode || parentCode === DEFAULT_DICT_PARENT_CODE
}

export const buildSystemDictionaryCodeMap = (rows = []) => {
  return extractRows(rows).reduce((map, row) => {
    const code = normalizeText(row.code)
    if (code && isDictionaryCategoryRow(row)) {
      map[code] = row
    }
    return map
  }, {})
}

export const clearSystemDictionaryCodeMapCache = () => {
  cachedDictionaryCodeMap = {}
}

export const collectDictionarySuggestionCodes = (rows = []) => {
  return normalizeCodeList(toArray(rows).map(row => row?.code || row?.existingCode))
}

export const loadSystemDictionaryCodeMap = async (codes = [], options = {}) => {
  const normalizedCodes = normalizeCodeList(codes)
  if (normalizedCodes.length === 0) {
    return {}
  }

  const cachedMatched = pickCodeMap(cachedDictionaryCodeMap, normalizedCodes)
  if (!options.force && Object.keys(cachedMatched).length === normalizedCodes.length) {
    return cachedMatched
  }

  const queryCodes = options.force ? normalizedCodes : normalizedCodes.filter(code => !cachedDictionaryCodeMap[code])
  if (queryCodes.length === 0) {
    return cachedMatched
  }

  const responses = await Promise.all(queryCodes.map(code => listDataAction('sys_dictionary', {
    filterDataArr: [
      {key: 'a.parent_code', type: 'eq', value: DEFAULT_DICT_PARENT_CODE},
      {key: 'a.code', type: 'eq', value: code},
    ],
    pageParam: {
      pageNo: 1,
      pageSize: 20,
    },
  }, DEFAULT_DICT_PARENT_CODE).catch(error => {
    console.warn('SystemDictionaryCodeLookupFailed', code, error)
    return null
  })))
  const nextCodeMap = responses.reduce((map, response) => ({
    ...map,
    ...buildSystemDictionaryCodeMap(response),
  }), {})
  if (options.force) {
    const refreshedCodeMap = {
      ...cachedDictionaryCodeMap,
    }
    queryCodes.forEach(code => {
      delete refreshedCodeMap[code]
    })
    cachedDictionaryCodeMap = {
      ...refreshedCodeMap,
      ...nextCodeMap,
    }
  } else {
    cachedDictionaryCodeMap = {
      ...cachedDictionaryCodeMap,
      ...nextCodeMap,
    }
  }
  return pickCodeMap(cachedDictionaryCodeMap, normalizedCodes)
}

export const applyExistingDictionaryMatch = (row = {}, codeMap = {}, options = {}) => {
  const code = normalizeText(row.code || row.existingCode)
  const matchedDictionary = codeMap[code]
  if (!code || !matchedDictionary) {
    return row
  }
  if (row.userEdited === true && options.forceUserEdited !== true) {
    return row
  }
  const mode = normalizeText(row.mode)
  if (mode === 'ignore') {
    return row
  }
  return {
    ...row,
    mode: 'use-existing',
    code,
    existingCode: code,
    existingDictionaryId: matchedDictionary.id || row.existingDictionaryId || '',
    existingDictionaryName: matchedDictionary.name || row.existingDictionaryName || '',
    autoMatchedExistingDictionary: true,
  }
}

export const applyExistingDictionaryMatchesToMap = (confirmationMap = {}, codeMap = {}, options = {}) => {
  return Object.entries(confirmationMap || {}).reduce((nextMap, [key, row]) => {
    nextMap[key] = applyExistingDictionaryMatch(row, codeMap, options)
    return nextMap
  }, {})
}
