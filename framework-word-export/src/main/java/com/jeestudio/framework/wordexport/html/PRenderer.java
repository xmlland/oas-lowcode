package com.jeestudio.framework.wordexport.html;

import org.ddr.poi.html.ElementRenderer;
import org.ddr.poi.html.HtmlRenderContext;
import org.jsoup.nodes.Element;

/**
 * @Description: HTML段落渲染器
 */
public class PRenderer implements ElementRenderer {
    private static final String[] TAGS = new String[]{"p"};

    @Override
    public boolean renderStart(Element element, HtmlRenderContext htmlRenderContext) {
        return true;
    }

    @Override
    public void renderEnd(Element element, HtmlRenderContext context) {
        ElementRenderer.super.renderEnd(element, context);
    }

    @Override
    public String[] supportedTags() {
        return TAGS;
    }

    @Override
    public boolean renderAsBlock() {
        return false;
    }
}

