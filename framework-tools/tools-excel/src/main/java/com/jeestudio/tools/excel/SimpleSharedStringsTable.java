package com.jeestudio.tools.excel;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 轻量级共享字符串表实现
 * <p>
 * 使用 SAX 解析 sharedStrings.xml，将所有字符串存储为纯 {@code String}，
 * 完全避免 xmlbeans（{@code XSSFRichTextString} / {@code Locale}）的创建开销。
 * <p>
 * 对比 POI 自带实现：
 * <ul>
 *   <li>{@code SharedStringsTable}：将全部共享字符串对象预加载到内存，大文件会 OOM。</li>
 *   <li>{@code ReadOnlySharedStringsTable}：每次 {@code getItemAt} 都用 xmlbeans 重新解析，
 *       大量调用会产生海量 {@code Locale} 对象导致 OOM。</li>
 *   <li>本类：一次 SAX 解析，结果存为 {@code List<String>}，无 xmlbeans 依赖，内存极低。</li>
 * </ul>
 */
public class SimpleSharedStringsTable implements SharedStrings {

    /** 所有共享字符串，按索引顺序存储 */
    private final List<String> strings = new ArrayList<>();

    /**
     * 从 OPCPackage 中解析 sharedStrings.xml
     *
     * @param pkg 已打开的 OPCPackage（通过 File 打开，不占用额外内存）
     * @throws Exception 解析失败时抛出
     */
    public SimpleSharedStringsTable(OPCPackage pkg) throws Exception {
        List<PackagePart> parts = pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
        if (parts.isEmpty()) {
            // 没有共享字符串表（文件中全是内联字符串或数字），正常情况
            return;
        }
        try (InputStream is = parts.get(0).getInputStream()) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser saxParser = factory.newSAXParser();
            XMLReader reader = saxParser.getXMLReader();
            reader.setContentHandler(new SharedStringsHandler());
            reader.parse(new InputSource(is));
        }
    }

    @Override
    public XSSFRichTextString getItemAt(int idx) {
        // 此方法为满足 SharedStrings 接口而保留，但调用者应优先使用 getString(int) 以完全避开 xmlbeans
        // XSSFRichTextString(String) 构造函数会触发 xmlbeans Locale 创建，大文件下可能导致 OOM
        // 若必须调用此方法，请确保堆内存足够
        return new XSSFRichTextString(getString(idx));
    }
    
    /**
     * 直接返回索引对应的字符串，完全绕开 xmlbeans。
     * {@link ExcelSaxImportHandler} 应优先调用此方法而非 {@link #getItemAt(int)}。
     *
     * @param idx 共享字符串索引
     * @return 索引对应的字符串，越界时返回空字符串
     */
    public String getString(int idx) {
        return (idx >= 0 && idx < strings.size()) ? strings.get(idx) : "";
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public int getUniqueCount() {
        return strings.size();
    }

    // ==================== 内部 SAX 解析器 ====================

    /**
     * 解析 sharedStrings.xml 的 SAX handler。
     * <p>
     * sharedStrings.xml 结构示例：
     * <pre>
     * &lt;sst&gt;
     *   &lt;si&gt;&lt;t&gt;Hello&lt;/t&gt;&lt;/si&gt;
     *   &lt;si&gt;&lt;r&gt;&lt;t&gt;Rich&lt;/t&gt;&lt;/r&gt;&lt;r&gt;&lt;t&gt; Text&lt;/t&gt;&lt;/r&gt;&lt;/si&gt;
     * &lt;/sst&gt;
     * </pre>
     * 每个 {@code <si>} 为一条记录，{@code <t>} 为文本片段（富文本时有多个 {@code <r><t>}</t>}）。
     */
    private class SharedStringsHandler extends DefaultHandler {

        private static final String TAG_SI = "si";
        private static final String TAG_T = "t";

        /** 当前 &lt;si&gt; 累积的文本内容 */
        private StringBuilder currentText;
        /** 是否在 &lt;t&gt; 标签内 */
        private boolean inT = false;
        /** 是否在 &lt;si&gt; 标签内 */
        private boolean inSi = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            String tag = localName.isEmpty() ? qName : localName;
            if (TAG_SI.equals(tag)) {
                inSi = true;
                currentText = new StringBuilder();
            } else if (TAG_T.equals(tag) && inSi) {
                inT = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            String tag = localName.isEmpty() ? qName : localName;
            if (TAG_T.equals(tag)) {
                inT = false;
            } else if (TAG_SI.equals(tag)) {
                inSi = false;
                strings.add(currentText != null ? currentText.toString() : "");
                currentText = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (inT && currentText != null) {
                currentText.append(ch, start, length);
            }
        }
    }
}
