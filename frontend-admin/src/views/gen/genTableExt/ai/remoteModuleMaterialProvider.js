import {postAction} from "@/api/action";
import {validateFormDesignDslSchema} from "@/views/gen/genTableExt/ai/dslSchema";
import {normalizeRemoteDsl} from "@/views/gen/genTableExt/ai/remoteAiDraftProvider";
import {
  summarizeModuleMaterialIssues,
  validateModuleMaterialSchema,
} from "@/views/gen/genTableExt/ai/moduleMaterialSchema";

const API_PREFIX = 'gen/aiFormDesign'

const normalizeText = (value) => String(value || '').trim()

const toArray = (value) => Array.isArray(value) ? value : []

const parseModuleMaterialJson = (materialJson) => {
  if (typeof materialJson !== 'string' || !materialJson.trim()) {
    throw new Error('远程 AI 未返回 ModuleMaterial JSON')
  }
  try {
    return JSON.parse(materialJson)
  } catch (error) {
    throw new Error(`远程 AI ModuleMaterial JSON 解析失败：${error.message}`)
  }
}

const parseFormDesignDslJson = (dslJson) => {
  if (typeof dslJson !== 'string' || !dslJson.trim()) {
    throw new Error('远程 AI 未返回 FormDesignDSL JSON')
  }
  try {
    return JSON.parse(dslJson)
  } catch (error) {
    throw new Error(`远程 AI FormDesignDSL JSON 解析失败：${error.message}`)
  }
}

const normalizeIssue = (issue = {}, index = 0, source = 'remote-module-material') => ({
  id: issue.id || issue.code || `${source}-${index + 1}`,
  code: issue.code || issue.id || `${source}-${index + 1}`,
  level: issue.level || 'warning',
  title: issue.title || '远程 AI 识别提示',
  description: issue.description || issue.suggestion || '',
  suggestion: issue.suggestion || '',
  menuId: issue.menuId || '',
  pageId: issue.pageId || '',
  formId: issue.formId || '',
  fieldId: issue.fieldId || '',
  fieldLabel: issue.fieldLabel || '',
  fixable: issue.fixable === true,
  meta: issue.meta || {},
})

const remoteModuleMaterialErrorTextMap = {
  AI_MODULE_MATERIAL_EMPTY: '请先输入模块材料',
  AI_MODULE_MATERIAL_SCHEMA_INVALID: 'AI 返回的模块材料结构不完整，请调整材料后重试',
  AI_NOT_CONFIGURED: '当前用户还没有可用的 AI 配置',
  AI_OUTPUT_INVALID_JSON: 'AI 返回内容不是有效 JSON，请重试',
  AI_FILE_EMPTY: '请先选择文件',
  AI_FILE_TOO_LARGE: '文件过大，请压缩后再上传',
  AI_FILE_TYPE_UNSUPPORTED: '当前文件类型暂不支持',
  AI_FILE_PARSE_FAILED: '文件解析失败，请检查文件内容',
  AI_FILE_TEXT_EMPTY: '文件中没有可读取文字，扫描 PDF 或图片需要 OCR 能力',
  AI_OCR_NOT_CONFIGURED: '当前环境没有可用的图片识别能力，请在系统设置中切换到支持视觉的 AI 模型后重试',
  AI_OCR_PROVIDER_UNAVAILABLE: '图片识别 Provider 不可用，请确认 AI 模型是否支持图片输入',
  AI_OCR_FAILED: '图片 OCR 识别失败，请检查图片清晰度或稍后重试',
  AI_OCR_LOW_CONFIDENCE: '图片 OCR 识别置信度较低，请更换更清晰的图片',
  AI_URL_EMPTY: '请先输入原型 URL',
  AI_URL_UNSUPPORTED: '当前 URL 不支持，请使用 http 或 https 地址',
  AI_URL_BLOCKED: '该 URL 出于安全原因暂不能采集',
  AI_URL_FETCH_FAILED: '原型 URL 采集失败，请检查地址是否可访问',
  AI_URL_TEXT_EMPTY: '原型页面中没有可读取的文字内容',
}

const createRemoteModuleMaterialError = (result = {}) => {
  const errorCode = result.errorCode || 'AI_REMOTE_MODULE_MATERIAL_FAILED'
  const error = new Error(result.message || result.errorCode || '远程 AI 模块材料识别失败')
  if (remoteModuleMaterialErrorTextMap[errorCode]) {
    const detailMessage = normalizeText(result.message)
    error.message = detailMessage && detailMessage !== errorCode
        ? `${remoteModuleMaterialErrorTextMap[errorCode]} - ${detailMessage}`
        : remoteModuleMaterialErrorTextMap[errorCode]
  }
  error.code = errorCode
  error.requestId = result.requestId || ''
  error.result = result
  return error
}

