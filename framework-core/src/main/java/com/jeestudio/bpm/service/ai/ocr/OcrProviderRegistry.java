package com.jeestudio.bpm.service.ai.ocr;

import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: OCR服务商注册中心
 */
@Service
public class OcrProviderRegistry {

    public static final String PROVIDER_AUTO = "auto";
    public static final String PROVIDER_NONE = "none";

    private final List<OcrProvider> providers;

    @Autowired
    public OcrProviderRegistry(ObjectProvider<OcrProvider> providers) {
        this.providers = providers == null
                ? new ArrayList<>()
                : providers.orderedStream().collect(Collectors.toList());
    }

    public OcrResult recognize(OcrRequest request) {
        OcrProvider provider = resolveProvider(request);
        long startedAt = System.currentTimeMillis();
        try {
            OcrResult result = provider.recognize(request);
            if (result == null) {
                throw OcrProviderException.failed("OCR provider returned empty result");
            }
            result.setProviderCode(provider.getProviderCode());
            Map<String, Object> meta = result.getMeta() == null ? new LinkedHashMap<>() : result.getMeta();
            meta.putIfAbsent("providerCode", provider.getProviderCode());
            meta.putIfAbsent("providerName", provider.getProviderName());
            meta.putIfAbsent("elapsedMs", System.currentTimeMillis() - startedAt);
            result.setMeta(meta);
            return result;
        } catch (OcrProviderException ex) {
            throw ex;
        } catch (Exception ex) {
            throw OcrProviderException.failed("OCR request failed: " + ex.getMessage(), ex);
        }
    }

    public List<OcrProviderStatus> listProviderStatuses(String loginName, OcrRequest request) {
        List<OcrProviderStatus> statuses = new ArrayList<>();
        for (OcrProvider provider : providers) {
            OcrProviderStatus status = new OcrProviderStatus();
            status.setProviderCode(provider.getProviderCode());
            status.setProviderName(provider.getProviderName());
            boolean supported = request == null || provider.supports(request);
            boolean available = provider.isAvailable(loginName);
            status.setSupported(supported);
            status.setAvailable(available);
            if (!supported) {
                status.setMessage("OCR provider does not support this input");
            } else if (!available) {
                status.setMessage(defaultText(provider.getStatusMessage(loginName, request),
                        "OCR provider is not available for current user/configuration"));
            } else {
                status.setMessage(defaultText(provider.getStatusMessage(loginName, request), "available"));
            }
            statuses.add(status);
        }
        return statuses;
    }

    public boolean hasAvailableProvider(String loginName, OcrRequest request) {
        for (OcrProvider provider : providers) {
            if ((request == null || provider.supports(request)) && provider.isAvailable(loginName)) {
                return true;
            }
        }
        return false;
    }

    private OcrProvider resolveProvider(OcrRequest request) {
        String requestedProvider = normalizeProviderCode(request == null ? "" : request.getProviderCode());
        String loginName = request == null ? "" : request.getLoginName();
        if (PROVIDER_NONE.equals(requestedProvider)) {
            throw OcrProviderException.notConfigured("OCR provider is disabled");
        }
        if (providers.isEmpty()) {
            throw OcrProviderException.notConfigured("OCR provider is not configured; image and scanned PDF recognition need OCR");
        }
        if (!PROVIDER_AUTO.equals(requestedProvider)) {
            OcrProvider provider = findProvider(requestedProvider);
            if (provider == null) {
                throw OcrProviderException.providerUnavailable("OCR provider is unavailable: " + requestedProvider);
            }
            if (!provider.supports(request)) {
                throw OcrProviderException.providerUnavailable("OCR provider does not support this input: " + requestedProvider);
            }
            if (!provider.isAvailable(loginName)) {
                throw OcrProviderException.providerUnavailable("OCR provider is not available: " + requestedProvider);
            }
            return provider;
        }
        for (OcrProvider provider : providers) {
            if (provider.supports(request) && provider.isAvailable(loginName)) {
                return provider;
            }
        }
        throw OcrProviderException.notConfigured("No available OCR provider; " + summarizeStatuses(loginName, request));
    }

    private String summarizeStatuses(String loginName, OcrRequest request) {
        List<OcrProviderStatus> statuses = listProviderStatuses(loginName, request);
        if (statuses.isEmpty()) {
            return "provider list is empty";
        }
        return statuses.stream()
                .map(status -> status.getProviderCode()
                        + "(supported=" + status.getSupported()
                        + ", available=" + status.getAvailable()
                        + ", message=" + defaultText(status.getMessage(), "") + ")")
                .collect(Collectors.joining("; "));
    }

    private OcrProvider findProvider(String providerCode) {
        for (OcrProvider provider : providers) {
            if (normalizeProviderCode(provider.getProviderCode()).equals(providerCode)) {
                return provider;
            }
        }
        return null;
    }

    private String normalizeProviderCode(String providerCode) {
        String value = StringUtil.isBlank(providerCode) ? PROVIDER_AUTO : providerCode.trim().toLowerCase(Locale.ROOT);
        return StringUtil.isBlank(value) ? PROVIDER_AUTO : value;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtil.isBlank(value) ? defaultValue : value;
    }
}
