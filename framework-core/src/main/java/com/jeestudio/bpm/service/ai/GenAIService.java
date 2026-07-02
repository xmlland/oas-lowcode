package com.jeestudio.bpm.service.ai;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.gen.GenTableColumnView;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @Description: 通用大模型 AI 服务，封装 OpenAI Chat Completions 接口调用
 */
@Service
public class GenAIService {

    protected static final Logger logger = LoggerFactory.getLogger(GenAIService.class);

    private static final int AI_TIMEOUT_SECONDS = 120;

    private static final String SYSTEM_PROMPT_TABLE_COLUMNS =
            "你是一个数据库表结构设计专家。用户会给你一个中文表描述或表名，你需要为这张表设计合理的业务字段。\n" +
            "请只返回字段的中文名称，用英文逗号分隔，不要包含主键id字段和通用审计字段（创建人、创建时间、修改人、修改时间、备注、排序等）。\n" +
            "只输出字段名列表，不要输出任何解释。";

    private static final String SYSTEM_PROMPT_COLUMN_DEFINITION =
            "你是一个数据库表结构设计专家。用户会给你一个或多个中文字段描述，你需要返回每个字段的英文字段名、中文描述和数据库类型。\n" +
            "规则：\n" +
            "1. field_name：使用英文蛇形命名（snake_case），全小写，简洁准确\n" +
            "2. field_dsc：中文描述，与用户输入保持一致\n" +
            "3. data_type：PostgreSQL数据库类型，常用类型包括 varchar(64)、varchar(100)、varchar(255)、varchar(500)、integer、numeric(10,2)、timestamp、text\n" +
            "4. 如果字段名为SQL关键字（如type、describe、order等），请在末尾加下划线\n" +
            "请严格按照以下JSON格式返回，不要输出其他内容：\n" +
            "{\"fields\":[{\"field_name\":\"xxx\",\"field_dsc\":\"xxx\",\"data_type\":\"xxx\"}]}";

    @Autowired
    private GenTableService genTableService;

