import {postAction} from "@/api/action";
import {
  summarizeFormMaterialIssues,
  validateFormMaterialSchema,
} from "@/views/gen/genTableExt/ai/formMaterialSchema";
import {normalizeFormMaterialSuggestions} from "@/views/gen/genTableExt/ai/formMaterialQualityRules";

const API_PREFIX = 'gen/aiFormDesign'

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const parseMaterialJson = (materialJson) => {
  if (typeof materialJson !== 'string' || !materialJson.trim()) {
    throw new Error('远程 AI 未返回 FormMaterial JSON')
  }
  try {
    return JSON.parse(materialJson)
  } catch (error) {
    throw new Error(`远程 AI FormMaterial JSON 解析失败：${error.message}`)
  }
}

const normalizeIssue = (issue = {}, index = 0, source = 'remote') => ({
  id: issue.id || issue.code || `${source}-${index + 1}`,
  code: issue.code || issue.id || `${source}-${index + 1}`,
  level: issue.level || 'warning',
  title: issue.title || '远程 AI 识别提示',
  description: issue.description || issue.suggestion || '',
  suggestion: issue.suggestion || '',
  fieldId: issue.fieldId || '',
  fieldLabel: issue.fieldLabel || '',
  fixable: issue.fixable === true,
  meta: issue.meta || {},
})

const normalizeRemoteIssues = (issues = []) => {
  return toArray(issues).map((issue, index) => normalizeIssue(issue, index, 'remote-material'))
}

const remoteMaterialErrorTextMap = {
  AI_OCR_NOT_CONFIGURED: '当前环境没有可用的图片识别能力，请在系统设置中切换到支持视觉的 AI 模型后重试',
  AI_OCR_PROVIDER_UNAVAILABLE: '图片识别 Provider 不可用，请确认 AI 模型是否支持图片输入',
  AI_OCR_FAILED: '图片 OCR 识别失败，请检查图片清晰度或稍后重试',
  AI_OCR_LOW_CONFIDENCE: '图片 OCR 识别置信度较低，请更换更清晰的图片',
  AI_FILE_TOO_LARGE: '文件过大，请压缩后再上传',
  AI_FILE_TYPE_UNSUPPORTED: '当前文件类型暂不支持',
  AI_FILE_TEXT_EMPTY: '文件中没有可读取文字，扫描 PDF 或图片需要 OCR 能力',
}

const createRemoteMaterialError = (result = {}) => {
  const errorCode = result.errorCode || 'AI_REMOTE_MATERIAL_FAILED'
  const error = new Error(result.message || result.errorCode || '远程 AI 材料识别失败')
  if (remoteMaterialErrorTextMap[errorCode]) {
    const detailMessage = normalizeText(result.message)
    error.message = detailMessage && detailMessage !== errorCode
        ? `${remoteMaterialErrorTextMap[errorCode]} - ${detailMessage}`
        : remoteMaterialErrorTextMap[errorCode]
  }
  error.code = errorCode
  error.requestId = result.requestId || ''
  error.result = result
  return error
}

const readFileAsBase64 = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => {
    const dataUrl = String(reader.result || '')
    const commaIndex = dataUrl.indexOf(',')
    resolve(commaIndex >= 0 ? dataUrl.substring(commaIndex + 1) : dataUrl)
  }
  reader.onerror = () => reject(reader.error || new Error('文件读取失败'))
  reader.readAsDataURL(file)
})

const buildCurrentFormPayload = (target = {}) => ({
  genTableId: target.genTableId || '',
  formName: target.formName || '',
  formTitle: target.formTitle || '',
  moduleName: target.module || '',
})

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

const compactMaterialForRevision = (material = {}) => {
  const compact = clonePlain(material) || {}
  if (Array.isArray(compact.tables)) {
    compact.tables = compact.tables.map(table => ({
      ...table,
      rows: Array.isArray(table.rows) ? table.rows.slice(0, 8) : [],
    }))
  }
  if (compact.rawText && String(compact.rawText).length > 6000) {
    compact.rawText = `${String(compact.rawText).slice(0, 6000)}\n...`
  }
  return compact
}

const buildMaterialRevisionPrompt = ({
  material = {},
  instruction = '',
  sourceContent = '',
}) => {
  const compactMaterial = compactMaterialForRevision(material)
  return [
    '请基于当前 FormMaterial 进行二次修正，并返回修正后的完整 FormMaterial JSON。',
    '要求：',
    normalizeText(instruction),
    '',
    '当前 FormMaterial：',
    JSON.stringify(compactMaterial, null, 2),
    '',
    '原始材料：',
    normalizeText(sourceContent || material.rawText || ''),
    '',
    '只输出修正后的完整 FormMaterial JSON，不要输出解释。',
  ].join('\n')
}

