package com.jeestudio.bpm.service.ai.ocr;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeResponse;
import com.jeestudio.bpm.service.ai.GenAIService;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @Description: 视觉AI OCR服务商
 */
@Service
@Order(100)
public class VisionAiOcrProvider implements OcrProvider {

    public static final String PROVIDER_CODE = "vision-ai";

    private static final int OCR_MAX_TOKENS = 4096;

    @Autowired
    private GenAIService genAIService;

    @Override
    public String getProviderCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String getProviderName() {
        return "Vision AI OCR";
    }

    @Override
    public boolean supports(OcrRequest request) {
        if (request == null) {
            return false;
        }
        String sourceType = defaultText(request.getSourceType(), "").toLowerCase(Locale.ROOT);
        String mimeType = defaultText(request.getMimeType(), "").toLowerCase(Locale.ROOT);
        String fileName = defaultText(request.getFileName(), "").toLowerCase(Locale.ROOT);
        return "image".equals(sourceType)
                || mimeType.startsWith("image/")
                || fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".webp");
    }

    @Override
    public boolean isAvailable(String loginName) {
        return genAIService != null && genAIService.isVisionAvailable(loginName);
    }

    @Override
    public String getStatusMessage(String loginName, OcrRequest request) {
        if (genAIService == null) {
            return "Vision AI OCR is unavailable: GenAIService is not initialized";
        }
        try {
            String model = genAIService.getConfiguredModel(loginName);
            boolean aiConfigured = genAIService.isAvailable(loginName);
            boolean visionModel = genAIService.isLikelyVisionModel(model);
            return "Vision AI OCR status: model=" + defaultText(model, "<empty>")
                    + ", aiConfigured=" + aiConfigured
                    + ", visionModel=" + visionModel;
        } catch (Exception ex) {
            return "Vision AI OCR status check failed: " + ex.getMessage();
        }
    }

    @Override
    public OcrResult recognize(OcrRequest request) throws Exception {
        if (request == null || request.getImageBytes() == null || request.getImageBytes().length == 0) {
            throw OcrProviderException.failed("OCR image bytes cannot be empty");
        }
        if (!isAvailable(request.getLoginName())) {
            throw OcrProviderException.providerUnavailable(getStatusMessage(request.getLoginName(), request));
        }

        String imageBase64 = Base64.getEncoder().encodeToString(request.getImageBytes());
        String rawOutput = genAIService.callChatCompletionsWithImage(
                buildSystemPrompt(),
                buildUserPrompt(request),
                imageBase64,
                defaultText(request.getMimeType(), "image/png"),
                request.getLoginName(),
                0.0,
                OCR_MAX_TOKENS
        );
        if (StringUtil.isBlank(rawOutput)) {
            throw OcrProviderException.failed("Vision AI OCR returned empty output");
        }

        OcrResult result = parseOcrOutput(rawOutput);
        result.setProviderCode(PROVIDER_CODE);
        Map<String, Object> meta = result.getMeta() == null ? new LinkedHashMap<>() : result.getMeta();
        meta.put("rawOutputPreview", preview(rawOutput));
        meta.put("model", safeModel(request.getLoginName()));
        meta.put("sourceType", defaultText(request.getSourceType(), "image"));
        meta.put("fileName", defaultText(request.getFileName(), ""));
        result.setMeta(meta);
        if (StringUtil.isBlank(result.getText())) {
            throw OcrProviderException.failed("Vision AI OCR returned no readable text");
        }
        return result;
    }

    private String buildSystemPrompt() {
        return "You are an OCR engine for Chinese business form screenshots. "
                + "Extract only visible text from the image. "
                + "Preserve useful line breaks. For table-like content, keep rows on separate lines and use tabs between cells when possible. "
                + "Do not infer business fields, English field names, default values, list suggestions, or query suggestions. "
                + "Return one compact JSON object only: "
                + "{\"text\":\"visible text with line breaks\",\"confidence\":0.0,\"blocks\":[{\"text\":\"block text\",\"confidence\":0.0}],\"warnings\":[]}.";
    }

    private String buildUserPrompt(OcrRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("File name: ").append(defaultText(request.getFileName(), "")).append("\n");
        prompt.append("Mime type: ").append(defaultText(request.getMimeType(), "")).append("\n");
        prompt.append("Language hint: ").append(defaultText(request.getLanguage(), "chi_sim+eng")).append("\n");
        if (request.getMaxChars() != null && request.getMaxChars() > 0) {
            prompt.append("Maximum text chars: ").append(request.getMaxChars()).append("\n");
        }
        prompt.append("Please extract the visible OCR text from this image now.");
        return prompt.toString();
    }

    private OcrResult parseOcrOutput(String rawOutput) {
        String jsonText = genAIService.extractJson(rawOutput);
        if (StringUtil.isBlank(jsonText)) {
            OcrResult result = new OcrResult();
            result.setText(normalizeOcrText(rawOutput));
            result.setConfidence(0.6);
            return result;
        }
        try {
            JSONObject json = JSONObject.parseObject(jsonText);
            OcrResult result = new OcrResult();
            result.setText(normalizeOcrText(json.getString("text")));
            result.setConfidence(json.getDouble("confidence"));
            result.setBlocks(parseBlocks(json.getJSONArray("blocks")));
            result.setIssues(parseWarnings(json.getJSONArray("warnings")));
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("jsonParsed", true);
            result.setMeta(meta);
            if (result.getConfidence() == null) {
                result.setConfidence(0.7);
            }
            return result;
        } catch (Exception ex) {
            OcrResult result = new OcrResult();
            result.setText(normalizeOcrText(rawOutput));
            result.setConfidence(0.5);
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("jsonParsed", false);
            meta.put("parseError", ex.getMessage());
            result.setMeta(meta);
            return result;
        }
    }

    private List<OcrBlock> parseBlocks(JSONArray blocks) {
        List<OcrBlock> result = new ArrayList<>();
        if (blocks == null) {
            return result;
        }
        for (int i = 0; i < blocks.size(); i++) {
            JSONObject item = blocks.getJSONObject(i);
            if (item == null || StringUtil.isBlank(item.getString("text"))) {
                continue;
            }
            OcrBlock block = new OcrBlock();
            block.setText(normalizeOcrText(item.getString("text")));
            block.setConfidence(item.getDouble("confidence"));
            block.setX(item.getInteger("x"));
            block.setY(item.getInteger("y"));
            block.setWidth(item.getInteger("width"));
            block.setHeight(item.getInteger("height"));
            result.add(block);
        }
        return result;
    }

    private List<AiFormMaterialRecognizeResponse.Issue> parseWarnings(JSONArray warnings) {
        List<AiFormMaterialRecognizeResponse.Issue> result = new ArrayList<>();
        if (warnings == null) {
            return result;
        }
        for (int i = 0; i < warnings.size(); i++) {
            String warning = warnings.getString(i);
            if (StringUtil.isBlank(warning)) {
                continue;
            }
            AiFormMaterialRecognizeResponse.Issue issue = new AiFormMaterialRecognizeResponse.Issue();
            issue.setLevel("warning");
            issue.setCode("OCR_WARNING");
            issue.setTitle("OCR warning");
            issue.setDescription(warning);
            result.add(issue);
        }
        return result;
    }

    private String normalizeOcrText(String value) {
        return defaultText(value, "")
                .replace('\u00a0', ' ')
                .replaceAll("[\\t\\r]+", " ")
                .replaceAll(" *\\n+ *", "\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private String safeModel(String loginName) {
        try {
            return genAIService.getConfiguredModel(loginName);
        } catch (Exception ex) {
            return "";
        }
    }

    private String preview(String text) {
        String value = defaultText(text, "").trim();
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtil.isBlank(value) ? defaultValue : value;
    }
}
