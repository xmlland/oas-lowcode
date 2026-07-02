package com.jeestudio.tools.excel;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: xlsx SAX 事件处理器，逐行解析 sheet XML，每行数据通过 RowHandler 回调传出
 * 避免将整个 Workbook 加载到内存，适用于大文件导入场景。
 */
public class ExcelSaxImportHandler extends DefaultHandler {

    /**
     * 行数据回调接口
     */
    @FunctionalInterface
    public interface RowHandler {
        /**
         * @param rowIndex  行索引（从 0 开始）
         * @param cellValues 本行各列的字符串值（按列索引顺序排列，空列以空字符串占位）
         */
        void handle(int rowIndex, List<String> cellValues);
    }

    // xlsx 单元格类型常量
    private static final String CELL_TYPE_SHARED_STRING = "s";
    private static final String CELL_TYPE_INLINE_STRING = "inlineStr";
    private static final String CELL_TYPE_FORMULA = "f";
    private static final String CELL_TYPE_BOOLEAN = "b";
    private static final String CELL_TYPE_ERROR = "e";

    private static final String TAG_ROW = "row";
    private static final String TAG_CELL = "c";
    private static final String TAG_VALUE = "v";
    private static final String TAG_INLINE_STR = "t";

    // 日期格式
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";

    private final SimpleSharedStringsTable sharedStrings;
    private final StylesTable stylesTable;
    private final RowHandler rowHandler;

    // 当前行状态
    private int currentRow = -1;
    /** 当前列的列号（0-based） */
    private int currentColIndex = -1;
    /** 当前列的最大列号，用于保证列索引连续 */
    private int maxColIndex = -1;
    private String currentCellType = "";
    private int currentStyleIndex = -1;
    private boolean isDate = false;
    private StringBuilder currentValue = new StringBuilder();
    private boolean inValue = false;
    private boolean inInlineStr = false;

    /** 当前行各列数据（按列索引，空列以 "" 占位） */
    private List<String> currentRowData = new ArrayList<>();

    public ExcelSaxImportHandler(SimpleSharedStringsTable sharedStrings, StylesTable stylesTable, RowHandler rowHandler) {
        this.sharedStrings = sharedStrings;
        this.stylesTable = stylesTable;
        this.rowHandler = rowHandler;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        String tagName = qName.isEmpty() ? localName : qName;

        if (TAG_ROW.equals(tagName)) {
            // 解析行号属性 r="1"（1-based），转换为 0-based
            String rowAttr = attributes.getValue("r");
            if (rowAttr != null) {
                currentRow = Integer.parseInt(rowAttr) - 1;
            } else {
                currentRow++;
            }
            currentRowData = new ArrayList<>();
            maxColIndex = -1;
        } else if (TAG_CELL.equals(tagName)) {
            // 解析列引用 r="A1" 获取列号
            String cellRef = attributes.getValue("r");
            currentColIndex = cellRefToColIndex(cellRef);

            // 获取单元格类型
            currentCellType = attributes.getValue("t");
            if (currentCellType == null) {
                currentCellType = "";
            }

            // 获取样式索引，判断是否为日期格式
            String styleStr = attributes.getValue("s");
            isDate = false;
            if (styleStr != null && stylesTable != null) {
                currentStyleIndex = Integer.parseInt(styleStr);
                XSSFCellStyle cellStyle = stylesTable.getStyleAt(currentStyleIndex);
                if (cellStyle != null) {
                    int dataFormat = cellStyle.getDataFormat();
                    String dataFormatString = cellStyle.getDataFormatString();
                    if (dataFormatString == null) {
                        dataFormatString = BuiltinFormats.getBuiltinFormat(dataFormat);
                    }
                    isDate = DateUtil.isADateFormat(dataFormat, dataFormatString);
                }
            } else {
                currentStyleIndex = -1;
            }

            currentValue = new StringBuilder();
            inValue = false;
            inInlineStr = false;
        } else if (TAG_VALUE.equals(tagName)) {
            inValue = true;
            currentValue = new StringBuilder();
        } else if (TAG_INLINE_STR.equals(tagName) && CELL_TYPE_INLINE_STRING.equals(currentCellType)) {
            inInlineStr = true;
            currentValue = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String tagName = qName.isEmpty() ? localName : qName;

        if (TAG_VALUE.equals(tagName) && inValue) {
            inValue = false;
        } else if (TAG_INLINE_STR.equals(tagName) && inInlineStr) {
            inInlineStr = false;
        } else if (TAG_CELL.equals(tagName)) {
            // 将当前单元格的值填充到行数据列表，空列用 "" 占位
            String cellValueStr = resolveCellValue();
            fillRowData(currentColIndex, cellValueStr);
            maxColIndex = Math.max(maxColIndex, currentColIndex);
        } else if (TAG_ROW.equals(tagName)) {
            // 行结束，触发回调
            rowHandler.handle(currentRow, new ArrayList<>(currentRowData));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inValue || inInlineStr) {
            currentValue.append(ch, start, length);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据单元格类型和原始值内容，转换为最终字符串
     */
    private String resolveCellValue() {
        String rawValue = currentValue.toString();

        if (CELL_TYPE_SHARED_STRING.equals(currentCellType)) {
            // 共享字符串：直接调用 getString() 而非 getItemAt().getString()，完全绕开 XSSFRichTextString / xmlbeans
            if (!rawValue.isEmpty()) {
                int index = Integer.parseInt(rawValue);
                return sharedStrings.getString(index);
            }
            return "";
        } else if (CELL_TYPE_BOOLEAN.equals(currentCellType)) {
            return "1".equals(rawValue) ? "TRUE" : "FALSE";
        } else if (CELL_TYPE_ERROR.equals(currentCellType)) {
            return "";
        } else if (CELL_TYPE_INLINE_STRING.equals(currentCellType)) {
            return rawValue;
        } else {
            // 数字类型（包括日期）
            if (rawValue.isEmpty()) {
                return "";
            }
            if (isDate) {
                // 将 Excel 日期序列号转换为日期字符串
                try {
                    double numericValue = Double.parseDouble(rawValue);
                    Date date = DateUtil.getJavaDate(numericValue);
                    // 判断是否包含时间部分
                    long time = date.getTime() % 86400000L;
                    String fmt = time == 0 ? DATE_ONLY_FORMAT : DATE_FORMAT;
                    return new SimpleDateFormat(fmt).format(date);
                } catch (NumberFormatException e) {
                    return rawValue;
                }
            }
            return rawValue;
        }
    }

    /**
     * 将单元格值填充到指定列索引，中间空列以 "" 补齐
     */
    private void fillRowData(int colIndex, String value) {
        // 补齐中间空列
        while (currentRowData.size() <= colIndex) {
            currentRowData.add("");
        }
        currentRowData.set(colIndex, value);
    }

    /**
     * 将单元格引用（如 "A1", "BC3"）解析为 0-based 列索引
     */
    private static int cellRefToColIndex(String cellRef) {
        if (cellRef == null || cellRef.isEmpty()) {
            return 0;
        }
        int colIndex = 0;
        for (int i = 0; i < cellRef.length(); i++) {
            char c = cellRef.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                colIndex = colIndex * 26 + (c - 'A' + 1);
            } else {
                // 遇到数字，列字母部分结束
                break;
            }
        }
        return colIndex - 1; // 转为 0-based
    }
}
