package com.jeestudio.bpm.service.ai;

import com.jeestudio.bpm.common.entity.gen.AiFormMaterialExcelParseRequest;
import com.jeestudio.bpm.common.entity.gen.AiFormMaterialFileParseRequest;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderException;
import com.jeestudio.bpm.service.ai.ocr.OcrProviderRegistry;
import com.jeestudio.bpm.service.ai.ocr.OcrRequest;
import com.jeestudio.bpm.service.ai.ocr.OcrResult;
import com.jeestudio.bpm.utils.StringUtil;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 文件材料提取服务
 */
@Service
public class FileMaterialExtractionService {

    private static final int MAX_EXCEL_FILE_BYTES = 15 * 1024 * 1024;
    private static final int MAX_WORD_FILE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_PDF_FILE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_IMAGE_FILE_BYTES = 8 * 1024 * 1024;
    private static final int DEFAULT_EXCEL_MAX_ROWS = 80;
    private static final int DEFAULT_EXCEL_MAX_COLUMNS = 60;
    private static final int DEFAULT_WORD_MAX_CHARS = 16000;
    private static final int DEFAULT_PDF_MAX_PAGES = 3;
    private static final int DEFAULT_PDF_MAX_CHARS = 16000;
    private static final int DEFAULT_OCR_MAX_CHARS = 16000;
    private static final int DEFAULT_PDF_OCR_DPI = 160;
    private static final String DEFAULT_OCR_LANGUAGE = "chi_sim+eng";
    private static final int DEFAULT_EXCEL_MULTI_SHEET_MAX_ROWS = 80;

    public FileMaterialContent extract(AiFormMaterialFileParseRequest request) throws Exception {
        return extract(request, "", "");
    }

    public FileMaterialContent extract(AiFormMaterialFileParseRequest request, String loginName) throws Exception {
        return extract(request, loginName, "");
    }

    @Autowired
    private OcrProviderRegistry ocrProviderRegistry;

