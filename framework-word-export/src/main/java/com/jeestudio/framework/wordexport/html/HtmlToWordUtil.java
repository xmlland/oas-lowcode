package com.jeestudio.framework.wordexport.html;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import org.ddr.poi.html.ElementRenderer;
import org.ddr.poi.html.HtmlRenderConfig;
import org.ddr.poi.html.HtmlRenderPolicy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @Description: HTML转Word工具
 */
public class HtmlToWordUtil {

    public static ByteArrayOutputStream htmlToWord(String html) {
        ConfigureBuilder configureBuilder = Configure.builder();
        // html渲染插件
        HtmlRenderConfig htmlRenderConfig = new HtmlRenderConfig();
        ArrayList<ElementRenderer> arrayList = new ArrayList<>();
        arrayList.add(new PRenderer());
        htmlRenderConfig.setCustomRenderers(arrayList);
        htmlRenderConfig.setGlobalFont("宋体");
        HtmlRenderPolicy htmlRenderPolicy = new HtmlRenderPolicy(htmlRenderConfig);
        configureBuilder.bind("html", htmlRenderPolicy);
        XWPFTemplate template = XWPFTemplate.compile(Objects.requireNonNull(HtmlToWordUtil.class.getResourceAsStream("/htmlWord.docx")), configureBuilder.build()).render(new HashMap<String, Object>() {{
            put("html", html);
        }});
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            template.write(byteArrayOutputStream);
            template.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream;
    }
}