    /**
     * 根据表描述生成字段名列表（逗号分隔的中文字段名）
     * 对应原远端 getColumnsByTableComments 接口
     */
    public String getColumnNamesByTableComments(String tableComments, String loginName) {
        try {
            String result = callChatCompletions(SYSTEM_PROMPT_TABLE_COLUMNS,
                    "请为以下表设计字段：" + tableComments, loginName);
            if (StringUtil.isNotEmpty(result)) {
                // 清理返回结果：去除换行、多余空格
                result = result.replaceAll("[\\r\\n]+", "").trim();
                return result;
            }
        } catch (Exception e) {
            logger.warn("getColumnNamesByTableComments error: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据中文字段描述生成字段定义（field_name、field_dsc、data_type）
     * 对应原远端 getColumnsByComments 接口
     */
    public GenTableColumnView getColumnDefinitionByComments(String comments, String loginName) {
        try {
            String result = callChatCompletions(SYSTEM_PROMPT_COLUMN_DEFINITION, comments, loginName);
            if (StringUtil.isEmpty(result)) {
                return null;
            }
            // 容错：提取 JSON 内容（AI 可能返回 markdown 代码块包裹的 JSON）
            String json = extractJson(result);
            if (json == null) {
                logger.warn("getColumnDefinitionByComments: failed to extract JSON from AI response");
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(json);
            JSONArray fields = jsonObject.getJSONArray("fields");
            if (fields == null || fields.isEmpty()) {
                return null;
            }
            JSONObject theColumn = fields.getJSONObject(0);
            String name = theColumn.getString("field_name");
            if (name == null || name.isEmpty()) {
                return null;
            }
            // 避免 SQL 关键字冲突
            if ("type".equals(name) || "describe".equals(name) || "order".equals(name)) {
                name += "_";
            }
            GenTableColumnView column = new GenTableColumnView();
            column.setName(name);
            column.setComments(theColumn.getString("field_dsc"));
            column.setJdbcType(theColumn.getString("data_type"));
            return column;
        } catch (Exception e) {
            logger.warn("getColumnDefinitionByComments error: " + e.getMessage());
        }
        return null;
    }

    /**
     * 检查 AI 配置是否可用
     */
    public boolean isAvailable(String loginName) {
        try {
            String aiUri = genTableService.getAIUri(loginName);
            String aiApiKey = resolveApiKey(genTableService.getAIApiKey(loginName));
            return StringUtil.isNotEmpty(aiUri) && StringUtil.isNotEmpty(aiApiKey);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 读取当前登录用户可用的 AI 模型配置。
     */
    public String getConfiguredModel(String loginName) throws Exception {
        return genTableService.getAIModel(loginName);
    }

    /**
     * 判断当前 AI 配置是否具备图片输入能力。
     */
    public boolean isVisionAvailable(String loginName) {
        try {
            return isAvailable(loginName) && isLikelyVisionModel(getConfiguredModel(loginName));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据模型名称做轻量级视觉能力判断，用于前置提示和兜底校验。
     */
    public boolean isLikelyVisionModel(String model) {
        if (StringUtil.isEmpty(model)) {
            return false;
        }
        String normalized = normalizeModelName(model);
        return normalized.startsWith("qwen3.7-plus")
                || normalized.startsWith("qwen3.6-plus")
                || normalized.startsWith("qwen3.6-flash")
                || normalized.startsWith("qwen3.6-35b")
                || normalized.startsWith("qwen3.5-plus")
                || normalized.startsWith("qwen3.5-flash")
                || normalized.contains("vision")
                || normalized.contains("visual")
                || normalized.contains("omni")
                || normalized.contains("gpt-4o")
                || normalized.contains("gpt-4.1")
                || normalized.contains("qwen-vl")
                || normalized.contains("qwen2-vl")
                || normalized.contains("qwen2.5-vl")
                || normalized.contains("qwen3-vl")
                || normalized.contains("qvq")
                || normalized.contains("doubao-vision")
                || normalized.contains("gemini")
                || normalized.contains("claude-3");
    }

    /**
     * 统一模型名称格式，避免空格、大小写等差异影响能力判断。
     */
    public String normalizeModelName(String model) {
        if (model == null) {
            return "";
        }
        return model.replace('\u00a0', ' ').trim().toLowerCase(Locale.ROOT);
    }

    private boolean shouldDisableThinking(String model) {
        String normalized = normalizeModelName(model);
        return normalized.startsWith("qwen3");
    }

    /**
     * 解析 API Key：支持 PATH:ENV_VAR_NAME 格式从环境变量读取
     */
    private String resolveApiKey(String rawKey) {
        if (StringUtil.isEmpty(rawKey)) {
            return rawKey;
        }
        if (rawKey.startsWith("PATH:")) {
            String envName = rawKey.substring(5);
            String envValue = System.getenv(envName);
            if (StringUtil.isEmpty(envValue)) {
                logger.warn("Environment variable '{}' not found for gen.AIApiKey", envName);
            }
            return envValue;
        }
        return rawKey;
    }

    /**
     * 底层方法：调用 OpenAI Chat Completions 接口
     * 使用 Java 原生 HttpURLConnection，避免 OkHttp 版本冲突
     */
    public String callChatCompletions(String systemPrompt, String userPrompt, String loginName) throws Exception {
        return callChatCompletions(systemPrompt, userPrompt, loginName, 0.3);
    }

    /**
     * 调用文本 Chat Completions，并允许指定温度参数。
     */
    public String callChatCompletions(String systemPrompt, String userPrompt, String loginName, Double temperature) throws Exception {
        return callChatCompletions(systemPrompt, userPrompt, loginName, temperature, null);
    }

    /**
     * 调用文本 Chat Completions，并允许指定温度和最大输出长度。
     */
    public String callChatCompletions(String systemPrompt, String userPrompt, String loginName, Double temperature, Integer maxTokens) throws Exception {
        return callChatCompletions(systemPrompt, userPrompt, loginName, temperature, maxTokens, false);
    }

    /**
     * 调用 Chat Completions 的 JSON Object 输出模式，适合 DSL、材料识别等结构化生成场景。
     */
    public String callChatCompletionsJsonObject(String systemPrompt, String userPrompt, String loginName,
                                                Double temperature, Integer maxTokens) throws Exception {
        return callChatCompletions(systemPrompt, userPrompt, loginName, temperature, maxTokens, true);
    }

    private String callChatCompletions(String systemPrompt, String userPrompt, String loginName,
                                       Double temperature, Integer maxTokens, boolean jsonObjectResponse) throws Exception {
        String aiUri = genTableService.getAIUri(loginName);
        String aiApiKey = resolveApiKey(genTableService.getAIApiKey(loginName));
        String aiModel = genTableService.getAIModel(loginName);

        if (StringUtil.isEmpty(aiUri) || StringUtil.isEmpty(aiApiKey)) {
            logger.debug("AI configuration not set (gen.AIUri or gen.AIApiKey is empty)");
            return null;
        }

        // 确保 URI 不以 / 结尾
        if (aiUri.endsWith("/")) {
            aiUri = aiUri.substring(0, aiUri.length() - 1);
        }
        // 兼容两种配置格式：
        // 1) https://dashscope.aliyuncs.com/compatible-mode/v1  (已含 /v1，只拼 /chat/completions)
        // 2) https://api.openai.com                             (不含 /v1，拼 /v1/chat/completions)
        String urlStr;
        if (aiUri.endsWith("/v1")) {
            urlStr = aiUri + "/chat/completions";
        } else {
            urlStr = aiUri + "/v1/chat/completions";
        }

        // 构造请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiModel);
        requestBody.put("temperature", temperature != null ? temperature : 0.3);
        if (maxTokens != null && maxTokens > 0) {
            requestBody.put("max_tokens", maxTokens);
        }
        if (shouldDisableThinking(aiModel)) {
            requestBody.put("enable_thinking", false);
        }
        if (jsonObjectResponse) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
        }

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 使用 Java 原生 HttpURLConnection 发送请求
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + aiApiKey);
        conn.setConnectTimeout(AI_TIMEOUT_SECONDS * 1000);
        conn.setReadTimeout(AI_TIMEOUT_SECONDS * 1000);
        conn.setDoOutput(true);

        byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(input);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorBody = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                errorBody = sb.toString();
            } catch (Exception ignored) {}
            throw new RuntimeException("AI API returned " + responseCode + ": " + errorBody);
        }

        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseStr.append(line);
            }
        }

        // 解析 Chat Completions 响应
        JSONObject response = JSONObject.parseObject(responseStr.toString());
        JSONArray choices = response.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message != null) {
                return message.getString("content");
            }
        }
        return null;
    }