const createRemoteModuleFormDslError = (result = {}) => {
  const error = new Error(result.message || result.errorCode || '远程 AI 表单草稿生成失败')
  error.code = result.errorCode || 'AI_REMOTE_MODULE_FORM_DSL_FAILED'
  error.requestId = result.requestId || ''
  error.result = result
  return error
}

const hasSchemaError = (schemaIssues = []) => {
  return toArray(schemaIssues).some(issue => issue.level === 'error')
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

const normalizeUrlMaterialPage = (page = {}, index = 0) => ({
  id: page.id || `url_page_${index + 1}`,
  title: page.title || page.url || `页面${index + 1}`,
  url: page.url || '',
  rawText: page.rawText || '',
  textPreview: page.textPreview || '',
  headings: toArray(page.headings),
  labels: toArray(page.labels),
  inputs: toArray(page.inputs),
  buttons: toArray(page.buttons),
  links: toArray(page.links),
  tables: toArray(page.tables),
  qualityScore: Number.isFinite(Number(page.qualityScore)) ? Number(page.qualityScore) : null,
  qualityLevel: page.qualityLevel || '',
  qualityReasons: toArray(page.qualityReasons),
  included: page.included !== false,
})

const normalizeUrlMaterialResult = (result = {}) => {
  if (!result.success) {
    throw createRemoteModuleMaterialError(result)
  }
  return {
    success: true,
    sourceUrl: result.sourceUrl || '',
    rawText: result.rawText || '',
    pages: toArray(result.pages).map((page, index) => normalizeUrlMaterialPage(page, index)),
    extraction: result.extraction || {},
    message: result.message || '',
    requestId: result.requestId || '',
    elapsedMs: result.elapsedMs || 0,
    fromCache: result.fromCache === true,
    remoteResult: result,
  }
}

const normalizeModuleMaterialResult = (result = {}, sourceContent = '') => {
  if (!result.success && !result.materialJson) {
    throw createRemoteModuleMaterialError(result)
  }

  const material = parseModuleMaterialJson(result.materialJson)
  material.source = material.source || {}
  material.source.rawText = material.source.rawText || sourceContent
  material.meta = {
    ...(material.meta || {}),
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    requestId: result.requestId || '',
    elapsedMs: result.elapsedMs || 0,
  }

  const materialIssues = toArray(material.issues).map((issue, index) => normalizeIssue(issue, index, 'ai-module-material'))
  const remoteIssues = toArray(result.issues).map((issue, index) => normalizeIssue(issue, index, 'remote-module-material'))
  const schemaIssues = validateModuleMaterialSchema(material)
  const summary = summarizeModuleMaterialIssues(remoteIssues.concat(materialIssues).concat(schemaIssues))

  return {
    material,
    parseIssues: remoteIssues.concat(materialIssues),
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

export const extractUrlModuleMaterial = async (sourceUrl = '', options = {}) => {
  const normalizedUrl = normalizeText(sourceUrl)
  if (!normalizedUrl) {
    throw new Error('请先输入原型 URL')
  }

  const response = await postAction(`${API_PREFIX}/extractUrlModuleMaterial`, {
    sourceUrl: normalizedUrl,
    scene: options.scene || 'auto',
    moduleName: options.moduleName || '',
    moduleTitle: options.moduleTitle || '',
    supplementText: options.supplementText || '',
    options: {
      maxPages: options.maxPages ?? 20,
      maxChars: options.maxChars ?? 16000,
      timeoutSeconds: options.timeoutSeconds ?? 8,
      collectSameOriginLinks: options.collectSameOriginLinks !== false,
      dynamicRender: options.dynamicRender === true,
      fallbackToStatic: options.fallbackToStatic !== false,
      forceRefresh: options.forceRefresh === true,
      dynamicWaitMillis: options.dynamicWaitMillis ?? 1200,
    },
  })

  const result = response.data?.result || {}
  return normalizeUrlMaterialResult(result)
}

export const recognizeRemoteModuleMaterial = async (rawText = '', options = {}) => {
  const sourceContent = normalizeText(rawText)
  if (!sourceContent) {
    throw new Error('请先输入模块材料')
  }

  const response = await postAction(`${API_PREFIX}/recognizeModuleMaterial`, {
    rawText: sourceContent,
    sourceType: options.sourceType || 'text',
    scene: options.scene || 'auto',
    moduleName: options.moduleName || '',
    moduleTitle: options.moduleTitle || '',
    sourceUrl: options.sourceUrl || '',
    options: {
      temperature: options.temperature ?? 0.1,
    },
  })

  const result = response.data?.result || {}
  return normalizeModuleMaterialResult(result, sourceContent)
}

export const recognizeFileModuleMaterial = async (file, options = {}) => {
  const rawFile = file?.originFileObj || file
  if (!rawFile) {
    throw new Error('请先选择文件')
  }
  const fileBase64 = await readFileAsBase64(rawFile)
  const response = await postAction(`${API_PREFIX}/parseFileModuleMaterial`, {
    fileName: rawFile.name || file?.name || '',
    mimeType: rawFile.type || file?.type || '',
    sourceType: options.sourceType || 'auto',
    fileBase64,
    scene: options.scene || 'auto',
    moduleName: options.moduleName || '',
    moduleTitle: options.moduleTitle || '',
    options: {
      temperature: options.temperature ?? 0.1,
      sheetIndex: options.sheetIndex,
      extractAllSheets: options.extractAllSheets === true,
      maxRows: options.maxRows || 120,
      maxColumns: options.maxColumns || 80,
      maxPages: options.maxPages || 5,
      pageIndexes: options.pageIndexes,
      maxChars: options.maxChars || 16000,
      extractTables: options.extractTables !== false,
      extractHeaders: options.extractHeaders !== false,
      ocrProvider: options.ocrProvider || 'auto',
      ocrLanguage: options.ocrLanguage || 'chi_sim+eng',
    },
  })

  const result = response.data?.result || {}
  return normalizeModuleMaterialResult(result)
}

export const generateRemoteModuleFormDesignDsl = async (payload = {}, options = {}) => {
  if (!payload.form) {
    throw new Error('请先选择要生成的表单')
  }

  const response = await postAction(`${API_PREFIX}/generateModuleFormDsl`, {
    module: payload.module || {},
    page: payload.page || {},
    form: payload.form || {},
    dictionaries: toArray(payload.dictionaries),
    relations: toArray(payload.relations),
    sourceType: payload.sourceType || 'module-material',
    sourceUrl: payload.sourceUrl || '',
    scene: payload.scene || payload.module?.scene || 'normal',
    options: {
      temperature: options.temperature ?? 0.15,
      currentDslJson: options.currentDslJson || '',
    },
  })

  const result = response.data?.result || {}
  if (!result.success) {
    throw createRemoteModuleFormDslError(result)
  }

  const dsl = normalizeRemoteDsl(parseFormDesignDslJson(result.dslJson))
  const generatedAt = new Date().toISOString()
  dsl.generator = dsl.generator || 'remote-ai'
  dsl.generatedAt = dsl.generatedAt || generatedAt
  dsl.raw = {
    ...(dsl.raw || {}),
    sourceType: 'module-form-ai',
    moduleName: payload.module?.nameHint || payload.module?.name || '',
    moduleTitle: payload.module?.title || '',
    pageId: payload.page?.id || '',
    pageTitle: payload.page?.title || '',
    formId: payload.form?.id || '',
    formName: payload.form?.nameHint || payload.form?.formName || '',
    formTitle: payload.form?.title || '',
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    requestId: result.requestId || '',
  }

  const schemaIssues = validateFormDesignDslSchema(dsl)
  if (options.rejectOnSchemaError !== false && hasSchemaError(schemaIssues)) {
    const error = createRemoteModuleFormDslError({
      ...result,
      errorCode: 'AI_FRONTEND_SCHEMA_INVALID',
      message: '远程 AI 已返回 DSL，但前端结构检查未通过',
    })
    error.schemaIssues = schemaIssues
    throw error
  }

  return {
    dsl,
    generatedAt: dsl.generatedAt || generatedAt,
    schemaIssues,
    backendIssues: toArray(result.issues),
    message: result.message || '',
    fromCache: /cache hit/i.test(result.message || ''),
    requestId: result.requestId || '',
    provider: result.provider || 'remote',
    model: result.model || '',
    promptVersion: result.promptVersion || '',
    elapsedMs: result.elapsedMs || 0,
    rawOutputPreview: result.rawOutputPreview || '',
    remoteResult: result,
  }
}

export default recognizeRemoteModuleMaterial
