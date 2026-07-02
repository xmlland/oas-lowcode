package com.jeestudio.bpm.service.ai.ocr;

import com.jeestudio.bpm.common.entity.gen.AiFormMaterialRecognizeResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: OCR识别结果
 */
public class OcrResult {

    private String providerCode;
    private String text;
    private Double confidence;
    private List<OcrBlock> blocks = new ArrayList<>();
    private List<AiFormMaterialRecognizeResponse.Issue> issues = new ArrayList<>();
    private Map<String, Object> meta = new LinkedHashMap<>();

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public List<OcrBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<OcrBlock> blocks) {
        this.blocks = blocks == null ? new ArrayList<>() : blocks;
    }

    public List<AiFormMaterialRecognizeResponse.Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<AiFormMaterialRecognizeResponse.Issue> issues) {
        this.issues = issues == null ? new ArrayList<>() : issues;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta == null ? new LinkedHashMap<>() : meta;
    }
}