    /**
     * 调用支持视觉输入的 Chat Completions，用于图片材料和扫描件 OCR/理解场景。
     */
    public String callChatCompletionsWithImage(String systemPrompt, String userPrompt, String imageBase64,
                                               String mimeType, String loginName, Double temperature,
                                               Integer maxTokens) throws Exception {
        String aiUri = genTableService.getAIUri(loginName);
        String aiApiKey = resolveApiKey(genTableService.getAIApiKey(loginName));
        String aiModel = genTableService.getAIModel(loginName);

        if (StringUtil.isEmpty(aiUri) || StringUtil.isEmpty(aiApiKey)) {
            logger.debug("AI configuration not set (gen.AIUri or gen.AIApiKey is empty)");
            return null;
        }
        if (!isLikelyVisionModel(aiModel)) {
            throw new RuntimeException("configured model does not look like a vision model: " + aiModel);
        }
        if (StringUtil.isEmpty(imageBase64)) {
            throw new RuntimeException("imageBase64 cannot be empty");
        }

        if (aiUri.endsWith("/")) {
            aiUri = aiUri.substring(0, aiUri.length() - 1);
        }
        String urlStr;
        if (aiUri.endsWith("/v1")) {
            urlStr = aiUri + "/chat/completions";
        } else {
            urlStr = aiUri + "/v1/chat/completions";
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiModel);
        requestBody.put("temperature", temperature != null ? temperature : 0.1);
        if (maxTokens != null && maxTokens > 0) {
            requestBody.put("max_tokens", maxTokens);
        }
        if (shouldDisableThinking(aiModel)) {
            requestBody.put("enable_thinking", false);
        }

        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        JSONArray userContent = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("type", "text");
        textPart.put("text", userPrompt);
        userContent.add(textPart);

        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:" + defaultMimeType(mimeType) + ";base64," + imageBase64);
        imageUrl.put("detail", "high");
        JSONObject imagePart = new JSONObject();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrl);
        userContent.add(imagePart);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + aiApiKey);
        conn.setConnectTimeout(AI_TIMEOUT_SECONDS * 1000);
        conn.setReadTimeout(AI_TIMEOUT_SECONDS * 1000);
        conn.setDoOutput(true);

        byte[] input = requestBody.toJSONString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(input);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorBody = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                errorBody = sb.toString();
            } catch (Exception ignored) {}
            throw new RuntimeException("AI API returned " + responseCode + ": " + errorBody);
        }

        StringBuilder responseStr = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseStr.append(line);
            }
        }

        JSONObject response = JSONObject.parseObject(responseStr.toString());
        JSONArray choices = response.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            if (message != null) {
                return message.getString("content");
            }
        }
        return null;
    }

    private String defaultMimeType(String mimeType) {
        return StringUtil.isEmpty(mimeType) ? "image/png" : mimeType;
    }

    /**
     * 从大模型返回文本中提取 JSON 内容，兼容 markdown 代码块和前后说明文字。
     */
    public String extractJson(String text) {
        String robustJson = extractJsonRobust(text);
        if (robustJson != null) {
            return robustJson;
        }
        if (text == null) return null;
        text = text.trim();
        // 处理 ```json ... ``` 包裹
        if (text.contains("```")) {
            int start = text.indexOf("```");
            // 跳过 ```json 行
            int contentStart = text.indexOf("\n", start);
            if (contentStart == -1) return null;
            contentStart++;
            int end = text.indexOf("```", contentStart);
            if (end == -1) return null;
            text = text.substring(contentStart, end).trim();
        }
        // 提取第一个 { 到最后一个 } 之间的内容
        int braceStart = text.indexOf('{');
        int braceEnd = text.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1);
        }
        return null;
    }

    private String extractJsonRobust(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = stripThinkBlocks(text.trim());
        List<String> candidates = new ArrayList<>();
        candidates.addAll(extractCodeFenceContents(cleaned));
        candidates.add(cleaned);
        for (String candidateText : candidates) {
            String parsed = findParsableJsonObject(candidateText);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private String stripThinkBlocks(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }
        return text.replaceAll("(?is)<think>.*?</think>", "").trim();
    }

    private List<String> extractCodeFenceContents(String text) {
        List<String> result = new ArrayList<>();
        if (StringUtil.isEmpty(text) || !text.contains("```")) {
            return result;
        }
        int searchFrom = 0;
        while (searchFrom >= 0 && searchFrom < text.length()) {
            int start = text.indexOf("```", searchFrom);
            if (start < 0) {
                break;
            }
            int contentStart = text.indexOf('\n', start);
            if (contentStart < 0) {
                break;
            }
            contentStart++;
            int end = text.indexOf("```", contentStart);
            if (end < 0) {
                break;
            }
            String content = text.substring(contentStart, end).trim();
            if (StringUtil.isNotBlank(content)) {
                result.add(content);
            }
            searchFrom = end + 3;
        }
        return result;
    }

    private String findParsableJsonObject(String text) {
        if (StringUtil.isEmpty(text)) {
            return null;
        }
        String firstValid = null;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '{') {
                continue;
            }
            int end = findJsonObjectEnd(text, i);
            if (end <= i) {
                continue;
            }
            String candidate = text.substring(i, end + 1).trim();
            try {
                JSONObject object = JSONObject.parseObject(candidate);
                if (isLikelyBusinessJsonObject(object)) {
                    return candidate;
                }
                if (firstValid == null) {
                    firstValid = candidate;
                }
            } catch (Exception ignored) {
            }
        }
        return firstValid;
    }

    private int findJsonObjectEnd(String text, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }
            if (ch == '"') {
                inString = true;
            } else if (ch == '{') {
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isLikelyBusinessJsonObject(JSONObject object) {
        return object != null
                && (object.containsKey("version")
                || object.containsKey("fields")
                || object.containsKey("form")
                || object.containsKey("layout")
                || object.containsKey("source")
                || object.containsKey("generator"));
    }
}
