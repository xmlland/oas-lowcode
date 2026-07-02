package com.jeestudio.bpm.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDslGenerateRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormDesignDslGenerateResponse;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialExcelParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialFileParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeResponse;
import com.jeestudio.bpm.common.entity.gen.AiModuleFormDslGenerateRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialFileParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialRecognizeRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialRecognizeResponse;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseResponse;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderException;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderRegistry;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderStatus;
import com.jeestudio.bpm.service.ai.ocr.OcrRequest;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: AI表单设计服务
 */
@Service
public class FormDesignAIService {

    private static final Logger logger = LoggerFactory.getLogger(FormDesignAIService.class);

    public static final String PROMPT_VERSION_FORM_DSL_V1 = "form-dsl-v1";
    public static final String PROMPT_VERSION_FORM_MATERIAL_V1 = "form-material-v1";
    public static final String PROMPT_VERSION_MODULE_MATERIAL_V1 = "module-material-v1";

    private static final String DSL_SCHEMA_VERSION = "1.0";
    private static final String FORM_MATERIAL_SCHEMA_VERSION = "1.0";
    private static final String MODULE_MATERIAL_SCHEMA_VERSION = "1.0";
    private static final int MAX_RAW_OUTPUT_PREVIEW_LENGTH = 4000;
    private static final int MAX_CURRENT_DSL_CONTEXT_LENGTH = 12000;
    private static final int MAX_MATERIAL_CONTEXT_LENGTH = 16000;
    private static final int FORM_DSL_MAX_TOKENS = 4096;
    private static final int FORM_MATERIAL_MAX_TOKENS = 4096;
    private static final int MODULE_MATERIAL_MAX_TOKENS = 8192;
    private static final int MODULE_BLUEPRINT_MAX_FORMS = 20;
    private static final int MODULE_MATERIAL_RECOGNIZE_CACHE_MAX_SIZE = 5;
    private static final long MODULE_MATERIAL_RECOGNIZE_CACHE_TTL_HOURS = 24L;
    private static final String MODULE_MATERIAL_RECOGNIZE_CACHE_VERSION = "module-material-v3";
    private static final String MODULE_MATERIAL_RECOGNIZE_CACHE_INDEX_PREFIX = "ai:formDesign:moduleMaterial:index:";
    private static final String MODULE_MATERIAL_RECOGNIZE_CACHE_ENTRY_PREFIX = "ai:formDesign:moduleMaterial:entry:";
    private static final int MODULE_FORM_DSL_GENERATE_CACHE_MAX_SIZE = 10;
    private static final long MODULE_FORM_DSL_GENERATE_CACHE_TTL_HOURS = 24L;
    private static final String MODULE_FORM_DSL_GENERATE_CACHE_VERSION = "module-form-dsl-v2";
    private static final String MODULE_FORM_DSL_GENERATE_CACHE_INDEX_PREFIX = "ai:formDesign:moduleFormDsl:index:";
    private static final String MODULE_FORM_DSL_GENERATE_CACHE_ENTRY_PREFIX = "ai:formDesign:moduleFormDsl:entry:";
    private static final List<String> MATERIAL_SOURCE_TYPE_OPTIONS = Arrays.asList(
            "text",
            "table-text",
            "excel",
            "image",
            "word",
            "pdf"
    );
    private static final List<String> MATERIAL_SCENE_OPTIONS = Arrays.asList(
            "",
            "normal",
            "document-form",
            "approval",
            "ledger"
    );
    private static final List<String> MODULE_MATERIAL_SOURCE_TYPE_OPTIONS = Arrays.asList(
            "text",
            "url",
            "excel",
            "word",
            "docx",
            "pdf",
            "image",
            "screenshot",
            "mixed"
    );
    private static final List<String> MODULE_MATERIAL_SCENE_OPTIONS = Arrays.asList(
            "",
            "normal",
            "document",
            "document-form",
            "approval",
            "ledger",
            "mixed"
    );
    private static final List<String> MODULE_PAGE_TYPE_OPTIONS = Arrays.asList(
            "",
            "list_form",
            "form",
            "list",
            "detail",
            "config",
            "workflow",
            "dashboard",
            "report",
            "external",
            "unknown"
    );
    private static final List<String> MODULE_RELATION_TYPE_OPTIONS = Arrays.asList(
            "",
            "master_detail",
            "reference",
            "workflow",
            "lookup",
            "attachment",
            "statistics",
            "unknown"
    );
    private static final List<String> MATERIAL_QUERY_MODE_OPTIONS = Arrays.asList(
            "",
            "input",
            "like",
            "exact",
            "select",
            "date-range",
            "range"
    );
    private static final List<String> FIELD_TYPE_OPTIONS = Arrays.asList(
            "text",
            "textarea",
            "integer",
            "decimal",
            "select",
            "radio",
            "checkbox",
            "switch",
            "date",
            "user",
            "users",
            "office",
            "area",
            "tree",
            "modalSelect",
            "modalMultiSelect",
            "upload",
            "imageUpload",
            "onlineFile",
            "richText",
            "serialNo"
    );

    @Autowired
    private GenAIService genAIService;

    @Autowired
    private FileMaterialExtractionService fileMaterialExtractionService;

    @Autowired
    private UrlModuleMaterialExtractionService urlModuleMaterialExtractionService;

