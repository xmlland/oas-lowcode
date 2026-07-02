package com.jeestudio.bpm.service.ai.ocr;

/**
 * @Description: OCR服务商异常
 */
public class OcrProviderException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static final String ERROR_OCR_NOT_CONFIGURED = "AI_OCR_NOT_CONFIGURED";
    public static final String ERROR_OCR_PROVIDER_UNAVAILABLE = "AI_OCR_PROVIDER_UNAVAILABLE";
    public static final String ERROR_OCR_FAILED = "AI_OCR_FAILED";
    public static final String ERROR_OCR_LOW_CONFIDENCE = "AI_OCR_LOW_CONFIDENCE";

    private final String errorCode;

    public OcrProviderException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OcrProviderException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static OcrProviderException notConfigured(String message) {
        return new OcrProviderException(ERROR_OCR_NOT_CONFIGURED, message);
    }

    public static OcrProviderException providerUnavailable(String message) {
        return new OcrProviderException(ERROR_OCR_PROVIDER_UNAVAILABLE, message);
    }

    public static OcrProviderException failed(String message) {
        return new OcrProviderException(ERROR_OCR_FAILED, message);
    }

    public static OcrProviderException failed(String message, Throwable cause) {
        return new OcrProviderException(ERROR_OCR_FAILED, message, cause);
    }

    public static OcrProviderException lowConfidence(String message) {
        return new OcrProviderException(ERROR_OCR_LOW_CONFIDENCE, message);
    }
}
