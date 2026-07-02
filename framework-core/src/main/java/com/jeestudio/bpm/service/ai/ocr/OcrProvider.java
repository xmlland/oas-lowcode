package com.jeestudio.bpm.service.ai.ocr;

/**
 * @Description: OCR服务商接口
 */
public interface OcrProvider {

    String getProviderCode();

    default String getProviderName() {
        return getProviderCode();
    }

    default boolean supports(OcrRequest request) {
        return true;
    }

    default String getStatusMessage(String loginName, OcrRequest request) {
        return "";
    }

    boolean isAvailable(String loginName);

    OcrResult recognize(OcrRequest request) throws Exception;
}