    public FileMaterialContent extract(AiFormMaterialFileParseRequest request, String loginName, String requestId) throws Exception {
        byte[] bytes = decodeBase64(request == null ? null : request.getFileBase64());
        if (bytes.length == 0) {
            throw new IllegalArgumentException("empty file");
        }
        String sourceType = inferSourceType(
                request == null ? "" : request.getSourceType(),
                request == null ? "" : request.getFileName(),
                request == null ? "" : request.getMimeType()
        );
        if ("excel".equals(sourceType)) {
            if (bytes.length > MAX_EXCEL_FILE_BYTES) {
                throw new IllegalArgumentException("file is larger than 15MB");
            }
            FileMaterialContent content = extractExcelBytes(bytes,
                    request == null ? "" : request.getFileName(),
                    request == null ? "" : request.getMimeType(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getSheetIndex(),
                    request != null && request.getOptions() != null && Boolean.TRUE.equals(request.getOptions().getExtractAllSheets()),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxRows(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxColumns()
            );
            content.setSize(bytes.length);
            return content;
        }
        if ("word".equals(sourceType)) {
            if (bytes.length > MAX_WORD_FILE_BYTES) {
                throw new IllegalArgumentException("file is larger than 10MB");
            }
            FileMaterialContent content = extractWordBytes(bytes,
                    request == null ? "" : request.getFileName(),
                    request == null ? "" : request.getMimeType(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxChars(),
                    request != null && request.getOptions() != null && Boolean.FALSE.equals(request.getOptions().getExtractTables()),
                    request != null && request.getOptions() != null && Boolean.FALSE.equals(request.getOptions().getExtractHeaders())
            );
            content.setSize(bytes.length);
            return content;
        }
        if ("pdf".equals(sourceType)) {
            if (bytes.length > MAX_PDF_FILE_BYTES) {
                throw new IllegalArgumentException("file is larger than 10MB");
            }
            FileMaterialContent content = extractPdfBytes(bytes,
                    request == null ? "" : request.getFileName(),
                    request == null ? "" : request.getMimeType(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxPages(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getPageIndexes(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxChars(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getOcrProvider(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getOcrLanguage(),
                    loginName,
                    requestId
            );
            content.setSize(bytes.length);
            return content;
        }
        if ("image".equals(sourceType)) {
            if (bytes.length > MAX_IMAGE_FILE_BYTES) {
                throw new IllegalArgumentException("file is larger than 8MB");
            }
            FileMaterialContent content = extractImageBytes(bytes,
                    request == null ? "" : request.getFileName(),
                    request == null ? "" : request.getMimeType(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getMaxChars(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getOcrProvider(),
                    request == null || request.getOptions() == null ? null : request.getOptions().getOcrLanguage(),
                    loginName,
                    requestId
            );
            content.setSize(bytes.length);
            return content;
        }
        throw new IllegalArgumentException("unsupported file type: " + defaultText(sourceType, "unknown"));
    }

    public FileMaterialContent extractExcel(AiFormMaterialExcelParseRequest request) throws Exception {
        byte[] bytes = decodeBase64(request.getFileBase64());
        if (bytes.length == 0) {
            throw new IllegalArgumentException("empty file");
        }
        if (bytes.length > MAX_EXCEL_FILE_BYTES) {
            throw new IllegalArgumentException("file is larger than 15MB");
        }

        FileMaterialContent content = extractExcelBytes(bytes,
                request.getFileName(),
                "",
                request.getOptions() == null ? null : request.getOptions().getSheetIndex(),
                false,
                request.getOptions() == null ? null : request.getOptions().getMaxRows(),
                request.getOptions() == null ? null : request.getOptions().getMaxColumns());
        content.setSize(bytes.length);
        return content;
    }

    private FileMaterialContent extractExcelBytes(byte[] bytes, String fileName, String mimeType,
                                                  Integer requestedSheetIndex, boolean extractAllSheets, Integer maxRowsOption,
                                                  Integer maxColumnsOption) throws Exception {
        int maxRows = normalizeLimit(maxRowsOption,
                DEFAULT_EXCEL_MAX_ROWS, DEFAULT_EXCEL_MAX_ROWS);
        if (extractAllSheets && requestedSheetIndex == null) {
            maxRows = Math.min(maxRows, DEFAULT_EXCEL_MULTI_SHEET_MAX_ROWS);
        }
        int maxColumns = normalizeLimit(maxColumnsOption,
                DEFAULT_EXCEL_MAX_COLUMNS, DEFAULT_EXCEL_MAX_COLUMNS);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            if (workbook.getNumberOfSheets() <= 0) {
                throw new IllegalArgumentException("workbook has no sheet");
            }
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            if (requestedSheetIndex != null) {
                if (requestedSheetIndex < 0 || requestedSheetIndex >= workbook.getNumberOfSheets()) {
                    throw new IllegalArgumentException("sheet index is out of range");
                }
                FileMaterialContent content = extractExcelSheet(workbook.getSheetAt(requestedSheetIndex), fileName, mimeType,
                        requestedSheetIndex, maxRows, maxColumns, formatter, evaluator);
                if (content == null) {
                    throw new IllegalArgumentException("selected sheet is empty");
                }
                return content;
            }

            if (extractAllSheets) {
                FileMaterialContent content = extractExcelWorkbook(workbook, fileName, mimeType,
                        maxRows, maxColumns, formatter, evaluator);
                if (content != null) {
                    return content;
                }
                throw new IllegalArgumentException("workbook has no readable rows");
            }

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                FileMaterialContent content = extractExcelSheet(workbook.getSheetAt(i), fileName, mimeType,
                        i, maxRows, maxColumns, formatter, evaluator);
                if (content != null) {
                    return content;
                }
            }
        }
        throw new IllegalArgumentException("workbook has no readable rows");
    }

    private FileMaterialContent extractExcelWorkbook(Workbook workbook, String fileName, String mimeType,
                                                     int maxRows, int maxColumns,
                                                     DataFormatter formatter, FormulaEvaluator evaluator) {
        List<FileMaterialTable> tables = new ArrayList<>();
        List<Map<String, Object>> sheetMetaList = new ArrayList<>();
        int sheetCount = workbook == null ? 0 : workbook.getNumberOfSheets();
        int readableSheetCount = 0;
        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            List<FileMaterialTable> sheetTables = extractExcelSheetTables(sheet, i,
                    "table_" + (tables.size() + 1), maxRows, maxColumns, formatter, evaluator);
            boolean readable = !sheetTables.isEmpty();
            if (readable) {
                readableSheetCount++;
            }
            for (int tableIndex = 0; tableIndex < sheetTables.size(); tableIndex++) {
                sheetTables.get(tableIndex).setId("table_" + (tables.size() + 1));
                tables.add(sheetTables.get(tableIndex));
            }
            Map<String, Object> sheetMeta = new LinkedHashMap<>();
            sheetMeta.put("sheetIndex", i);
            sheetMeta.put("sheetName", sheet == null ? "" : defaultText(sheet.getSheetName(), ""));
            sheetMeta.put("readable", readable);
            sheetMeta.put("tableCount", sheetTables.size());
            List<String> tableTitles = new ArrayList<>();
            int rowCount = 0;
            int dataRowCount = 0;
            for (FileMaterialTable table : sheetTables) {
                tableTitles.add(defaultText(table.getTitle(), ""));
                rowCount += table.getRawRows() == null ? 0 : table.getRawRows().size();
                dataRowCount += table.getRows() == null ? 0 : table.getRows().size();
            }
            sheetMeta.put("tableTitles", tableTitles);
            sheetMeta.put("rowCount", rowCount);
            sheetMeta.put("dataRowCount", dataRowCount);
            sheetMetaList.add(sheetMeta);
        }
        if (tables.isEmpty()) {
            return null;
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fileName", defaultText(fileName, ""));
        meta.put("sheetCount", sheetCount);
        meta.put("extractedSheetCount", readableSheetCount);
        meta.put("tableBlockCount", tables.size());
        meta.put("excelRowCount", tables.stream()
                .mapToInt(table -> table.getRawRows() == null ? 0 : table.getRawRows().size())
                .sum());
        meta.put("maxRows", maxRows);
        meta.put("maxColumns", maxColumns);
        meta.put("extractAllSheets", true);
        meta.put("sheets", sheetMetaList);

        FileMaterialContent content = new FileMaterialContent();
        content.setSourceType("excel");
        content.setFileName(defaultText(fileName, ""));
        content.setMimeType(defaultText(mimeType, ""));
        content.setRawText(buildExcelWorkbookRawText(fileName, tables));
        content.setTables(tables);
        content.setMeta(meta);
        return content;
    }

    private FileMaterialContent extractExcelSheet(Sheet sheet, String fileName, String mimeType, int sheetIndex, int maxRows, int maxColumns,
                                                  DataFormatter formatter, FormulaEvaluator evaluator) {
        FileMaterialTable table = extractExcelSheetTable(sheet, sheetIndex, 0, "table_1", maxRows, maxColumns, formatter, evaluator);
        if (table == null) {
            return null;
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fileName", defaultText(fileName, ""));
        meta.put("sheetName", defaultText(sheet == null ? "" : sheet.getSheetName(), ""));
        meta.put("sheetIndex", sheetIndex);
        meta.put("sheetCount", 1);
        meta.put("extractedSheetCount", 1);
        meta.put("tableBlockCount", 1);
        meta.put("excelRowCount", table.getRawRows() == null ? 0 : table.getRawRows().size());
        meta.put("maxRows", maxRows);
        meta.put("maxColumns", maxColumns);
        meta.put("extractAllSheets", false);

        FileMaterialContent content = new FileMaterialContent();
        content.setSourceType("excel");
        content.setFileName(defaultText(fileName, ""));
        content.setMimeType(defaultText(mimeType, ""));
        content.setRawText(buildExcelRawText(fileName, table.getTitle(), table.getRawRows()));
        content.setTables(List.of(table));
        content.setMeta(meta);
        return content;
    }

    private List<FileMaterialTable> extractExcelSheetTables(Sheet sheet, int sheetIndex, String tableIdPrefix,
                                                            int maxRows, int maxColumns,
                                                            DataFormatter formatter, FormulaEvaluator evaluator) {
        List<ExcelSheetRow> sheetRows = extractSheetRowsWithBlanks(sheet, formatter, evaluator, maxRows, maxColumns);
        List<List<ExcelSheetRow>> segments = splitSheetRowsIntoTableSegments(sheetRows);
        List<FileMaterialTable> tables = new ArrayList<>();
        for (List<ExcelSheetRow> segment : segments) {
            FileMaterialTable table = buildExcelTableFromSegment(sheet, sheetIndex, tables.size(),
                    defaultText(tableIdPrefix, "table") + "_" + (tables.size() + 1), segment);
            if (table != null) {
                tables.add(table);
            }
        }
        return tables;
    }

    private FileMaterialTable extractExcelSheetTable(Sheet sheet, int sheetIndex, int tableIndexInSheet, String tableId,
                                                     int maxRows, int maxColumns,
                                                     DataFormatter formatter, FormulaEvaluator evaluator) {
        List<List<String>> rows = extractSheetRows(sheet, formatter, evaluator, maxRows, maxColumns);
        if (rows.isEmpty()) {
            return null;
        }
        return buildExcelTable(sheet, sheetIndex, tableIndexInSheet, tableId, rows, 0, sheet == null ? "" : sheet.getSheetName());
    }

    private FileMaterialTable buildExcelTableFromSegment(Sheet sheet, int sheetIndex, int tableIndexInSheet,
                                                         String tableId, List<ExcelSheetRow> segment) {
        if (segment == null || segment.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        for (ExcelSheetRow row : segment) {
            if (row != null && row.hasValue()) {
                rows.add(row.getValues());
            }
        }
        if (rows.isEmpty()) {
            return null;
        }
        int headerSearchOffset = 0;
        String sheetName = sheet == null ? "" : sheet.getSheetName();
        String tableTitle = sheetName;
        if (rows.size() > 1 && countNonBlank(rows.get(0)) == 1 && countNonBlank(rows.get(1)) >= 2) {
            tableTitle = defaultText(firstNonBlank(rows.get(0)), sheetName);
            headerSearchOffset = 1;
        }
        return buildExcelTable(sheet, sheetIndex, tableIndexInSheet, tableId, rows, headerSearchOffset, tableTitle);
    }

    private FileMaterialTable buildExcelTable(Sheet sheet, int sheetIndex, int tableIndexInSheet, String tableId,
                                              List<List<String>> rows, int headerSearchOffset, String title) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<List<String>> headerRows = rows.subList(Math.min(headerSearchOffset, rows.size()), rows.size());
        if (headerRows.isEmpty()) {
            return null;
        }
        int localHeaderRowIndex = findHeaderRowIndex(headerRows);
        int headerRowIndex = Math.min(headerSearchOffset + localHeaderRowIndex, rows.size() - 1);
        List<String> headers = rows.get(headerRowIndex);
        if (countNonBlank(headers) == 0) {
            return null;
        }
        List<List<String>> dataRows = new ArrayList<>();
        for (int i = headerRowIndex + 1; i < rows.size(); i++) {
            dataRows.add(rows.get(i));
        }

        String sheetName = sheet == null ? "" : sheet.getSheetName();
        String tableTitle = defaultText(title, sheetName);
        if (StringUtil.isNotBlank(sheetName)
                && StringUtil.isNotBlank(tableTitle)
                && !sheetName.equals(tableTitle)) {
            tableTitle = sheetName + " - " + tableTitle;
        }

        FileMaterialTable table = new FileMaterialTable();
        table.setId(defaultText(tableId, "table_1"));
        table.setTitle(tableTitle);
        table.setSheetName(sheetName);
        table.setSheetIndex(sheetIndex);
        table.setTableIndexInSheet(tableIndexInSheet);
        table.setHeaders(headers);
        table.setRows(dataRows);
        table.setRawRows(rows);
        table.setHeaderRowIndex(headerRowIndex);
        table.setConfidence(rows.size() > 1 ? 0.62 : 0.45);
        return table;
    }

    private FileMaterialContent extractWordBytes(byte[] bytes, String fileName, String mimeType, Integer maxCharsOption,
                                                 boolean skipTables, boolean skipHeaders) throws Exception {
        int maxChars = normalizeLimit(maxCharsOption, DEFAULT_WORD_MAX_CHARS, DEFAULT_WORD_MAX_CHARS);
        List<FileMaterialTable> tables = new ArrayList<>();
        Map<String, Object> meta = new LinkedHashMap<>();
        StringBuilder rawText = new StringBuilder();
        appendLine(rawText, "Word file: " + defaultText(fileName, ""), maxChars);

        int[] paragraphCount = new int[]{0};
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            if (!skipHeaders) {
                extractHeaderFooterText(document.getHeaderList(), document.getFooterList(), tables, rawText,
                        paragraphCount, skipTables, maxChars);
            }
            extractWordBodyElements(document.getBodyElements(), tables, rawText, paragraphCount, skipTables, maxChars);

            meta.put("fileName", defaultText(fileName, ""));
            meta.put("sourceType", "word");
            meta.put("mimeType", defaultText(mimeType, ""));
            meta.put("size", bytes.length);
            meta.put("wordParagraphCount", paragraphCount[0]);
            meta.put("wordTableCount", tables.size());
            meta.put("maxChars", maxChars);
            meta.put("usedOcr", false);
            meta.put("ocrProvider", "none");
        }

        String text = rawText.toString().trim();
        if (StringUtil.isBlank(text) && tables.isEmpty()) {
            throw new IllegalArgumentException("word document has no readable text");
        }

        FileMaterialContent content = new FileMaterialContent();
        content.setSourceType("word");
        content.setFileName(defaultText(fileName, ""));
        content.setMimeType(defaultText(mimeType, ""));
        content.setSize(bytes.length);
        content.setRawText(text);
        content.setTables(tables);
        content.setMeta(meta);
        return content;
    }

    private void extractHeaderFooterText(List<XWPFHeader> headers, List<XWPFFooter> footers,
                                         List<FileMaterialTable> tables, StringBuilder rawText,
                                         int[] paragraphCount, boolean skipTables, int maxChars) {
        for (XWPFHeader header : headers) {
            extractWordParagraphs(header.getParagraphs(), rawText, paragraphCount, maxChars);
            if (!skipTables) {
                extractWordTables(header.getTables(), tables, rawText, maxChars);
            }
        }
        for (XWPFFooter footer : footers) {
            extractWordParagraphs(footer.getParagraphs(), rawText, paragraphCount, maxChars);
            if (!skipTables) {
                extractWordTables(footer.getTables(), tables, rawText, maxChars);
            }
        }
    }

    private void extractWordBodyElements(List<IBodyElement> elements, List<FileMaterialTable> tables,
                                         StringBuilder rawText, int[] paragraphCount,
                                         boolean skipTables, int maxChars) {
        for (IBodyElement element : elements) {
            if (element == null) {
                continue;
            }
            if (element.getElementType() == BodyElementType.PARAGRAPH) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                appendParagraph(paragraph, rawText, paragraphCount, maxChars);
            } else if (element.getElementType() == BodyElementType.TABLE && !skipTables) {
                XWPFTable table = (XWPFTable) element;
                extractWordTable(table, tables, rawText, maxChars);
            }
        }
    }

    private void extractWordParagraphs(List<XWPFParagraph> paragraphs, StringBuilder rawText,
                                       int[] paragraphCount, int maxChars) {
        for (XWPFParagraph paragraph : paragraphs) {
            appendParagraph(paragraph, rawText, paragraphCount, maxChars);
        }
    }

    private void appendParagraph(XWPFParagraph paragraph, StringBuilder rawText, int[] paragraphCount, int maxChars) {
        String text = normalizeWordText(paragraph == null ? "" : paragraph.getText());
        if (StringUtil.isBlank(text)) {
            return;
        }
        paragraphCount[0]++;
        appendLine(rawText, text, maxChars);
    }

    private void extractWordTables(List<XWPFTable> wordTables, List<FileMaterialTable> tables,
                                   StringBuilder rawText, int maxChars) {
        for (XWPFTable table : wordTables) {
            extractWordTable(table, tables, rawText, maxChars);
        }
    }

    private void extractWordTable(XWPFTable wordTable, List<FileMaterialTable> tables,
                                  StringBuilder rawText, int maxChars) {
        List<List<String>> rawRows = extractWordTableRows(wordTable);
        if (rawRows.isEmpty()) {
            return;
        }
        FileMaterialTable table = new FileMaterialTable();
        table.setId("table_" + (tables.size() + 1));
        table.setTitle("Word表格" + (tables.size() + 1));
        table.setHeaders(rawRows.get(0));
        table.setRows(rawRows.size() > 1 ? new ArrayList<>(rawRows.subList(1, rawRows.size())) : new ArrayList<>());
        table.setRawRows(rawRows);
        table.setHeaderRowIndex(0);
        table.setConfidence(0.55);
        tables.add(table);

        appendLine(rawText, "Table " + tables.size() + ", tab separated:", maxChars);
        for (List<String> row : rawRows) {
            appendLine(rawText, joinTab(row), maxChars);
        }
    }

    private List<List<String>> extractWordTableRows(XWPFTable wordTable) {
        List<List<String>> rows = new ArrayList<>();
        if (wordTable == null) {
            return rows;
        }
        for (XWPFTableRow tableRow : wordTable.getRows()) {
            List<String> values = new ArrayList<>();
            boolean hasValue = false;
            for (XWPFTableCell cell : tableRow.getTableCells()) {
                String text = normalizeWordText(cell == null ? "" : cell.getText());
                if (StringUtil.isNotBlank(text)) {
                    hasValue = true;
                }
                values.add(text);
            }
            trimTrailingEmpty(values);
            if (hasValue) {
                rows.add(values);
            }
        }
        return rows;
    }

    private String joinTab(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append('\t');
            }
            builder.append(defaultText(values.get(i), ""));
        }
        return builder.toString();
    }

    private void appendLine(StringBuilder builder, String text, int maxChars) {
        if (builder.length() >= maxChars || StringUtil.isBlank(text)) {
            return;
        }
        int remaining = maxChars - builder.length();
        String line = text.length() + 1 <= remaining ? text : text.substring(0, Math.max(0, remaining - 1));
        builder.append(line).append('\n');
    }

    private String normalizeWordText(String value) {
        return defaultText(value, "").replaceAll("[\\t\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private FileMaterialContent extractPdfBytes(byte[] bytes, String fileName, String mimeType,
                                                Integer maxPagesOption, List<Integer> requestedPageIndexes,
                                                Integer maxCharsOption, String requestedOcrProvider,
                                                String ocrLanguage, String loginName, String requestId) throws Exception {
        int maxPages = normalizeLimit(maxPagesOption, DEFAULT_PDF_MAX_PAGES, DEFAULT_PDF_MAX_PAGES);
        int maxChars = normalizeLimit(maxCharsOption, DEFAULT_PDF_MAX_CHARS, DEFAULT_PDF_MAX_CHARS);
        PdfReader reader = null;
        try {
            reader = new PdfReader(bytes);
            int pageCount = reader.getNumberOfPages();
            List<Integer> pageNumbers = resolvePdfPageNumbers(pageCount, maxPages, requestedPageIndexes);
            List<Integer> extractedPageIndexes = new ArrayList<>();
            StringBuilder rawText = new StringBuilder();
            appendLine(rawText, "PDF file: " + defaultText(fileName, ""), maxChars);
            for (Integer pageNumber : pageNumbers) {
                if (rawText.length() >= maxChars) {
                    break;
                }
                String pageText = normalizePdfText(PdfTextExtractor.getTextFromPage(reader, pageNumber));
                if (StringUtil.isBlank(pageText)) {
                    continue;
                }
                extractedPageIndexes.add(pageNumber - 1);
                appendLine(rawText, "Page " + pageNumber + ":", maxChars);
                appendLine(rawText, pageText, maxChars);
            }

            String text = rawText.toString().trim();
            String textWithoutHeader = text.replace("PDF file: " + defaultText(fileName, ""), "").trim();
            if (extractedPageIndexes.isEmpty() || StringUtil.isBlank(textWithoutHeader)) {
                return extractScannedPdfBytes(bytes, fileName, mimeType, maxPages, pageNumbers, maxChars,
                        requestedOcrProvider, ocrLanguage, loginName, requestId, pageCount);
            }

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("fileName", defaultText(fileName, ""));
            meta.put("sourceType", "pdf");
            meta.put("mimeType", defaultText(mimeType, ""));
            meta.put("size", bytes.length);
            meta.put("pageCount", pageCount);
            meta.put("extractedPageCount", extractedPageIndexes.size());
            meta.put("pageIndexes", extractedPageIndexes);
            meta.put("pdfTextLength", text.length());
            meta.put("maxPages", maxPages);
            meta.put("maxChars", maxChars);
            meta.put("usedOcr", false);
            meta.put("ocrProvider", "none");

            FileMaterialContent content = new FileMaterialContent();
            content.setSourceType("pdf");
            content.setFileName(defaultText(fileName, ""));
            content.setMimeType(defaultText(mimeType, ""));
            content.setSize(bytes.length);
            content.setRawText(text);
            content.setTables(new ArrayList<>());
            content.setMeta(meta);
            return content;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private FileMaterialContent extractScannedPdfBytes(byte[] bytes, String fileName, String mimeType,
                                                       int maxPages, List<Integer> pageNumbers, int maxChars,
                                                       String requestedOcrProvider, String ocrLanguage,
                                                       String loginName, String requestId, int pageCount) throws Exception {
        if (ocrProviderRegistry == null) {
            throw OcrProviderException.notConfigured("OCR provider is not configured; scanned PDF recognition needs OCR");
        }
        String language = defaultText(ocrLanguage, DEFAULT_OCR_LANGUAGE);
        List<Integer> ocrPageIndexes = new ArrayList<>();
        List<Map<String, Object>> ocrPageMetaList = new ArrayList<>();
        StringBuilder rawText = new StringBuilder();
        appendLine(rawText, "PDF file: " + defaultText(fileName, ""), maxChars);
        appendLine(rawText, "Scanned PDF OCR text:", maxChars);

        String ocrProvider = defaultText(requestedOcrProvider, OcrProviderRegistry.PROVIDER_AUTO);
        Double confidenceTotal = 0.0;
        int confidenceCount = 0;
        int blockCount = 0;
        int issueCount = 0;
        String lastProviderCode = "";

        try (PDDocument document = PDDocument.load(bytes)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageLimit = Math.min(maxPages, pageNumbers == null ? 0 : pageNumbers.size());
            for (int i = 0; i < pageLimit; i++) {
                if (rawText.length() >= maxChars) {
                    break;
                }
                Integer pageNumber = pageNumbers.get(i);
                if (pageNumber == null || pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                    continue;
                }
                byte[] imageBytes = renderPdfPageAsPng(renderer, pageNumber - 1);
                OcrRequest ocrRequest = new OcrRequest();
                ocrRequest.setRequestId(defaultText(requestId, "") + "-pdf-page-" + pageNumber);
                ocrRequest.setLoginName(defaultText(loginName, ""));
                ocrRequest.setProviderCode(ocrProvider);
                ocrRequest.setSourceType("image");
                ocrRequest.setFileName(defaultText(fileName, "") + "#page-" + pageNumber + ".png");
                ocrRequest.setMimeType("image/png");
                ocrRequest.setImageBytes(imageBytes);
                ocrRequest.setLanguage(language);
                ocrRequest.setPageIndex(pageNumber - 1);
                ocrRequest.setMaxChars(maxChars);
                Map<String, Object> ocrOptions = new LinkedHashMap<>();
                ocrOptions.put("sourceType", "pdf");
                ocrOptions.put("pageNumber", pageNumber);
                ocrOptions.put("dpi", DEFAULT_PDF_OCR_DPI);
                ocrOptions.put("maxChars", maxChars);
                ocrRequest.setOptions(ocrOptions);

                OcrResult ocrResult = ocrProviderRegistry.recognize(ocrRequest);
                String pageText = normalizeOcrText(ocrResult == null ? "" : ocrResult.getText());
                if (StringUtil.isBlank(pageText)) {
                    continue;
                }
                ocrPageIndexes.add(pageNumber - 1);
                appendLine(rawText, "Page " + pageNumber + " OCR text:", maxChars);
                appendLine(rawText, pageText, maxChars);
                lastProviderCode = ocrResult == null ? lastProviderCode : defaultText(ocrResult.getProviderCode(), lastProviderCode);
                if (ocrResult != null && ocrResult.getConfidence() != null) {
                    confidenceTotal += ocrResult.getConfidence();
                    confidenceCount++;
                }
                blockCount += ocrResult == null || ocrResult.getBlocks() == null ? 0 : ocrResult.getBlocks().size();
                issueCount += ocrResult == null || ocrResult.getIssues() == null ? 0 : ocrResult.getIssues().size();
                Map<String, Object> pageMeta = new LinkedHashMap<>();
                pageMeta.put("pageIndex", pageNumber - 1);
                pageMeta.put("provider", ocrResult == null ? "" : defaultText(ocrResult.getProviderCode(), ""));
                pageMeta.put("confidence", ocrResult == null ? null : ocrResult.getConfidence());
                pageMeta.put("blockCount", ocrResult == null || ocrResult.getBlocks() == null ? 0 : ocrResult.getBlocks().size());
                pageMeta.put("issueCount", ocrResult == null || ocrResult.getIssues() == null ? 0 : ocrResult.getIssues().size());
                pageMeta.put("meta", ocrResult == null ? null : ocrResult.getMeta());
                ocrPageMetaList.add(pageMeta);
            }
        }

        String text = rawText.toString().trim();
        String textWithoutHeader = text
                .replace("PDF file: " + defaultText(fileName, ""), "")
                .replace("Scanned PDF OCR text:", "")
                .trim();
        if (ocrPageIndexes.isEmpty() || StringUtil.isBlank(textWithoutHeader)) {
            throw OcrProviderException.failed("Scanned PDF OCR result has no readable text");
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fileName", defaultText(fileName, ""));
        meta.put("sourceType", "pdf");
        meta.put("mimeType", defaultText(mimeType, ""));
        meta.put("size", bytes.length);
        meta.put("pageCount", pageCount);
        meta.put("extractedPageCount", ocrPageIndexes.size());
        meta.put("pageIndexes", ocrPageIndexes);
        meta.put("pdfTextLength", 0);
        meta.put("maxPages", maxPages);
        meta.put("maxChars", maxChars);
        meta.put("usedOcr", true);
        meta.put("ocrProvider", defaultText(lastProviderCode, ocrProvider));
        meta.put("ocrLanguage", language);
        meta.put("ocrDpi", DEFAULT_PDF_OCR_DPI);
        meta.put("ocrPageCount", ocrPageIndexes.size());
        meta.put("ocrConfidence", confidenceCount == 0 ? null : confidenceTotal / confidenceCount);
        meta.put("ocrBlockCount", blockCount);
        meta.put("ocrIssueCount", issueCount);
        meta.put("ocrPages", ocrPageMetaList);

        FileMaterialContent content = new FileMaterialContent();
        content.setSourceType("pdf");
        content.setFileName(defaultText(fileName, ""));
        content.setMimeType(defaultText(mimeType, ""));
        content.setSize(bytes.length);
        content.setRawText(text);
        content.setTables(new ArrayList<>());
        content.setMeta(meta);
        return content;
    }

    private byte[] renderPdfPageAsPng(PDFRenderer renderer, int pageIndex) throws Exception {
        BufferedImage image = renderer.renderImageWithDPI(pageIndex, DEFAULT_PDF_OCR_DPI, ImageType.RGB);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        }
    }

    private List<Integer> resolvePdfPageNumbers(int pageCount, int maxPages, List<Integer> requestedPageIndexes) {
        List<Integer> pageNumbers = new ArrayList<>();
        if (pageCount <= 0) {
            return pageNumbers;
        }
        if (requestedPageIndexes != null && !requestedPageIndexes.isEmpty()) {
            for (Integer pageIndex : requestedPageIndexes) {
                if (pageIndex == null) {
                    continue;
                }
                int pageNumber = pageIndex + 1;
                if (pageNumber >= 1 && pageNumber <= pageCount && !pageNumbers.contains(pageNumber)) {
                    pageNumbers.add(pageNumber);
                }
                if (pageNumbers.size() >= maxPages) {
                    return pageNumbers;
                }
            }
            if (!pageNumbers.isEmpty()) {
                return pageNumbers;
            }
        }
        int limit = Math.min(pageCount, maxPages);
        for (int pageNumber = 1; pageNumber <= limit; pageNumber++) {
            pageNumbers.add(pageNumber);
        }
        return pageNumbers;
    }

    private String normalizePdfText(String value) {
        return defaultText(value, "")
                .replace('\u00a0', ' ')
                .replaceAll("[\\t\\r]+", " ")
                .replaceAll(" *\\n+ *", "\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private FileMaterialContent extractImageBytes(byte[] bytes, String fileName, String mimeType, Integer maxCharsOption,
                                                  String requestedOcrProvider, String ocrLanguage,
                                                  String loginName, String requestId) {
        if (ocrProviderRegistry == null) {
            throw OcrProviderException.notConfigured("OCR provider is not configured; image recognition needs OCR");
        }
        int maxChars = normalizeLimit(maxCharsOption, DEFAULT_OCR_MAX_CHARS, DEFAULT_OCR_MAX_CHARS);
        ImageInfo imageInfo = inspectImage(bytes);
        String language = defaultText(ocrLanguage, DEFAULT_OCR_LANGUAGE);

        OcrRequest ocrRequest = new OcrRequest();
        ocrRequest.setRequestId(defaultText(requestId, ""));
        ocrRequest.setLoginName(defaultText(loginName, ""));
        ocrRequest.setProviderCode(defaultText(requestedOcrProvider, OcrProviderRegistry.PROVIDER_AUTO));
        ocrRequest.setSourceType("image");
        ocrRequest.setFileName(defaultText(fileName, ""));
        ocrRequest.setMimeType(defaultText(mimeType, ""));
        ocrRequest.setImageBytes(bytes);
        ocrRequest.setLanguage(language);
        ocrRequest.setPageIndex(0);
        ocrRequest.setMaxChars(maxChars);
        Map<String, Object> ocrOptions = new LinkedHashMap<>();
        ocrOptions.put("maxChars", maxChars);
        ocrOptions.put("imageWidth", imageInfo.getWidth());
        ocrOptions.put("imageHeight", imageInfo.getHeight());
        ocrOptions.put("size", bytes.length);
        ocrRequest.setOptions(ocrOptions);

        OcrResult ocrResult = ocrProviderRegistry.recognize(ocrRequest);
        String text = normalizeOcrText(ocrResult == null ? "" : ocrResult.getText());
        if (StringUtil.isBlank(text)) {
            throw OcrProviderException.failed("OCR result has no readable text");
        }

        StringBuilder rawText = new StringBuilder();
        appendLine(rawText, "Image file: " + defaultText(fileName, ""), maxChars);
        appendLine(rawText, "OCR text:", maxChars);
        appendLine(rawText, text, maxChars);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fileName", defaultText(fileName, ""));
        meta.put("sourceType", "image");
        meta.put("mimeType", defaultText(mimeType, ""));
        meta.put("size", bytes.length);
        meta.put("imageWidth", imageInfo.getWidth());
        meta.put("imageHeight", imageInfo.getHeight());
        meta.put("maxChars", maxChars);
        meta.put("usedOcr", true);
        meta.put("ocrProvider", defaultText(ocrResult.getProviderCode(), requestedOcrProvider));
        meta.put("ocrLanguage", language);
        meta.put("ocrConfidence", ocrResult.getConfidence());
        meta.put("ocrBlockCount", ocrResult.getBlocks() == null ? 0 : ocrResult.getBlocks().size());
        meta.put("ocrIssueCount", ocrResult.getIssues() == null ? 0 : ocrResult.getIssues().size());
        meta.put("ocrMeta", ocrResult.getMeta());

        FileMaterialContent content = new FileMaterialContent();
        content.setSourceType("image");
        content.setFileName(defaultText(fileName, ""));
        content.setMimeType(defaultText(mimeType, ""));
        content.setSize(bytes.length);
        content.setRawText(rawText.toString().trim());
        content.setTables(new ArrayList<>());
        content.setMeta(meta);
        return content;
    }

    private ImageInfo inspectImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image != null) {
                return new ImageInfo(image.getWidth(), image.getHeight());
            }
        } catch (Exception ignored) {
        }
        return new ImageInfo(null, null);
    }

    private String normalizeOcrText(String value) {
        return defaultText(value, "")
                .replace('\u00a0', ' ')
                .replaceAll("[\\t\\r]+", " ")
                .replaceAll(" *\\n+ *", "\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private byte[] decodeBase64(String fileBase64) {
        String normalized = defaultText(fileBase64, "").trim();
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(commaIndex + 1);
        }
        normalized = normalized.replace(" ", "+").replaceAll("\\s+", "");
        return Base64.getDecoder().decode(normalized);
    }

    private int normalizeLimit(Integer value, int defaultValue, int maxValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return Math.min(value, maxValue);
    }

    private List<List<String>> extractSheetRows(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator,
                                                int maxRows, int maxColumns) {
        List<List<String>> rows = new ArrayList<>();
        if (sheet == null) {
            return rows;
        }
        int firstRow = Math.max(sheet.getFirstRowNum(), 0);
        int lastRow = sheet.getLastRowNum();
        for (int rowIndex = firstRow; rowIndex <= lastRow && rows.size() < maxRows; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            int lastCell = row == null ? -1 : row.getLastCellNum();
            int width = Math.min(Math.max(lastCell, findMergedLastColumn(sheet, rowIndex)), maxColumns);
            if (width <= 0) {
                continue;
            }
            List<String> values = new ArrayList<>();
            boolean hasValue = false;
            for (int columnIndex = 0; columnIndex < width; columnIndex++) {
                String text = getCellText(sheet, rowIndex, columnIndex, formatter, evaluator);
                if (StringUtil.isNotBlank(text)) {
                    hasValue = true;
                }
                values.add(text);
            }
            trimTrailingEmpty(values);
            if (hasValue) {
                rows.add(values);
            }
        }
        return rows;
    }

    private List<ExcelSheetRow> extractSheetRowsWithBlanks(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator,
                                                           int maxRows, int maxColumns) {
        List<ExcelSheetRow> rows = new ArrayList<>();
        if (sheet == null) {
            return rows;
        }
        int firstRow = Math.max(sheet.getFirstRowNum(), 0);
        int lastRow = sheet.getLastRowNum();
        int nonBlankCount = 0;
        boolean seenContent = false;
        for (int rowIndex = firstRow; rowIndex <= lastRow && nonBlankCount < maxRows; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            int lastCell = row == null ? -1 : row.getLastCellNum();
            int width = Math.min(Math.max(lastCell, findMergedLastColumn(sheet, rowIndex)), maxColumns);
            List<String> values = new ArrayList<>();
            boolean hasValue = false;
            if (width > 0) {
                for (int columnIndex = 0; columnIndex < width; columnIndex++) {
                    String text = getCellText(sheet, rowIndex, columnIndex, formatter, evaluator);
                    if (StringUtil.isNotBlank(text)) {
                        hasValue = true;
                    }
                    values.add(text);
                }
                trimTrailingEmpty(values);
            }
            if (hasValue) {
                seenContent = true;
                nonBlankCount++;
                rows.add(new ExcelSheetRow(rowIndex, values, true));
            } else if (seenContent) {
                rows.add(new ExcelSheetRow(rowIndex, values, false));
            }
        }
        trimTrailingBlankRows(rows);
        return rows;
    }

    private void trimTrailingBlankRows(List<ExcelSheetRow> rows) {
        if (rows == null) {
            return;
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            ExcelSheetRow row = rows.get(i);
            if (row != null && row.hasValue()) {
                return;
            }
            rows.remove(i);
        }
    }

    private List<List<ExcelSheetRow>> splitSheetRowsIntoTableSegments(List<ExcelSheetRow> sheetRows) {
        List<List<ExcelSheetRow>> segments = new ArrayList<>();
        List<ExcelSheetRow> current = new ArrayList<>();
        for (int i = 0; i < sheetRows.size(); i++) {
            ExcelSheetRow row = sheetRows.get(i);
            if (row == null) {
                continue;
            }
            if (!row.hasValue()) {
                flushExcelSegment(segments, current);
                current = new ArrayList<>();
                continue;
            }

            ExcelSheetRow nextNonBlank = findNextNonBlankRow(sheetRows, i + 1);
            if (isLikelyTableTitleRow(row, nextNonBlank)) {
                flushExcelSegment(segments, current);
                current = new ArrayList<>();
            }
            current.add(row);
        }
        flushExcelSegment(segments, current);
        return segments;
    }

    private void flushExcelSegment(List<List<ExcelSheetRow>> segments, List<ExcelSheetRow> current) {
        if (current == null || current.isEmpty()) {
            return;
        }
        boolean hasValue = current.stream().anyMatch(ExcelSheetRow::hasValue);
        if (hasValue) {
            segments.add(new ArrayList<>(current));
        }
    }

    private ExcelSheetRow findNextNonBlankRow(List<ExcelSheetRow> rows, int startIndex) {
        if (rows == null) {
            return null;
        }
        for (int i = Math.max(startIndex, 0); i < rows.size(); i++) {
            ExcelSheetRow row = rows.get(i);
            if (row != null && row.hasValue()) {
                return row;
            }
            if (row != null && !row.hasValue()) {
                return null;
            }
        }
        return null;
    }

    private boolean isLikelyTableTitleRow(ExcelSheetRow row, ExcelSheetRow nextNonBlank) {
        if (row == null || nextNonBlank == null) {
            return false;
        }
        return countNonBlank(row.getValues()) == 1
                && countDistinctNonBlank(nextNonBlank.getValues()) >= 2;
    }

    private int findMergedLastColumn(Sheet sheet, int rowIndex) {
        int lastColumn = -1;
        int mergedCount = sheet.getNumMergedRegions();
        for (int i = 0; i < mergedCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.getFirstRow() <= rowIndex && rowIndex <= range.getLastRow()) {
                lastColumn = Math.max(lastColumn, range.getLastColumn() + 1);
            }
        }
        return lastColumn;
    }

    private String getCellText(Sheet sheet, int rowIndex, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        Row row = sheet.getRow(rowIndex);
        Cell cell = row == null ? null : row.getCell(columnIndex);
        String text = formatCell(cell, formatter, evaluator);
        if (StringUtil.isNotBlank(text)) {
            return text;
        }
        CellRangeAddress range = findMergedRegion(sheet, rowIndex, columnIndex);
        if (range == null || (range.getFirstRow() == rowIndex && range.getFirstColumn() == columnIndex)) {
            return "";
        }
        Row firstRow = sheet.getRow(range.getFirstRow());
        Cell firstCell = firstRow == null ? null : firstRow.getCell(range.getFirstColumn());
        return formatCell(firstCell, formatter, evaluator);
    }

    private CellRangeAddress findMergedRegion(Sheet sheet, int rowIndex, int columnIndex) {
        int mergedCount = sheet.getNumMergedRegions();
        for (int i = 0; i < mergedCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(rowIndex, columnIndex)) {
                return range;
            }
        }
        return null;
    }

    private String formatCell(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        try {
            return normalizeCellText(formatter.formatCellValue(cell, evaluator));
        } catch (Exception ex) {
            return normalizeCellText(formatter.formatCellValue(cell));
        }
    }

    private String normalizeCellText(String value) {
        return defaultText(value, "").replaceAll("[\\t\\r\\n]+", " ").trim();
    }

    private void trimTrailingEmpty(List<String> values) {
        for (int i = values.size() - 1; i >= 0; i--) {
            if (StringUtil.isNotBlank(values.get(i))) {
                return;
            }
            values.remove(i);
        }
    }

    private int findHeaderRowIndex(List<List<String>> rows) {
        int scanLimit = Math.min(rows.size(), 6);
        int bestIndex = 0;
        int bestScore = -1;
        for (int i = 0; i < scanLimit; i++) {
            int nonBlankCount = countNonBlank(rows.get(i));
            int distinctCount = countDistinctNonBlank(rows.get(i));
            int score = distinctCount * 2 + Math.min(nonBlankCount, distinctCount + 2);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private int countNonBlank(List<String> row) {
        int count = 0;
        if (row == null) {
            return count;
        }
        for (String value : row) {
            if (StringUtil.isNotBlank(value)) {
                count++;
            }
        }
        return count;
    }

    private int countDistinctNonBlank(List<String> row) {
        List<String> values = new ArrayList<>();
        if (row == null) {
            return 0;
        }
        for (String value : row) {
            String text = defaultText(value, "");
            if (StringUtil.isNotBlank(text) && !values.contains(text)) {
                values.add(text);
            }
        }
        return values.size();
    }

    private String firstNonBlank(List<String> row) {
        if (row == null) {
            return "";
        }
        for (String value : row) {
            if (StringUtil.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String buildExcelRawText(String fileName, String sheetName, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("Excel file: ").append(defaultText(fileName, "")).append("\n");
        builder.append("Sheet: ").append(defaultText(sheetName, "")).append("\n");
        builder.append("Table data, tab separated:\n");
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    builder.append('\t');
                }
                builder.append(defaultText(row.get(i), ""));
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private String buildExcelWorkbookRawText(String fileName, List<FileMaterialTable> tables) {
        StringBuilder builder = new StringBuilder();
        builder.append("Excel file: ").append(defaultText(fileName, "")).append("\n");
        builder.append("Workbook contains ").append(tables == null ? 0 : tables.size())
                .append(" readable table blocks. Each Table block should be considered a candidate page/form.\n");
        if (tables == null) {
            return builder.toString();
        }
        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            FileMaterialTable table = tables.get(tableIndex);
            builder.append("\n");
            builder.append("Table block ").append(tableIndex + 1).append(": ")
                    .append(defaultText(table == null ? "" : table.getTitle(), "Untitled sheet")).append("\n");
            builder.append("Sheet: ")
                    .append(defaultText(table == null ? "" : table.getSheetName(), ""))
                    .append(", tableIndexInSheet: ")
                    .append(table == null || table.getTableIndexInSheet() == null ? "" : table.getTableIndexInSheet())
                    .append("\n");
            builder.append("Headers: ").append(joinTab(table == null ? null : table.getHeaders())).append("\n");
            builder.append("Table data, tab separated:\n");
            List<List<String>> rows = table == null ? null : table.getRawRows();
            if (rows == null) {
                continue;
            }
            for (List<String> row : rows) {
                builder.append(joinTab(row)).append('\n');
            }
        }
        return builder.toString();
    }

    private String inferSourceType(String sourceType, String fileName, String mimeType) {
        String normalized = defaultText(sourceType, "").trim().toLowerCase();
        if (StringUtil.isNotBlank(normalized) && !"auto".equals(normalized)) {
            return normalized;
        }
        String name = defaultText(fileName, "").trim().toLowerCase();
        String mime = defaultText(mimeType, "").trim().toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls") || mime.contains("spreadsheet") || mime.contains("excel")) {
            return "excel";
        }
        if (name.endsWith(".docx") || mime.contains("wordprocessingml.document")) {
            return "word";
        }
        if (name.endsWith(".pdf") || "application/pdf".equals(mime)) {
            return "pdf";
        }
        if (mime.startsWith("image/") || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp")) {
            return "image";
        }
        return normalized;
    }

    private static class ExcelSheetRow {

        private final int rowIndex;
        private final List<String> values;
        private final boolean hasValue;

        private ExcelSheetRow(int rowIndex, List<String> values, boolean hasValue) {
            this.rowIndex = rowIndex;
            this.values = values == null ? new ArrayList<>() : values;
            this.hasValue = hasValue;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public List<String> getValues() {
            return values;
        }

        public boolean hasValue() {
            return hasValue;
        }
    }

    private static class ImageInfo {

        private final Integer width;
        private final Integer height;

        private ImageInfo(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtil.isBlank(value) ? defaultValue : value;
    }

    public static class FileMaterialContent {

        private String sourceType;
        private String fileName;
        private String mimeType;
        private long size;
        private String rawText;
        private List<FileMaterialTable> tables = new ArrayList<>();
        private Map<String, Object> meta = new LinkedHashMap<>();

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getRawText() {
            return rawText;
        }

        public void setRawText(String rawText) {
            this.rawText = rawText;
        }

        public List<FileMaterialTable> getTables() {
            return tables;
        }

        public void setTables(List<FileMaterialTable> tables) {
            this.tables = tables;
        }

        public Map<String, Object> getMeta() {
            return meta;
        }

        public void setMeta(Map<String, Object> meta) {
            this.meta = meta;
        }

        public FileMaterialTable getPrimaryTable() {
            return tables == null || tables.isEmpty() ? null : tables.get(0);
        }
    }

    public static class FileMaterialTable {

        private String id;
        private String title;
        private Integer sheetIndex;
        private String sheetName;
        private Integer tableIndexInSheet;
        private List<String> headers = new ArrayList<>();
        private List<List<String>> rows = new ArrayList<>();
        private List<List<String>> rawRows = new ArrayList<>();
        private Integer headerRowIndex;
        private Double confidence;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getSheetIndex() {
            return sheetIndex;
        }

        public void setSheetIndex(Integer sheetIndex) {
            this.sheetIndex = sheetIndex;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public Integer getTableIndexInSheet() {
            return tableIndexInSheet;
        }

        public void setTableIndexInSheet(Integer tableIndexInSheet) {
            this.tableIndexInSheet = tableIndexInSheet;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        public void setRows(List<List<String>> rows) {
            this.rows = rows;
        }

        public List<List<String>> getRawRows() {
            return rawRows;
        }

        public void setRawRows(List<List<String>> rawRows) {
            this.rawRows = rawRows;
        }

        public Integer getHeaderRowIndex() {
            return headerRowIndex;
        }

        public void setHeaderRowIndex(Integer headerRowIndex) {
            this.headerRowIndex = headerRowIndex;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
    }
}