    @Autowired
    private OcrProviderRegistry ocrProviderRegistry;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 识别单表单创建材料，调用大模型生成 FormMaterial v1，并完成结构校验和响应封装。
     */
    public AiFormMaterialRecognizeResponse recognizeMaterial(AiFormMaterialRecognizeRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        if (request == null || StringUtil.isBlank(request.getRawText())) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_MATERIAL_EMPTY,
                    "Material text cannot be empty"
            );
            return completeMaterialResponse(response, requestId, null, request, startedAt);
        }

        if (!genAIService.isAvailable(loginName)) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_NOT_CONFIGURED,
                    "AI configuration is not available"
            );
            return completeMaterialResponse(response, requestId, null, request, startedAt);
        }

        String model = genAIService.getConfiguredModel(loginName);
        String rawOutput;
        long aiStartedAt = System.currentTimeMillis();
        try {
            rawOutput = genAIService.callChatCompletionsJsonObject(
                    buildMaterialSystemPrompt(),
                    buildMaterialUserPrompt(request),
                    loginName,
                    normalizeMaterialTemperature(request),
                    FORM_MATERIAL_MAX_TOKENS
            );
        } catch (Exception ex) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            return completeMaterialResponse(response, requestId, model, request, startedAt);
        }
        long aiElapsedMs = System.currentTimeMillis() - aiStartedAt;
        logger.info("FormDesignAI recognizeMaterial chatCompletions returned requestId={} model={} aiElapsedMs={} rawLength={}",
                requestId, model, aiElapsedMs, rawOutput == null ? 0 : rawOutput.length());

        String jsonText = genAIService.extractJson(rawOutput);
        if (StringUtil.isBlank(jsonText)) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output is not valid JSON"
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeMaterialResponse(response, requestId, model, request, startedAt);
        }

        JSONObject material;
        try {
            material = JSONObject.parseObject(jsonText);
        } catch (Exception ex) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output JSON parse failed: " + ex.getMessage()
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeMaterialResponse(response, requestId, model, request, startedAt);
        }

        normalizeMaterial(material, request);
        List<AiFormMaterialRecognizeResponse.Issue> issues = validateMaterial(material);
        if (hasMaterialError(issues)) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_MATERIAL_SCHEMA_INVALID,
                    "AI output does not match FormMaterial v1"
            );
            response.setMaterialJson(material.toJSONString());
            response.setSummary(buildMaterialSummary(material));
            response.setIssues(issues);
            response.setRawOutputPreview(preview(rawOutput));
            return completeMaterialResponse(response, requestId, model, request, startedAt);
        }

        AiFormMaterialRecognizeResponse response = new AiFormMaterialRecognizeResponse();
        response.setSuccess(true);
        response.setProvider(AiFormMaterialRecognizeResponse.PROVIDER_REMOTE);
        response.setModel(model);
        response.setPromptVersion(PROMPT_VERSION_FORM_MATERIAL_V1);
        response.setMaterialJson(material.toJSONString());
        response.setSummary(buildMaterialSummary(material));
        response.setIssues(issues);
        response.setRawOutputPreview(preview(rawOutput));
        response.setMessage("FormMaterial recognized");
        return completeMaterialResponse(response, requestId, model, request, startedAt);
    }

    /**
     * 解析 Excel 表单材料，先抽取多 Sheet/多表格文本，再复用材料识别链路。
     */
    public AiFormMaterialRecognizeResponse parseExcelMaterial(AiFormMaterialExcelParseRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        AiFormMaterialRecognizeRequest recognizeRequest = buildExcelRecognizeRequest(request, "");
        if (request == null || StringUtil.isBlank(request.getFileBase64())) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_EXCEL_FILE_EMPTY,
                    "Excel file cannot be empty"
            );
            return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        FileMaterialExtractionService.FileMaterialContent fileMaterial;
        try {
            fileMaterial = fileMaterialExtractionService.extractExcel(request);
        } catch (Exception ex) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_EXCEL_FILE_INVALID,
                    "Excel parse failed: " + ex.getMessage()
            );
            return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        recognizeRequest = buildExcelRecognizeRequest(request, fileMaterial.getRawText());
        AiFormMaterialRecognizeResponse remoteResponse;
        try {
            remoteResponse = recognizeMaterial(recognizeRequest, loginName);
            enrichFileMaterialResponse(remoteResponse, fileMaterial);
            if (Boolean.TRUE.equals(remoteResponse.getSuccess())) {
                return remoteResponse;
            }
        } catch (Exception ex) {
            remoteResponse = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            remoteResponse = completeMaterialResponse(remoteResponse, requestId, null, recognizeRequest, startedAt);
        }

        return remoteResponse;
    }

    /**
     * 解析 Word、PDF、图片等文件材料，统一完成文本抽取/OCR 后进入单表单材料识别。
     */
    public AiFormMaterialRecognizeResponse parseFileMaterial(AiFormMaterialFileParseRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        AiFormMaterialRecognizeRequest recognizeRequest = buildFileRecognizeRequest(request, "", "");
        if (request == null || StringUtil.isBlank(request.getFileBase64())) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_FILE_EMPTY,
                    "File cannot be empty"
            );
            return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        FileMaterialExtractionService.FileMaterialContent fileMaterial;
        try {
            fileMaterial = fileMaterialExtractionService.extract(request, loginName, requestId);
        } catch (Exception ex) {
            if (ex instanceof OcrProviderException) {
                OcrProviderException ocrException = (OcrProviderException) ex;
                AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                        defaultText(ocrException.getErrorCode(), AiFormMaterialRecognizeResponse.ERROR_OCR_FAILED),
                        ocrException.getMessage()
                );
                return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("no extractable text")) {
                AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                        AiFormMaterialRecognizeResponse.ERROR_FILE_TEXT_EMPTY,
                        "File has no extractable text; scanned PDF needs OCR"
                );
                return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("larger than")) {
                AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                        AiFormMaterialRecognizeResponse.ERROR_FILE_TOO_LARGE,
                        ex.getMessage()
                );
                return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("unsupported file type")) {
                AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                        AiFormMaterialRecognizeResponse.ERROR_FILE_TYPE_UNSUPPORTED,
                        ex.getMessage()
                );
                return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_FILE_PARSE_FAILED,
                    "File parse failed: " + ex.getMessage()
            );
            return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        if (fileMaterial == null || StringUtil.isBlank(fileMaterial.getRawText())) {
            AiFormMaterialRecognizeResponse response = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_FILE_TEXT_EMPTY,
                    "File has no readable text"
            );
            return completeMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        recognizeRequest = buildFileRecognizeRequest(request, fileMaterial.getRawText(), fileMaterial.getSourceType());
        AiFormMaterialRecognizeResponse remoteResponse;
        try {
            remoteResponse = recognizeMaterial(recognizeRequest, loginName);
            enrichFileMaterialResponse(remoteResponse, fileMaterial);
            if (Boolean.TRUE.equals(remoteResponse.getSuccess())) {
                return remoteResponse;
            }
        } catch (Exception ex) {
            remoteResponse = AiFormMaterialRecognizeResponse.failed(
                    AiFormMaterialRecognizeResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            remoteResponse = completeMaterialResponse(remoteResponse, requestId, null, recognizeRequest, startedAt);
        }

        return remoteResponse;
    }

    /**
     * 识别模块级材料，生成 ModuleMaterial v1 蓝图，并使用 Redis 缓存减少重复调试成本。
     */
    public AiModuleMaterialRecognizeResponse recognizeModuleMaterial(AiModuleMaterialRecognizeRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        if (request == null || StringUtil.isBlank(request.getRawText())) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_MATERIAL_EMPTY,
                    "Module material text cannot be empty"
            );
            return completeModuleMaterialResponse(response, requestId, null, request, startedAt);
        }

        if (!genAIService.isAvailable(loginName)) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_NOT_CONFIGURED,
                    "AI configuration is not available"
            );
            return completeModuleMaterialResponse(response, requestId, null, request, startedAt);
        }

        String model = genAIService.getConfiguredModel(loginName);
        ModuleMaterialCacheKey cacheKey = buildModuleMaterialCacheKey(request, loginName, model);
        AiModuleMaterialRecognizeResponse cachedResponse = getCachedModuleMaterialResponse(cacheKey, requestId, model, request, startedAt);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        String rawOutput;
        long aiStartedAt = System.currentTimeMillis();
        try {
            rawOutput = genAIService.callChatCompletionsJsonObject(
                    buildModuleMaterialSystemPrompt(),
                    buildModuleMaterialUserPrompt(request),
                    loginName,
                    normalizeModuleMaterialTemperature(request),
                    MODULE_MATERIAL_MAX_TOKENS
            );
        } catch (Exception ex) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            return completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        }
        long aiElapsedMs = System.currentTimeMillis() - aiStartedAt;
        logger.info("FormDesignAI recognizeModuleMaterial chatCompletions returned requestId={} model={} aiElapsedMs={} rawLength={}",
                requestId, model, aiElapsedMs, rawOutput == null ? 0 : rawOutput.length());

        String jsonText = genAIService.extractJson(rawOutput);
        if (StringUtil.isBlank(jsonText)) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output is not valid JSON"
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        }

        JSONObject material;
        try {
            material = JSONObject.parseObject(jsonText);
        } catch (Exception ex) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output JSON parse failed: " + ex.getMessage()
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        }

        normalizeModuleMaterial(material, request);
        List<AiModuleMaterialRecognizeResponse.Issue> issues = validateModuleMaterial(material);
        if (hasModuleBlueprintEmpty(issues)) {
            ModuleMaterialRetryResult retryResult = retryRecognizeModuleMaterial(request, loginName, model, material, issues, requestId);
            if (retryResult != null) {
                material = retryResult.getMaterial();
                issues = retryResult.getIssues();
                rawOutput = retryResult.getRawOutput();
            }
        }
        if (hasModuleMaterialError(issues)) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_MATERIAL_SCHEMA_INVALID,
                    "AI output does not match ModuleMaterial v1"
            );
            response.setMaterialJson(material.toJSONString());
            response.setSummary(buildModuleMaterialSummary(material));
            response.setIssues(issues);
            response.setRawOutputPreview(preview(rawOutput));
            return completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        }

        AiModuleMaterialRecognizeResponse response = new AiModuleMaterialRecognizeResponse();
        response.setSuccess(true);
        response.setProvider(AiModuleMaterialRecognizeResponse.PROVIDER_REMOTE);
        response.setModel(model);
        response.setPromptVersion(PROMPT_VERSION_MODULE_MATERIAL_V1);
        response.setMaterialJson(material.toJSONString());
        response.setSummary(buildModuleMaterialSummary(material));
        response.setIssues(issues);
        response.setRawOutputPreview(preview(rawOutput));
        response.setMessage("ModuleMaterial recognized");
        AiModuleMaterialRecognizeResponse completedResponse = completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        putCachedModuleMaterialResponse(cacheKey, completedResponse);
        return completedResponse;
    }

    /**
     * 解析模块级文件材料，抽取 Word、PDF、图片等内容后交给模块蓝图识别链路。
     */
    public AiModuleMaterialRecognizeResponse parseFileModuleMaterial(AiModuleMaterialFileParseRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        AiModuleMaterialRecognizeRequest recognizeRequest = buildModuleFileRecognizeRequest(request, "", "");
        if (request == null || StringUtil.isBlank(request.getFileBase64())) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_FILE_EMPTY,
                    "File cannot be empty"
            );
            return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        FileMaterialExtractionService.FileMaterialContent fileMaterial;
        try {
            fileMaterial = fileMaterialExtractionService.extract(buildModuleFileExtractionRequest(request), loginName, requestId);
        } catch (Exception ex) {
            if (ex instanceof OcrProviderException) {
                OcrProviderException ocrException = (OcrProviderException) ex;
                AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                        defaultText(ocrException.getErrorCode(), AiModuleMaterialRecognizeResponse.ERROR_OCR_FAILED),
                        ocrException.getMessage()
                );
                return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("no extractable text")) {
                AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                        AiModuleMaterialRecognizeResponse.ERROR_FILE_TEXT_EMPTY,
                        "File has no extractable text; scanned PDF needs OCR"
                );
                return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("larger than")) {
                AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                        AiModuleMaterialRecognizeResponse.ERROR_FILE_TOO_LARGE,
                        ex.getMessage()
                );
                return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("unsupported file type")) {
                AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                        AiModuleMaterialRecognizeResponse.ERROR_FILE_TYPE_UNSUPPORTED,
                        ex.getMessage()
                );
                return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
            }
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_FILE_PARSE_FAILED,
                    "File parse failed: " + ex.getMessage()
            );
            return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        if (fileMaterial == null || StringUtil.isBlank(fileMaterial.getRawText())) {
            AiModuleMaterialRecognizeResponse response = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_FILE_TEXT_EMPTY,
                    "File has no readable text"
            );
            return completeModuleMaterialResponse(response, requestId, null, recognizeRequest, startedAt);
        }

        recognizeRequest = buildModuleFileRecognizeRequest(request, fileMaterial.getRawText(), fileMaterial.getSourceType());
        AiModuleMaterialRecognizeResponse remoteResponse;
        try {
            remoteResponse = recognizeModuleMaterial(recognizeRequest, loginName);
            enrichFileModuleMaterialResponse(remoteResponse, fileMaterial);
            if (Boolean.TRUE.equals(remoteResponse.getSuccess())) {
                return remoteResponse;
            }
        } catch (Exception ex) {
            remoteResponse = AiModuleMaterialRecognizeResponse.failed(
                    AiModuleMaterialRecognizeResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            remoteResponse = completeModuleMaterialResponse(remoteResponse, requestId, null, recognizeRequest, startedAt);
        }

        return remoteResponse;
    }

    /**
     * 从原型 URL 采集页面候选内容，供模块材料识别前确认和选择。
     */
    public AiModuleMaterialUrlParseResponse extractUrlModuleMaterial(AiModuleMaterialUrlParseRequest request, String loginName) {
        return urlModuleMaterialExtractionService.extract(request, loginName);
    }

    /**
     * 查询当前文件材料可用的 OCR Provider 状态，用于前端提示图片/扫描件识别能力。
     */
    public List<OcrProviderStatus> getOcrProviderStatuses(AiFormMaterialFileParseRequest request, String loginName) {
        OcrRequest ocrRequest = new OcrRequest();
        ocrRequest.setLoginName(defaultText(loginName, ""));
        ocrRequest.setProviderCode(request == null || request.getOptions() == null
                ? OcrProviderRegistry.PROVIDER_AUTO
                : defaultText(request.getOptions().getOcrProvider(), OcrProviderRegistry.PROVIDER_AUTO));
        ocrRequest.setSourceType(inferSourceTypeFromRequest(request));
        ocrRequest.setFileName(request == null ? "" : defaultText(request.getFileName(), ""));
        ocrRequest.setMimeType(request == null ? "" : defaultText(request.getMimeType(), ""));
        ocrRequest.setLanguage(request == null || request.getOptions() == null
                ? ""
                : defaultText(request.getOptions().getOcrLanguage(), ""));
        return ocrProviderRegistry.listProviderStatuses(loginName, ocrRequest);
    }

    private ModuleMaterialCacheKey buildModuleMaterialCacheKey(AiModuleMaterialRecognizeRequest request,
                                                               String loginName,
                                                               String model) {
        if (request == null) {
            return null;
        }
        String userScope = shortHash(normalizeCacheText(defaultText(loginName, "anonymous")), 16);
        String rawTextHash = sha256(normalizeCacheText(request.getRawText()));
        StringBuilder canonical = new StringBuilder();
        canonical.append("promptVersion=").append(PROMPT_VERSION_MODULE_MATERIAL_V1).append('\n');
        canonical.append("cacheVersion=").append(MODULE_MATERIAL_RECOGNIZE_CACHE_VERSION).append('\n');
        canonical.append("model=").append(normalizeCacheText(model)).append('\n');
        canonical.append("sourceType=").append(normalizeCacheText(request.getSourceType()).toLowerCase()).append('\n');
        canonical.append("scene=").append(normalizeCacheText(request.getScene()).toLowerCase()).append('\n');
        canonical.append("moduleName=").append(normalizeCacheText(request.getModuleName()).toLowerCase()).append('\n');
        canonical.append("moduleTitle=").append(normalizeCacheText(request.getModuleTitle())).append('\n');
        canonical.append("sourceUrl=").append(normalizeCacheText(request.getSourceUrl())).append('\n');
        canonical.append("temperature=").append(normalizeModuleMaterialTemperature(request)).append('\n');
        canonical.append("maxTokens=").append(MODULE_MATERIAL_MAX_TOKENS).append('\n');
        canonical.append("rawTextSha256=").append(rawTextHash);

        String fingerprint = sha256(canonical.toString());
        return new ModuleMaterialCacheKey(
                MODULE_MATERIAL_RECOGNIZE_CACHE_INDEX_PREFIX + userScope,
                MODULE_MATERIAL_RECOGNIZE_CACHE_ENTRY_PREFIX + userScope + ":" + fingerprint,
                shortHash(fingerprint, 12)
        );
    }

    private AiModuleMaterialRecognizeResponse getCachedModuleMaterialResponse(ModuleMaterialCacheKey cacheKey,
                                                                              String requestId,
                                                                              String model,
                                                                              AiModuleMaterialRecognizeRequest request,
                                                                              long startedAt) {
        if (cacheKey == null || redisTemplate == null) {
            return null;
        }
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey.getEntryKey());
            if (!(cached instanceof String)) {
                return null;
            }
            AiModuleMaterialRecognizeResponse response = JSON.parseObject((String) cached, AiModuleMaterialRecognizeResponse.class);
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                return null;
            }
            touchModuleMaterialCacheEntry(cacheKey);
            response.setMessage(defaultText(response.getMessage(), "ModuleMaterial recognized") + " (cache hit)");
            logger.info("FormDesignAI recognizeModuleMaterial cache hit requestId={} cacheKey={}",
                    requestId, cacheKey.getLogKey());
            return completeModuleMaterialResponse(response, requestId, model, request, startedAt);
        } catch (Exception ex) {
            logger.warn("FormDesignAI recognizeModuleMaterial cache read skipped cacheKey={} error={}",
                    cacheKey.getLogKey(), ex.getMessage());
            return null;
        }
    }

    private void putCachedModuleMaterialResponse(ModuleMaterialCacheKey cacheKey,
                                                 AiModuleMaterialRecognizeResponse response) {
        if (cacheKey == null || redisTemplate == null || response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    cacheKey.getEntryKey(),
                    JSON.toJSONString(response),
                    MODULE_MATERIAL_RECOGNIZE_CACHE_TTL_HOURS,
                    TimeUnit.HOURS
            );
            touchModuleMaterialCacheEntry(cacheKey);
            logger.info("FormDesignAI recognizeModuleMaterial cache put cacheKey={} maxSize={} ttlHours={}",
                    cacheKey.getLogKey(), MODULE_MATERIAL_RECOGNIZE_CACHE_MAX_SIZE, MODULE_MATERIAL_RECOGNIZE_CACHE_TTL_HOURS);
        } catch (Exception ex) {
            logger.warn("FormDesignAI recognizeModuleMaterial cache write skipped cacheKey={} error={}",
                    cacheKey.getLogKey(), ex.getMessage());
        }
    }

    private void touchModuleMaterialCacheEntry(ModuleMaterialCacheKey cacheKey) {
        redisTemplate.opsForList().remove(cacheKey.getIndexKey(), 0, cacheKey.getEntryKey());
        redisTemplate.opsForList().leftPush(cacheKey.getIndexKey(), cacheKey.getEntryKey());
        redisTemplate.expire(cacheKey.getEntryKey(), MODULE_MATERIAL_RECOGNIZE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        List<Object> overflowKeys = redisTemplate.opsForList().range(
                cacheKey.getIndexKey(),
                MODULE_MATERIAL_RECOGNIZE_CACHE_MAX_SIZE,
                -1
        );
        redisTemplate.opsForList().trim(cacheKey.getIndexKey(), 0, MODULE_MATERIAL_RECOGNIZE_CACHE_MAX_SIZE - 1);
        redisTemplate.expire(cacheKey.getIndexKey(), MODULE_MATERIAL_RECOGNIZE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        if (overflowKeys == null || overflowKeys.isEmpty()) {
            return;
        }
        for (Object overflowKey : overflowKeys) {
            if (overflowKey instanceof String && !cacheKey.getEntryKey().equals(overflowKey)) {
                redisTemplate.delete((String) overflowKey);
            }
        }
    }

    private ModuleFormDslCacheKey buildModuleFormDslCacheKey(AiFormDesignDslGenerateRequest request,
                                                             String loginName,
                                                             String model) {
        if (request == null) {
            return null;
        }
        String userScope = shortHash(normalizeCacheText(defaultText(loginName, "anonymous")), 16);
        AiFormDesignDslGenerateRequest.CurrentForm currentForm = request.getCurrentForm();
        StringBuilder canonical = new StringBuilder();
        canonical.append("promptVersion=").append(PROMPT_VERSION_FORM_DSL_V1).append('\n');
        canonical.append("cacheVersion=").append(MODULE_FORM_DSL_GENERATE_CACHE_VERSION).append('\n');
        canonical.append("model=").append(normalizeCacheText(model)).append('\n');
        canonical.append("mode=").append(normalizeCacheText(request.getMode()).toLowerCase()).append('\n');
        canonical.append("scene=").append(normalizeCacheText(request.getScene()).toLowerCase()).append('\n');
        canonical.append("temperature=").append(normalizeTemperature(request)).append('\n');
        canonical.append("maxTokens=").append(FORM_DSL_MAX_TOKENS).append('\n');
        canonical.append("formName=").append(currentForm == null ? "" : normalizeCacheText(currentForm.getFormName()).toLowerCase()).append('\n');
        canonical.append("formTitle=").append(currentForm == null ? "" : normalizeCacheText(currentForm.getFormTitle())).append('\n');
        canonical.append("moduleName=").append(currentForm == null ? "" : normalizeCacheText(currentForm.getModuleName()).toLowerCase()).append('\n');
        canonical.append("currentDslSha256=").append(sha256(normalizeCacheText(request.getCurrentDslJson()))).append('\n');
        canonical.append("requirementSha256=").append(sha256(normalizeCacheText(request.getRequirement())));

        String fingerprint = sha256(canonical.toString());
        return new ModuleFormDslCacheKey(
                MODULE_FORM_DSL_GENERATE_CACHE_INDEX_PREFIX + userScope,
                MODULE_FORM_DSL_GENERATE_CACHE_ENTRY_PREFIX + userScope + ":" + fingerprint,
                shortHash(fingerprint, 12)
        );
    }

    private AiFormDesignDslGenerateResponse getCachedModuleFormDslResponse(ModuleFormDslCacheKey cacheKey,
                                                                           String model,
                                                                           AiFormDesignDslGenerateRequest request) {
        if (cacheKey == null || redisTemplate == null) {
            return null;
        }
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey.getEntryKey());
            if (!(cached instanceof String)) {
                return null;
            }
            AiFormDesignDslGenerateResponse response = JSON.parseObject((String) cached, AiFormDesignDslGenerateResponse.class);
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                return null;
            }
            touchModuleFormDslCacheEntry(cacheKey);
            response.setMessage(defaultText(response.getMessage(), "FormDesignDSL generated") + " (cache hit)");
            logger.info("FormDesignAI generateModuleFormDsl cache hit requestId={} cacheKey={}",
                    requestId, cacheKey.getLogKey());
            return completeResponse(response, requestId, model, request, startedAt);
        } catch (Exception ex) {
            logger.warn("FormDesignAI generateModuleFormDsl cache read skipped cacheKey={} error={}",
                    cacheKey.getLogKey(), ex.getMessage());
            return null;
        }
    }

    private void putCachedModuleFormDslResponse(ModuleFormDslCacheKey cacheKey,
                                                AiFormDesignDslGenerateResponse response) {
        if (cacheKey == null || redisTemplate == null || response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(
                    cacheKey.getEntryKey(),
                    JSON.toJSONString(response),
                    MODULE_FORM_DSL_GENERATE_CACHE_TTL_HOURS,
                    TimeUnit.HOURS
            );
            touchModuleFormDslCacheEntry(cacheKey);
            logger.info("FormDesignAI generateModuleFormDsl cache put cacheKey={} maxSize={} ttlHours={}",
                    cacheKey.getLogKey(), MODULE_FORM_DSL_GENERATE_CACHE_MAX_SIZE, MODULE_FORM_DSL_GENERATE_CACHE_TTL_HOURS);
        } catch (Exception ex) {
            logger.warn("FormDesignAI generateModuleFormDsl cache write skipped cacheKey={} error={}",
                    cacheKey.getLogKey(), ex.getMessage());
        }
    }

    private void touchModuleFormDslCacheEntry(ModuleFormDslCacheKey cacheKey) {
        redisTemplate.opsForList().remove(cacheKey.getIndexKey(), 0, cacheKey.getEntryKey());
        redisTemplate.opsForList().leftPush(cacheKey.getIndexKey(), cacheKey.getEntryKey());
        redisTemplate.expire(cacheKey.getEntryKey(), MODULE_FORM_DSL_GENERATE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        List<Object> overflowKeys = redisTemplate.opsForList().range(
                cacheKey.getIndexKey(),
                MODULE_FORM_DSL_GENERATE_CACHE_MAX_SIZE,
                -1
        );
        redisTemplate.opsForList().trim(cacheKey.getIndexKey(), 0, MODULE_FORM_DSL_GENERATE_CACHE_MAX_SIZE - 1);
        redisTemplate.expire(cacheKey.getIndexKey(), MODULE_FORM_DSL_GENERATE_CACHE_TTL_HOURS, TimeUnit.HOURS);
        if (overflowKeys == null || overflowKeys.isEmpty()) {
            return;
        }
        for (Object overflowKey : overflowKeys) {
            if (overflowKey instanceof String && !cacheKey.getEntryKey().equals(overflowKey)) {
                redisTemplate.delete((String) overflowKey);
            }
        }
    }

    private String normalizeCacheText(String value) {
        return defaultText(value, "").trim().replace("\r\n", "\n").replace('\r', '\n');
    }

    private String shortHash(String value, int length) {
        String hash = value != null && value.matches("^[0-9a-f]{64}$") ? value : sha256(defaultText(value, ""));
        return hash.substring(0, Math.min(length, hash.length()));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(defaultText(value, "").getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(hashByte & 0xff);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /**
     * 根据模块蓝图中的单个表单材料生成 FormDesignDSL，并使用模块表单级缓存加速重复生成。
     */
    public AiFormDesignDslGenerateResponse generateModuleFormDsl(AiModuleFormDslGenerateRequest request, String loginName) throws Exception {
        if (request == null || request.getForm() == null || request.getForm().isEmpty()) {
            return AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_REQUIREMENT_EMPTY,
                    "Module form material cannot be empty"
            );
        }

        Map<String, Object> module = request.getModule();
        Map<String, Object> page = request.getPage();
        Map<String, Object> form = request.getForm();
        String formName = firstNonBlank(mapText(form, "nameHint"), mapText(form, "formName"), mapText(form, "name"), mapText(form, "id"), "module_form");
        String formTitle = firstNonBlank(mapText(form, "title"), mapText(page, "title"), "模块表单");
        String moduleName = firstNonBlank(mapText(module, "nameHint"), mapText(module, "name"), "");
        String moduleTitle = firstNonBlank(mapText(module, "title"), "");

        AiFormDesignDslGenerateRequest dslRequest = new AiFormDesignDslGenerateRequest();
        dslRequest.setRequirement(buildModuleFormDslRequirement(request, moduleName, moduleTitle, formName, formTitle));
        dslRequest.setMode(AiFormDesignDslGenerateRequest.MODE_CREATE);
        dslRequest.setScene(normalizeModuleFormDslScene(firstNonBlank(request.getScene(), mapText(module, "scene"), "normal")));
        dslRequest.setCurrentDslJson(request.getOptions() == null ? "" : defaultText(request.getOptions().getCurrentDslJson(), ""));

        AiFormDesignDslGenerateRequest.CurrentForm currentForm = new AiFormDesignDslGenerateRequest.CurrentForm();
        currentForm.setFormName(formName);
        currentForm.setFormTitle(formTitle);
        currentForm.setModuleName(moduleName);
        dslRequest.setCurrentForm(currentForm);

        AiFormDesignDslGenerateRequest.Options options = new AiFormDesignDslGenerateRequest.Options();
        options.setFallbackToLocal(false);
        options.setSaveAsServerDraft(false);
        options.setTemperature(request.getOptions() == null ? 0.15 : request.getOptions().getTemperature());
        dslRequest.setOptions(options);

        if (!genAIService.isAvailable(loginName)) {
            return generateDsl(dslRequest, loginName);
        }

        String model = genAIService.getConfiguredModel(loginName);
        ModuleFormDslCacheKey cacheKey = buildModuleFormDslCacheKey(dslRequest, loginName, model);
        AiFormDesignDslGenerateResponse cachedResponse = getCachedModuleFormDslResponse(cacheKey, model, dslRequest);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        AiFormDesignDslGenerateResponse response = generateDsl(dslRequest, loginName);
        putCachedModuleFormDslResponse(cacheKey, response);
        return response;
    }

    private String buildModuleFormDslRequirement(AiModuleFormDslGenerateRequest request,
                                                 String moduleName,
                                                 String moduleTitle,
                                                 String formName,
                                                 String formTitle) {
        Map<String, Object> module = request.getModule();
        Map<String, Object> page = request.getPage();
        Map<String, Object> form = request.getForm();
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据模块级 AI 蓝图中的单个表单材料，生成完整可应用的 FormDesignDSL v1。\n");
        prompt.append("只生成当前这一个表单，不要生成整个模块的其它表单。\n\n");
        prompt.append("硬性要求：\n");
        prompt.append("- dsl.form.name 必须使用：").append(formName).append("\n");
        prompt.append("- dsl.form.title 必须使用：").append(formTitle).append("\n");
        if (StringUtil.isNotBlank(moduleName)) {
            prompt.append("- dsl.form.module 优先使用：").append(moduleName).append("\n");
        }
        prompt.append("- 字段 name 必须是有业务含义的小写英文 snake_case，禁止 field_01、col_1、unnamed_field。\n");
        prompt.append("- 严格参考 form.fields 里的 label/nameHint/typeHint/requiredHint/spanHint/listHint/queryHint/queryMode/valueExample。\n");
        prompt.append("- 列表字段和查询条件要参考 listHint/queryHint/listPriority/queryPriority，不要把所有字段都放到列表或查询。\n");
        prompt.append("- 下拉、单选、多选字段要生成 dictionaries 建议；已给 dictionaryHints 时优先沿用。\n");
        prompt.append("- 部门选择使用 office 类型，人员选择使用 user/users 类型，区域选择使用 area 类型。\n");
        prompt.append("- 描述、说明、备注、内容、意见、原因、要求、正文等长文本字段使用 textarea/richText，span=24，通常不进列表和查询。\n");
        prompt.append("- 表格样例数据只能作为 valueExample 参考，不能作为默认值。\n\n");
        prompt.append("模块信息：\n");
        prompt.append("- module.nameHint: ").append(moduleName).append("\n");
        prompt.append("- module.title: ").append(moduleTitle).append("\n");
        prompt.append("- sourceType: ").append(defaultText(request.getSourceType(), "")).append("\n");
        prompt.append("- sourceUrl: ").append(defaultText(request.getSourceUrl(), "")).append("\n\n");
        prompt.append("页面信息 JSON：\n").append(JSON.toJSONString(page == null ? new JSONObject() : page)).append("\n\n");
        prompt.append("当前表单材料 JSON：\n").append(JSON.toJSONString(form)).append("\n\n");
        prompt.append("模块级字典建议 JSON：\n").append(JSON.toJSONString(request.getDictionaries() == null ? new ArrayList<>() : request.getDictionaries())).append("\n\n");
        prompt.append("模块级表单关系 JSON：\n").append(JSON.toJSONString(request.getRelations() == null ? new ArrayList<>() : request.getRelations())).append("\n\n");
        prompt.append("请输出最终 FormDesignDSL JSON。");
        return prompt.toString();
    }

    private String normalizeModuleFormDslScene(String scene) {
        String normalized = defaultText(scene, "normal").trim();
        if ("document".equals(normalized)) {
            return "document-form";
        }
        if ("document-form".equals(normalized) || "approval".equals(normalized) || "ledger".equals(normalized)) {
            return normalized;
        }
        return "normal";
    }

    private String mapText(Map<String, Object> map, String key) {
        if (map == null || key == null || !map.containsKey(key) || map.get(key) == null) {
            return "";
        }
        return String.valueOf(map.get(key));
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    /**
     * 根据自然语言需求或当前草稿生成 FormDesignDSL，是单表单 AI 创建的核心入口。
     */
    public AiFormDesignDslGenerateResponse generateDsl(AiFormDesignDslGenerateRequest request, String loginName) throws Exception {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        if (request == null || StringUtil.isBlank(request.getRequirement())) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_REQUIREMENT_EMPTY,
                    "Requirement cannot be empty"
            );
            return completeResponse(response, requestId, null, request, startedAt);
        }

        if (!genAIService.isAvailable(loginName)) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_NOT_CONFIGURED,
                    "AI configuration is not available"
            );
            return completeResponse(response, requestId, null, request, startedAt);
        }

        String model = genAIService.getConfiguredModel(loginName);
        String rawOutput;
        long aiStartedAt = System.currentTimeMillis();
        try {
            rawOutput = genAIService.callChatCompletionsJsonObject(
                    buildSystemPrompt(),
                    buildUserPrompt(request),
                    loginName,
                    normalizeTemperature(request),
                    FORM_DSL_MAX_TOKENS
            );
        } catch (Exception ex) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_REQUEST_FAILED,
                    "AI request failed: " + ex.getMessage()
            );
            return completeResponse(response, requestId, model, request, startedAt);
        }
        long aiElapsedMs = System.currentTimeMillis() - aiStartedAt;
        logger.info("FormDesignAI chatCompletions returned requestId={} model={} aiElapsedMs={} rawLength={}",
                requestId, model, aiElapsedMs, rawOutput == null ? 0 : rawOutput.length());

        String jsonText = genAIService.extractJson(rawOutput);
        if (StringUtil.isBlank(jsonText)) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output is not valid JSON"
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeResponse(response, requestId, model, request, startedAt);
        }

        JSONObject dsl;
        try {
            dsl = JSONObject.parseObject(jsonText);
        } catch (Exception ex) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_OUTPUT_INVALID_JSON,
                    "AI output JSON parse failed: " + ex.getMessage()
            );
            response.setRawOutputPreview(preview(rawOutput));
            return completeResponse(response, requestId, model, request, startedAt);
        }

        List<AiFormDesignDslGenerateResponse.Issue> issues = validateDsl(dsl);
        if (!issues.isEmpty()) {
            AiFormDesignDslGenerateResponse response = AiFormDesignDslGenerateResponse.failed(
                    AiFormDesignDslGenerateResponse.ERROR_DSL_SCHEMA_INVALID,
                    "AI output does not match FormDesignDSL v1"
            );
            response.setDslJson(dsl.toJSONString());
            response.setIssues(issues);
            response.setRawOutputPreview(preview(rawOutput));
            response.setSummary(buildSummary(dsl));
            return completeResponse(response, requestId, model, request, startedAt);
        }

        AiFormDesignDslGenerateResponse response = new AiFormDesignDslGenerateResponse();
        response.setSuccess(true);
        response.setProvider(AiFormDesignDslGenerateResponse.PROVIDER_REMOTE);
        response.setModel(model);
        response.setPromptVersion(PROMPT_VERSION_FORM_DSL_V1);
        response.setDslJson(dsl.toJSONString());
        response.setSummary(buildSummary(dsl));
        response.setIssues(new ArrayList<>());
        response.setRawOutputPreview(preview(rawOutput));
        response.setMessage("FormDesignDSL generated");
        return completeResponse(response, requestId, model, request, startedAt);
    }

    private String buildMaterialSystemPrompt() {
        return "You are a senior form material recognition assistant. "
                + "Convert raw Chinese business material into one compact valid JSON object for FormMaterial v1 only. "
                + "No markdown, comments, explanations, or code fences. "
                + "Do not output reasoning, analysis, or <think> blocks. "
                + "Required top-level shape: "
                + "{\"version\":\"1.0\",\"source\":{\"type\":\"table-text\",\"name\":\"\"},\"title\":\"中文标题\",\"scene\":\"normal|document-form|approval|ledger\","
                + "\"language\":\"zh-CN\",\"fields\":[{\"id\":\"field_1\",\"label\":\"中文字段名\",\"nameHint\":\"meaningful_english_snake_case\","
                + "\"typeHint\":\"text\",\"groupKey\":\"base\",\"requiredHint\":false,\"listHint\":true,\"listPriority\":80,\"listReason\":\"适合作为列表主显示字段\","
                + "\"queryHint\":false,\"queryPriority\":0,\"queryMode\":\"\",\"queryReason\":\"不适合作为常用查询条件\",\"spanHint\":12,"
                + "\"optionItems\":[{\"value\":\"normal\",\"text\":\"普通\"}],\"valueExample\":\"\",\"confidence\":0.86,\"rawText\":\"原始字段文本\",\"issues\":[]}],"
                + "\"groups\":[{\"key\":\"base\",\"title\":\"基本信息\"}],"
                + "\"tables\":[{\"id\":\"table_1\",\"title\":\"\",\"headers\":[\"字段1\"],\"rows\":[[\"样例值\"]],\"confidence\":0.86}],"
                + "\"rawText\":\"原始材料\",\"confidence\":0.86,\"issues\":[],\"meta\":{}}. "
                + "Allowed source.type values: " + String.join(", ", MATERIAL_SOURCE_TYPE_OPTIONS) + ". "
                + "Allowed scene values: normal, document-form, approval, ledger. "
                + "Allowed field typeHint values: " + String.join(", ", FIELD_TYPE_OPTIONS) + ". "
                + "Use typeHint=user for single person selectors, users for multiple person selectors, office for department/organization selectors, area for region selectors, tree for tree selectors, and modalSelect for custom modal selectors. "
                + "For select/radio/checkbox fields, infer optionItems from visible material facts when possible; each item must have stable English value and Chinese text. Leave optionItems empty when the material does not provide enough evidence. "
                + "For each field, nameHint must be a meaningful lowercase English snake_case name that starts with a letter. "
                + "Never use meaningless names like field_01, field_02, col_1, or unnamed_field as nameHint; use a conservative business English name based on the visible label and material context. "
                + "Use common business English: 项目名称=project_name, 项目编号=project_code, 联系人电话=contact_phone, 责任部门=responsible_office. "
                + "For list suggestions, set listHint=true only for practical summary columns such as 名称/标题/编号/文号/状态/类型/日期/申请人/部门/单位/金额; give listPriority 0-100. "
                + "Keep normal lists compact: usually 4 to 7 fields. Do not mark every field as listHint=true. "
                + "For query suggestions, set queryHint=true only for common filters such as 名称/标题/编号/文号/状态/类型/日期/部门/人员; give queryPriority 0-100. "
                + "Keep query area compact: usually 3 to 5 fields. Do not mark every field as queryHint=true. "
                + "Allowed queryMode values: " + String.join(", ", MATERIAL_QUERY_MODE_OPTIONS) + ". Use like for names/titles, exact for codes/status/type, date-range for dates, range for numbers. "
                + "Long text fields such as 描述, 说明, 备注, 内容, 意见, 原因, 要求 should use typeHint=textarea, spanHint=24, listHint=false, queryHint=false, listPriority=0, queryPriority=0. "
                + "Sample data from pasted tables belongs in valueExample only; it is not a form default value. Do not invent defaultValue. "
                + "If a date label contains 年/月, 年月, 月份, or the sample looks like 2024-06, keep the field as date and preserve this clue in rawText/valueExample so the converter can use yyyy-MM. "
                + "If the material is a pasted data table, treat the first row as headers and following rows as sample rows. "
                + "If the material is a field definition table with headers such as 字段/类型/必填/列表/查询, treat each data row as one field. "
                + "Only recognize material facts and suggestions; do not generate final FormDesignDSL.";
    }

    private String buildMaterialUserPrompt(AiFormMaterialRecognizeRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Source type: ").append(defaultText(request.getSourceType(), "table-text")).append("\n");
        prompt.append("Preferred scene: ").append(defaultText(request.getScene(), "auto")).append("\n");
        if (StringUtil.isNotBlank(request.getTitle())) {
            prompt.append("Preferred title: ").append(request.getTitle()).append("\n");
        }
        if (request.getCurrentForm() != null) {
            prompt.append("Current form context:\n");
            prompt.append("- genTableId: ").append(defaultText(request.getCurrentForm().getGenTableId(), "")).append("\n");
            prompt.append("- formName: ").append(defaultText(request.getCurrentForm().getFormName(), "")).append("\n");
            prompt.append("- formTitle: ").append(defaultText(request.getCurrentForm().getFormTitle(), "")).append("\n");
            prompt.append("- moduleName: ").append(defaultText(request.getCurrentForm().getModuleName(), "")).append("\n");
        }
        prompt.append("\nRaw material:\n");
        prompt.append(truncate(request.getRawText(), MAX_MATERIAL_CONTEXT_LENGTH)).append("\n\n");
        prompt.append("Return the FormMaterial JSON object now.");
        return prompt.toString();
    }

    private String buildModuleMaterialSystemPrompt() {
        return "You are a senior enterprise module analyst. "
                + "Return one compact valid JSON object for ModuleMaterial v1 only. "
                + "No markdown, comments, code fences, reasoning, analysis, or <think> blocks. "
                + "This is INITIAL MODULE BLUEPRINT recognition, not final form generation. "
                + "Use this top-level shape: "
                + "{\"version\":\"1.0\",\"source\":{\"type\":\"text\"},"
                + "\"module\":{\"nameHint\":\"module_snake_case\",\"title\":\"中文模块名\",\"description\":\"\",\"scene\":\"normal\"},"
                + "\"menus\":[{\"id\":\"menu_01\",\"title\":\"菜单名\",\"pathHint\":\"\",\"parentId\":\"\",\"sourceRef\":\"\"}],"
                + "\"pages\":[{\"id\":\"page_01\",\"menuId\":\"menu_01\",\"title\":\"页面名\",\"pageType\":\"list_form\",\"description\":\"\",\"sourceRefs\":[],\"confidence\":0.86}],"
                + "\"forms\":[{\"id\":\"form_01\",\"pageId\":\"page_01\",\"title\":\"表单名\",\"nameHint\":\"form_snake_case\",\"fields\":[],\"groups\":[],\"listHints\":[],\"queryHints\":[],\"dictionaryHints\":[],\"confidence\":0.86}],"
                + "\"relations\":[],\"confidence\":0.86,\"issues\":[],\"meta\":{}}. "
                + "Allowed source.type values: " + String.join(", ", MODULE_MATERIAL_SOURCE_TYPE_OPTIONS) + ". "
                + "Allowed module.scene values: normal, document, document-form, approval, ledger, mixed. "
                + "Allowed pageType values: " + String.join(", ", MODULE_PAGE_TYPE_OPTIONS) + ". "
                + "Allowed relation.type values: " + String.join(", ", MODULE_RELATION_TYPE_OPTIONS) + ". "
                + "Allowed field typeHint values: " + String.join(", ", FIELD_TYPE_OPTIONS) + ". "
                + "Recognize module facts, menu/page/form boundaries, main relations, and only representative fields. "
                + "If the material clearly contains many independent business pages/forms, extract up to " + MODULE_BLUEPRINT_MAX_FORMS + " high-confidence forms. Do not exceed " + MODULE_BLUEPRINT_MAX_FORMS + " forms in forms[]. "
                + "Do not generate FormDesignDSL, SQL, Java code, routes, or final save payloads. "
                + "Keep output small: source.rawText should be omitted or empty; do not copy the raw material into JSON. "
                + "For each form, output at most 8 representative fields in this blueprint step. Later steps will generate full fields page by page. "
                + "For each form, output at most 4 dictionaryHints, 6 listHints, and 5 queryHints. "
                + "If raw material contains page names, menu names, numbered page sections, or phrases ending with 页面, 表, 登记, 审批, 归档, 台账, 统计, menus/pages/forms must not be empty. Empty pages and empty forms are invalid for such material. "
                + "If the material describes multiple business pages, split them into pages and forms. If a page is only a list or report, keep pageType=list/report and do not invent unnecessary form fields. "
                + "For Excel module material, the raw text may contain multiple sections like 'Table block 1: xxx' from one sheet or many sheets. Treat each readable table block as a candidate page/form; sheet names and table block titles are strong page/form title hints. Do not collapse unrelated table blocks into one form. "
                + "For URL prototype material, the raw text may contain sections like 'Confirmed candidate page n'. Treat each confirmed page as a candidate page boundary. Use page title, headings, form labels, inputs, buttons, and table headers as clues. Ignore navigation, footer, copyright, breadcrumbs, and repeated layout text. "
                + "When Excel sheets reference each other by names such as related contract, customer, project, order, or master/detail wording, add relations[] with type=reference or master_detail. "
                + "For each field, nameHint must be meaningful lowercase English snake_case based on the Chinese label and module context. "
                + "Never use meaningless names like field_01, field_02, col_1, column_1, or unnamed_field as nameHint. "
                + "Use common business English: 收文=receive_doc, 发文=send_doc, 标题=title, 文号=document_no, 状态=status, 部门=office, 人员=user, 日期=date. "
                + "Use typeHint=user for single person selectors, users for multiple person selectors, office for department/organization selectors, area for region selectors, upload/imageUpload/onlineFile for attachments. "
                + "Long text fields such as 描述, 说明, 备注, 内容, 意见, 原因, 要求, 正文 should use typeHint=textarea or richText and spanHint=24; they should usually not be list/query fields. "
                + "For select/radio/checkbox fields, infer optionItems from visible material facts when possible; otherwise leave optionItems empty and add a dictionaryHints item with mode=need-confirm. "
                + "For dictionaryHints, use stable code suggestions. New dictionary code should usually be form.nameHint + '_' + field.nameHint or module.nameHint + '_' + business concept. "
                + "For list suggestions, keep each form compact: usually 4 to 7 practical summary fields. Do not mark every field as listHint=true. "
                + "For query suggestions, keep each form compact: usually 3 to 5 filters. Allowed queryMode values: " + String.join(", ", MATERIAL_QUERY_MODE_OPTIONS) + ". "
                + "Sample data from tables belongs in valueExample only; it is not a default value. "
                + "If date label contains 年/月, 年月, 月份, or samples like 2024-06, preserve this clue in rawText/valueExample. "
                + "Prefer fewer high-confidence pages/forms over many speculative ones. Use issues[] to record uncertainty.";
    }

    private String buildModuleMaterialUserPrompt(AiModuleMaterialRecognizeRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Source type: ").append(defaultText(request.getSourceType(), "text")).append("\n");
        prompt.append("Preferred module scene: ").append(defaultText(request.getScene(), "auto")).append("\n");
        if (StringUtil.isNotBlank(request.getModuleName())) {
            prompt.append("Preferred module nameHint: ").append(request.getModuleName()).append("\n");
        }
        if (StringUtil.isNotBlank(request.getModuleTitle())) {
            prompt.append("Preferred module title: ").append(request.getModuleTitle()).append("\n");
        }
        if (StringUtil.isNotBlank(request.getSourceUrl())) {
            prompt.append("Source URL: ").append(request.getSourceUrl()).append("\n");
        }
        prompt.append("\nRaw module material:\n");
        prompt.append(truncate(request.getRawText(), MAX_MATERIAL_CONTEXT_LENGTH)).append("\n\n");
        prompt.append("Extraction rules for this material:\n");
        prompt.append("- If the text contains headings like 1. xxx页面, each heading is one pages[] item.\n");
        prompt.append("- If a page collects or edits business data, create a matching forms[] item and connect it by pageId.\n");
        prompt.append("- If a page is only statistics/report/query display, keep it in pages[] and do not invent a form.\n");
        prompt.append("- For Excel source, each 'Table block n: title' section is a candidate page/form. Use the table block title and Sheet title as the main page/form title clue and keep fields under their own table block.\n");
        prompt.append("- For Excel source with multiple sheets or multiple table blocks in one sheet, do not merge them into one form unless the material clearly says they are one table split across blocks.\n");
        prompt.append("- For Excel source, use sheet headers as field labels and sample rows only as valueExample/type clues, never as default values.\n");
        prompt.append("- For URL source, each 'Confirmed candidate page n' section is a candidate page boundary. Use headings, form labels, inputs, buttons, and table headers as page/form clues.\n");
        prompt.append("- For URL source, ignore navigation, footer, copyright, breadcrumbs, repeated layout text, and generic buttons unless they clearly belong to the current business page.\n");
        prompt.append("- If the material clearly contains many independent business pages/forms, extract up to ").append(MODULE_BLUEPRINT_MAX_FORMS).append(" high-confidence forms; do not exceed ").append(MODULE_BLUEPRINT_MAX_FORMS).append(" forms.\n");
        prompt.append("- If moduleName/moduleTitle appears in the raw material, fill module.nameHint and module.title.\n");
        prompt.append("- This step only confirms the module blueprint. Keep fields compact: at most 8 representative fields per form.\n");
        prompt.append("- Do not copy long raw material or long field descriptions into source.rawText, field.rawText, valueExample, or issues.\n");
        prompt.append("- Return at least one page or one form when the material describes a module.\n\n");
        prompt.append("Return the ModuleMaterial JSON object now.");
        return prompt.toString();
    }

    private String buildModuleMaterialRepairUserPrompt(AiModuleMaterialRecognizeRequest request,
                                                       JSONObject previousMaterial,
                                                       List<AiModuleMaterialRecognizeResponse.Issue> previousIssues) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("The previous ModuleMaterial output is invalid because it did not extract a usable module blueprint.\n");
        prompt.append("You must re-read the raw material and return a corrected complete ModuleMaterial JSON object.\n\n");
        prompt.append("Previous issues:\n");
        if (previousIssues != null) {
            for (AiModuleMaterialRecognizeResponse.Issue issue : previousIssues) {
                prompt.append("- ").append(defaultText(issue.getCode(), "issue"))
                        .append(": ").append(defaultText(issue.getDescription(), issue.getTitle())).append("\n");
            }
        }
        prompt.append("\nPrevious invalid output:\n");
        prompt.append(previousMaterial == null ? "{}" : truncate(previousMaterial.toJSONString(), 4000)).append("\n\n");
        prompt.append("Raw module material:\n");
        prompt.append(truncate(request.getRawText(), MAX_MATERIAL_CONTEXT_LENGTH)).append("\n\n");
        prompt.append("Mandatory correction rules:\n");
        prompt.append("1. Do not return empty menus/pages/forms when the raw material lists pages or forms.\n");
        prompt.append("2. For every section named 'xxx页面', create one pages[] item.\n");
        prompt.append("3. For every page with fields after words like 包括/包含/录入/登记, create one forms[] item with those fields.\n");
        prompt.append("4. For statistics/query-only pages, create pages[] only, with pageType=report or list.\n");
        prompt.append("5. Fill module.title and module.nameHint from the raw material when available.\n");
        prompt.append("6. If the material enumerates pages with 1/2/3 numbering, pages[].length should usually match those business page sections.\n");
        prompt.append("7. If the material clearly contains many independent business pages/forms, extract up to ").append(MODULE_BLUEPRINT_MAX_FORMS).append(" high-confidence forms; do not exceed ").append(MODULE_BLUEPRINT_MAX_FORMS).append(" forms.\n");
        prompt.append("8. Keep the correction compact: at most 8 representative fields per form, and omit source.rawText.\n");
        prompt.append("9. Field nameHint must be meaningful English snake_case, never field_01/field_02.\n");
        prompt.append("10. The corrected output must pass this check: pages or forms contains at least one item.\n\n");
        prompt.append("Return the corrected ModuleMaterial JSON object only.");
        return prompt.toString();
    }

    private AiFormMaterialRecognizeRequest buildExcelRecognizeRequest(AiFormMaterialExcelParseRequest request, String rawText) {
        AiFormMaterialRecognizeRequest recognizeRequest = new AiFormMaterialRecognizeRequest();
        recognizeRequest.setRawText(defaultText(rawText, ""));
        recognizeRequest.setSourceType("excel");
        recognizeRequest.setScene(request == null ? "auto" : defaultText(request.getScene(), "auto"));
        recognizeRequest.setTitle(request == null ? "" : defaultText(request.getTitle(), inferTitleFromFileName(request.getFileName())));
        recognizeRequest.setCurrentForm(request == null ? null : request.getCurrentForm());
        AiFormMaterialRecognizeRequest.Options options = new AiFormMaterialRecognizeRequest.Options();
        if (request != null && request.getOptions() != null) {
            options.setTemperature(request.getOptions().getTemperature());
        }
        recognizeRequest.setOptions(options);
        return recognizeRequest;
    }

    private AiFormMaterialRecognizeRequest buildFileRecognizeRequest(AiFormMaterialFileParseRequest request, String rawText, String sourceType) {
        AiFormMaterialRecognizeRequest recognizeRequest = new AiFormMaterialRecognizeRequest();
        recognizeRequest.setRawText(defaultText(rawText, ""));
        recognizeRequest.setSourceType(defaultText(sourceType, inferSourceTypeFromRequest(request)));
        recognizeRequest.setScene(request == null ? "auto" : defaultText(request.getScene(), "auto"));
        recognizeRequest.setTitle(request == null ? "" : defaultText(request.getTitle(), inferTitleFromFileName(request.getFileName())));
        recognizeRequest.setCurrentForm(request == null ? null : request.getCurrentForm());
        AiFormMaterialRecognizeRequest.Options options = new AiFormMaterialRecognizeRequest.Options();
        if (request != null && request.getOptions() != null) {
            options.setTemperature(request.getOptions().getTemperature());
        }
        recognizeRequest.setOptions(options);
        return recognizeRequest;
    }

    private AiModuleMaterialRecognizeRequest buildModuleFileRecognizeRequest(AiModuleMaterialFileParseRequest request, String rawText, String sourceType) {
        AiModuleMaterialRecognizeRequest recognizeRequest = new AiModuleMaterialRecognizeRequest();
        recognizeRequest.setRawText(defaultText(rawText, ""));
        recognizeRequest.setSourceType(defaultText(sourceType, inferSourceTypeFromModuleRequest(request)));
        recognizeRequest.setScene(request == null ? "auto" : defaultText(request.getScene(), "auto"));
        recognizeRequest.setModuleName(request == null ? "" : defaultText(request.getModuleName(), ""));
        recognizeRequest.setModuleTitle(request == null ? "" : defaultText(request.getModuleTitle(), inferTitleFromFileName(request.getFileName())));
        AiModuleMaterialRecognizeRequest.Options options = new AiModuleMaterialRecognizeRequest.Options();
        if (request != null && request.getOptions() != null) {
            options.setTemperature(request.getOptions().getTemperature());
        }
        recognizeRequest.setOptions(options);
        return recognizeRequest;
    }

    private AiFormMaterialFileParseRequest buildModuleFileExtractionRequest(AiModuleMaterialFileParseRequest request) {
        AiFormMaterialFileParseRequest extractionRequest = new AiFormMaterialFileParseRequest();
        if (request == null) {
            return extractionRequest;
        }
        extractionRequest.setFileName(request.getFileName());
        extractionRequest.setMimeType(request.getMimeType());
        extractionRequest.setSourceType(request.getSourceType());
        extractionRequest.setFileBase64(request.getFileBase64());
        extractionRequest.setScene(request.getScene());
        extractionRequest.setTitle(request.getModuleTitle());
        if (request.getOptions() != null) {
            AiFormMaterialFileParseRequest.Options options = new AiFormMaterialFileParseRequest.Options();
            options.setTemperature(request.getOptions().getTemperature());
            options.setSheetIndex(request.getOptions().getSheetIndex());
            options.setExtractAllSheets(request.getOptions().getExtractAllSheets());
            options.setMaxRows(request.getOptions().getMaxRows());
            options.setMaxColumns(request.getOptions().getMaxColumns());
            options.setMaxPages(request.getOptions().getMaxPages());
            options.setPageIndexes(request.getOptions().getPageIndexes());
            options.setMaxChars(request.getOptions().getMaxChars());
            options.setExtractTables(request.getOptions().getExtractTables());
            options.setExtractHeaders(request.getOptions().getExtractHeaders());
            options.setOcrProvider(request.getOptions().getOcrProvider());
            options.setOcrLanguage(request.getOptions().getOcrLanguage());
            if (options.getSheetIndex() == null
                    && options.getExtractAllSheets() == null
                    && "excel".equals(inferSourceTypeFromModuleRequest(request))) {
                options.setExtractAllSheets(true);
            }
            extractionRequest.setOptions(options);
        } else if ("excel".equals(inferSourceTypeFromModuleRequest(request))) {
            AiFormMaterialFileParseRequest.Options options = new AiFormMaterialFileParseRequest.Options();
            options.setExtractAllSheets(true);
            extractionRequest.setOptions(options);
        }
        return extractionRequest;
    }

    private void enrichFileMaterialResponse(AiFormMaterialRecognizeResponse response,
                                            FileMaterialExtractionService.FileMaterialContent fileMaterial) {
        if (response == null || StringUtil.isBlank(response.getMaterialJson())) {
            return;
        }
        try {
            JSONObject material = JSONObject.parseObject(response.getMaterialJson());
            JSONObject source = material.getJSONObject("source");
            if (source == null) {
                source = new JSONObject();
                material.put("source", source);
            }
            source.put("type", defaultText(fileMaterial.getSourceType(), "excel"));
            if (StringUtil.isBlank(source.getString("name"))) {
                source.put("name", defaultText(fileMaterial.getFileName(), ""));
            }
            if (StringUtil.isBlank(source.getString("mimeType"))) {
                source.put("mimeType", defaultText(fileMaterial.getMimeType(), ""));
            }
            if (fileMaterial.getSize() > 0 && source.getLong("size") == null) {
                source.put("size", fileMaterial.getSize());
            }
            JSONObject meta = material.getJSONObject("meta");
            if (meta == null) {
                meta = new JSONObject();
                material.put("meta", meta);
            }
            copyFileMaterialMeta(meta, fileMaterial);
            JSONObject extraction = buildFileMaterialExtraction(fileMaterial);
            meta.put("extraction", extraction);
            if (StringUtil.isBlank(material.getString("rawText"))) {
                material.put("rawText", fileMaterial.getRawText());
            }
            response.setMaterialJson(material.toJSONString());
            response.setSummary(buildMaterialSummary(material));
            response.setExtraction(extraction);
        } catch (Exception ex) {
            logger.warn("FormDesignAI enrich file material response failed fileName={}", fileMaterial == null ? "" : fileMaterial.getFileName(), ex);
        }
    }

    private void enrichFileModuleMaterialResponse(AiModuleMaterialRecognizeResponse response,
                                                  FileMaterialExtractionService.FileMaterialContent fileMaterial) {
        if (response == null || StringUtil.isBlank(response.getMaterialJson())) {
            return;
        }
        try {
            JSONObject material = JSONObject.parseObject(response.getMaterialJson());
            JSONObject source = material.getJSONObject("source");
            if (source == null) {
                source = new JSONObject();
                material.put("source", source);
            }
            source.put("type", defaultText(fileMaterial.getSourceType(), "excel"));
            if (StringUtil.isBlank(source.getString("name"))) {
                source.put("name", defaultText(fileMaterial.getFileName(), ""));
            }
            if (StringUtil.isBlank(source.getString("fileName"))) {
                source.put("fileName", defaultText(fileMaterial.getFileName(), ""));
            }
            if (StringUtil.isBlank(source.getString("mimeType"))) {
                source.put("mimeType", defaultText(fileMaterial.getMimeType(), ""));
            }
            if (fileMaterial.getSize() > 0 && source.getLong("size") == null) {
                source.put("size", fileMaterial.getSize());
            }
            JSONObject meta = material.getJSONObject("meta");
            if (meta == null) {
                meta = new JSONObject();
                material.put("meta", meta);
            }
            copyFileMaterialMeta(meta, fileMaterial);
            JSONObject extraction = buildFileMaterialExtraction(fileMaterial);
            meta.put("extraction", extraction);
            if (StringUtil.isBlank(source.getString("rawText"))) {
                source.put("rawText", fileMaterial.getRawText());
            }
            response.setMaterialJson(material.toJSONString());
            response.setSummary(buildModuleMaterialSummary(material));
            response.setExtraction(extraction);
        } catch (Exception ex) {
            logger.warn("FormDesignAI enrich file module material response failed fileName={}", fileMaterial == null ? "" : fileMaterial.getFileName(), ex);
        }
    }

    private void copyFileMaterialMeta(JSONObject target, FileMaterialExtractionService.FileMaterialContent fileMaterial) {
        if (fileMaterial == null || fileMaterial.getMeta() == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : fileMaterial.getMeta().entrySet()) {
            target.put(entry.getKey(), entry.getValue());
        }
    }

    private JSONObject buildFileMaterialExtraction(FileMaterialExtractionService.FileMaterialContent fileMaterial) {
        JSONObject extraction = new JSONObject();
        if (fileMaterial == null) {
            return extraction;
        }
        extraction.put("sourceType", defaultText(fileMaterial.getSourceType(), ""));
        extraction.put("fileName", defaultText(fileMaterial.getFileName(), ""));
        extraction.put("mimeType", defaultText(fileMaterial.getMimeType(), ""));
        extraction.put("size", fileMaterial.getSize());
        extraction.put("rawTextLength", fileMaterial.getRawText() == null ? 0 : fileMaterial.getRawText().length());
        extraction.put("tableCount", fileMaterial.getTables() == null ? 0 : fileMaterial.getTables().size());
        extraction.put("usedOcr", false);
        extraction.put("ocrProvider", "none");
        JSONArray tables = new JSONArray();
        if (fileMaterial.getTables() != null) {
            for (FileMaterialExtractionService.FileMaterialTable table : fileMaterial.getTables()) {
                JSONObject tableJson = new JSONObject();
                tableJson.put("id", defaultText(table.getId(), ""));
                tableJson.put("title", defaultText(table.getTitle(), ""));
                tableJson.put("sheetIndex", table.getSheetIndex());
                tableJson.put("sheetName", defaultText(table.getSheetName(), ""));
                tableJson.put("tableIndexInSheet", table.getTableIndexInSheet());
                tableJson.put("headers", table.getHeaders() == null ? new JSONArray() : JSON.parseArray(JSON.toJSONString(table.getHeaders())));
                tableJson.put("headerRowIndex", table.getHeaderRowIndex());
                tableJson.put("rowCount", table.getRawRows() == null ? 0 : table.getRawRows().size());
                tableJson.put("dataRowCount", table.getRows() == null ? 0 : table.getRows().size());
                tableJson.put("confidence", table.getConfidence());
                tables.add(tableJson);
            }
        }
        extraction.put("tables", tables);
        if (fileMaterial.getMeta() != null) {
            for (Map.Entry<String, Object> entry : fileMaterial.getMeta().entrySet()) {
                extraction.put(entry.getKey(), entry.getValue());
            }
        }
        return extraction;
    }

    private String inferSourceTypeFromRequest(AiFormMaterialFileParseRequest request) {
        if (request == null) {
            return "text";
        }
        String sourceType = defaultText(request.getSourceType(), "").trim().toLowerCase();
        if (StringUtil.isNotBlank(sourceType) && !"auto".equals(sourceType)) {
            return sourceType;
        }
        String fileName = defaultText(request.getFileName(), "").toLowerCase();
        String mimeType = defaultText(request.getMimeType(), "").toLowerCase();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls") || mimeType.contains("spreadsheet") || mimeType.contains("excel")) {
            return "excel";
        }
        if (fileName.endsWith(".docx") || mimeType.contains("wordprocessingml.document")) {
            return "word";
        }
        if (fileName.endsWith(".pdf") || "application/pdf".equals(mimeType)) {
            return "pdf";
        }
        if (mimeType.startsWith("image/") || fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".webp")) {
            return "image";
        }
        return "text";
    }

    private String inferSourceTypeFromModuleRequest(AiModuleMaterialFileParseRequest request) {
        if (request == null) {
            return "text";
        }
        AiFormMaterialFileParseRequest formRequest = new AiFormMaterialFileParseRequest();
        formRequest.setSourceType(request.getSourceType());
        formRequest.setFileName(request.getFileName());
        formRequest.setMimeType(request.getMimeType());
        return inferSourceTypeFromRequest(formRequest);
    }

    private String inferTitleFromFileName(String fileName) {
        String text = defaultText(fileName, "").trim();
        int slashIndex = Math.max(text.lastIndexOf('/'), text.lastIndexOf('\\'));
        if (slashIndex >= 0) {
            text = text.substring(slashIndex + 1);
        }
        int dotIndex = text.lastIndexOf('.');
        if (dotIndex > 0) {
            text = text.substring(0, dotIndex);
        }
        return defaultText(text, "Excel导入表单");
    }

    private String buildSystemPrompt() {
        return "You are a senior metadata form designer. Return one compact valid JSON object for FormDesignDSL v1 only. "
                + "No markdown, comments, explanations, or code fences. "
                + "Do not output reasoning, analysis, or <think> blocks. "
                + "Required top-level keys: version, generator, form, layout, fields, list. "
                + "Use this compact shape: "
                + "{\"version\":\"1.0\",\"generator\":\"remote-ai\",\"form\":{\"name\":\"snake_case\",\"title\":\"中文标题\",\"module\":\"oas\",\"tableType\":\"0\",\"pkColumnName\":\"id\"},"
                + "\"layout\":{\"style\":\"normal|document-form|dense|approval\",\"labelWidth\":100,\"groups\":[{\"key\":\"base\",\"title\":\"基本信息\"}]},"
                + "\"fields\":[{\"name\":\"snake_case\",\"label\":\"中文字段名\",\"type\":\"text\",\"group\":\"base\",\"jdbcType\":\"varchar\",\"javaType\":\"String\",\"required\":false,\"readonly\":false,\"span\":12,\"isForm\":true,\"isList\":true,\"isQuery\":false}],"
                + "\"dictionaries\":[{\"fieldName\":\"status\",\"code\":\"form_name_status\",\"name\":\"表单标题-状态\",\"mode\":\"create\",\"items\":[{\"value\":\"enabled\",\"text\":\"启用\"}]}],"
                + "\"list\":{\"buttons\":[\"add\",\"batch-delete\",\"export\"],\"rowButtons\":[\"view\",\"edit\"],\"queryArea\":{\"labelWidth\":80}}}. "
                + "Do not generate nested field.db, field.form, field.list, field.query, actions, rules, or raw unless strictly necessary. "
                + "Allowed field type values: " + String.join(", ", FIELD_TYPE_OPTIONS) + ". "
                + "For select/radio/checkbox fields, add a matching dictionaries item. Dictionary code should normally be form.name + '_' + field.name. Dictionary name should normally be form.title + '-' + field.label. Use mode=create for new dictionaries, use-existing only when the requirement clearly names an existing system dictionary, and ignore when the field should not use a system dictionary. "
                + "Dictionary items must use stable English value and Chinese text. Leave items empty if options are unknown; do not invent business-critical options without evidence. "
                + "For entity selection fields, use the exact Java type: user/users -> com.jeestudio.bpm.common.entity.system.User, "
                + "office -> com.jeestudio.bpm.common.entity.system.Office, area -> com.jeestudio.bpm.common.entity.system.Area, "
                + "tree/modalSelect -> com.jeestudio.bpm.common.entity.common.Zform. Do not use String for these selection entity types. "
                + "Names must be lowercase snake_case and start with a letter. "
                + "Use 6 to 16 practical business fields. "
                + "For document forms, use layout.style=document-form; title/opinion/result/attachment fields should use span 24; textarea/richText/upload fields should usually set isList=false. "
                + "For approval forms, include applicant/office/date/opinion/attachment fields when useful. "
                + "Return JSON only.";
    }

    private AiFormMaterialRecognizeResponse completeMaterialResponse(AiFormMaterialRecognizeResponse response, String requestId,
                                                                     String model, AiFormMaterialRecognizeRequest request,
                                                                     long startedAt) {
        long elapsedMs = System.currentTimeMillis() - startedAt;
        response.setRequestId(requestId);
        if (StringUtil.isBlank(response.getProvider())) {
            response.setProvider(AiFormMaterialRecognizeResponse.PROVIDER_REMOTE);
        }
        response.setPromptVersion(PROMPT_VERSION_FORM_MATERIAL_V1);
        if (StringUtil.isBlank(response.getModel()) && StringUtil.isNotBlank(model)) {
            response.setModel(model);
        }
        response.setElapsedMs(elapsedMs);

        int issueCount = response.getIssues() == null ? 0 : response.getIssues().size();
        int fieldCount = response.getSummary() == null || response.getSummary().getFieldCount() == null
                ? 0
                : response.getSummary().getFieldCount();
        int rawLength = request == null || request.getRawText() == null ? 0 : request.getRawText().length();
        String sourceType = request == null ? "" : defaultText(request.getSourceType(), "table-text");
        String scene = request == null ? "" : defaultText(request.getScene(), "auto");

        if (Boolean.TRUE.equals(response.getSuccess())) {
            logger.info("FormDesignAI recognizeMaterial success requestId={} model={} promptVersion={} elapsedMs={} sourceType={} scene={} rawLength={} fieldCount={} issueCount={}",
                    requestId, response.getModel(), response.getPromptVersion(), elapsedMs, sourceType, scene, rawLength, fieldCount, issueCount);
        } else {
            logger.warn("FormDesignAI recognizeMaterial failed requestId={} errorCode={} model={} promptVersion={} elapsedMs={} sourceType={} scene={} rawLength={} issueCount={}",
                    requestId, response.getErrorCode(), response.getModel(), response.getPromptVersion(), elapsedMs, sourceType, scene, rawLength, issueCount);
        }
        return response;
    }

    private AiModuleMaterialRecognizeResponse completeModuleMaterialResponse(AiModuleMaterialRecognizeResponse response, String requestId,
                                                                             String model, AiModuleMaterialRecognizeRequest request,
                                                                             long startedAt) {
        long elapsedMs = System.currentTimeMillis() - startedAt;
        response.setRequestId(requestId);
        if (StringUtil.isBlank(response.getProvider())) {
            response.setProvider(AiModuleMaterialRecognizeResponse.PROVIDER_REMOTE);
        }
        response.setPromptVersion(PROMPT_VERSION_MODULE_MATERIAL_V1);
        if (StringUtil.isBlank(response.getModel()) && StringUtil.isNotBlank(model)) {
            response.setModel(model);
        }
        response.setElapsedMs(elapsedMs);

        int issueCount = response.getIssues() == null ? 0 : response.getIssues().size();
        int pageCount = response.getSummary() == null || response.getSummary().getPageCount() == null
                ? 0
                : response.getSummary().getPageCount();
        int formCount = response.getSummary() == null || response.getSummary().getFormCount() == null
                ? 0
                : response.getSummary().getFormCount();
        int rawLength = request == null || request.getRawText() == null ? 0 : request.getRawText().length();
        String sourceType = request == null ? "" : defaultText(request.getSourceType(), "text");
        String scene = request == null ? "" : defaultText(request.getScene(), "auto");

        if (Boolean.TRUE.equals(response.getSuccess())) {
            logger.info("FormDesignAI recognizeModuleMaterial success requestId={} model={} promptVersion={} elapsedMs={} sourceType={} scene={} rawLength={} pageCount={} formCount={} issueCount={}",
                    requestId, response.getModel(), response.getPromptVersion(), elapsedMs, sourceType, scene, rawLength, pageCount, formCount, issueCount);
        } else {
            logger.warn("FormDesignAI recognizeModuleMaterial failed requestId={} errorCode={} model={} promptVersion={} elapsedMs={} sourceType={} scene={} rawLength={} issueCount={}",
                    requestId, response.getErrorCode(), response.getModel(), response.getPromptVersion(), elapsedMs, sourceType, scene, rawLength, issueCount);
        }
        return response;
    }

    private AiFormDesignDslGenerateResponse completeResponse(AiFormDesignDslGenerateResponse response, String requestId,
                                                            String model, AiFormDesignDslGenerateRequest request,
                                                            long startedAt) {
        long elapsedMs = System.currentTimeMillis() - startedAt;
        response.setRequestId(requestId);
        response.setProvider(AiFormDesignDslGenerateResponse.PROVIDER_REMOTE);
        response.setPromptVersion(PROMPT_VERSION_FORM_DSL_V1);
        if (StringUtil.isBlank(response.getModel()) && StringUtil.isNotBlank(model)) {
            response.setModel(model);
        }
        response.setElapsedMs(elapsedMs);

        int issueCount = response.getIssues() == null ? 0 : response.getIssues().size();
        int fieldCount = response.getSummary() == null || response.getSummary().getFieldCount() == null
                ? 0
                : response.getSummary().getFieldCount();
        int requirementLength = request == null || request.getRequirement() == null ? 0 : request.getRequirement().length();
        String mode = request == null ? "" : defaultText(request.getMode(), AiFormDesignDslGenerateRequest.MODE_CREATE);
        String scene = request == null ? "" : defaultText(request.getScene(), "normal");

        if (Boolean.TRUE.equals(response.getSuccess())) {
            logger.info("FormDesignAI generateDsl success requestId={} model={} promptVersion={} elapsedMs={} mode={} scene={} requirementLength={} fieldCount={} issueCount={}",
                    requestId, response.getModel(), response.getPromptVersion(), elapsedMs, mode, scene, requirementLength, fieldCount, issueCount);
        } else {
            logger.warn("FormDesignAI generateDsl failed requestId={} errorCode={} model={} promptVersion={} elapsedMs={} mode={} scene={} requirementLength={} issueCount={}",
                    requestId, response.getErrorCode(), response.getModel(), response.getPromptVersion(), elapsedMs, mode, scene, requirementLength, issueCount);
        }
        return response;
    }

    private String buildUserPrompt(AiFormDesignDslGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Requirement:\n").append(request.getRequirement()).append("\n\n");
        prompt.append("Mode: ").append(defaultText(request.getMode(), AiFormDesignDslGenerateRequest.MODE_CREATE)).append("\n");
        prompt.append("Scene: ").append(defaultText(request.getScene(), "normal")).append("\n");
        if (StringUtil.isNotBlank(request.getTemplateCode())) {
            prompt.append("Template code: ").append(request.getTemplateCode()).append("\n");
        }
        if (request.getCurrentForm() != null) {
            prompt.append("Current form context:\n");
            prompt.append("- genTableId: ").append(defaultText(request.getCurrentForm().getGenTableId(), "")).append("\n");
            prompt.append("- formName: ").append(defaultText(request.getCurrentForm().getFormName(), "")).append("\n");
            prompt.append("- formTitle: ").append(defaultText(request.getCurrentForm().getFormTitle(), "")).append("\n");
            prompt.append("- moduleName: ").append(defaultText(request.getCurrentForm().getModuleName(), "")).append("\n");
        }
        if (StringUtil.isNotBlank(request.getCurrentDslJson())) {
            prompt.append("Current FormDesignDSL for optimization reference:\n");
            prompt.append(truncate(request.getCurrentDslJson(), MAX_CURRENT_DSL_CONTEXT_LENGTH)).append("\n");
        }
        prompt.append("\nGenerate the final FormDesignDSL JSON object now.");
        return prompt.toString();
    }

    private Double normalizeTemperature(AiFormDesignDslGenerateRequest request) {
        Double temperature = request.getOptions() == null ? null : request.getOptions().getTemperature();
        if (temperature == null) {
            return 0.2;
        }
        if (temperature < 0) {
            return 0.0;
        }
        if (temperature > 1) {
            return 1.0;
        }
        return temperature;
    }

    private Double normalizeMaterialTemperature(AiFormMaterialRecognizeRequest request) {
        Double temperature = request.getOptions() == null ? null : request.getOptions().getTemperature();
        if (temperature == null) {
            return 0.1;
        }
        if (temperature < 0) {
            return 0.0;
        }
        if (temperature > 1) {
            return 1.0;
        }
        return temperature;
    }

    private Double normalizeModuleMaterialTemperature(AiModuleMaterialRecognizeRequest request) {
        Double temperature = request.getOptions() == null ? null : request.getOptions().getTemperature();
        if (temperature == null) {
            return 0.1;
        }
        if (temperature < 0) {
            return 0.0;
        }
        if (temperature > 1) {
            return 1.0;
        }
        return temperature;
    }

    private void normalizeMaterial(JSONObject material, AiFormMaterialRecognizeRequest request) {
        if (material == null) {
            return;
        }
        if (StringUtil.isBlank(material.getString("version"))) {
            material.put("version", FORM_MATERIAL_SCHEMA_VERSION);
        }
        JSONObject source = material.getJSONObject("source");
        if (source == null) {
            source = new JSONObject();
            material.put("source", source);
        }
        if (StringUtil.isBlank(source.getString("type"))) {
            source.put("type", defaultText(request.getSourceType(), "table-text"));
        }
        if (StringUtil.isBlank(material.getString("rawText"))) {
            material.put("rawText", request.getRawText());
        }
        if (StringUtil.isBlank(material.getString("language"))) {
            material.put("language", "zh-CN");
        }
        if (StringUtil.isBlank(material.getString("scene"))) {
            material.put("scene", normalizeMaterialScene(request.getScene()));
        }
        if (material.getJSONArray("groups") == null) {
            material.put("groups", new JSONArray());
        }
        if (material.getJSONArray("tables") == null) {
            material.put("tables", new JSONArray());
        }
        if (material.getJSONObject("meta") == null) {
            material.put("meta", new JSONObject());
        }
        if (material.getJSONArray("issues") == null) {
            material.put("issues", new JSONArray());
        }
        JSONArray fields = material.getJSONArray("fields");
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.size(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (field == null) {
                continue;
            }
            if (StringUtil.isBlank(field.getString("id"))) {
                field.put("id", "field_" + (i + 1));
            }
            if (StringUtil.isBlank(field.getString("groupKey"))) {
                field.put("groupKey", "base");
            }
            if (field.get("requiredHint") == null) {
                field.put("requiredHint", false);
            }
            if (field.get("listHint") == null) {
                field.put("listHint", inferMaterialListHint(field));
            }
            if (field.get("listPriority") == null) {
                field.put("listPriority", inferMaterialListPriority(field));
            }
            if (StringUtil.isBlank(field.getString("listReason"))) {
                field.put("listReason", Boolean.TRUE.equals(field.getBoolean("listHint")) ? "适合作为列表摘要字段" : "不适合作为列表摘要字段");
            }
            if (field.get("queryHint") == null) {
                field.put("queryHint", inferMaterialQueryHint(field));
            }
            if (field.get("queryPriority") == null) {
                field.put("queryPriority", inferMaterialQueryPriority(field));
            }
            if (StringUtil.isBlank(field.getString("queryMode")) && Boolean.TRUE.equals(field.getBoolean("queryHint"))) {
                field.put("queryMode", inferMaterialQueryMode(field));
            }
            if (StringUtil.isBlank(field.getString("queryReason"))) {
                field.put("queryReason", Boolean.TRUE.equals(field.getBoolean("queryHint")) ? "适合作为常用查询条件" : "不适合作为常用查询条件");
            }
            if (isFullSpanMaterialType(field.getString("typeHint"))) {
                field.put("spanHint", 24);
            } else if (field.get("spanHint") == null) {
                field.put("spanHint", 12);
            }
            if (field.get("confidence") == null) {
                field.put("confidence", 0.8);
            }
            if (field.getJSONArray("issues") == null) {
                field.put("issues", new JSONArray());
            }
        }
    }

    private void normalizeModuleMaterial(JSONObject material, AiModuleMaterialRecognizeRequest request) {
        if (material == null) {
            return;
        }
        String rawText = request == null ? "" : defaultText(request.getRawText(), "");
        String requestSourceType = request == null ? "" : defaultText(request.getSourceType(), "");
        String requestSourceUrl = request == null ? "" : defaultText(request.getSourceUrl(), "");
        String requestModuleName = request == null ? "" : defaultText(request.getModuleName(), "");
        String requestModuleTitle = request == null ? "" : defaultText(request.getModuleTitle(), "");
        String requestScene = request == null ? "" : defaultText(request.getScene(), "");
        String explicitModuleName = inferExplicitModuleName(rawText);
        String explicitModuleTitle = inferExplicitModuleTitle(rawText);
        if (StringUtil.isBlank(material.getString("version"))) {
            material.put("version", MODULE_MATERIAL_SCHEMA_VERSION);
        }

        JSONObject source = material.getJSONObject("source");
        if (source == null) {
            source = new JSONObject();
            material.put("source", source);
        }
        if (StringUtil.isBlank(source.getString("type"))) {
            source.put("type", defaultText(requestSourceType, "text"));
        }
        if (StringUtil.isBlank(source.getString("url"))) {
            source.put("url", requestSourceUrl);
        }
        if (StringUtil.isBlank(source.getString("name"))) {
            source.put("name", defaultText(requestModuleTitle, explicitModuleTitle));
        }
        if (StringUtil.isBlank(source.getString("rawText"))) {
            source.put("rawText", rawText);
        }
        if (source.getJSONArray("tables") == null) {
            source.put("tables", new JSONArray());
        }
        if (source.getJSONArray("screenshots") == null) {
            source.put("screenshots", new JSONArray());
        }
        if (source.getJSONObject("meta") == null) {
            source.put("meta", new JSONObject());
        }

        JSONObject module = material.getJSONObject("module");
        if (module == null) {
            module = new JSONObject();
            material.put("module", module);
        }
        if (StringUtil.isBlank(module.getString("nameHint"))) {
            module.put("nameHint", defaultText(requestModuleName, explicitModuleName));
        }
        if (StringUtil.isBlank(module.getString("title"))) {
            module.put("title", defaultText(requestModuleTitle, defaultText(explicitModuleTitle, source.getString("name"))));
        }
        if (StringUtil.isBlank(module.getString("description"))) {
            module.put("description", "");
        }
        if (StringUtil.isBlank(module.getString("scene"))) {
            module.put("scene", normalizeModuleScene(requestScene));
        }

        if (material.getJSONArray("menus") == null) {
            material.put("menus", new JSONArray());
        }
        if (material.getJSONArray("pages") == null) {
            material.put("pages", new JSONArray());
        }
        if (material.getJSONArray("forms") == null) {
            material.put("forms", new JSONArray());
        }
        if (material.getJSONArray("relations") == null) {
            material.put("relations", new JSONArray());
        }
        if (material.getJSONArray("issues") == null) {
            material.put("issues", new JSONArray());
        }
        if (material.getJSONObject("meta") == null) {
            material.put("meta", new JSONObject());
        }
        if (material.get("confidence") == null) {
            material.put("confidence", 0.8);
        }

        normalizeModuleMenus(material.getJSONArray("menus"));
        normalizeModulePages(material.getJSONArray("pages"), material.getJSONArray("menus"));
        normalizeModuleForms(material.getJSONArray("forms"), material.getJSONArray("pages"), module);
        normalizeModuleRelations(material.getJSONArray("relations"));
    }

    private void normalizeModuleMenus(JSONArray menus) {
        if (menus == null) {
            return;
        }
        for (int i = 0; i < menus.size(); i++) {
            JSONObject menu = menus.getJSONObject(i);
            if (menu == null) {
                continue;
            }
            if (StringUtil.isBlank(menu.getString("id"))) {
                menu.put("id", "menu_" + (i + 1));
            }
            if (StringUtil.isBlank(menu.getString("pathHint"))) {
                menu.put("pathHint", "");
            }
            if (StringUtil.isBlank(menu.getString("parentId"))) {
                menu.put("parentId", "");
            }
            if (StringUtil.isBlank(menu.getString("sourceRef"))) {
                menu.put("sourceRef", "");
            }
        }
    }

    private void normalizeModulePages(JSONArray pages, JSONArray menus) {
        if (pages == null) {
            return;
        }
        String firstMenuId = "";
        if (menus != null && !menus.isEmpty() && menus.getJSONObject(0) != null) {
            firstMenuId = defaultText(menus.getJSONObject(0).getString("id"), "");
        }
        for (int i = 0; i < pages.size(); i++) {
            JSONObject page = pages.getJSONObject(i);
            if (page == null) {
                continue;
            }
            if (StringUtil.isBlank(page.getString("id"))) {
                page.put("id", "page_" + (i + 1));
            }
            if (StringUtil.isBlank(page.getString("menuId"))) {
                page.put("menuId", firstMenuId);
            }
            if (StringUtil.isBlank(page.getString("pageType"))) {
                page.put("pageType", "list_form");
            }
            if (StringUtil.isBlank(page.getString("description"))) {
                page.put("description", "");
            }
            if (page.getJSONArray("sourceRefs") == null) {
                page.put("sourceRefs", new JSONArray());
            }
            if (page.get("confidence") == null) {
                page.put("confidence", 0.8);
            }
        }
    }

    private void normalizeModuleForms(JSONArray forms, JSONArray pages, JSONObject module) {
        if (forms == null) {
            return;
        }
        String firstPageId = "";
        if (pages != null && !pages.isEmpty() && pages.getJSONObject(0) != null) {
            firstPageId = defaultText(pages.getJSONObject(0).getString("id"), "");
        }
        for (int i = 0; i < forms.size(); i++) {
            JSONObject form = forms.getJSONObject(i);
            if (form == null) {
                continue;
            }
            if (StringUtil.isBlank(form.getString("id"))) {
                form.put("id", "form_" + (i + 1));
            }
            if (StringUtil.isBlank(form.getString("pageId"))) {
                form.put("pageId", firstPageId);
            }
            if (StringUtil.isBlank(form.getString("nameHint"))) {
                String moduleName = module == null ? "" : defaultText(module.getString("nameHint"), "");
                form.put("nameHint", StringUtil.isBlank(moduleName) ? "" : moduleName + "_" + (i + 1));
            }
            if (form.getJSONArray("fields") == null) {
                form.put("fields", new JSONArray());
            }
            if (form.getJSONArray("groups") == null) {
                form.put("groups", new JSONArray());
            }
            if (form.getJSONArray("listHints") == null) {
                form.put("listHints", new JSONArray());
            }
            if (form.getJSONArray("queryHints") == null) {
                form.put("queryHints", new JSONArray());
            }
            if (form.getJSONArray("dictionaryHints") == null) {
                form.put("dictionaryHints", new JSONArray());
            }
            if (form.get("confidence") == null) {
                form.put("confidence", 0.8);
            }
            normalizeModuleFields(form.getJSONArray("fields"));
        }
    }

    private void normalizeModuleFields(JSONArray fields) {
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.size(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (field == null) {
                continue;
            }
            if (StringUtil.isBlank(field.getString("id"))) {
                field.put("id", "field_" + (i + 1));
            }
            if (StringUtil.isBlank(field.getString("typeHint"))) {
                field.put("typeHint", "text");
            }
            if (StringUtil.isBlank(field.getString("groupKey"))) {
                field.put("groupKey", "base");
            }
            if (field.get("requiredHint") == null) {
                field.put("requiredHint", false);
            }
            if (field.get("listHint") == null) {
                field.put("listHint", inferMaterialListHint(field));
            }
            if (field.get("listPriority") == null) {
                field.put("listPriority", inferMaterialListPriority(field));
            }
            if (StringUtil.isBlank(field.getString("listReason"))) {
                field.put("listReason", Boolean.TRUE.equals(field.getBoolean("listHint")) ? "适合作为列表摘要字段" : "不适合作为列表摘要字段");
            }
            if (field.get("queryHint") == null) {
                field.put("queryHint", inferMaterialQueryHint(field));
            }
            if (field.get("queryPriority") == null) {
                field.put("queryPriority", inferMaterialQueryPriority(field));
            }
            if (StringUtil.isBlank(field.getString("queryMode")) && Boolean.TRUE.equals(field.getBoolean("queryHint"))) {
                field.put("queryMode", inferMaterialQueryMode(field));
            }
            if (StringUtil.isBlank(field.getString("queryReason"))) {
                field.put("queryReason", Boolean.TRUE.equals(field.getBoolean("queryHint")) ? "适合作为常用查询条件" : "不适合作为常用查询条件");
            }
            if (isFullSpanMaterialType(field.getString("typeHint"))) {
                field.put("spanHint", 24);
            } else if (field.get("spanHint") == null) {
                field.put("spanHint", 12);
            }
            if (field.getJSONArray("optionItems") == null) {
                field.put("optionItems", new JSONArray());
            }
            if (field.getJSONArray("issues") == null) {
                field.put("issues", new JSONArray());
            }
            if (field.get("confidence") == null) {
                field.put("confidence", 0.8);
            }
        }
    }

    private void normalizeModuleRelations(JSONArray relations) {
        if (relations == null) {
            return;
        }
        for (int i = 0; i < relations.size(); i++) {
            JSONObject relation = relations.getJSONObject(i);
            if (relation == null) {
                continue;
            }
            if (StringUtil.isBlank(relation.getString("type"))) {
                relation.put("type", "unknown");
            }
            if (StringUtil.isBlank(relation.getString("description"))) {
                relation.put("description", "");
            }
        }
    }

    private String normalizeMaterialScene(String scene) {
        String normalized = defaultText(scene, "normal");
        if ("auto".equals(normalized)) {
            return "normal";
        }
        return MATERIAL_SCENE_OPTIONS.contains(normalized) && StringUtil.isNotBlank(normalized)
                ? normalized
                : "normal";
    }

    private String normalizeModuleScene(String scene) {
        String normalized = defaultText(scene, "normal");
        if ("auto".equals(normalized)) {
            return "normal";
        }
        return MODULE_MATERIAL_SCENE_OPTIONS.contains(normalized) && StringUtil.isNotBlank(normalized)
                ? normalized
                : "normal";
    }

    private String inferExplicitModuleName(String rawText) {
        String text = defaultText(rawText, "");
        Matcher matcher = Pattern.compile("(?:模块编码|模块code|moduleName|module_code)(?:建议)?(?:为|是|[:：])\\s*([A-Za-z][A-Za-z0-9_-]*)")
                .matcher(text);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1).replace('-', '_').toLowerCase();
    }

    private String inferExplicitModuleTitle(String rawText) {
        String text = defaultText(rawText, "");
        Matcher matcher = Pattern.compile("(?:模块名称|模块名|模块标题)(?:建议)?(?:为|是|[:：])\\s*([^，,。；;\\r\\n]+)")
                .matcher(text);
        if (!matcher.find()) {
            return "";
        }
        String title = matcher.group(1).trim();
        return title.length() > 40 ? "" : title;
    }

    private boolean inferMaterialListHint(JSONObject field) {
        return inferMaterialListPriority(field) >= 60;
    }

    private boolean inferMaterialQueryHint(JSONObject field) {
        return inferMaterialQueryPriority(field) >= 70;
    }

    private int inferMaterialListPriority(JSONObject field) {
        String label = defaultText(field.getString("label"), "");
        String typeHint = defaultText(field.getString("typeHint"), "");
        if (isMaterialLongTextField(field) || isFullSpanMaterialType(typeHint)) {
            return 0;
        }
        if (containsAny(label, "名称", "标题", "主题")) {
            return 95;
        }
        if (containsAny(label, "编号", "文号", "单号", "编码")) {
            return 90;
        }
        if (containsAny(label, "状态", "类型", "类别", "缓急", "密级")) {
            return 85;
        }
        if (containsAny(label, "日期", "时间")) {
            return 80;
        }
        if (containsAny(label, "申请人", "经办人", "负责人", "部门", "单位", "机构")) {
            return 75;
        }
        if (containsAny(label, "金额", "数量", "费用", "合计")) {
            return 70;
        }
        if (Arrays.asList("select", "radio", "checkbox", "switch", "date", "user", "office").contains(typeHint)) {
            return 65;
        }
        return 40;
    }

    private int inferMaterialQueryPriority(JSONObject field) {
        String label = defaultText(field.getString("label"), "");
        String typeHint = defaultText(field.getString("typeHint"), "");
        if (isMaterialLongTextField(field) || isFullSpanMaterialType(typeHint)) {
            return 0;
        }
        if (containsAny(label, "名称", "标题", "主题", "编号", "文号", "单号", "编码")) {
            return 95;
        }
        if (containsAny(label, "日期", "时间")) {
            return 90;
        }
        if (containsAny(label, "状态", "类型", "类别", "缓急", "密级")) {
            return 85;
        }
        if (containsAny(label, "申请人", "经办人", "负责人", "部门", "单位", "机构")) {
            return 80;
        }
        if (containsAny(label, "金额", "数量", "费用", "合计")) {
            return 65;
        }
        return 30;
    }

    private String inferMaterialQueryMode(JSONObject field) {
        String label = defaultText(field.getString("label"), "");
        String typeHint = defaultText(field.getString("typeHint"), "");
        if ("date".equals(typeHint) || containsAny(label, "日期", "时间")) {
            return "date-range";
        }
        if ("integer".equals(typeHint) || "decimal".equals(typeHint) || containsAny(label, "金额", "数量", "费用", "合计")) {
            return "range";
        }
        if (Arrays.asList("select", "radio", "checkbox", "switch").contains(typeHint)
                || containsAny(label, "状态", "类型", "类别", "缓急", "密级")) {
            return "select";
        }
        if (containsAny(label, "编号", "文号", "单号", "编码")) {
            return "exact";
        }
        return "like";
    }

    private boolean isMaterialLongTextField(JSONObject field) {
        String label = defaultText(field.getString("label"), "");
        String typeHint = defaultText(field.getString("typeHint"), "");
        return "textarea".equals(typeHint)
                || "richText".equals(typeHint)
                || containsAny(label, "描述", "说明", "备注", "内容", "意见", "原因", "要求", "批示", "批分", "结果");
    }

    private boolean containsAny(String text, String... keywords) {
        if (StringUtil.isBlank(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<AiModuleMaterialRecognizeResponse.Issue> validateModuleMaterial(JSONObject material) {
        List<AiModuleMaterialRecognizeResponse.Issue> issues = new ArrayList<>();
        if (material == null) {
            issues.add(moduleMaterialIssue("error", "module-material-root-invalid", "ModuleMaterial root invalid", "ModuleMaterial must be a JSON object."));
            return issues;
        }
        if (!MODULE_MATERIAL_SCHEMA_VERSION.equals(material.getString("version"))) {
            issues.add(moduleMaterialIssue("error", "module-material-version-invalid", "ModuleMaterial version invalid", "version must be 1.0."));
        }
        JSONObject source = material.getJSONObject("source");
        if (source == null) {
            issues.add(moduleMaterialIssue("error", "module-material-source-missing", "ModuleMaterial source missing", "source object is required."));
        } else if (!MODULE_MATERIAL_SOURCE_TYPE_OPTIONS.contains(source.getString("type"))) {
            issues.add(moduleMaterialIssue("error", "module-material-source-type-invalid", "ModuleMaterial source type invalid", "source.type is not supported."));
        }
        JSONObject module = material.getJSONObject("module");
        if (module == null) {
            issues.add(moduleMaterialIssue("error", "module-material-module-missing", "Module missing", "module object is required."));
        } else {
            if (StringUtil.isBlank(module.getString("title"))) {
                issues.add(moduleMaterialIssue("warning", "module-material-module-title-missing", "Module title missing", "module.title is recommended for user confirmation."));
            }
            String moduleName = defaultText(module.getString("nameHint"), "");
            if (StringUtil.isNotBlank(moduleName) && !moduleName.matches("^[a-z][a-z0-9_]*$")) {
                issues.add(moduleMaterialIssue("warning", "module-material-module-name-invalid", "Module name hint invalid", "module.nameHint should be lowercase snake_case and start with a letter."));
            }
            String scene = defaultText(module.getString("scene"), "");
            if (StringUtil.isNotBlank(scene) && !MODULE_MATERIAL_SCENE_OPTIONS.contains(scene)) {
                issues.add(moduleMaterialIssue("warning", "module-material-module-scene-invalid", "Module scene invalid", "module.scene is not supported, frontend will treat it as normal."));
            }
        }

        JSONArray menus = material.getJSONArray("menus");
        JSONArray pages = material.getJSONArray("pages");
        JSONArray forms = material.getJSONArray("forms");
        if ((pages == null || pages.isEmpty()) && (forms == null || forms.isEmpty())) {
            issues.add(moduleMaterialIssue("error", "module-material-blueprint-empty", "Module blueprint empty", "pages or forms must contain at least one recognized item."));
            return issues;
        }
        if (menus == null || menus.isEmpty()) {
            issues.add(moduleMaterialIssue("warning", "module-material-menus-empty", "Menus empty", "menus is empty; frontend can still show page/form blueprint, but menu planning is incomplete."));
        }
        validateModulePages(pages, issues);
        validateModuleForms(forms, issues);
        validateModuleRelations(material.getJSONArray("relations"), issues);
        return issues;
    }

    private void validateModulePages(JSONArray pages, List<AiModuleMaterialRecognizeResponse.Issue> issues) {
        if (pages == null) {
            return;
        }
        for (int i = 0; i < pages.size(); i++) {
            JSONObject page = pages.getJSONObject(i);
            if (page == null) {
                issues.add(moduleMaterialIssue("error", "module-material-page-invalid", "Page invalid", "pages[" + i + "] must be an object."));
                continue;
            }
            String pageId = defaultText(page.getString("id"), "page_" + (i + 1));
            String pageType = defaultText(page.getString("pageType"), "");
            if (StringUtil.isBlank(page.getString("title"))) {
                AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-page-title-missing", "Page title missing", "pages[" + i + "].title is recommended.");
                issue.setPageId(pageId);
                issues.add(issue);
            }
            if (StringUtil.isNotBlank(pageType) && !MODULE_PAGE_TYPE_OPTIONS.contains(pageType)) {
                AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-page-type-invalid", "Page type invalid", "pageType is not supported.");
                issue.setPageId(pageId);
                issues.add(issue);
            }
        }
    }

    private void validateModuleForms(JSONArray forms, List<AiModuleMaterialRecognizeResponse.Issue> issues) {
        if (forms == null) {
            return;
        }
        for (int i = 0; i < forms.size(); i++) {
            JSONObject form = forms.getJSONObject(i);
            if (form == null) {
                issues.add(moduleMaterialIssue("error", "module-material-form-invalid", "Form invalid", "forms[" + i + "] must be an object."));
                continue;
            }
            String formId = defaultText(form.getString("id"), "form_" + (i + 1));
            if (StringUtil.isBlank(form.getString("title"))) {
                AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-form-title-missing", "Form title missing", "forms[" + i + "].title is recommended.");
                issue.setFormId(formId);
                issues.add(issue);
            }
            String nameHint = defaultText(form.getString("nameHint"), "");
            if (StringUtil.isNotBlank(nameHint) && !nameHint.matches("^[a-z][a-z0-9_]*$")) {
                AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-form-name-invalid", "Form name hint invalid", "form.nameHint should be lowercase snake_case and start with a letter.");
                issue.setFormId(formId);
                issues.add(issue);
            }
            JSONArray fields = form.getJSONArray("fields");
            if (fields == null || fields.isEmpty()) {
                AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-form-fields-empty", "Form fields empty", "This form has no recognized fields yet.");
                issue.setFormId(formId);
                issues.add(issue);
                continue;
            }
            for (int j = 0; j < fields.size(); j++) {
                validateModuleField(fields.getJSONObject(j), issues, formId, j);
            }
        }
    }

    private void validateModuleField(JSONObject field, List<AiModuleMaterialRecognizeResponse.Issue> issues, String formId, int index) {
        if (field == null) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("error", "module-material-field-invalid", "Field invalid", "fields[" + index + "] must be an object.");
            issue.setFormId(formId);
            issues.add(issue);
            return;
        }
        String fieldId = defaultText(field.getString("id"), "field_" + (index + 1));
        String fieldLabel = defaultText(field.getString("label"), "");
        if (StringUtil.isBlank(fieldLabel)) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("error", "module-material-field-label-missing", "Field label missing", "field.label is required.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issues.add(issue);
        }
        String nameHint = defaultText(field.getString("nameHint"), "");
        if (StringUtil.isBlank(nameHint)) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-name-missing", "Field name hint missing", "field.nameHint is recommended for later DSL generation.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        } else if (isMeaninglessNameHint(nameHint)) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-name-meaningless", "Field name hint meaningless", "field.nameHint is too generic; AI should regenerate a meaningful business field name.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        } else if (!nameHint.matches("^[a-z][a-z0-9_]*$")) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-name-invalid", "Field name hint invalid", "field.nameHint should be lowercase snake_case and start with a letter.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        }
        String typeHint = defaultText(field.getString("typeHint"), "");
        if (StringUtil.isNotBlank(typeHint) && !FIELD_TYPE_OPTIONS.contains(typeHint)) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-type-invalid", "Field type hint invalid", "typeHint is not supported, frontend may infer it again.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        }
        if (!isValidMaterialPriority(field.get("listPriority"))) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-list-priority-invalid", "Field list priority invalid", "listPriority should be a number between 0 and 100.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        }
        if (!isValidMaterialPriority(field.get("queryPriority"))) {
            AiModuleMaterialRecognizeResponse.Issue issue = moduleMaterialIssue("warning", "module-material-field-query-priority-invalid", "Field query priority invalid", "queryPriority should be a number between 0 and 100.");
            issue.setFormId(formId);
            issue.setFieldId(fieldId);
            issue.setFieldLabel(fieldLabel);
            issues.add(issue);
        }
    }

    private void validateModuleRelations(JSONArray relations, List<AiModuleMaterialRecognizeResponse.Issue> issues) {
        if (relations == null) {
            return;
        }
        for (int i = 0; i < relations.size(); i++) {
            JSONObject relation = relations.getJSONObject(i);
            if (relation == null) {
                issues.add(moduleMaterialIssue("warning", "module-material-relation-invalid", "Relation invalid", "relations[" + i + "] should be an object."));
                continue;
            }
            String type = defaultText(relation.getString("type"), "");
            if (StringUtil.isNotBlank(type) && !MODULE_RELATION_TYPE_OPTIONS.contains(type)) {
                issues.add(moduleMaterialIssue("warning", "module-material-relation-type-invalid", "Relation type invalid", "relation.type is not supported."));
            }
        }
    }

    private ModuleMaterialRetryResult retryRecognizeModuleMaterial(AiModuleMaterialRecognizeRequest request,
                                                                   String loginName,
                                                                   String model,
                                                                   JSONObject previousMaterial,
                                                                   List<AiModuleMaterialRecognizeResponse.Issue> previousIssues,
                                                                   String requestId) {
        String retryRawOutput;
        long retryStartedAt = System.currentTimeMillis();
        try {
            retryRawOutput = genAIService.callChatCompletionsJsonObject(
                    buildModuleMaterialSystemPrompt(),
                    buildModuleMaterialRepairUserPrompt(request, previousMaterial, previousIssues),
                    loginName,
                    0.0,
                    MODULE_MATERIAL_MAX_TOKENS
            );
        } catch (Exception ex) {
            logger.warn("FormDesignAI recognizeModuleMaterial retry request failed requestId={} model={} error={}",
                    requestId, model, ex.getMessage());
            return null;
        }
        long retryElapsedMs = System.currentTimeMillis() - retryStartedAt;
        logger.info("FormDesignAI recognizeModuleMaterial retry returned requestId={} model={} retryElapsedMs={} rawLength={}",
                requestId, model, retryElapsedMs, retryRawOutput == null ? 0 : retryRawOutput.length());

        String retryJsonText = genAIService.extractJson(retryRawOutput);
        if (StringUtil.isBlank(retryJsonText)) {
            logger.warn("FormDesignAI recognizeModuleMaterial retry invalid json requestId={} rawPreview={}",
                    requestId, preview(retryRawOutput));
            return null;
        }

        JSONObject retryMaterial;
        try {
            retryMaterial = JSONObject.parseObject(retryJsonText);
        } catch (Exception ex) {
            logger.warn("FormDesignAI recognizeModuleMaterial retry json parse failed requestId={} error={} rawPreview={}",
                    requestId, ex.getMessage(), preview(retryRawOutput));
            return null;
        }

        normalizeModuleMaterial(retryMaterial, request);
        List<AiModuleMaterialRecognizeResponse.Issue> retryIssues = validateModuleMaterial(retryMaterial);
        return new ModuleMaterialRetryResult(retryMaterial, retryIssues, retryRawOutput);
    }

    private boolean hasModuleMaterialError(List<AiModuleMaterialRecognizeResponse.Issue> issues) {
        if (issues == null) {
            return false;
        }
        for (AiModuleMaterialRecognizeResponse.Issue issue : issues) {
            if ("error".equals(issue.getLevel())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasModuleBlueprintEmpty(List<AiModuleMaterialRecognizeResponse.Issue> issues) {
        if (issues == null) {
            return false;
        }
        for (AiModuleMaterialRecognizeResponse.Issue issue : issues) {
            if ("module-material-blueprint-empty".equals(issue.getCode())) {
                return true;
            }
        }
        return false;
    }

    private List<AiFormMaterialRecognizeResponse.Issue> validateMaterial(JSONObject material) {
        List<AiFormMaterialRecognizeResponse.Issue> issues = new ArrayList<>();
        if (material == null) {
            issues.add(materialIssue("error", "material-root-invalid", "Material root invalid", "FormMaterial must be a JSON object."));
            return issues;
        }
        if (!FORM_MATERIAL_SCHEMA_VERSION.equals(material.getString("version"))) {
            issues.add(materialIssue("error", "material-version-invalid", "Material version invalid", "version must be 1.0."));
        }
        JSONObject source = material.getJSONObject("source");
        if (source == null) {
            issues.add(materialIssue("error", "material-source-missing", "Material source missing", "source object is required."));
        } else if (!MATERIAL_SOURCE_TYPE_OPTIONS.contains(source.getString("type"))) {
            issues.add(materialIssue("error", "material-source-type-invalid", "Material source type invalid", "source.type is not supported."));
        }
        String scene = defaultText(material.getString("scene"), "");
        if (StringUtil.isNotBlank(scene) && !MATERIAL_SCENE_OPTIONS.contains(scene)) {
            issues.add(materialIssue("warning", "material-scene-invalid", "Material scene invalid", "scene is not supported, frontend will treat it as normal."));
        }
        JSONArray fields = material.getJSONArray("fields");
        if (fields == null || fields.isEmpty()) {
            issues.add(materialIssue("error", "material-fields-empty", "Fields empty", "fields must contain at least one recognized field."));
            return issues;
        }
        for (int i = 0; i < fields.size(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (field == null) {
                issues.add(materialIssue("error", "material-field-invalid", "Field invalid", "fields[" + i + "] must be an object."));
                continue;
            }
            String fieldId = defaultText(field.getString("id"), "field_" + (i + 1));
            String fieldLabel = defaultText(field.getString("label"), "");
            if (StringUtil.isBlank(fieldLabel)) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("error", "material-field-label-missing", "Field label missing", "fields[" + i + "].label is required.");
                issue.setFieldId(fieldId);
                issues.add(issue);
            }
            String typeHint = field.getString("typeHint");
            if (StringUtil.isNotBlank(typeHint) && !FIELD_TYPE_OPTIONS.contains(typeHint)) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("warning", "material-field-type-invalid", "Field type hint invalid", "typeHint is not supported, frontend may infer it again.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            }
            String nameHint = field.getString("nameHint");
            if (StringUtil.isBlank(nameHint)) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("error", "material-field-name-missing", "Field name hint missing", "nameHint is missing; AI must provide a meaningful English snake_case field name.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            } else if (isMeaninglessNameHint(nameHint)) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("error", "material-field-name-meaningless", "Field name hint meaningless", "nameHint is too generic; AI must regenerate a meaningful business field name.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            } else if (!nameHint.matches("^[a-z][a-z0-9_]*$")) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("warning", "material-field-name-invalid", "Field name hint invalid", "nameHint should be lowercase snake_case and start with a letter.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            }
            if (!isValidMaterialPriority(field.get("listPriority"))) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("warning", "material-field-list-priority-invalid", "Field list priority invalid", "listPriority should be a number between 0 and 100.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            }
            if (!isValidMaterialPriority(field.get("queryPriority"))) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("warning", "material-field-query-priority-invalid", "Field query priority invalid", "queryPriority should be a number between 0 and 100.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            }
            String queryMode = defaultText(field.getString("queryMode"), "");
            if (StringUtil.isNotBlank(queryMode) && !MATERIAL_QUERY_MODE_OPTIONS.contains(queryMode)) {
                AiFormMaterialRecognizeResponse.Issue issue = materialIssue("warning", "material-field-query-mode-invalid", "Field query mode invalid", "queryMode is not supported, frontend may infer it again.");
                issue.setFieldId(fieldId);
                issue.setFieldLabel(fieldLabel);
                issues.add(issue);
            }
        }
        return issues;
    }

    private boolean isValidMaterialPriority(Object value) {
        if (value == null || StringUtil.isBlank(String.valueOf(value))) {
            return true;
        }
        try {
            double number = Double.parseDouble(String.valueOf(value));
            return number >= 0 && number <= 100;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean hasMaterialError(List<AiFormMaterialRecognizeResponse.Issue> issues) {
        if (issues == null) {
            return false;
        }
        for (AiFormMaterialRecognizeResponse.Issue issue : issues) {
            if ("error".equals(issue.getLevel())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMeaninglessNameHint(String nameHint) {
        if (StringUtil.isBlank(nameHint)) {
            return false;
        }
        String normalized = nameHint.trim().toLowerCase();
        return normalized.matches("^(field|col|column)_?\\d+$")
                || "field".equals(normalized)
                || "column".equals(normalized)
                || "unnamed_field".equals(normalized);
    }

    private boolean isFullSpanMaterialType(String typeHint) {
        return "textarea".equals(typeHint)
                || "richText".equals(typeHint)
                || "upload".equals(typeHint)
                || "imageUpload".equals(typeHint)
                || "onlineFile".equals(typeHint);
    }

    private List<AiFormDesignDslGenerateResponse.Issue> validateDsl(JSONObject dsl) {
        List<AiFormDesignDslGenerateResponse.Issue> issues = new ArrayList<>();
        if (dsl == null) {
            issues.add(issue("schema-root-invalid", "DSL root invalid", "FormDesignDSL must be a JSON object."));
            return issues;
        }
        if (!DSL_SCHEMA_VERSION.equals(dsl.getString("version"))) {
            issues.add(issue("schema-version-invalid", "DSL version invalid", "version must be 1.0."));
        }
        JSONObject form = dsl.getJSONObject("form");
        if (form == null) {
            issues.add(issue("schema-form-missing", "Form missing", "form object is required."));
        } else {
            validateName(form.getString("name"), "schema-form-name-invalid", "Form name invalid", issues);
            validateName(form.getString("module"), "schema-form-module-invalid", "Form module invalid", issues);
            if (StringUtil.isBlank(form.getString("title"))) {
                issues.add(issue("schema-form-title-missing", "Form title missing", "form.title is required."));
            }
        }
        JSONObject layout = dsl.getJSONObject("layout");
        if (layout == null) {
            issues.add(issue("schema-layout-missing", "Layout missing", "layout object is required."));
        } else if (StringUtil.isNotBlank(layout.getString("style"))
                && !Arrays.asList("normal", "document-form", "dense", "approval").contains(layout.getString("style"))) {
            issues.add(issue("schema-layout-style-invalid", "Layout style invalid", "layout.style is not supported."));
        }
        JSONArray fields = dsl.getJSONArray("fields");
        if (fields == null || fields.isEmpty()) {
            issues.add(issue("schema-fields-empty", "Fields empty", "fields must contain at least one field."));
        } else {
            for (int i = 0; i < fields.size(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field == null) {
                    issues.add(issue("schema-field-invalid", "Field invalid", "fields[" + i + "] must be an object."));
                    continue;
                }
                validateName(field.getString("name"), "schema-field-name-invalid", "Field name invalid", issues);
                if (StringUtil.isBlank(field.getString("label"))) {
                    issues.add(issue("schema-field-label-missing", "Field label missing", "fields[" + i + "].label is required."));
                }
                String type = field.getString("type");
                if (StringUtil.isBlank(type) || !FIELD_TYPE_OPTIONS.contains(type)) {
                    issues.add(issue("schema-field-type-invalid", "Field type invalid", "fields[" + i + "].type is not supported."));
                }
            }
        }
        if (dsl.getJSONObject("list") == null) {
            issues.add(issue("schema-list-missing", "List config missing", "list object is required."));
        }
        return issues;
    }

    private void validateName(String value, String code, String title, List<AiFormDesignDslGenerateResponse.Issue> issues) {
        if (StringUtil.isBlank(value) || !value.matches("^[a-z][a-z0-9_]*$")) {
            issues.add(issue(code, title, "Name must be lowercase snake_case and start with a letter."));
        }
    }

    private AiFormDesignDslGenerateResponse.Summary buildSummary(JSONObject dsl) {
        if (dsl == null) {
            return null;
        }
        AiFormDesignDslGenerateResponse.Summary summary = new AiFormDesignDslGenerateResponse.Summary();
        JSONObject form = dsl.getJSONObject("form");
        JSONObject layout = dsl.getJSONObject("layout");
        JSONArray fields = dsl.getJSONArray("fields");
        summary.setTitle(form == null ? "" : form.getString("title"));
        summary.setLayoutStyle(layout == null ? "" : layout.getString("style"));
        summary.setFieldCount(fields == null ? 0 : fields.size());
        return summary;
    }

    private AiFormMaterialRecognizeResponse.Summary buildMaterialSummary(JSONObject material) {
        if (material == null) {
            return null;
        }
        AiFormMaterialRecognizeResponse.Summary summary = new AiFormMaterialRecognizeResponse.Summary();
        JSONArray fields = material.getJSONArray("fields");
        JSONArray tables = material.getJSONArray("tables");
        summary.setTitle(material.getString("title"));
        summary.setScene(material.getString("scene"));
        summary.setFieldCount(fields == null ? 0 : fields.size());
        summary.setTableCount(tables == null ? 0 : tables.size());
        return summary;
    }

    private AiModuleMaterialRecognizeResponse.Summary buildModuleMaterialSummary(JSONObject material) {
        if (material == null) {
            return null;
        }
        AiModuleMaterialRecognizeResponse.Summary summary = new AiModuleMaterialRecognizeResponse.Summary();
        JSONObject module = material.getJSONObject("module");
        JSONArray menus = material.getJSONArray("menus");
        JSONArray pages = material.getJSONArray("pages");
        JSONArray forms = material.getJSONArray("forms");
        JSONArray relations = material.getJSONArray("relations");
        int fieldCount = 0;
        int dictionaryHintCount = 0;
        if (forms != null) {
            for (int i = 0; i < forms.size(); i++) {
                JSONObject form = forms.getJSONObject(i);
                if (form == null) {
                    continue;
                }
                JSONArray fields = form.getJSONArray("fields");
                if (fields != null) {
                    fieldCount += fields.size();
                }
                JSONArray dictionaryHints = form.getJSONArray("dictionaryHints");
                if (dictionaryHints != null) {
                    dictionaryHintCount += dictionaryHints.size();
                }
            }
        }
        summary.setTitle(module == null ? "" : module.getString("title"));
        summary.setScene(module == null ? "" : module.getString("scene"));
        summary.setMenuCount(menus == null ? 0 : menus.size());
        summary.setPageCount(pages == null ? 0 : pages.size());
        summary.setFormCount(forms == null ? 0 : forms.size());
        summary.setFieldCount(fieldCount);
        summary.setDictionaryHintCount(dictionaryHintCount);
        summary.setRelationCount(relations == null ? 0 : relations.size());
        return summary;
    }

    private AiFormDesignDslGenerateResponse.Issue issue(String code, String title, String description) {
        AiFormDesignDslGenerateResponse.Issue issue = new AiFormDesignDslGenerateResponse.Issue();
        issue.setLevel("error");
        issue.setCode(code);
        issue.setTitle(title);
        issue.setDescription(description);
        return issue;
    }

    private AiFormMaterialRecognizeResponse.Issue materialIssue(String level, String code, String title, String description) {
        AiFormMaterialRecognizeResponse.Issue issue = new AiFormMaterialRecognizeResponse.Issue();
        issue.setLevel(level);
        issue.setCode(code);
        issue.setTitle(title);
        issue.setDescription(description);
        return issue;
    }

    private AiModuleMaterialRecognizeResponse.Issue moduleMaterialIssue(String level, String code, String title, String description) {
        AiModuleMaterialRecognizeResponse.Issue issue = new AiModuleMaterialRecognizeResponse.Issue();
        issue.setLevel(level);
        issue.setCode(code);
        issue.setTitle(title);
        issue.setDescription(description);
        return issue;
    }

    private String preview(String text) {
        return truncate(text, MAX_RAW_OUTPUT_PREVIEW_LENGTH);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtil.isBlank(value) ? defaultValue : value;
    }

    private static class ModuleMaterialCacheKey {

        private final String indexKey;
        private final String entryKey;
        private final String logKey;

        private ModuleMaterialCacheKey(String indexKey, String entryKey, String logKey) {
            this.indexKey = indexKey;
            this.entryKey = entryKey;
            this.logKey = logKey;
        }

        private String getIndexKey() {
            return indexKey;
        }

        private String getEntryKey() {
            return entryKey;
        }

        private String getLogKey() {
            return logKey;
        }
    }

    private static class ModuleFormDslCacheKey {

        private final String indexKey;
        private final String entryKey;
        private final String logKey;

        private ModuleFormDslCacheKey(String indexKey, String entryKey, String logKey) {
            this.indexKey = indexKey;
            this.entryKey = entryKey;
            this.logKey = logKey;
        }

        private String getIndexKey() {
            return indexKey;
        }

        private String getEntryKey() {
            return entryKey;
        }

        private String getLogKey() {
            return logKey;
        }
    }

    private static class ModuleMaterialRetryResult {

        private final JSONObject material;
        private final List<AiModuleMaterialRecognizeResponse.Issue> issues;
        private final String rawOutput;

        private ModuleMaterialRetryResult(JSONObject material,
                                          List<AiModuleMaterialRecognizeResponse.Issue> issues,
                                          String rawOutput) {
            this.material = material;
            this.issues = issues;
            this.rawOutput = rawOutput;
        }

        private JSONObject getMaterial() {
            return material;
        }

        private List<AiModuleMaterialRecognizeResponse.Issue> getIssues() {
            return issues;
        }

        private String getRawOutput() {
            return rawOutput;
        }
    }
}