const normalizeMaterialResult = (result = {}, sourceContent = '') => {
  if (!result.success && !result.materialJson) {
    throw createRemoteMaterialError(result)
  }

  const material = parseMaterialJson(result.materialJson)
  material.rawText = material.rawText || sourceContent
  material.meta = {
    ...(material.meta || {}),
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    requestId: result.requestId || '',
    elapsedMs: result.elapsedMs || 0,
  }
  normalizeFormMaterialSuggestions(material)

  const materialIssues = toArray(material.issues).map((issue, index) => normalizeIssue(issue, index, 'ai-material'))
  const parseIssues = normalizeRemoteIssues(result.issues).concat(materialIssues)
  const schemaIssues = validateFormMaterialSchema(material)
  const summary = summarizeFormMaterialIssues(parseIssues.concat(schemaIssues))

  return {
    material,
    parseIssues,
    schemaIssues,
    summary,
    remoteResult: result,
    success: result.success === true,
    errorCode: result.errorCode || '',
    message: result.message || '',
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    requestId: result.requestId || '',
    elapsedMs: result.elapsedMs || 0,
    extraction: result.extraction || null,
    rawOutputPreview: result.rawOutputPreview || '',
  }
}

export const recognizeRemoteFormMaterial = async (rawText = '', options = {}) => {
  const sourceContent = normalizeText(rawText)
  if (!sourceContent) {
    throw new Error('请先粘贴材料文本')
  }

  const response = await postAction(`${API_PREFIX}/recognizeMaterial`, {
    rawText: sourceContent,
    sourceType: options.sourceType || 'table-text',
    scene: options.scene || 'auto',
    title: options.title || '',
    currentForm: buildCurrentFormPayload(options.target),
    options: {
      temperature: options.temperature ?? 0.1,
    },
  })

  const result = response.data?.result || {}
  return normalizeMaterialResult(result, sourceContent)
}

export const reviseRemoteFormMaterial = async (material = {}, instruction = '', options = {}) => {
  const revisionInstruction = normalizeText(instruction)
  if (!material || !Array.isArray(material.fields) || material.fields.length === 0) {
    throw new Error('请先完成材料识别')
  }
  if (!revisionInstruction) {
    throw new Error('请先输入修正要求')
  }

  const sourceContent = normalizeText(options.sourceContent || material.rawText || '')
  const rawText = buildMaterialRevisionPrompt({
    material,
    instruction: revisionInstruction,
    sourceContent,
  })

  const response = await postAction(`${API_PREFIX}/recognizeMaterial`, {
    rawText,
    sourceType: 'text',
    scene: options.scene || material.scene || 'auto',
    title: options.title || material.title || '',
    currentForm: buildCurrentFormPayload(options.target),
    options: {
      temperature: options.temperature ?? 0.1,
      revision: true,
    },
  })

  const result = response.data?.result || {}
  return normalizeMaterialResult(result, sourceContent || rawText)
}

export const recognizeExcelFormMaterial = async (file, options = {}) => {
  const rawFile = file?.originFileObj || file
  if (!rawFile) {
    throw new Error('请先选择 Excel 文件')
  }
  const fileBase64 = await readFileAsBase64(rawFile)
  const response = await postAction(`${API_PREFIX}/parseExcelMaterial`, {
    fileName: rawFile.name || file?.name || '',
    fileBase64,
    scene: options.scene || 'auto',
    title: options.title || '',
    currentForm: buildCurrentFormPayload(options.target),
    options: {
      temperature: options.temperature ?? 0.1,
      sheetIndex: options.sheetIndex,
      maxRows: options.maxRows || 80,
      maxColumns: options.maxColumns || 60,
    },
  })

  const result = response.data?.result || {}
  return normalizeMaterialResult(result)
}

export const recognizeFileFormMaterial = async (file, options = {}) => {
  const rawFile = file?.originFileObj || file
  if (!rawFile) {
    throw new Error('请先选择文件')
  }
  const fileBase64 = await readFileAsBase64(rawFile)
  const response = await postAction(`${API_PREFIX}/parseFileMaterial`, {
    fileName: rawFile.name || file?.name || '',
    mimeType: rawFile.type || file?.type || '',
    sourceType: options.sourceType || 'auto',
    fileBase64,
    scene: options.scene || 'auto',
    title: options.title || '',
    currentForm: buildCurrentFormPayload(options.target),
    options: {
      temperature: options.temperature ?? 0.1,
      sheetIndex: options.sheetIndex,
      maxRows: options.maxRows || 80,
      maxColumns: options.maxColumns || 60,
      maxPages: options.maxPages || 3,
      pageIndexes: options.pageIndexes,
      maxChars: options.maxChars || 16000,
      extractTables: options.extractTables !== false,
      extractHeaders: options.extractHeaders !== false,
      ocrProvider: options.ocrProvider || 'auto',
      ocrLanguage: options.ocrLanguage || 'chi_sim+eng',
    },
  })

  const result = response.data?.result || {}
  return normalizeMaterialResult(result)
}

export default recognizeRemoteFormMaterial
