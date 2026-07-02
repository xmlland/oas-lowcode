package com.jeestudio.bpm.service.ai;

import com.alibaba.fastjson.JSON;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiModuleMaterialUrlParseResponse;
import com.jeestudio.bpm.utils.StringUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * @Description: URL模块材料提取服务
 */
@Service
public class UrlModuleMaterialExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(UrlModuleMaterialExtractionService.class);

    private static final int DEFAULT_MAX_PAGES = 5;
    private static final int HARD_MAX_PAGES = 20;
    private static final int DEFAULT_MAX_CHARS = 16000;
    private static final int HARD_MAX_CHARS = 30000;
    private static final int DEFAULT_TIMEOUT_SECONDS = 8;
    private static final int HARD_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_DYNAMIC_WAIT_MILLIS = 2000;
    private static final int HARD_DYNAMIC_WAIT_MILLIS = 5000;
    private static final int HARD_AXURE_SAMPLE_PAGES = 40;
    private static final int URL_MATERIAL_CACHE_MAX_SIZE = 10;
    private static final long URL_MATERIAL_CACHE_TTL_HOURS = 24L;
    private static final String URL_MATERIAL_CACHE_VERSION = "url-material-v6";
    private static final String URL_MATERIAL_CACHE_INDEX_PREFIX = "ai:formDesign:urlMaterial:index:";
    private static final String URL_MATERIAL_CACHE_ENTRY_PREFIX = "ai:formDesign:urlMaterial:entry:";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public AiModuleMaterialUrlParseResponse extract(AiModuleMaterialUrlParseRequest request, String loginName) {
        long startedAt = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        if (request == null || StringUtil.isBlank(request.getSourceUrl())) {
            return complete(AiModuleMaterialUrlParseResponse.failed(
                    AiModuleMaterialUrlParseResponse.ERROR_URL_EMPTY,
                    "Prototype URL cannot be empty"
            ), requestId, "", startedAt);
        }

        UrlOptions options = resolveOptions(request);
        String normalizedUrl;
        try {
            normalizedUrl = normalizeAndValidateUrl(request.getSourceUrl());
        } catch (Exception ex) {
            return complete(AiModuleMaterialUrlParseResponse.failed(
                    ex instanceof BlockedUrlException
                            ? AiModuleMaterialUrlParseResponse.ERROR_URL_BLOCKED
                            : AiModuleMaterialUrlParseResponse.ERROR_URL_UNSUPPORTED,
                    ex.getMessage()
            ), requestId, request.getSourceUrl(), startedAt);
        }

        UrlMaterialCacheKey cacheKey = buildCacheKey(request, loginName, normalizedUrl, options);
        if (!options.forceRefresh) {
            AiModuleMaterialUrlParseResponse cachedResponse = getCachedResponse(cacheKey, requestId, normalizedUrl, startedAt);
            if (cachedResponse != null) {
                return cachedResponse;
            }
        }

        try {
            List<AiModuleMaterialUrlParseResponse.Page> pages = collectPages(normalizedUrl, options);
            if (pages.isEmpty()) {
                return complete(AiModuleMaterialUrlParseResponse.failed(
                        AiModuleMaterialUrlParseResponse.ERROR_URL_TEXT_EMPTY,
                        "URL has no readable HTML text"
                ), requestId, normalizedUrl, startedAt);
            }
            optimizePageCandidates(pages, options);
            String rawText = buildRawText(normalizedUrl, request, pages, options.maxChars);
            if (StringUtil.isBlank(rawText)) {
                return complete(AiModuleMaterialUrlParseResponse.failed(
                        AiModuleMaterialUrlParseResponse.ERROR_URL_TEXT_EMPTY,
                        "URL has no readable prototype material"
                ), requestId, normalizedUrl, startedAt);
            }

            AiModuleMaterialUrlParseResponse response = new AiModuleMaterialUrlParseResponse();
            response.setSuccess(true);
            response.setSourceUrl(normalizedUrl);
            response.setPages(pages);
            response.setRawText(rawText);
            response.setExtraction(buildExtraction(normalizedUrl, pages, options));
            response.setMessage("URL prototype extracted");
            response.setFromCache(false);
            AiModuleMaterialUrlParseResponse completed = complete(response, requestId, normalizedUrl, startedAt);
            putCachedResponse(cacheKey, completed);
            return completed;
        } catch (Exception ex) {
            logger.warn("UrlModuleMaterialExtraction extract failed requestId={} url={} error={}",
                    requestId, normalizedUrl, ex.getMessage());
            return complete(AiModuleMaterialUrlParseResponse.failed(
                    AiModuleMaterialUrlParseResponse.ERROR_URL_FETCH_FAILED,
                    "URL fetch failed: " + ex.getMessage()
            ), requestId, normalizedUrl, startedAt);
        }
    }

    private List<AiModuleMaterialUrlParseResponse.Page> collectPages(String sourceUrl, UrlOptions options) throws Exception {
        if (options.dynamicRender) {
            try {
                List<AiModuleMaterialUrlParseResponse.Page> pages = collectDynamicPages(sourceUrl, options);
                if (!pages.isEmpty()) {
                    if (StringUtil.isBlank(options.collectorMode)) {
                        options.collectorMode = "playwright";
                    }
                    if (StringUtil.isBlank(options.collectorStatus)) {
                        options.collectorStatus = "success";
                    }
                    if (StringUtil.isBlank(options.collectorMessage) && !"axure".equals(options.collectorMode)) {
                        options.collectorMessage = "";
                    }
                    return pages;
                }
                options.collectorMessage = "动态渲染未读取到有效页面内容，已降级为静态 HTML 采集。";
                if (!options.fallbackToStatic) {
                    options.collectorMode = "playwright";
                    options.collectorStatus = "empty";
                    return pages;
                }
                logger.warn("UrlModuleMaterialExtraction dynamic empty, fallback static url={}", sourceUrl);
            } catch (Exception ex) {
                options.collectorMessage = summarizeDynamicCollectorError(ex);
                if (!options.fallbackToStatic) {
                    options.collectorMode = "playwright";
                    options.collectorStatus = "failed";
                    throw ex;
                }
                logger.warn("UrlModuleMaterialExtraction dynamic failed, fallback static url={} error={}",
                        sourceUrl, ex.getMessage());
            }
            options.collectorMode = "static";
            options.collectorStatus = "fallback";
        }
        List<AiModuleMaterialUrlParseResponse.Page> pages = collectStaticPages(sourceUrl, options);
        if (!options.dynamicRender) {
            options.collectorMode = "static";
            options.collectorStatus = "success";
            options.collectorMessage = "";
        } else if (StringUtil.isBlank(options.collectorMessage)) {
            options.collectorMessage = "动态渲染采集未完成，已降级为静态 HTML 采集。";
        }
        return pages;
    }

    private List<AiModuleMaterialUrlParseResponse.Page> collectStaticPages(String sourceUrl, UrlOptions options) throws Exception {
        List<AiModuleMaterialUrlParseResponse.Page> pages = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Set<String> queued = new LinkedHashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(sourceUrl);
        queued.add(sourceUrl);

        while (!queue.isEmpty() && pages.size() < options.maxPages) {
            String nextUrl = queue.removeFirst();
            queued.remove(nextUrl);
            if (visited.contains(nextUrl)) {
                continue;
            }
            visited.add(nextUrl);

            FetchedHtml fetched = fetchHtml(nextUrl, options.timeoutSeconds);
            AiModuleMaterialUrlParseResponse.Page page = parsePage(
                    fetched.url,
                    fetched.html,
                    "url_page_" + (pages.size() + 1),
                    Math.max(1000, options.maxChars / Math.max(options.maxPages, 1))
            );
            if (StringUtil.isBlank(page.getRawText())) {
                continue;
            }
            pages.add(page);

            if (!options.collectSameOriginLinks) {
                continue;
            }
            for (AiModuleMaterialUrlParseResponse.Link link : page.getLinks()) {
                String linkUrl = normalizeUrlSafely(link.getUrl());
                if (StringUtil.isBlank(linkUrl)
                        || visited.contains(linkUrl)
                        || queued.contains(linkUrl)
                        || !isSameOrigin(sourceUrl, linkUrl)
                        || !isLikelyHtmlPage(linkUrl)) {
                    continue;
                }
                queue.add(linkUrl);
                queued.add(linkUrl);
                if (queue.size() + pages.size() >= options.maxPages * 4) {
                    break;
                }
            }
        }
        return pages;
    }

    private List<AiModuleMaterialUrlParseResponse.Page> collectDynamicPages(String sourceUrl, UrlOptions options) throws Exception {
        List<AiModuleMaterialUrlParseResponse.Page> pages = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Set<String> queued = new LinkedHashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(sourceUrl);
        queued.add(sourceUrl);
        int timeoutMillis = options.timeoutSeconds * 1000;
        Set<String> seenPageFingerprints = new LinkedHashSet<>();

        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setTimeout(timeoutMillis);
            try (Browser browser = playwright.chromium().launch(launchOptions);
                 BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                         .setJavaScriptEnabled(true)
                         .setIgnoreHTTPSErrors(true)
                         .setBypassCSP(true)
                         .setUserAgent("Mozilla/5.0 (compatible; JeeStudio-AI-PrototypeCollector/1.0)"))) {
                List<AiModuleMaterialUrlParseResponse.Page> axurePages = collectAxurePrototypePages(
                        context, sourceUrl, options, timeoutMillis, seenPageFingerprints
                );
                if (!axurePages.isEmpty()) {
                    options.collectorMode = "axure";
                    options.collectorStatus = "success";
                    options.axureCollectedPageCount = axurePages.size();
                    return axurePages;
                }
                while (!queue.isEmpty() && pages.size() < options.maxPages) {
                    String nextUrl = queue.removeFirst();
                    queued.remove(nextUrl);
                    if (visited.contains(nextUrl)) {
                        continue;
                    }
                    visited.add(nextUrl);

                    com.microsoft.playwright.Page browserPage = context.newPage();
                    try {
                        navigateDynamicPage(browserPage, nextUrl, options, timeoutMillis);
                        AiModuleMaterialUrlParseResponse.Page page = addDynamicPageSnapshot(
                                browserPage, options, pages, seenPageFingerprints, ""
                        );
                        enqueueDynamicPageLinks(page, sourceUrl, visited, queued, queue, options, pages);
                        if (pages.size() < options.maxPages) {
                            collectDynamicClickedPages(browserPage, sourceUrl, options, timeoutMillis,
                                    pages, seenPageFingerprints, visited, queued, queue);
                        }
                    } finally {
                        browserPage.close();
                    }
                }
            }
        } catch (PlaywrightException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
        return pages;
    }

    private List<AiModuleMaterialUrlParseResponse.Page> collectAxurePrototypePages(BrowserContext context,
                                                                                   String sourceUrl,
                                                                                   UrlOptions options,
                                                                                   int timeoutMillis,
                                                                                   Set<String> seenPageFingerprints) {
        List<AiModuleMaterialUrlParseResponse.Page> pages = new ArrayList<>();
        com.microsoft.playwright.Page sitemapPage = context.newPage();
        try {
            navigateDynamicPage(sitemapPage, sourceUrl, options, timeoutMillis);
            List<AxurePageRef> pageRefs = readAxureSitemapPageRefs(sitemapPage);
            options.axureSitemapPageCount = pageRefs.size();
            if (pageRefs.isEmpty()) {
                return pages;
            }
            logger.info("UrlModuleMaterialExtraction axure sitemap detected url={} pageCount={}",
                    sourceUrl, pageRefs.size());
            List<AxurePageRef> sortedPageRefs = sortAxurePageRefs(pageRefs);
            int sampleLimit = Math.min(sortedPageRefs.size(),
                    Math.max(options.maxPages, Math.min(HARD_AXURE_SAMPLE_PAGES, options.maxPages * 2)));
            for (AxurePageRef pageRef : sortedPageRefs) {
                if (pages.size() >= sampleLimit) {
                    break;
                }
                String pageUrl = resolvePrototypePageUrl(sourceUrl, pageRef.url);
                if (StringUtil.isBlank(pageUrl) || !isSameOrigin(sourceUrl, pageUrl) || !isLikelyHtmlPage(pageUrl)) {
                    continue;
                }
                com.microsoft.playwright.Page page = context.newPage();
                try {
                    navigateDynamicPage(page, pageUrl, options, Math.min(timeoutMillis, 12000));
                    addDynamicPageSnapshot(page, options, pages, seenPageFingerprints, pageRef.name);
                } catch (Exception ex) {
                    logger.debug("UrlModuleMaterialExtraction axure page skipped name={} url={} error={}",
                            pageRef.name, pageUrl, ex.getMessage());
                } finally {
                    page.close();
                }
            }
            options.candidateSamplePageCount = pages.size();
            if (!pages.isEmpty()) {
                options.collectorMessage = "识别到 Axure 原型页面树，已采集 "
                        + Math.min(pages.size(), options.maxPages) + "/" + pageRefs.size()
                        + " 个页面候选，已按表单页可能性排序。";
            }
        } catch (Exception ex) {
            logger.debug("UrlModuleMaterialExtraction axure sitemap skipped url={} error={}",
                    sourceUrl, ex.getMessage());
        } finally {
            sitemapPage.close();
        }
        return pages;
    }

    private List<AxurePageRef> sortAxurePageRefs(List<AxurePageRef> pageRefs) {
        List<AxurePageRef> sorted = new ArrayList<>(pageRefs == null ? new ArrayList<AxurePageRef>() : pageRefs);
        sorted.sort(Comparator
                .comparingInt(this::estimateAxurePageRefScore)
                .reversed()
                .thenComparing(ref -> ref.depth == null ? 0 : ref.depth));
        return sorted;
    }

    private int estimateAxurePageRefScore(AxurePageRef pageRef) {
        if (pageRef == null) {
            return 0;
        }
        String text = (defaultText(pageRef.name, "") + " " + defaultText(pageRef.url, "")).toLowerCase(Locale.ROOT);
        int score = 0;
        if (containsAny(text, "新增", "编辑", "详情", "登记", "申请", "办理", "审批", "配置", "维护",
                "档案", "信息", "管理", "表单", "列表", "查询", "台账", "合同", "污染源",
                "add", "edit", "detail", "form", "list", "manage", "setting", "config")) {
            score += 30;
        }
        if (containsAny(text, "首页", "index", "home", "综合分析", "dashboard", "统计", "分析", "图表", "大屏")) {
            score -= 20;
        }
        if (containsAny(text, "页面_", "page_")) {
            score -= 5;
        }
        if (pageRef.depth != null && pageRef.depth > 0) {
            score += Math.min(pageRef.depth * 2, 6);
        }
        return score;
    }

    private List<AxurePageRef> readAxureSitemapPageRefs(com.microsoft.playwright.Page page) {
        try {
            Object result = page.evaluate("async () => {\n"
                    + "  const flatten = (roots) => {\n"
                    + "    if (!Array.isArray(roots)) return [];\n"
                    + "    const rows = [];\n"
                    + "    const walk = (nodes, depth) => {\n"
                    + "      (nodes || []).forEach(node => {\n"
                    + "        const name = node.pageName || node.name || node.title || '';\n"
                    + "        const url = node.url || '';\n"
                    + "        const id = node.id || '';\n"
                    + "        if (url) rows.push({ id, name, url, depth });\n"
                    + "        walk(node.children || node.pages || [], depth + 1);\n"
                    + "      });\n"
                    + "    };\n"
                    + "    walk(roots, 0);\n"
                    + "    return rows.slice(0, 80);\n"
                    + "  };\n"
                    + "  const axure = window.$axure;\n"
                    + "  const doc = axure && (axure.document || (typeof axure.getDocument === 'function' ? axure.getDocument() : null));\n"
                    + "  const runtimeRows = flatten(doc && doc.sitemap && doc.sitemap.rootNodes);\n"
                    + "  if (runtimeRows.length > 0) return JSON.stringify(runtimeRows);\n"
                    + "  try {\n"
                    + "    const documentUrl = new URL('data/document.js', window.location.href).toString();\n"
                    + "    const response = await fetch(documentUrl, { cache: 'no-store' });\n"
                    + "    if (!response.ok) return '';\n"
                    + "    const script = await response.text();\n"
                    + "    let loadedDocument = null;\n"
                    + "    const previousAxure = window.$axure;\n"
                    + "    window.$axure = Object.assign({}, previousAxure || {}, {\n"
                    + "      loadDocument: value => { loadedDocument = value; }\n"
                    + "    });\n"
                    + "    try {\n"
                    + "      eval(script);\n"
                    + "    } finally {\n"
                    + "      if (previousAxure) window.$axure = previousAxure;\n"
                    + "      else delete window.$axure;\n"
                    + "    }\n"
                    + "    const fallbackRows = flatten(loadedDocument && loadedDocument.sitemap && loadedDocument.sitemap.rootNodes);\n"
                    + "    return fallbackRows.length > 0 ? JSON.stringify(fallbackRows) : '';\n"
                    + "  } catch (error) {\n"
                    + "    return '';\n"
                    + "  }\n"
                    + "}");
            String json = result == null ? "" : String.valueOf(result);
            if (StringUtil.isBlank(json)) {
                return new ArrayList<>();
            }
            List<AxurePageRef> refs = JSON.parseArray(json, AxurePageRef.class);
            return refs == null ? new ArrayList<>() : refs;
        } catch (Exception ex) {
            logger.debug("UrlModuleMaterialExtraction read axure sitemap failed error={}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    private String resolvePrototypePageUrl(String baseUrl, String pageUrl) {
        try {
            if (StringUtil.isBlank(baseUrl) || StringUtil.isBlank(pageUrl)) {
                return "";
            }
            URI base = new URI(baseUrl);
            URI resolved = base.resolve(toPrototypePageUri(pageUrl.trim()));
            String normalized = normalizeUrlSafely(resolved.toString());
            return StringUtil.isBlank(normalized) ? resolved.toString() : normalized;
        } catch (Exception ex) {
            return "";
        }
    }

    private URI toPrototypePageUri(String pageUrl) throws Exception {
        if (StringUtil.isBlank(pageUrl)) {
            return new URI("");
        }
        try {
            return new URI(pageUrl);
        } catch (Exception ignored) {
            // Axure exports may keep Chinese page filenames unescaped.
        }
        if (pageUrl.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
            return URI.create(pageUrl);
        }
        String target = pageUrl;
        String fragment = null;
        int fragmentIndex = target.indexOf('#');
        if (fragmentIndex >= 0) {
            fragment = target.substring(fragmentIndex + 1);
            target = target.substring(0, fragmentIndex);
        }
        String query = null;
        int queryIndex = target.indexOf('?');
        if (queryIndex >= 0) {
            query = target.substring(queryIndex + 1);
            target = target.substring(0, queryIndex);
        }
        return new URI(null, null, target, query, fragment);
    }

    private void collectDynamicClickedPages(com.microsoft.playwright.Page browserPage,
                                            String sourceUrl,
                                            UrlOptions options,
                                            int timeoutMillis,
                                            List<AiModuleMaterialUrlParseResponse.Page> pages,
                                            Set<String> seenPageFingerprints,
                                            Set<String> visited,
                                            Set<String> queued,
                                            ArrayDeque<String> queue) {
        List<DynamicClickCandidate> candidates = collectDynamicClickCandidates(browserPage);
        Set<String> clickedKeys = new LinkedHashSet<>();
        for (DynamicClickCandidate candidate : candidates) {
            if (pages.size() >= options.maxPages) {
                return;
            }
            if (candidate == null || StringUtil.isBlank(candidate.text)) {
                continue;
            }
            String clickKey = normalizeWhitespace(candidate.text).toLowerCase(Locale.ROOT);
            if (!clickedKeys.add(clickKey)) {
                continue;
            }
            try {
                Locator locator = browserPage.locator(candidate.selector).nth(candidate.index);
                if (!locator.isVisible()) {
                    continue;
                }
                locator.scrollIntoViewIfNeeded(new Locator.ScrollIntoViewIfNeededOptions().setTimeout(1000));
                locator.click(new Locator.ClickOptions().setTimeout(Math.min(timeoutMillis, 3000)));
                waitAfterDynamicInteraction(browserPage, options, timeoutMillis);
                AiModuleMaterialUrlParseResponse.Page clickedPage = addDynamicPageSnapshot(
                        browserPage, options, pages, seenPageFingerprints, candidate.text
                );
                if (clickedPage != null) {
                    options.dynamicClickSnapshotCount++;
                    enqueueDynamicPageLinks(clickedPage, sourceUrl, visited, queued, queue, options, pages);
                }
            } catch (Exception ex) {
                logger.debug("UrlModuleMaterialExtraction dynamic click skipped text={} selector={} index={} error={}",
                        candidate.text, candidate.selector, candidate.index, ex.getMessage());
            }
        }
    }

    private List<DynamicClickCandidate> collectDynamicClickCandidates(com.microsoft.playwright.Page browserPage) {
        List<DynamicClickCandidate> candidates = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        String[] selectors = new String[]{
                "aside a",
                "nav a",
                ".ant-layout-sider a",
                ".ant-menu a",
                ".el-menu a",
                ".sidebar a",
                ".sider a",
                ".side-menu a",
                ".menu a",
                "[class*='sidebar'] a",
                "[class*='sider'] a",
                "[class*='menu'] a",
                ".ant-menu-item",
                ".el-menu-item",
                "[role='menuitem']",
                "aside [role='button']",
                "nav [role='button']",
                "[class*='menu'] [role='button']"
        };
        for (String selector : selectors) {
            try {
                Locator locator = browserPage.locator(selector);
                int count = Math.min(locator.count(), 80);
                for (int i = 0; i < count && candidates.size() < 80; i++) {
                    Locator item = locator.nth(i);
                    String text = normalizeWhitespace(defaultText(item.textContent(
                            new Locator.TextContentOptions().setTimeout(500)
                    ), ""));
                    if (shouldSkipDynamicClickCandidate(text)) {
                        continue;
                    }
                    String key = selector + "|" + text.toLowerCase(Locale.ROOT);
                    if (!seen.add(key)) {
                        continue;
                    }
                    candidates.add(new DynamicClickCandidate(selector, i, text));
                }
            } catch (Exception ex) {
                logger.debug("UrlModuleMaterialExtraction dynamic selector skipped selector={} error={}",
                        selector, ex.getMessage());
            }
        }
        return candidates;
    }

    private boolean shouldSkipDynamicClickCandidate(String text) {
        String value = normalizeWhitespace(text);
        if (StringUtil.isBlank(value) || value.length() > 40) {
            return true;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("http")
                || value.contains("保存")
                || value.contains("删除")
                || value.contains("移除")
                || value.contains("提交")
                || value.contains("取消")
                || value.contains("确定")
                || value.contains("查询")
                || value.contains("搜索")
                || value.contains("重置")
                || value.contains("导出")
                || value.contains("导入")
                || value.contains("登录")
                || value.contains("退出")
                || value.contains("注销");
    }

    private AiModuleMaterialUrlParseResponse.Page addDynamicPageSnapshot(com.microsoft.playwright.Page browserPage,
                                                                         UrlOptions options,
                                                                         List<AiModuleMaterialUrlParseResponse.Page> pages,
                                                                         Set<String> seenPageFingerprints,
                                                                         String titleHint) {
        try {
            DynamicHtml dynamicHtml = readDynamicHtml(browserPage);
            AiModuleMaterialUrlParseResponse.Page page = parsePage(
                    dynamicHtml.url,
                    dynamicHtml.html,
                    "url_page_" + (pages.size() + 1),
                    Math.max(1000, options.maxChars / Math.max(options.maxPages, 1)),
                    titleHint
            );
            if (StringUtil.isBlank(page.getRawText())) {
                return null;
            }
            String fingerprint = buildDynamicPageFingerprint(page);
            if (StringUtil.isBlank(fingerprint) || !seenPageFingerprints.add(fingerprint)) {
                return null;
            }
            pages.add(page);
            return page;
        } catch (Exception ex) {
            logger.debug("UrlModuleMaterialExtraction dynamic snapshot skipped error={}", ex.getMessage());
            return null;
        }
    }

    private void enqueueDynamicPageLinks(AiModuleMaterialUrlParseResponse.Page page,
                                         String sourceUrl,
                                         Set<String> visited,
                                         Set<String> queued,
                                         ArrayDeque<String> queue,
                                         UrlOptions options,
                                         List<AiModuleMaterialUrlParseResponse.Page> pages) {
        if (page == null || !options.collectSameOriginLinks || pages.size() >= options.maxPages) {
            return;
        }
        for (AiModuleMaterialUrlParseResponse.Link link : page.getLinks()) {
            String linkUrl = normalizeUrlSafely(link.getUrl());
            if (StringUtil.isBlank(linkUrl)
                    || visited.contains(linkUrl)
                    || queued.contains(linkUrl)
                    || !isSameOrigin(sourceUrl, linkUrl)
                    || !isLikelyHtmlPage(linkUrl)) {
                continue;
            }
            queue.add(linkUrl);
            queued.add(linkUrl);
            if (queue.size() + pages.size() >= options.maxPages * 4) {
                break;
            }
        }
    }

    private String buildDynamicPageFingerprint(AiModuleMaterialUrlParseResponse.Page page) {
        if (page == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(normalizeWhitespace(defaultText(page.getUrl(), ""))).append('\n');
        builder.append(normalizeWhitespace(defaultText(page.getTitle(), ""))).append('\n');
        builder.append(normalizeWhitespace(defaultText(page.getTextPreview(), ""))).append('\n');
        builder.append(String.join("|", page.getHeadings())).append('\n');
        builder.append(String.join("|", page.getLabels())).append('\n');
        builder.append(String.join("|", page.getInputs())).append('\n');
        builder.append(String.join("|", page.getButtons()));
        return shortHash(builder.toString(), 24);
    }

    private void navigateDynamicPage(com.microsoft.playwright.Page page, String url, UrlOptions options, int timeoutMillis) throws Exception {
        String currentUrl = normalizeAndValidateUrl(url);
        page.setDefaultTimeout(timeoutMillis);
        page.setDefaultNavigationTimeout(timeoutMillis);
        page.navigate(currentUrl, new com.microsoft.playwright.Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.COMMIT)
                .setTimeout(timeoutMillis));
        waitAfterDynamicInteraction(page, options, timeoutMillis);
    }

    private void waitAfterDynamicInteraction(com.microsoft.playwright.Page page, UrlOptions options, int timeoutMillis) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED,
                    new com.microsoft.playwright.Page.WaitForLoadStateOptions().setTimeout(Math.min(timeoutMillis, 8000)));
        } catch (PlaywrightException ignored) {
            // Some prototype systems keep loading resources. A committed document plus short wait can still provide usable DOM.
        }
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE,
                    new com.microsoft.playwright.Page.WaitForLoadStateOptions().setTimeout(Math.min(timeoutMillis, 2000)));
        } catch (PlaywrightException ignored) {
            // Some prototype systems keep polling. A short idle wait is only a bonus signal.
        }
        if (options.dynamicWaitMillis > 0) {
            page.waitForTimeout(options.dynamicWaitMillis);
        }
    }

    private DynamicHtml readDynamicHtml(com.microsoft.playwright.Page page) {
        String finalUrl = normalizeUrlSafely(page.url());
        StringBuilder html = new StringBuilder(defaultText(page.content(), ""));
        Frame mainFrame = page.mainFrame();
        for (Frame frame : page.frames()) {
            if (frame == null || frame.equals(mainFrame)) {
                continue;
            }
            try {
                String frameUrl = normalizeUrlSafely(frame.url());
                String frameContent = frame.content();
                if (StringUtil.isBlank(frameContent)) {
                    continue;
                }
                html.append('\n')
                        .append("<section data-ai-prototype-frame=\"true\">")
                        .append("<h2>Frame: ").append(defaultText(frameUrl, "embedded prototype page")).append("</h2>")
                        .append(frameContent)
                        .append("</section>");
            } catch (Exception ex) {
                logger.debug("UrlModuleMaterialExtraction frame content skipped url={} error={}", frame.url(), ex.getMessage());
            }
        }
        return new DynamicHtml(StringUtil.isBlank(finalUrl) ? "" : finalUrl, html.toString());
    }

    private DynamicHtml fetchDynamicHtml(BrowserContext context, String url, UrlOptions options, int timeoutMillis) throws Exception {
        com.microsoft.playwright.Page page = context.newPage();
        try {
            navigateDynamicPage(page, url, options, timeoutMillis);
            DynamicHtml dynamicHtml = readDynamicHtml(page);
            String finalUrl = StringUtil.isBlank(dynamicHtml.url) ? normalizeAndValidateUrl(url) : normalizeAndValidateUrl(dynamicHtml.url);
            return new DynamicHtml(finalUrl, dynamicHtml.html);
        } finally {
            page.close();
        }
    }

    private String summarizeDynamicCollectorError(Exception ex) {
        String message = ex == null ? "" : defaultText(ex.getMessage(), "");
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        if (lowerMessage.contains("timeout")) {
            return "动态渲染等待页面响应超时，已降级为静态 HTML 采集。";
        }
        if (lowerMessage.contains("browser") || lowerMessage.contains("executable") || lowerMessage.contains("install")) {
            return "当前环境的动态浏览器能力不可用，已降级为静态 HTML 采集。";
        }
        if (lowerMessage.contains("net::err") || lowerMessage.contains("navigation failed")) {
            return "动态渲染访问页面失败，已降级为静态 HTML 采集。";
        }
        return "动态渲染采集失败，已降级为静态 HTML 采集。";
    }

    private FetchedHtml fetchHtml(String url, int timeoutSeconds) throws Exception {
        normalizeAndValidateUrl(url);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        String currentUrl = url;
        for (int redirectCount = 0; redirectCount <= 3; redirectCount++) {
            currentUrl = normalizeAndValidateUrl(currentUrl);
            Request request = new Request.Builder()
                    .url(currentUrl)
                    .header("User-Agent", "Mozilla/5.0 (compatible; JeeStudio-AI-PrototypeCollector/1.0)")
                    .header("Accept", "text/html,application/xhtml+xml,text/plain;q=0.8,*/*;q=0.5")
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (isRedirectStatus(response.code())) {
                    String location = response.header("Location", "");
                    if (StringUtil.isBlank(location)) {
                        throw new IOException("redirect without Location");
                    }
                    currentUrl = resolveRedirectUrl(currentUrl, location);
                    continue;
                }
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP " + response.code());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("empty response body");
                }
                String contentType = response.header("Content-Type", "");
                if (StringUtil.isNotBlank(contentType)
                        && !contentType.toLowerCase(Locale.ROOT).contains("html")
                        && !contentType.toLowerCase(Locale.ROOT).contains("text")) {
                    throw new IOException("unsupported content type " + contentType);
                }
                String finalUrl = response.request() == null ? currentUrl : response.request().url().toString();
                String normalizedFinalUrl = normalizeUrlSafely(finalUrl);
                return new FetchedHtml(StringUtil.isBlank(normalizedFinalUrl) ? currentUrl : normalizedFinalUrl, body.string());
            }
        }
        throw new IOException("too many redirects");
    }

    private AiModuleMaterialUrlParseResponse.Page parsePage(String url, String html, String pageId, int maxPageChars) {
        return parsePage(url, html, pageId, maxPageChars, "");
    }

    private AiModuleMaterialUrlParseResponse.Page parsePage(String url, String html, String pageId, int maxPageChars, String titleHint) {
        Document doc = Jsoup.parse(defaultText(html, ""), url);
        doc.select("script,style,noscript,svg,canvas,iframe").remove();

        AiModuleMaterialUrlParseResponse.Page page = new AiModuleMaterialUrlParseResponse.Page();
        page.setId(pageId);
        page.setUrl(url);
        page.setTitle(defaultText(firstNonBlank(titleHint, doc.title()), inferTitleFromUrl(url)));
        page.setHeadings(pickDistinctText(doc.select("h1,h2,h3,h4"), 24));
        page.setLabels(extractLabels(doc));
        page.setInputs(extractInputs(doc));
        page.setButtons(extractButtons(doc));
        page.setTables(extractTables(doc));
        page.setLinks(extractLinks(doc, url));

        String visibleText = normalizeWhitespace(doc.body() == null ? doc.text() : doc.body().text());
        String rawText = buildPageRawText(page, visibleText, maxPageChars);
        page.setRawText(rawText);
        page.setTextPreview(truncate(visibleText, 260));
        page.setIncluded(true);
        return page;
    }

    private List<String> extractLabels(Document doc) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        addAll(values, pickDistinctText(doc.select("label"), 40), 40);
        addAll(values, pickDistinctText(doc.select(".label,.form-label,.ant-form-item-label,.el-form-item__label"), 40), 40);
        addAll(values, pickDistinctText(doc.select("th"), 40), 40);
        addAll(values, pickDistinctText(doc.select("dt"), 20), 40);
        return new ArrayList<>(values);
    }

    private List<String> extractInputs(Document doc) {
        List<String> inputs = new ArrayList<>();
        Elements elements = doc.select("input,select,textarea");
        Elements labels = doc.select("label[for]");
        for (Element element : elements) {
            String text = firstNonBlank(
                    element.attr("aria-label"),
                    element.attr("placeholder"),
                    element.attr("name"),
                    element.attr("id")
            );
            String id = element.attr("id");
            if (StringUtil.isNotBlank(id)) {
                for (Element label : labels) {
                    if (id.equals(label.attr("for")) && StringUtil.isNotBlank(label.text())) {
                        text = label.text();
                        break;
                    }
                }
            }
            String type = element.tagName();
            if ("input".equals(type)) {
                type = firstNonBlank(element.attr("type"), "input");
            }
            text = normalizeWhitespace(text);
            if (StringUtil.isBlank(text)) {
                continue;
            }
            addDistinct(inputs, text + " [" + type + "]", 60);
        }
        return inputs;
    }

    private List<String> extractButtons(Document doc) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        addAll(values, pickDistinctText(doc.select("button"), 30), 30);
        addAll(values, pickDistinctAttr(doc.select("input[type=button],input[type=submit],input[type=reset]"), "value", 20), 40);
        addAll(values, pickDistinctText(doc.select("a.btn,a.button,.ant-btn,.el-button"), 30), 50);
        return new ArrayList<>(values);
    }

    private List<AiModuleMaterialUrlParseResponse.Table> extractTables(Document doc) {
        List<AiModuleMaterialUrlParseResponse.Table> tables = new ArrayList<>();
        Elements tableElements = doc.select("table");
        for (int i = 0; i < tableElements.size() && tables.size() < 12; i++) {
            Element tableElement = tableElements.get(i);
            AiModuleMaterialUrlParseResponse.Table table = new AiModuleMaterialUrlParseResponse.Table();
            table.setTitle(firstNonBlank(tableElement.selectFirst("caption") == null ? "" : tableElement.selectFirst("caption").text(), "表格" + (i + 1)));
            table.setHeaders(extractTableHeaders(tableElement));
            table.setRowCount(tableElement.select("tr").size());
            if (!table.getHeaders().isEmpty() || table.getRowCount() > 0) {
                tables.add(table);
            }
        }
        return tables;
    }

    private List<String> extractTableHeaders(Element tableElement) {
        Elements headers = tableElement.select("thead th");
        if (headers.isEmpty()) {
            headers = tableElement.select("tr").isEmpty()
                    ? new Elements()
                    : tableElement.select("tr").get(0).select("th,td");
        }
        return pickDistinctText(headers, 30);
    }

    private List<AiModuleMaterialUrlParseResponse.Link> extractLinks(Document doc, String baseUrl) {
        List<AiModuleMaterialUrlParseResponse.Link> links = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        Elements linkElements = doc.select("a[href]");
        for (Element element : linkElements) {
            String href = normalizeUrlSafely(element.absUrl("href"));
            if (StringUtil.isBlank(href)
                    || seen.contains(href)
                    || !isLikelyHtmlPage(href)) {
                continue;
            }
            String text = normalizeWhitespace(element.text());
            if (StringUtil.isBlank(text)) {
                text = inferTitleFromUrl(href);
            }
            AiModuleMaterialUrlParseResponse.Link link = new AiModuleMaterialUrlParseResponse.Link();
            link.setText(truncate(text, 80));
            link.setUrl(href);
            links.add(link);
            seen.add(href);
            if (links.size() >= 60) {
                break;
            }
        }
        return links;
    }

    private String buildPageRawText(AiModuleMaterialUrlParseResponse.Page page, String visibleText, int maxPageChars) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "Prototype page: " + defaultText(page.getTitle(), ""), maxPageChars);
        appendLine(builder, "URL: " + defaultText(page.getUrl(), ""), maxPageChars);
        appendList(builder, "Headings", page.getHeadings(), maxPageChars);
        appendList(builder, "Form labels", page.getLabels(), maxPageChars);
        appendList(builder, "Inputs", page.getInputs(), maxPageChars);
        appendList(builder, "Buttons", page.getButtons(), maxPageChars);
        appendTables(builder, page.getTables(), maxPageChars);
        appendLine(builder, "Visible text excerpt:", maxPageChars);
        appendLine(builder, truncate(visibleText, Math.max(800, maxPageChars / 2)), maxPageChars);
        return builder.toString().trim();
    }

    private void optimizePageCandidates(List<AiModuleMaterialUrlParseResponse.Page> pages, UrlOptions options) {
        if (pages == null || pages.isEmpty()) {
            return;
        }
        Map<AiModuleMaterialUrlParseResponse.Page, Integer> originalOrder = new LinkedHashMap<>();
        for (int i = 0; i < pages.size(); i++) {
            AiModuleMaterialUrlParseResponse.Page page = pages.get(i);
            originalOrder.put(page, i);
            applyPageQualityScore(page);
        }
        pages.sort(Comparator
                .comparingInt((AiModuleMaterialUrlParseResponse.Page page) ->
                        page.getQualityScore() == null ? 0 : page.getQualityScore())
                .reversed()
                .thenComparing(page -> originalOrder.getOrDefault(page, 0)));
        int sampledCount = pages.size();
        while (pages.size() > options.maxPages) {
            pages.remove(pages.size() - 1);
        }
        options.candidateQualityOptimized = true;
        options.candidateSamplePageCount = Math.max(options.candidateSamplePageCount, sampledCount);
    }

    private void applyPageQualityScore(AiModuleMaterialUrlParseResponse.Page page) {
        List<String> reasons = new ArrayList<>();
        int score = calculatePageQualityScore(page, reasons);
        page.setQualityScore(score);
        page.setQualityLevel(score >= 70 ? "high" : score >= 40 ? "medium" : "low");
        page.setQualityReasons(reasons);
    }

    private int calculatePageQualityScore(AiModuleMaterialUrlParseResponse.Page page, List<String> reasons) {
        if (page == null) {
            return 0;
        }
        int labelCount = page.getLabels() == null ? 0 : page.getLabels().size();
        int inputCount = page.getInputs() == null ? 0 : page.getInputs().size();
        int buttonCount = page.getButtons() == null ? 0 : page.getButtons().size();
        int linkCount = page.getLinks() == null ? 0 : page.getLinks().size();
        int tableCount = page.getTables() == null ? 0 : page.getTables().size();
        int tableHeaderCount = countTableHeaders(page.getTables());
        int rawTextLength = defaultText(page.getRawText(), "").length();
        String titleText = (defaultText(page.getTitle(), "") + " " + defaultText(page.getUrl(), "")
                + " " + defaultText(page.getTextPreview(), "")).toLowerCase(Locale.ROOT);

        int score = 10;
        if (labelCount > 0) {
            int value = Math.min(32, labelCount * 4);
            score += value;
            reasons.add("表单标签 " + labelCount + " 个");
        }
        if (inputCount > 0) {
            int value = Math.min(30, inputCount * 6);
            score += value;
            reasons.add("输入控件 " + inputCount + " 个");
        }
        if (tableCount > 0) {
            int value = Math.min(24, tableCount * 6 + Math.min(tableHeaderCount, 12));
            score += value;
            reasons.add("表格结构 " + tableCount + " 个");
        }
        if (hasActionButton(page.getButtons())) {
            score += 10;
            reasons.add("包含业务操作按钮");
        }
        if (containsAny(titleText, "新增", "编辑", "详情", "登记", "申请", "办理", "审批", "配置", "维护",
                "档案", "信息", "管理", "表单", "台账", "合同", "污染源",
                "add", "edit", "detail", "form", "manage", "setting", "config")) {
            score += 14;
            reasons.add("标题或内容包含业务页关键词");
        } else if (containsAny(titleText, "列表", "查询", "list", "search")) {
            score += 8;
            reasons.add("疑似列表或查询页");
        }
        if (rawTextLength >= 180) {
            score += Math.min(10, rawTextLength / 80);
        }

        if (isIndexLikePage(page)) {
            score -= 28;
            reasons.add("首页或入口页降权");
        }
        if (labelCount + inputCount + tableHeaderCount == 0) {
            score -= 24;
            reasons.add("表单结构较少");
        }
        if (linkCount >= 20 && inputCount == 0 && labelCount < 3 && tableCount == 0) {
            score -= 22;
            reasons.add("导航链接较多");
        }
        if (containsAny(titleText, "综合分析", "dashboard", "统计", "分析", "图表", "大屏")) {
            score -= 14;
            reasons.add("偏统计展示页");
        }
        return Math.max(0, Math.min(100, score));
    }

    private int countTableHeaders(List<AiModuleMaterialUrlParseResponse.Table> tables) {
        int count = 0;
        if (tables == null) {
            return count;
        }
        for (AiModuleMaterialUrlParseResponse.Table table : tables) {
            count += table == null || table.getHeaders() == null ? 0 : table.getHeaders().size();
        }
        return count;
    }

    private boolean hasActionButton(List<String> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return false;
        }
        String text = String.join(" ", buttons).toLowerCase(Locale.ROOT);
        return containsAny(text, "保存", "提交", "确定", "新增", "编辑", "删除", "查询", "重置", "导入", "导出",
                "save", "submit", "ok", "add", "edit", "delete", "search", "reset", "import", "export");
    }

    private boolean isIndexLikePage(AiModuleMaterialUrlParseResponse.Page page) {
        String title = defaultText(page.getTitle(), "").trim().toLowerCase(Locale.ROOT);
        String url = defaultText(page.getUrl(), "").toLowerCase(Locale.ROOT);
        return "index".equals(title)
                || "home".equals(title)
                || "首页".equals(title)
                || url.endsWith("/")
                || url.endsWith("/index.html")
                || url.endsWith("/home.html")
                || url.endsWith("/首页.html");
    }

    private String buildRawText(String sourceUrl, AiModuleMaterialUrlParseRequest request,
                                List<AiModuleMaterialUrlParseResponse.Page> pages, int maxChars) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, "Prototype URL material", maxChars);
        appendLine(builder, "Source URL: " + defaultText(sourceUrl, ""), maxChars);
        appendLine(builder, "Collected pages: " + pages.size(), maxChars);
        if (StringUtil.isNotBlank(request.getSupplementText())) {
            appendLine(builder, "User supplement:", maxChars);
            appendLine(builder, request.getSupplementText(), maxChars);
        }
        for (int i = 0; i < pages.size(); i++) {
            AiModuleMaterialUrlParseResponse.Page page = pages.get(i);
            appendLine(builder, "", maxChars);
            appendLine(builder, "Confirmed candidate page " + (i + 1) + ":", maxChars);
            appendLine(builder, defaultText(page.getRawText(), ""), maxChars);
        }
        return builder.toString().trim();
    }

    private Map<String, Object> buildExtraction(String sourceUrl, List<AiModuleMaterialUrlParseResponse.Page> pages, UrlOptions options) {
        Map<String, Object> extraction = new LinkedHashMap<>();
        extraction.put("sourceType", "url");
        extraction.put("sourceUrl", sourceUrl);
        extraction.put("pageCount", pages.size());
        extraction.put("maxPages", options.maxPages);
        extraction.put("maxChars", options.maxChars);
        extraction.put("collectSameOriginLinks", options.collectSameOriginLinks);
        extraction.put("dynamicRender", options.dynamicRender);
        extraction.put("fallbackToStatic", options.fallbackToStatic);
        extraction.put("forceRefresh", options.forceRefresh);
        extraction.put("dynamicWaitMillis", options.dynamicWaitMillis);
        extraction.put("dynamicClickSnapshotCount", options.dynamicClickSnapshotCount);
        extraction.put("axureSitemapPageCount", options.axureSitemapPageCount);
        extraction.put("axureCollectedPageCount", options.axureCollectedPageCount);
        extraction.put("candidateQualityOptimized", options.candidateQualityOptimized);
        extraction.put("candidateSamplePageCount", options.candidateSamplePageCount);
        extraction.put("collectorMode", defaultText(options.collectorMode, options.dynamicRender ? "playwright" : "static"));
        extraction.put("collectorStatus", defaultText(options.collectorStatus, "success"));
        extraction.put("collectorMessage", defaultText(options.collectorMessage, ""));
        extraction.put("dynamicCollector", defaultText(options.collectorMode, options.dynamicRender ? "playwright" : "static"));
        extraction.put("dynamicCollectorStatus", defaultText(options.collectorStatus, "success"));
        return extraction;
    }

    private String normalizeAndValidateUrl(String sourceUrl) throws Exception {
        URI uri = new URI(defaultText(sourceUrl, "").trim());
        String scheme = defaultText(uri.getScheme(), "").toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("Only http/https prototype URLs are supported");
        }
        if (StringUtil.isNotBlank(uri.getUserInfo())) {
            throw new BlockedUrlException("URL with user info is not allowed");
        }
        String host = defaultText(uri.getHost(), "").toLowerCase(Locale.ROOT);
        if (StringUtil.isBlank(host)) {
            throw new IllegalArgumentException("URL host is empty");
        }
        if ("localhost".equals(host) || host.endsWith(".localhost")) {
            throw new BlockedUrlException("Localhost URL is not allowed");
        }
        if ("169.254.169.254".equals(host)) {
            throw new BlockedUrlException("Cloud metadata address is not allowed");
        }
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isMulticastAddress()) {
                throw new BlockedUrlException("Special local network address is not allowed");
            }
        }
        String path = StringUtil.isBlank(uri.getRawPath()) ? "/" : uri.getRawPath();
        URI clean = new URI(scheme, uri.getRawAuthority(), path, uri.getRawQuery(), null);
        return clean.normalize().toString();
    }

    private boolean isSameOrigin(String sourceUrl, String targetUrl) {
        try {
            URI source = new URI(sourceUrl);
            URI target = new URI(targetUrl);
            return defaultText(source.getScheme(), "").equalsIgnoreCase(defaultText(target.getScheme(), ""))
                    && defaultText(source.getHost(), "").equalsIgnoreCase(defaultText(target.getHost(), ""))
                    && normalizePort(source) == normalizePort(target);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isRedirectStatus(int statusCode) {
        return statusCode == 301
                || statusCode == 302
                || statusCode == 303
                || statusCode == 307
                || statusCode == 308;
    }

    private String resolveRedirectUrl(String baseUrl, String location) throws Exception {
        URI base = new URI(baseUrl);
        URI resolved = base.resolve(location);
        return normalizeAndValidateUrl(resolved.toString());
    }

    private int normalizePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private boolean isLikelyHtmlPage(String url) {
        String normalized = defaultText(url, "").toLowerCase(Locale.ROOT);
        if (StringUtil.isBlank(normalized)
                || normalized.startsWith("javascript:")
                || normalized.startsWith("mailto:")
                || normalized.startsWith("tel:")) {
            return false;
        }
        int queryIndex = normalized.indexOf('?');
        String path = queryIndex >= 0 ? normalized.substring(0, queryIndex) : normalized;
        return !(path.endsWith(".pdf")
                || path.endsWith(".doc")
                || path.endsWith(".docx")
                || path.endsWith(".xls")
                || path.endsWith(".xlsx")
                || path.endsWith(".zip")
                || path.endsWith(".rar")
                || path.endsWith(".7z")
                || path.endsWith(".png")
                || path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".gif")
                || path.endsWith(".webp"));
    }

    private String normalizeUrlSafely(String url) {
        try {
            if (StringUtil.isBlank(url)) {
                return "";
            }
            URI uri = new URI(url.trim());
            if (StringUtil.isBlank(uri.getScheme()) || StringUtil.isBlank(uri.getHost())) {
                return "";
            }
            String path = StringUtil.isBlank(uri.getRawPath()) ? "/" : uri.getRawPath();
            return new URI(uri.getScheme().toLowerCase(Locale.ROOT), uri.getRawAuthority(), path, uri.getRawQuery(), null)
                    .normalize()
                    .toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private UrlOptions resolveOptions(AiModuleMaterialUrlParseRequest request) {
        AiModuleMaterialUrlParseRequest.Options options = request == null ? null : request.getOptions();
        UrlOptions resolved = new UrlOptions();
        resolved.maxPages = normalizeLimit(options == null ? null : options.getMaxPages(), DEFAULT_MAX_PAGES, HARD_MAX_PAGES);
        resolved.maxChars = normalizeLimit(options == null ? null : options.getMaxChars(), DEFAULT_MAX_CHARS, HARD_MAX_CHARS);
        resolved.timeoutSeconds = normalizeLimit(options == null ? null : options.getTimeoutSeconds(), DEFAULT_TIMEOUT_SECONDS, HARD_TIMEOUT_SECONDS);
        resolved.collectSameOriginLinks = options == null || !Boolean.FALSE.equals(options.getCollectSameOriginLinks());
        resolved.dynamicRender = options != null && Boolean.TRUE.equals(options.getDynamicRender());
        resolved.fallbackToStatic = options == null || !Boolean.FALSE.equals(options.getFallbackToStatic());
        resolved.forceRefresh = options != null && Boolean.TRUE.equals(options.getForceRefresh());
        resolved.dynamicWaitMillis = normalizeLimit(
                options == null ? null : options.getDynamicWaitMillis(),
                DEFAULT_DYNAMIC_WAIT_MILLIS,
                HARD_DYNAMIC_WAIT_MILLIS
        );
        return resolved;
    }

    private int normalizeLimit(Integer value, int defaultValue, int maxValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }

    private AiModuleMaterialUrlParseResponse complete(AiModuleMaterialUrlParseResponse response,
                                                      String requestId,
                                                      String sourceUrl,
                                                      long startedAt) {
        response.setRequestId(requestId);
        if (StringUtil.isBlank(response.getSourceUrl())) {
            response.setSourceUrl(defaultText(sourceUrl, ""));
        }
        if (response.getFromCache() == null) {
            response.setFromCache(false);
        }
        response.setElapsedMs(System.currentTimeMillis() - startedAt);
        logger.info("UrlModuleMaterialExtraction result requestId={} success={} url={} elapsedMs={} pageCount={} errorCode={} fromCache={}",
                requestId,
                response.getSuccess(),
                response.getSourceUrl(),
                response.getElapsedMs(),
                response.getPages() == null ? 0 : response.getPages().size(),
                response.getErrorCode(),
                response.getFromCache());
        return response;
    }

    private UrlMaterialCacheKey buildCacheKey(AiModuleMaterialUrlParseRequest request,
                                              String loginName,
                                              String sourceUrl,
                                              UrlOptions options) {
        String userScope = shortHash(normalizeCacheText(defaultText(loginName, "anonymous")), 16);
        StringBuilder canonical = new StringBuilder();
        canonical.append("cacheVersion=").append(URL_MATERIAL_CACHE_VERSION).append('\n');
        canonical.append("sourceUrl=").append(normalizeCacheText(sourceUrl)).append('\n');
        canonical.append("maxPages=").append(options.maxPages).append('\n');
        canonical.append("maxChars=").append(options.maxChars).append('\n');
        canonical.append("timeoutSeconds=").append(options.timeoutSeconds).append('\n');
        canonical.append("collectSameOriginLinks=").append(options.collectSameOriginLinks).append('\n');
        canonical.append("dynamicRender=").append(options.dynamicRender).append('\n');
        canonical.append("fallbackToStatic=").append(options.fallbackToStatic).append('\n');
        canonical.append("dynamicWaitMillis=").append(options.dynamicWaitMillis);
        String fingerprint = sha256(canonical.toString());
        return new UrlMaterialCacheKey(
                URL_MATERIAL_CACHE_INDEX_PREFIX + userScope,
                URL_MATERIAL_CACHE_ENTRY_PREFIX + userScope + ":" + fingerprint,
                shortHash(fingerprint, 12)
        );
    }

    private AiModuleMaterialUrlParseResponse getCachedResponse(UrlMaterialCacheKey cacheKey,
                                                               String requestId,
                                                               String sourceUrl,
                                                               long startedAt) {
        if (cacheKey == null || redisTemplate == null) {
            return null;
        }
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey.getEntryKey());
            if (!(cached instanceof String)) {
                return null;
            }
            AiModuleMaterialUrlParseResponse response = JSON.parseObject((String) cached, AiModuleMaterialUrlParseResponse.class);
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                return null;
            }
            touchCacheEntry(cacheKey);
            response.setMessage(defaultText(response.getMessage(), "URL prototype extracted") + " (cache hit)");
            response.setFromCache(true);
            logger.info("UrlModuleMaterialExtraction cache hit requestId={} cacheKey={}", requestId, cacheKey.getLogKey());
            return complete(response, requestId, sourceUrl, startedAt);
        } catch (Exception ex) {
            logger.warn("UrlModuleMaterialExtraction cache read skipped cacheKey={} error={}", cacheKey.getLogKey(), ex.getMessage());
            return null;
        }
    }

    private void putCachedResponse(UrlMaterialCacheKey cacheKey, AiModuleMaterialUrlParseResponse response) {
        if (cacheKey == null || redisTemplate == null || response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey.getEntryKey(), JSON.toJSONString(response),
                    URL_MATERIAL_CACHE_TTL_HOURS, TimeUnit.HOURS);
            touchCacheEntry(cacheKey);
            logger.info("UrlModuleMaterialExtraction cache put cacheKey={} maxSize={} ttlHours={}",
                    cacheKey.getLogKey(), URL_MATERIAL_CACHE_MAX_SIZE, URL_MATERIAL_CACHE_TTL_HOURS);
        } catch (Exception ex) {
            logger.warn("UrlModuleMaterialExtraction cache write skipped cacheKey={} error={}", cacheKey.getLogKey(), ex.getMessage());
        }
    }

    private void touchCacheEntry(UrlMaterialCacheKey cacheKey) {
        redisTemplate.opsForList().remove(cacheKey.getIndexKey(), 0, cacheKey.getEntryKey());
        redisTemplate.opsForList().leftPush(cacheKey.getIndexKey(), cacheKey.getEntryKey());
        redisTemplate.expire(cacheKey.getEntryKey(), URL_MATERIAL_CACHE_TTL_HOURS, TimeUnit.HOURS);
        List<Object> overflowKeys = redisTemplate.opsForList().range(cacheKey.getIndexKey(), URL_MATERIAL_CACHE_MAX_SIZE, -1);
        redisTemplate.opsForList().trim(cacheKey.getIndexKey(), 0, URL_MATERIAL_CACHE_MAX_SIZE - 1);
        redisTemplate.expire(cacheKey.getIndexKey(), URL_MATERIAL_CACHE_TTL_HOURS, TimeUnit.HOURS);
        if (overflowKeys == null || overflowKeys.isEmpty()) {
            return;
        }
        for (Object overflowKey : overflowKeys) {
            if (overflowKey instanceof String && !cacheKey.getEntryKey().equals(overflowKey)) {
                redisTemplate.delete((String) overflowKey);
            }
        }
    }

    private void appendList(StringBuilder builder, String title, List<String> values, int maxChars) {
        if (values == null || values.isEmpty()) {
            return;
        }
        appendLine(builder, title + ":", maxChars);
        for (String value : values) {
            appendLine(builder, "- " + value, maxChars);
        }
    }

    private void appendTables(StringBuilder builder, List<AiModuleMaterialUrlParseResponse.Table> tables, int maxChars) {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        appendLine(builder, "Tables:", maxChars);
        for (AiModuleMaterialUrlParseResponse.Table table : tables) {
            appendLine(builder, "- " + defaultText(table.getTitle(), "table")
                    + " headers: " + String.join(", ", table.getHeaders())
                    + "; rows: " + defaultText(table.getRowCount() == null ? "" : String.valueOf(table.getRowCount()), "0"), maxChars);
        }
    }

    private void appendLine(StringBuilder builder, String line, int maxChars) {
        if (builder.length() >= maxChars) {
            return;
        }
        String text = defaultText(line, "");
        int remaining = maxChars - builder.length();
        if (text.length() > remaining) {
            text = text.substring(0, Math.max(0, remaining));
        }
        builder.append(text).append('\n');
    }

    private List<String> pickDistinctText(Elements elements, int max) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (elements == null) {
            return new ArrayList<>();
        }
        for (Element element : elements) {
            String text = normalizeWhitespace(element.text());
            if (StringUtil.isBlank(text)) {
                continue;
            }
            values.add(truncate(text, 120));
            if (values.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(values);
    }

    private List<String> pickDistinctAttr(Elements elements, String attr, int max) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (elements == null) {
            return new ArrayList<>();
        }
        for (Element element : elements) {
            String text = normalizeWhitespace(element.attr(attr));
            if (StringUtil.isBlank(text)) {
                continue;
            }
            values.add(truncate(text, 120));
            if (values.size() >= max) {
                break;
            }
        }
        return new ArrayList<>(values);
    }

    private void addAll(LinkedHashSet<String> target, List<String> values, int max) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (StringUtil.isBlank(value)) {
                continue;
            }
            target.add(value);
            if (target.size() >= max) {
                return;
            }
        }
    }

    private void addDistinct(List<String> target, String value, int max) {
        if (target == null || StringUtil.isBlank(value) || target.contains(value) || target.size() >= max) {
            return;
        }
        target.add(value);
    }

    private String normalizeWhitespace(String value) {
        return defaultText(value, "").replace('\u00a0', ' ')
                .replaceAll("[\\t\\r\\n]+", " ")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private String truncate(String value, int maxLength) {
        String text = defaultText(value, "");
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength));
    }

    private String inferTitleFromUrl(String url) {
        try {
            URI uri = new URI(defaultText(url, ""));
            String path = defaultText(uri.getPath(), "");
            if (StringUtil.isBlank(path) || "/".equals(path)) {
                return defaultText(uri.getHost(), "原型页面");
            }
            String[] parts = path.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (StringUtil.isNotBlank(parts[i])) {
                    return parts[i];
                }
            }
        } catch (Exception ignored) {
        }
        return "原型页面";
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

    private boolean containsAny(String value, String... keywords) {
        if (StringUtil.isBlank(value) || keywords == null || keywords.length == 0) {
            return false;
        }
        String text = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (StringUtil.isNotBlank(keyword) && text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
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
            byte[] encoded = digest.digest(defaultText(value, "").getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (Exception ex) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtil.isBlank(value) ? defaultValue : value;
    }

    private static class UrlOptions {
        private int maxPages;
        private int maxChars;
        private int timeoutSeconds;
        private boolean collectSameOriginLinks;
        private boolean dynamicRender;
        private boolean fallbackToStatic;
        private boolean forceRefresh;
        private int dynamicWaitMillis;
        private int dynamicClickSnapshotCount;
        private int axureSitemapPageCount;
        private int axureCollectedPageCount;
        private boolean candidateQualityOptimized;
        private int candidateSamplePageCount;
        private String collectorMode;
        private String collectorStatus;
        private String collectorMessage;
    }

    public static class AxurePageRef {
        public String id;
        public String name;
        public String url;
        public Integer depth;
    }

    private static class DynamicClickCandidate {
        private final String selector;
        private final int index;
        private final String text;

        private DynamicClickCandidate(String selector, int index, String text) {
            this.selector = selector;
            this.index = index;
            this.text = text;
        }
    }

    private static class FetchedHtml {
        private final String url;
        private final String html;

        private FetchedHtml(String url, String html) {
            this.url = url;
            this.html = html;
        }
    }

    private static class DynamicHtml {
        private final String url;
        private final String html;

        private DynamicHtml(String url, String html) {
            this.url = url;
            this.html = html;
        }
    }

    private static class UrlMaterialCacheKey {
        private final String indexKey;
        private final String entryKey;
        private final String logKey;

        private UrlMaterialCacheKey(String indexKey, String entryKey, String logKey) {
            this.indexKey = indexKey;
            this.entryKey = entryKey;
            this.logKey = logKey;
        }

        public String getIndexKey() {
            return indexKey;
        }

        public String getEntryKey() {
            return entryKey;
        }

        public String getLogKey() {
            return logKey;
        }
    }

    private static class BlockedUrlException extends Exception {
        private BlockedUrlException(String message) {
            super(message);
        }
    }
}
