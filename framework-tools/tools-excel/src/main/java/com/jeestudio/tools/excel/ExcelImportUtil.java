package com.jeestudio.tools.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.jeestudio.tools.base.annotation.Excel;
import com.jeestudio.tools.base.enums.ImportValidEnum;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import java.io.File;
import java.nio.file.Files;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Description: Excel导入工具
 */
public class ExcelImportUtil extends ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportUtil.class);

    /**
     * 创建标题行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param titles   标题
     * @param <T>
     */
    private static <T> void createTitleRow(Workbook workBook, Sheet sheet, List<ExcelField> titles) {
        // 创建第一行
        Row row = sheet.createRow((short) 0);
        int i = 0;
        //设置列名

        //excel的列号
        int k = 0;

        Sheet hidden = workBook.createSheet("hidden");
        int start = 1;
        for (ExcelField excelField : titles) {
            Cell cell = row.createCell(i);
            cell.setCellValue(excelField.getFieldTitle());
            //绑定导入的下拉
            LinkedHashMap<Object, Object> dataMap = excelField.getDataMap();
            if (excelField.isDictSingleSheet()) {
                if (dataMap != null && dataMap.size() > 0) {
                    Sheet dictSheet = workBook.createSheet(excelField.getFieldTitle());
                    int dictIndex = 0;

                    for (Object value : dataMap.values()) {
                        Row dictSheetRow = dictSheet.createRow(dictIndex);
                        Cell sheetRowCell = dictSheetRow.createCell(0);
                        sheetRowCell.setCellValue(Convert.toStr(value));
                        dictIndex++;
                    }
                    dictSheet.autoSizeColumn(0); //调整第0列宽度

                    ClientAnchor clientAnchor = new XSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6);
                    Drawing drawing = sheet.createDrawingPatriarch();
                    Comment comment = drawing.createCellComment(clientAnchor);
                    String separator = ",";
                    if (excelField.isDictMultiple()) {
                        separator = excelField.getDictSeparator();
                    }

                    comment.setString(new XSSFRichTextString(StrUtil.format("请从{}sheet中选择填写项,多项以{}分隔", excelField.getFieldTitle(), separator)));
                    cell.setCellComment(comment);
                }
            } else {
                if (dataMap != null && dataMap.size() > 0) {
                    int p = 0;
                    for (Object value : dataMap.values()) {
                        Cell cell_h = null;
                        //根据i创建相应的行对象（说明我们将会把每个元素单独放一行）
                        Row row_h = hidden.createRow(start + p - 1);
                        //创建每一行中的第一个单元格
                        cell_h = row_h.createCell(0);
                        //然后将数组中的元素赋值给这个单元格
                        cell_h.setCellValue(Convert.toStr(value));
                        p++;
                    }
                    // 创建名称，可被其他单元格引用
                    Name namedCell = workBook.createName();
                    namedCell.setNameName("hidden" + excelField.getFieldValue());
                    // 设置名称引用的公式
                    namedCell.setRefersToFormula("hidden!$A$" + start + ":$A$" + (start + dataMap.size() - 1));
                    start += dataMap.size();

                    //加载数据,将名称为hidden的sheet中的数据转换为List形式
                    XSSFDataValidationConstraint validationConstraint = new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.LIST, "hidden" + excelField.getFieldValue());
                    CellRangeAddressList regions = new CellRangeAddressList(1, 65535, k, k);
                    // 将设置下拉选的位置和数据的对应关系 绑定到一起
                    XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
                    DataValidation dataValidation = dvHelper.createValidation(validationConstraint, regions);
                    sheet.addValidationData(dataValidation);
                }

            }
            cell.setCellStyle(getTitleStyle(workBook));
            i++;
            k++;
        }
        //将第二个sheet设置为隐藏
        workBook.setSheetHidden(workBook.getSheetIndex("hidden"), true);
        for (short index = 1; index < titles.size() + 1; index++) {
            sheet.autoSizeColumn(index); //调整第i列宽度
        }
    }

    /**
     * 导出导入模板
     *
     * @param tClass 实体
     * @param <T>
     * @return
     */
    public static <T> Workbook exportTemplate(Class<T> tClass) {
        String sheetName = "sheet1";
        Excel excel = tClass.getAnnotation(Excel.class);
        if (excel != null && StringUtils.isNotEmpty(excel.title())) {
            sheetName = excel.title();
        }
        return exportTemplate(sheetName, tClass, new ArrayList<>());
    }

    /**
     * 导出导入模板
     *
     * @param sheetName sheet
     * @param tClass    实体
     * @param <T>
     * @return
     */
    public static <T> Workbook exportTemplate(String sheetName, Class<T> tClass, List<String> ignoreFields) {
        Excel excel = tClass.getAnnotation(Excel.class);
        if (excel != null && StringUtils.isNotEmpty(excel.title())) {
            sheetName = excel.title();
        }
        List<ExcelField> exportTitleList = getImportTitleList(tClass);
        return exportTemplate(sheetName, exportTitleList, ignoreFields);
    }

    public static <T> Workbook exportTemplate(List<ExcelField> excelFieldList) {
        String sheetName = "sheet1";
        return exportTemplate(sheetName, excelFieldList);
    }

    public static <T> Workbook exportTemplate(String sheetName, List<ExcelField> excelFieldList) {
        return exportTemplate(sheetName, excelFieldList, new ArrayList<>());
    }

    public static List<ExcelField> getAllTitleList(List<ExcelField> titleList) {
        List<ExcelField> titleAllList = new ArrayList<>();
        for (ExcelField excelField : titleList) {
            if (excelField.getChildren() != null && excelField.getChildren().size() > 0) {
                titleAllList.addAll(getAllTitleList(excelField.getChildren()));
            } else {
                titleAllList.add(excelField);
            }
        }
        return titleAllList;
    }

    public static <T> Workbook exportTemplate(String sheetName, List<ExcelField> excelFieldList, List<String> ignoreFields) {
        Workbook workBook = createWorkBook(sheetName);
        Sheet sheet = workBook.getSheet(sheetName);
        List<ExcelField> titleList = new ArrayList<>();
        excelFieldList = getAllTitleList(excelFieldList);
        for (ExcelField excelField : excelFieldList) {
            if (!ignoreFields.contains(excelField.getFieldTitle()) && !excelField.isCollection()) {
                titleList.add(excelField);
            }
        }
        createTitleRow(workBook, sheet, titleList);
        return workBook;
    }

    /**
     * 导入所有sheet的数据
     *
     * @param tClass
     * @param inputStream
     * @param fileName
     * @param ignoreFields
     * @param <T>
     * @return
     * @throws ExcelImportException
     */
    public static <T> Map<String, List<T>> importDataAllSheet(Class<T> tClass, InputStream inputStream, String fileName, List<String> ignoreFields) throws ExcelImportException {
        Map<String, List<T>> map = new HashMap<>();
        List<T> list = new ArrayList<>();
        String extName = FileUtil.extName(fileName);
        ExcelImportResult<T> excelImportResult = null;
        HSSFWorkbook workbook1 = null;
        XSSFWorkbook workbook2 = null;
        int numberOfSheets = 0;
        if (EXCEL_2003_EXTEND_NAME.equals(extName)) {
            try {
                workbook1 = new HSSFWorkbook(inputStream);
                numberOfSheets = workbook1.getNumberOfSheets();
            } catch (IOException e) {
                logger.error(ExceptionUtil.stacktraceToString(e));
                throw new ExcelImportException(fileName + "：文件导入错误，请使用系统提供的模板！");
            }
        } else if (EXCEL_2007_EXTEND_NAME.equals(extName)) {
            try {
                workbook2 = new XSSFWorkbook(inputStream);
                numberOfSheets = workbook2.getNumberOfSheets();
            } catch (IOException e) {
                logger.error(ExceptionUtil.stacktraceToString(e));
                throw new ExcelImportException(fileName + "：文件导入错误，请使用系统提供的模板！");
            }
        } else {
            throw new ExcelImportException(fileName + "：文件格式错误，请使用系统提供的模板！");
        }
        for (int i = 1; i < numberOfSheets; i++) {
            String sheetName = "";
            if (EXCEL_2003_EXTEND_NAME.equals(extName)) {

                Sheet sheet = workbook1.getSheetAt(i);
                sheetName = sheet.getSheetName();
                excelImportResult = importData(tClass, 1, sheet);
            } else if (EXCEL_2007_EXTEND_NAME.equals(extName)) {

                Sheet sheet = workbook2.getSheetAt(i);
                sheetName = sheet.getSheetName();
                excelImportResult = importData(tClass, 1, sheet);

            }
            if (!excelImportResult.getResult()) {
                throw handleError(excelImportResult, fileName);
            }

            list = excelImportResult.getData();
            ExcelImportResult<T> tExcelImportResult = validData(tClass, 1, list, ignoreFields);
            if (!tExcelImportResult.getResult()) {
                throw handleError(tExcelImportResult, fileName);
            }
            List<T> data = tExcelImportResult.getData();

            if (data.size() > 0) {
                map.put(sheetName, data);
            }
        }


        return map;
    }

    /**
     * 导入数据
     *
     * @param tClass       接收数据实体类
     * @param inputStream  excel文件inputStream
     * @param fileName     文件名称
     * @param ignoreFields 忽略的字段
     * @param <T>          实体类型
     * @return
     * @throws ExcelImportException
     */
    public static <T> List<T> importData(Class<T> tClass, InputStream inputStream, String fileName, List<String> ignoreFields) throws ExcelImportException {
        String extName = FileUtil.extName(fileName);
        if (EXCEL_2007_EXTEND_NAME.equals(extName)) {
            // .xlsx 使用 SAX 流式解析，避免大文件内存溢出
            return importDataSax(tClass, inputStream, fileName, ignoreFields);
        }
        // .xls 继续使用原有 DOM 方式
        String sheetName = "";
        Excel excel = tClass.getAnnotation(Excel.class);
        if (excel != null && StringUtils.isNotEmpty(excel.title())) {
            sheetName = excel.title();
        }
        if (!EXCEL_2003_EXTEND_NAME.equals(extName)) {
            throw new ExcelImportException(fileName + "：文件格式错误，请使用系统提供的模板！");
        }
        ExcelImportResult<T> excelImportResult;
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            Sheet sheet;
            if (StringUtils.isNotEmpty(sheetName)) {
                sheet = workbook.getSheet(sheetName);
            } else {
                sheet = workbook.getSheetAt(0);
            }
            excelImportResult = importData(tClass, 1, sheet);
        } catch (IOException e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new ExcelImportException(fileName + "：文件导入错误，请使用系统提供的模板！");
        }
        if (!excelImportResult.getResult()) {
            throw handleError(excelImportResult, fileName);
        }
        if (excelImportResult.getData().size() == 0) {
            throw new ExcelImportException(fileName + "：未读取到数据！");
        }
        ExcelImportResult<T> tExcelImportResult = validData(tClass, 1, excelImportResult.getData(), ignoreFields);
        if (!tExcelImportResult.getResult()) {
            throw handleError(tExcelImportResult, fileName);
        }
        return tExcelImportResult.getData();
    }

    public static List<Map<String, Object>> importData(String sheetName, List<ExcelField> importFields, InputStream inputStream, String fileName, List<String> ignoreFields) throws ExcelImportException {
        String extName = FileUtil.extName(fileName);
        if (EXCEL_2007_EXTEND_NAME.equals(extName)) {
            // .xlsx 使用 SAX 流式解析，避免大文件内存溢出
            return importDataSax(sheetName, importFields, inputStream, fileName, ignoreFields);
        }
        // .xls 继续使用原有 DOM 方式
        if (!EXCEL_2003_EXTEND_NAME.equals(extName)) {
            throw new ExcelImportException(fileName + "：文件格式错误，请使用系统提供的模板！");
        }
        ExcelImportResult<Map<String, Object>> excelImportResult;
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            Sheet sheet;
            if (StringUtils.isNotEmpty(sheetName)) {
                sheet = workbook.getSheet(sheetName);
            } else {
                sheet = workbook.getSheetAt(0);
            }
            excelImportResult = importData(importFields, 1, sheet);
        } catch (IOException e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new ExcelImportException(fileName + "：文件导入错误，请使用系统提供的模板！");
        }
        if (!excelImportResult.getResult()) {
            throw handleError(excelImportResult, fileName);
        }
        if (excelImportResult.getData().size() == 0) {
            throw new ExcelImportException(fileName + "：未读取到数据！");
        }
        ExcelImportResult<Map<String, Object>> tExcelImportResult = validData(importFields, 1, excelImportResult.getData(), ignoreFields);
        if (!tExcelImportResult.getResult()) {
            throw handleError(tExcelImportResult, fileName);
        }
        return tExcelImportResult.getData();
    }

    // ======================== SAX 流式导入（大文件推荐，仅支持 .xlsx） ========================

    /**
     * SAX 流式导入（大文件推荐，仅支持 .xlsx）。
     * 使用事件驱动模式逐行读取，不会将整个文件加载到内存，显著降低大文件导入时的内存占用。
     *
     * @param tClass       接收数据实体类
     * @param inputStream  excel 文件输入流
     * @param fileName     文件名称（用于判断格式及错误提示）
     * @param ignoreFields 校验时忽略的字段标题列表
     * @param <T>          实体类型
     * @return 解析并校验后的数据列表
     * @throws ExcelImportException 格式错误或业务校验不通过时抛出
     */
    public static <T> List<T> importDataSax(Class<T> tClass, InputStream inputStream, String fileName, List<String> ignoreFields) throws ExcelImportException {
        String extName = FileUtil.extName(fileName);
        if (!EXCEL_2007_EXTEND_NAME.equals(extName)) {
            throw new ExcelImportException(fileName + "：SAX 流式导入仅支持 .xlsx 格式，.xls 文件请使用 importData 方法！");
        }
        String sheetName = "";
        Excel excel = tClass.getAnnotation(Excel.class);
        if (excel != null && StringUtils.isNotEmpty(excel.title())) {
            sheetName = excel.title();
        }
        List<ExcelField> importTitleList = getImportTitleList(tClass);
        List<T> dataList = new ArrayList<>();
        // titleIndexMap 在第 0 行（表头）回调中初始化
        Map<String, Integer> titleIndexMap = new LinkedHashMap<>();
        // dataMap 用于合并含子表的多行数据，key 为主键聚合字符串
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        final String targetSheetName = sheetName;
        // 先将 InputStream 写入临时文件，避免 OPCPackage.open(InputStream) 将整个流缓冲到内存
        File tempFile = null;
        try {
            tempFile = Files.createTempFile("excel_import_", ".xlsx").toFile();
            java.nio.file.Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try (OPCPackage pkg = OPCPackage.open(tempFile)) {
                XSSFReader xssfReader = new XSSFReader(pkg);
                // 使用 SimpleSharedStringsTable：SAX 一次解析为纯 String 列表，
                // 避免 ReadOnlySharedStringsTable.getItemAt 每次调用都创建 xmlbeans Locale 对象导致 OOM
                SimpleSharedStringsTable sharedStrings = new SimpleSharedStringsTable(pkg);
                StylesTable stylesTable = xssfReader.getStylesTable();
                // 定位目标 sheet
                InputStream sheetStream = getSaxSheetStream(xssfReader, targetSheetName);
                if (sheetStream == null) {
                    throw new ExcelImportException(fileName + "：未找到对应的 Sheet，请使用系统提供的模板！");
                }
                ExcelSaxImportHandler handler = new ExcelSaxImportHandler(sharedStrings, stylesTable, (rowIndex, cellValues) -> {
                    if (rowIndex == 0) {
                        // 解析表头
                        for (int i = 0; i < cellValues.size(); i++) {
                            String title = cellValues.get(i);
                            if (StringUtils.isNotEmpty(title)) {
                                titleIndexMap.put(title, i);
                            }
                        }
                    } else {
                        // 解析数据行，传入 rowIndex 供 dataKey 使用
                        buildObjectFromRawRow(dataMap, tClass, cellValues, importTitleList, titleIndexMap, rowIndex);
                    }
                });
                XMLReader xmlReader = SAXHelper.newXMLReader();
                xmlReader.setContentHandler((ContentHandler) handler);
                xmlReader.parse(new InputSource(sheetStream));
                sheetStream.close();
            }
        } catch (ExcelImportException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            // OOM 是 Error 不是 Exception，必须单独捕获，否则会被吞掉导致前端收到空 msg
            logger.error("导入 Excel 失败：内存不足，请联系管理员调大 JVM 堆内存（-Xmx）", e);
            throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
        } catch (Exception e) {
            // 提取真实原因：SAX 解析引擎会将回调内部抛出的异常包裹为 SAXException
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            // OOM 也可能被 SAXException 包裹，需单独判断
            if (cause instanceof OutOfMemoryError) {
                logger.error("导入 Excel 失败：内存不足，请联系管理员调大 JVM 堆内存（-Xmx）", cause);
                throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
            }
            logger.error("导入 Excel 失败", cause);
            if (cause instanceof ExcelImportException) {
                throw (ExcelImportException) cause;
            }
            if (cause instanceof BusinessException) {
                throw new ExcelImportException(cause.getMessage());
            }
            throw new ExcelImportException(fileName + "：文件导入错误，" + cause.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        if (titleIndexMap.isEmpty()) {
            ExcelImportResult<T> emptyResult = new ExcelImportResult<>(false);
            emptyResult.addErrorString("请使用系统导出的excel");
            throw handleError(emptyResult, fileName);
        }
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            dataList.add((T) entry.getValue());
        }
        if (dataList.isEmpty()) {
            throw new ExcelImportException(fileName + "：未读取到数据！");
        }
        ExcelImportResult<T> validResult = validData(tClass, 1, dataList, ignoreFields);
        if (!validResult.getResult()) {
            throw handleError(validResult, fileName);
        }
        return validResult.getData();
    }

    /**
     * SAX 流式导入——动态字段版本（大文件推荐，仅支持 .xlsx）。
     *
     * @param sheetName    目标 Sheet 名称，传空则取第一个 Sheet
     * @param importFields 字段映射列表
     * @param inputStream  excel 文件输入流
     * @param fileName     文件名称
     * @param ignoreFields 校验时忽略的字段标题列表
     * @return 解析并校验后的 Map 数据列表
     * @throws ExcelImportException 格式错误或业务校验不通过时抛出
     */
    public static List<Map<String, Object>> importDataSax(String sheetName, List<ExcelField> importFields, InputStream inputStream, String fileName, List<String> ignoreFields) throws ExcelImportException {
        String extName = FileUtil.extName(fileName);
        if (!EXCEL_2007_EXTEND_NAME.equals(extName)) {
            throw new ExcelImportException(fileName + "：SAX 流式导入仅支持 .xlsx 格式，.xls 文件请使用 importData 方法！");
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Integer> titleIndexMap = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Object>> dataMap = new LinkedHashMap<>();
        // 先将 InputStream 写入临时文件，避免 OPCPackage.open(InputStream) 将整个流缓冲到内存
        File tempFile = null;
        try {
            tempFile = Files.createTempFile("excel_import_", ".xlsx").toFile();
            java.nio.file.Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try (OPCPackage pkg = OPCPackage.open(tempFile)) {
                XSSFReader xssfReader = new XSSFReader(pkg);
                // 使用 SimpleSharedStringsTable：SAX 一次解析为纯 String 列表，
                // 避免 ReadOnlySharedStringsTable.getItemAt 每次调用都创建 xmlbeans Locale 对象导致 OOM
                SimpleSharedStringsTable sharedStrings = new SimpleSharedStringsTable(pkg);
                StylesTable stylesTable = xssfReader.getStylesTable();
                InputStream sheetStream = getSaxSheetStream(xssfReader, sheetName);
                if (sheetStream == null) {
                    throw new ExcelImportException(fileName + "：未找到对应的 Sheet，请使用系统提供的模板！");
                }
                ExcelSaxImportHandler handler = new ExcelSaxImportHandler(sharedStrings, stylesTable, (rowIndex, cellValues) -> {
                    if (rowIndex == 0) {
                        for (int i = 0; i < cellValues.size(); i++) {
                            String title = cellValues.get(i);
                            if (StringUtils.isNotEmpty(title)) {
                                titleIndexMap.put(title, i);
                            }
                        }
                    } else {
                        // 解析数据行，传入 rowIndex 供 dataKey 使用
                        buildMapFromRawRow(dataMap, cellValues, importFields, titleIndexMap, rowIndex);
                    }
                });
                XMLReader xmlReader = SAXHelper.newXMLReader();
                xmlReader.setContentHandler((ContentHandler) handler);
                xmlReader.parse(new InputSource(sheetStream));
                sheetStream.close();
            }
        } catch (ExcelImportException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            // OOM 是 Error 不是 Exception，必须单独捕获，否则会被吞掉导致前端收到空 msg
            logger.error("导入 Excel 失败：内存不足，请联系管理员调大 JVM 堆内存（-Xmx）", e);
            throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
        } catch (Exception e) {
            // 提取真实原因：SAX 解析引擎会将回调内部抛出的异常包裹为 SAXException
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            // OOM 也可能被 SAXException 包裹，需单独判断
            if (cause instanceof OutOfMemoryError) {
                logger.error("导入 Excel 失败：内存不足，请联系管理员调大 JVM 堆内存（-Xmx）", cause);
                throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
            }
            logger.error("导入 Excel 失败", cause);
            if (cause instanceof ExcelImportException) {
                throw (ExcelImportException) cause;
            }
            if (cause instanceof BusinessException) {
                throw new ExcelImportException(cause.getMessage());
            }
            throw new ExcelImportException(fileName + "：文件导入错误，" + cause.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        if (titleIndexMap.isEmpty()) {
            ExcelImportResult<Map<String, Object>> emptyResult = new ExcelImportResult<>(false);
            emptyResult.addErrorString("请使用系统导出的excel");
            throw handleError(emptyResult, fileName);
        }
        for (Map.Entry<String, Map<String, Object>> entry : dataMap.entrySet()) {
            dataList.add(entry.getValue());
        }
        if (dataList.isEmpty()) {
            throw new ExcelImportException(fileName + "：未读取到数据！");
        }
        ExcelImportResult<Map<String, Object>> validResult = validData(importFields, 1, dataList, ignoreFields);
        if (!validResult.getResult()) {
            throw handleError(validResult, fileName);
        }
        return validResult.getData();
    }

    /**
     * SAX 流式导入——动态字段分批版本（超大文件专用，仅支持 .xlsx）。
     * <p>
     * 与 {@link #importDataSax(String, List, InputStream, String, List)} 不同，本方法不会将所有行数据堆积在内存，
     * 而是每解析 batchSize 行就通过 batchProcessor 回调处理，处理完后释放本批内存。
     *
     * @param sheetName       目标 Sheet 名称，传空则取第一个 Sheet
     * @param importFields    字段映射列表
     * @param inputStream     excel 文件输入流
     * @param fileName        文件名称
     * @param ignoreFields    校验时忽略的字段标题列表
     * @param batchSize       每批处理行数（建议 500~2000）
     * @param batchProcessor  批次处理回调，接收一批解析完成的 Map 数据
     * @return 总处理行数
     * @throws ExcelImportException 格式错误或业务校验不通过时抛出
     */
    @SuppressWarnings("unchecked")
    public static int importDataSaxBatched(
            String sheetName,
            List<ExcelField> importFields,
            InputStream inputStream,
            String fileName,
            List<String> ignoreFields,
            int batchSize,
            Consumer<List<Map<String, Object>>> batchProcessor) throws ExcelImportException {
        String extName = FileUtil.extName(fileName);
        if (!EXCEL_2007_EXTEND_NAME.equals(extName)) {
            throw new ExcelImportException(fileName + "：SAX 流式导入仅支持 .xlsx 格式！");
        }
        Map<String, Integer> titleIndexMap = new LinkedHashMap<>();
        final LinkedHashMap<String, Map<String, Object>>[] batchDataMap = new LinkedHashMap[]{new LinkedHashMap<>()};
        final int[] totalProcessed = {0};
        // PostgreSQL PreparedStatement 参数上限 65535，根据列数动态计算安全批次大小
        final int[] effectiveBatchSize = {batchSize};

        File tempFile = null;
        try {
            tempFile = Files.createTempFile("excel_import_", ".xlsx").toFile();
            java.nio.file.Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try (OPCPackage pkg = OPCPackage.open(tempFile)) {
                XSSFReader xssfReader = new XSSFReader(pkg);
                SimpleSharedStringsTable sharedStrings = new SimpleSharedStringsTable(pkg);
                StylesTable stylesTable = xssfReader.getStylesTable();
                InputStream sheetStream = getSaxSheetStream(xssfReader, sheetName);
                if (sheetStream == null) {
                    throw new ExcelImportException(fileName + "：未找到对应的 Sheet，请使用系统提供的模板！");
                }
                ExcelSaxImportHandler handler = new ExcelSaxImportHandler(sharedStrings, stylesTable, (rowIndex, cellValues) -> {
                    if (rowIndex == 0) {
                        for (int i = 0; i < cellValues.size(); i++) {
                            String title = cellValues.get(i);
                            if (StringUtils.isNotEmpty(title)) {
                                titleIndexMap.put(title, i);
                            }
                        }
                        // 根据实际列数动态调整批次大小，确保不超过 PostgreSQL 65535 参数限制
                        int columnCount = Math.max(titleIndexMap.size(), 1);
                        int safeBatch = 65535 / columnCount;
                        effectiveBatchSize[0] = Math.max(1, Math.min(batchSize, safeBatch));
                        logger.info("表头列数: {}, 有效批次大小: {}", columnCount, effectiveBatchSize[0]);
                    } else {
                        buildMapFromRawRow(batchDataMap[0], cellValues, importFields, titleIndexMap, rowIndex);
                        // 达到批次大小时，校验并回调处理，然后清空释放内存
                        if (batchDataMap[0].size() >= effectiveBatchSize[0]) {
                            List<Map<String, Object>> batchList = new ArrayList<>(batchDataMap[0].values());
                            ExcelImportResult<Map<String, Object>> batchResult = validData(importFields, 1, batchList, ignoreFields);
                            if (!batchResult.getResult()) {
                                throw new BusinessException(String.join("<br>", batchResult.getErrorMessages()));
                            }
                            batchProcessor.accept(batchResult.getData());
                            totalProcessed[0] += batchResult.getData().size();
                            batchDataMap[0] = new LinkedHashMap<>();
                            logger.info("导入进度：已处理 {} 条", totalProcessed[0]);
                        }
                    }
                });
                XMLReader xmlReader = SAXHelper.newXMLReader();
                xmlReader.setContentHandler((ContentHandler) handler);
                xmlReader.parse(new InputSource(sheetStream));
                sheetStream.close();
            }
        } catch (ExcelImportException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            logger.error("导入 Excel 失败：内存不足", e);
            throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof OutOfMemoryError) {
                logger.error("导入 Excel 失败：内存不足", cause);
                throw new ExcelImportException(fileName + "：文件过大，服务器内存不足，请分批导入或联系管理员");
            }
            logger.error("导入 Excel 失败", cause);
            if (cause instanceof ExcelImportException) throw (ExcelImportException) cause;
            if (cause instanceof BusinessException) throw new ExcelImportException(cause.getMessage());
            throw new ExcelImportException(fileName + "：文件导入错误，" + cause.getMessage());
        } finally {
            if (tempFile != null) tempFile.delete();
        }
        if (titleIndexMap.isEmpty()) {
            ExcelImportResult<Map<String, Object>> emptyResult = new ExcelImportResult<>(false);
            emptyResult.addErrorString("请使用系统导出的excel");
            throw handleError(emptyResult, fileName);
        }
        // 处理末尾不足一批的剩余数据
        if (!batchDataMap[0].isEmpty()) {
            List<Map<String, Object>> lastBatch = new ArrayList<>(batchDataMap[0].values());
            ExcelImportResult<Map<String, Object>> batchResult = validData(importFields, 1, lastBatch, ignoreFields);
            if (!batchResult.getResult()) {
                throw handleError(batchResult, fileName);
            }
            batchProcessor.accept(batchResult.getData());
            totalProcessed[0] += batchResult.getData().size();
            batchDataMap[0] = null;
        }
        if (totalProcessed[0] == 0) {
            throw new ExcelImportException(fileName + "：未读取到数据！");
        }
        return totalProcessed[0];
    }

    /**
     * 获取目标 Sheet 的输入流。sheetName 为空时取第一个 Sheet。
     */
    private static InputStream getSaxSheetStream(XSSFReader xssfReader, String sheetName) throws Exception {
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        InputStream first = null;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String name = iter.getSheetName();
            if (first == null) {
                first = stream;
            }
            if (StringUtils.isNotEmpty(sheetName) && sheetName.equals(name)) {
                return stream;
            } else if (StringUtils.isEmpty(sheetName)) {
                return first;
            }
        }
        // sheetName 非空但未匹配到时返回 null
        return StringUtils.isEmpty(sheetName) ? first : null;
    }

    /**
     * 根据 SAX 解析的原始行数据（字符串列表）构造目标对象，追加到 dataMap。
     * 逻辑与 {@link #getDataFormRow(LinkedHashMap, Class, Row, List, Map)} 对齐，
     * 但不依赖 POI Row/Cell 对象，适用于 SAX 流式解析场景。
     *
     * @param dataMap        结果容器，key 为行唯一标识字符串
     * @param tClass         目标实体类型
     * @param rawCellValues  当前行各列原始字符串值（按列索引排列，空列为 ""）
     * @param excelFields    字段映射列表
     * @param titleIndexMap  表头名称 -> 列索引 映射
     * @param rowIndex       行索引（从 1 开始），用于生成行唯一 key
     */
    private static <T> void buildObjectFromRawRow(
            LinkedHashMap<String, Object> dataMap,
            Class<T> tClass,
            List<String> rawCellValues,
            List<ExcelField> excelFields,
            Map<String, Integer> titleIndexMap,
            int rowIndex) {
        T t;
        try {
            t = ReflectUtil.newInstance(tClass);
        } catch (Exception e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new BusinessException("类型配置错误");
        }
        Map<Boolean, List<ExcelField>> map = excelFields.stream().collect(Collectors.groupingBy(ExcelField::isCollection));
        boolean validData = false;
        boolean hasCollection = map.containsKey(true);
        // 没有子表时：每行独立，直接用行索引作为 key，避免字符串无限内容拼接 OOM
        // 有子表时：拼接非子表字段的值作为 key，用于合并同一主表行的多行子表数据
        String dataKey;
        if (!hasCollection) {
            dataKey = String.valueOf(rowIndex);
        } else {
            StringBuilder keyBuilder = new StringBuilder();
            for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
                if (excelField.isOnlyForExcelTitle()) {
                    continue;
                }
                Integer colIndex = titleIndexMap.get(excelField.getFieldTitle());
                if (colIndex == null) {
                    continue;
                }
                String rawValue = colIndex < rawCellValues.size() ? rawCellValues.get(colIndex) : "";
                keyBuilder.append(rawValue);
            }
            dataKey = keyBuilder.length() > 0 ? keyBuilder.toString() :
                    (dataMap.isEmpty() ? String.valueOf(rowIndex) :
                            (String) dataMap.keySet().toArray()[dataMap.size() - 1]);
        }
        // 处理普通字段
        for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
            if (excelField.isOnlyForExcelTitle()) {
                continue;
            }
            Integer colIndex = titleIndexMap.get(excelField.getFieldTitle());
            if (colIndex == null) {
                continue;
            }
            String rawValue = colIndex < rawCellValues.size() ? rawCellValues.get(colIndex) : "";
            if (StringUtils.isEmpty(rawValue)) {
                continue;
            }
            Object cellValue = translateRawValue(rawValue, excelField);
            validData = true;
            try {
                ReflectUtil.setFieldValue(t, excelField.getFieldValue(), cellValue);
            } catch (Exception e) {
                logger.error(ExceptionUtil.stacktraceToString(e));
                throw new BusinessException("类型配置错误");
            }
        }
        if (hasCollection && dataMap.containsKey(dataKey)) {
            t = (T) dataMap.get(dataKey);
        }
        // 子表字段（collection）在 SAX 模式下暂不支持嵌套解析，跳过
        if (validData) {
            dataMap.put(dataKey, t);
        }
    }

    /**
     * 根据 SAX 解析的原始行数据（字符串列表）构造 Map，追加到 dataMap。
     * 对应动态字段版本的流式解析。
     *
     * @param rowIndex 行索引（从 1 开始），用于生成行唯一 key
     */
    private static void buildMapFromRawRow(
            LinkedHashMap<String, Map<String, Object>> dataMap,
            List<String> rawCellValues,
            List<ExcelField> excelFields,
            Map<String, Integer> titleIndexMap,
            int rowIndex) {
        Map<Boolean, List<ExcelField>> map = excelFields.stream().collect(Collectors.groupingBy(ExcelField::isCollection));
        Map<String, Object> rowData = new LinkedHashMap<>();
        boolean validData = false;
        boolean hasCollection = map.containsKey(true);
        // 没有子表时：每行独立，直接用行索引作为 key，避免字符串无限内容拼接 OOM
        // 有子表时：拼接非子表字段的值作为 key，用于合并同一主表行的多行子表数据
        String dataKey;
        if (!hasCollection) {
            dataKey = String.valueOf(rowIndex);
        } else {
            StringBuilder keyBuilder = new StringBuilder();
            for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
                if (excelField.isOnlyForExcelTitle()) {
                    continue;
                }
                Integer colIndex = titleIndexMap.get(excelField.getFieldTitle());
                if (colIndex == null) {
                    continue;
                }
                String rawValue = colIndex < rawCellValues.size() ? rawCellValues.get(colIndex) : "";
                keyBuilder.append(rawValue);
            }
            dataKey = keyBuilder.length() > 0 ? keyBuilder.toString() :
                    (dataMap.isEmpty() ? String.valueOf(rowIndex) :
                            (String) dataMap.keySet().toArray()[dataMap.size() - 1]);
        }
        for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
            if (excelField.isOnlyForExcelTitle()) {
                continue;
            }
            Integer colIndex = titleIndexMap.get(excelField.getFieldTitle());
            if (colIndex == null) {
                continue;
            }
            String rawValue = colIndex < rawCellValues.size() ? rawCellValues.get(colIndex) : "";
            Object cellValue = StringUtils.isEmpty(rawValue) ? null : translateRawValue(rawValue, excelField);
            if (cellValue != null && StringUtils.isNotEmpty(Convert.toStr(cellValue))) {
                validData = true;
            }
            rowData.put(excelField.getFieldValue(), cellValue);
        }
        if (hasCollection && dataMap.containsKey(dataKey)) {
            rowData = dataMap.get(dataKey);
        }
        if (validData) {
            dataMap.put(dataKey, rowData);
        }
    }

    /**
     * 将 SAX 解析出的原始字符串值按 ExcelField 配置进行字典/类型转换。
     * 对应 {@link #translateValue(Cell, ExcelField)} 的无 Cell 版本。
     *
     * @param rawValue  原始字符串（已由 ExcelSaxImportHandler 将日期序列号转为日期字符串）
     * @param excelField 字段配置
     * @return 转换后的值
     */
    private static Object translateRawValue(String rawValue, ExcelField excelField) {
        if (rawValue == null) {
            return null;
        }
        LinkedHashMap<Object, Object> dataMap = excelField.getDataMap();
        if (dataMap == null || dataMap.isEmpty()) {
            return rawValue;
        }
        if (excelField.isDictMultiple()) {
            List<String> cellValues = new ArrayList<>();
            String[] split = rawValue.split(excelField.getDictSeparator());
            LinkedHashMap<Object, Object> mapData = new LinkedHashMap<>(dataMap.size());
            for (Map.Entry<Object, Object> entry : dataMap.entrySet()) {
                mapData.put(entry.getValue(), entry.getKey());
            }
            for (String s : split) {
                Object orDefault = mapData.getOrDefault(s, null);
                if (orDefault != null) {
                    cellValues.add(Convert.toStr(orDefault));
                }
            }
            return StringUtils.join(cellValues, excelField.getDictSeparator());
        } else {
            for (Map.Entry<Object, Object> entry : dataMap.entrySet()) {
                if (rawValue.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    // ======================== 异常处理 ========================

    /**
     * 处理异常
     *
     * @param excelImportResult 导入结果
     * @param fileName          文件名称
     * @param <T>               实体类型
     * @return
     */
    private static <T> ExcelImportException handleError(ExcelImportResult<T> excelImportResult, String fileName) {
        List<String> errorMessages = excelImportResult.getErrorMessages();
        StringBuilder error = new StringBuilder(fileName + String.format("存在%s个问题：", errorMessages.size()));
        int count = 1;
        for (String errorMessage : errorMessages) {
            error.append("<br>");
            error.append(count);
            error.append(".");
            error.append(errorMessage);
            count++;
        }
        return new ExcelImportException(error.toString());
    }

    public static ExcelImportResult<Map<String, Object>> importData(List<ExcelField> importFields, int startRow, Sheet sheet) {
        ExcelImportResult<Map<String, Object>> result = new ExcelImportResult<>(true);
        Row rowTitle = sheet.getRow(0);
        if (rowTitle == null || rowTitle.getLastCellNum() <= 0) {
            result.setResult(false);
            result.addErrorString("请使用系统导出的excel");
            return result;
        }
        Map<String, Integer> excelTitleIndexMap = new HashMap<>();//存储表头文字和序号的关系
        for (int colIndex = 0; colIndex < rowTitle.getLastCellNum(); colIndex++) {
            Cell cell = rowTitle.getCell(colIndex);
            if (null != cell) {
                String string = Convert.toStr(getCellValue(cell));
                if (StringUtils.isNotEmpty(string)) {
                    excelTitleIndexMap.put(string, colIndex);
                }
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        LinkedHashMap<String, Map<String, Object>> dataMap = new LinkedHashMap<>();
        for (int rowIndex = startRow; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            getDataFormRow(dataMap, row, importFields, excelTitleIndexMap);
        }
        for (Map.Entry<String, Map<String, Object>> entry : dataMap.entrySet()) {
            dataList.add(entry.getValue());
        }
        for (ExcelField field : importFields) {
            if (!field.isOnlyForExcelTitle() && field.isCollection()) {
                //目前没什么用 TODO 2023-03-10 16:50:15 zry
                mergeMapChildren(dataList, field);
            }
        }
        result.setData(dataList);
        return result;
    }

    /**
     * 导入指定sheet数据
     *
     * @param tClass   接收数据实体类
     * @param startRow 数据起始行数
     * @param sheet    sheet对象
     * @param <T>      实体类型
     * @return
     */
    public static <T> ExcelImportResult<T> importData(Class<T> tClass, int startRow, Sheet sheet) {

        ExcelImportResult<T> result = new ExcelImportResult<T>(true);

        Row rowTitle = sheet.getRow(0);
        if (rowTitle == null || rowTitle.getLastCellNum() <= 0) {
            result.setResult(false);
            result.addErrorString("请使用系统导出的excel");
            return result;
        }
        List<ExcelField> importTitleList = getImportTitleList(tClass);
        Map<String, Integer> excelTitleIndexMap = new HashMap<>();//存储表头文字和序号的关系
        for (int colIndex = 0; colIndex < rowTitle.getLastCellNum(); colIndex++) {
            Cell cell = rowTitle.getCell(colIndex);
            if (null != cell) {
                String string = Convert.toStr(getCellValue(cell));
                if (StringUtils.isNotEmpty(string)) {
                    excelTitleIndexMap.put(string, colIndex);
                }
            }
        }
        List<T> dataList = new ArrayList<>();
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        for (int rowIndex = startRow; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            getDataFormRow(dataMap, tClass, row, importTitleList, excelTitleIndexMap);
        }
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            dataList.add((T) entry.getValue());
        }
        for (ExcelField field : importTitleList) {
            if (!field.isOnlyForExcelTitle() && field.isCollection()) {
                mergeChildren(dataList, field);
            }
        }
        result.setData(dataList);
        return result;
    }

    private static void mergeMapChildren(List<Map<String, Object>> list, ExcelField excelField) {
        if (list == null) {
            return;
        }
        for (Map<String, Object> data : list) {
            List<Map<String, Object>> value = (List<Map<String, Object>>) data.get(excelField.getFieldValue());
            if (value != null) {
                Map<Boolean, List<ExcelField>> map = excelField.getChildren().stream().collect(Collectors.groupingBy(ExcelField::isCollection));
                LinkedHashMap<String, Map<String, Object>> dataMap = new LinkedHashMap<>();
                if (!map.containsKey(true)) {
                    return;
                }
                for (Map<String, Object> o : value) {
                    StringBuilder dataKey = new StringBuilder();
                    for (ExcelField field : map.getOrDefault(false, new ArrayList<>())) {
                        if (!field.isOnlyForExcelTitle()) {
                            Object fieldValue = ReflectUtil.getFieldValue(o, field.getFieldValue());
                            if (fieldValue != null) {
                                dataKey.append(fieldValue);
                            }

                        }
                    }
                    if (StringUtils.isEmpty(dataKey) && dataMap.size() > 0) {
                        dataKey.append(dataMap.keySet().toArray()[dataMap.keySet().size() - 1]);
                    }
                    Map<String, Object> object = dataMap.getOrDefault(dataKey.toString(), o);
                    for (ExcelField field : map.getOrDefault(true, new ArrayList<>())) {
                        if (!field.isOnlyForExcelTitle()) {
                            List children = (List) object.get(field.getFieldValue());
                            if (object != o) {
                                List currentChildren = (List) o.get(field.getFieldValue());
                                if (currentChildren != null) {
                                    if (children == null) {
                                        children = new ArrayList();
                                    }
                                    children.addAll(currentChildren);
                                }
                            }
                            object.put(field.getFieldValue(), children);
                        }
                    }
                    dataMap.put(dataKey.toString(), object);
                }
                data.put(excelField.getFieldValue(), dataMap.values());
                for (ExcelField field : map.getOrDefault(true, new ArrayList<>())) {
                    value = (List) data.get(field.getFieldValue());
                    mergeMapChildren(value, field);
                }
            }
        }
    }

    /**
     * 合并子表数据
     *
     * @param list       待处理数据
     * @param excelField 需要合并的字段
     * @param <T>        实体类型
     */
    private static <T> void mergeChildren(List<T> list, ExcelField excelField) {
        for (T t : list) {
            List value = (List) ReflectUtil.getFieldValue(t, excelField.getFieldValue());
            if (value != null) {
                Map<Boolean, List<ExcelField>> map = excelField.getChildren().stream().collect(Collectors.groupingBy(ExcelField::isCollection));
                LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
                if (!map.containsKey(true)) {
                    return;
                }
                for (Object o : value) {
                    StringBuilder dataKey = new StringBuilder();
                    for (ExcelField field : map.getOrDefault(false, new ArrayList<>())) {
                        if (!field.isOnlyForExcelTitle()) {
                            Object fieldValue = ReflectUtil.getFieldValue(o, field.getFieldValue());
                            if (fieldValue != null) {
                                dataKey.append(fieldValue);
                            }

                        }
                    }
                    if (StringUtils.isEmpty(dataKey) && dataMap.size() > 0) {
                        dataKey.append(dataMap.keySet().toArray()[dataMap.keySet().size() - 1]);
                    }
                    Object object = dataMap.getOrDefault(dataKey.toString(), o);
                    for (ExcelField field : map.getOrDefault(true, new ArrayList<>())) {
                        if (!field.isOnlyForExcelTitle()) {
                            List children = (List) ReflectUtil.getFieldValue(object, field.getFieldValue());
                            if (object != o) {
                                List currentChildren = (List) ReflectUtil.getFieldValue(o, field.getFieldValue());
                                if (currentChildren != null) {
                                    if (children == null) {
                                        children = new ArrayList();
                                    }
                                    children.addAll(currentChildren);
                                }
                            }
                            ReflectUtil.setFieldValue(object, field.getFieldValue(), children);
                        }
                    }
                    dataMap.put(dataKey.toString(), object);
                }
                ReflectUtil.setFieldValue(t, excelField.getFieldValue(), dataMap.values());
                for (ExcelField field : map.getOrDefault(true, new ArrayList<>())) {
                    value = (List) ReflectUtil.getFieldValue(t, excelField.getFieldValue());
                    mergeChildren(value, field);
                }
            }

        }
    }

    private static Map<String, Object> getDataFormRow(LinkedHashMap<String, Map<String, Object>> dataMap, Row row, List<ExcelField> excelFields, Map<String, Integer> excelTitleIndexMap) {
        boolean validData = false;
        Map<String, Object> t = null;
        if (row != null) {
            t = new HashMap<>();
            Map<Boolean, List<ExcelField>> map = excelFields.stream().collect(Collectors.groupingBy(ExcelField::isCollection));
            //每个字段拼接起来形成该行数据的唯一值
            StringBuilder dataKey = new StringBuilder();
            for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
                if (!excelField.isOnlyForExcelTitle()) {
                    Integer colIndex = excelTitleIndexMap.get(excelField.getFieldTitle());
                    if (colIndex == null) {
                        continue;
                    }
                    Cell cell = row.getCell(colIndex);
                    if (cell == null) {
                        continue;
                    }
                    Object cellValue = translateValue(cell, excelField);
                    if (cellValue != null && StringUtils.isNotEmpty(Convert.toStr(cellValue))) {
                        validData = true;
                    }
                    try {
                        if (cellValue != null && Date.class.equals(cellValue.getClass())) {
                            if (!excelField.isDate()) {
                                cellValue = cn.hutool.core.date.DateUtil.formatDate((Date) cellValue);
                            }
                        }
                        t.put(excelField.getFieldValue(), cellValue);
                        //如果没有子表则不合并数据 增加一个随机id保证两行一样的数据会解析成两行
                        if (!map.containsKey(true)) {
                            dataKey.append(IdUtil.fastUUID());
                        }
                        dataKey.append(cellValue);
                    } catch (Exception e) {
                        logger.error(ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException("类型配置错误");
                    }
                }
            }
            //如果这个key为empty说明每一列都是空的，取上一行的key
            if (StringUtils.isEmpty(dataKey) && dataMap.size() > 0) {
                dataKey.append(dataMap.keySet().toArray()[dataMap.keySet().size() - 1]);
            }
            if (dataMap.containsKey(dataKey.toString())) {
                t = dataMap.get(dataKey.toString());
            }
            //处理子表数据
            for (ExcelField excelField : map.getOrDefault(true, new ArrayList<>())) {
                if (!excelField.isOnlyForExcelTitle()) {
                    LinkedHashMap<String, Map<String, Object>> childDataMap = new LinkedHashMap<>();
                    Map<String, Object> child = getDataFormRow(childDataMap, row, excelField.getChildren(), excelTitleIndexMap);
                    if (child != null) {
                        validData = true;
                        List<Map<String, Object>> children = (List<Map<String, Object>>) t.get(excelField.getFieldValue());
                        if (children == null) {
                            children = new ArrayList();
                        }
                        children.add(child);
                        t.put(excelField.getFieldValue(), children);
                    }
                }
            }
            if (validData) {
                dataMap.put(dataKey.toString(), t);
            }
        }
        if (validData) {
            return t;
        }
        return null;
    }

    /**
     * 从row中获取数据并转换为实体
     *
     * @param dataMap            接收数据的map
     * @param tClass             接收数据实体类
     * @param row                excel row对象
     * @param excelFields        字段
     * @param excelTitleIndexMap excel表头index对应关系
     * @param <T>                实体类型
     * @return
     */
    private static <T> T getDataFormRow(LinkedHashMap<String, Object> dataMap, Class<T> tClass, Row row, List<ExcelField> excelFields, Map<String, Integer> excelTitleIndexMap) {
        boolean validData = false;
        T t = null;
        if (row != null) {
            try {
                t = ReflectUtil.newInstance(tClass);
            } catch (Exception e) {
                logger.error(ExceptionUtil.stacktraceToString(e));
                throw new BusinessException("类型配置错误");
            }
            Map<Boolean, List<ExcelField>> map = excelFields.stream().collect(Collectors.groupingBy(ExcelField::isCollection));
            //每个字段拼接起来形成该行数据的唯一值
            StringBuilder dataKey = new StringBuilder();
            for (ExcelField excelField : map.getOrDefault(false, new ArrayList<>())) {
                if (!excelField.isOnlyForExcelTitle()) {
                    Integer colIndex = excelTitleIndexMap.get(excelField.getFieldTitle());
                    if (colIndex == null) {
                        continue;
                    }
                    Cell cell = row.getCell(colIndex);
                    if (cell == null) {
                        continue;
                    }
                    Object cellValue = translateValue(cell, excelField);
                    if (cellValue != null && StringUtils.isNotEmpty(Convert.toStr(cellValue))) {
                        validData = true;
                    }
                    try {
                        if (cellValue != null && Date.class.equals(cellValue.getClass())) {
                            if (excelField.getField().getType().equals(String.class)) {
                                cellValue = cn.hutool.core.date.DateUtil.formatDate((Date) cellValue);
                            }
                        }
                        ReflectUtil.setFieldValue(t, excelField.getFieldValue(), cellValue);
                        //如果没有子表则不合并数据 增加一个随机id保证两行一行的数据会解析成两行
                        if (!map.containsKey(true)) {
                            dataKey.append(IdUtil.fastUUID());
                        }
                        dataKey.append(cellValue);
                    } catch (Exception e) {
                        logger.error(ExceptionUtil.stacktraceToString(e));
                        throw new BusinessException("类型配置错误");
                    }
                }
            }
            //如果这个key为empty说明每一列都是空的，取上一行的key
            if (StringUtils.isEmpty(dataKey) && dataMap.size() > 0) {
                dataKey.append(dataMap.keySet().toArray()[dataMap.keySet().size() - 1]);
            }
            if (dataMap.containsKey(dataKey.toString())) {
                t = (T) dataMap.get(dataKey.toString());
            }
            //处理子表数据
            for (ExcelField excelField : map.getOrDefault(true, new ArrayList<>())) {
                if (!excelField.isOnlyForExcelTitle()) {
                    LinkedHashMap<String, Object> childDataMap = new LinkedHashMap<>();
                    Object child = getDataFormRow(childDataMap, excelField.getDefineClass(), row, excelField.getChildren(), excelTitleIndexMap);
                    if (child != null) {
                        validData = true;
                        List<Object> children = (List) ReflectUtil.getFieldValue(t, excelField.getFieldValue());
                        if (children == null) {
                            children = new ArrayList();
                        }
                        children.add(child);
                        ReflectUtil.setFieldValue(t, excelField.getFieldValue(), children);
                    }
                }
            }
            if (validData) {
                dataMap.put(dataKey.toString(), t);
            }
        }
        if (validData) {
            return t;
        }
        return null;
    }

    /**
     * 校验导入的数据
     *
     * @param tClass
     * @param startRow
     * @param list
     * @param ignoreFields
     * @param <T>
     * @return
     */
    public static <T> ExcelImportResult<T> validData(Class<T> tClass, int startRow, List<T> list, List<String> ignoreFields) {
        ExcelImportResult<T> result = new ExcelImportResult<T>(true);
        Excel excel = tClass.getAnnotation(Excel.class);
        if (excel != null && excel.skipImportValid()) {
            result.setData(list);
            return result;
        }
        List<ExcelField> importTitleList = getImportTitleList(tClass);
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (T t : list) {
            mapList.add(BeanUtil.beanToMap(t));
        }
        List<String> errorMessages = validCellValue(importTitleList, startRow, mapList, ignoreFields);
        if (errorMessages.size() > 0) {
            result.setResult(false);
            result.addErrorStrings(errorMessages);
        }
        result.setData(list);
        return result;
    }

    public static List<String> validCellValue(List<ExcelField> importFields, int startRow, List<Map<String, Object>> list, List<String> ignoreFields) {
        LinkedHashMap<String, FieldRepeatError> repeatValueMap = new LinkedHashMap<>();
        Map<String, Integer> kvMap = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        for (Map<String, Object> t : list) {
            List<String> errors = new ArrayList<>();
            List<String> oerrors = new ArrayList<>();
            for (ExcelField excelField : importFields) {
                if (ignoreFields.contains(excelField.getFieldTitle())) {
                    continue;
                }
                if (!excelField.isSkipImportValid()) {
                    ImportValidEnum[] importValidEnums = excelField.getImportValidEnums();
                    if (importValidEnums != null) {
                        for (ImportValidEnum importValidEnum : importValidEnums) {
                            String value = Convert.toStr(t.get(excelField.getFieldValue()));
                            switch (importValidEnum) {
                                case NOT_EMPTY:
                                    if (StringUtils.isEmpty(value)) {
                                        errors.add(excelField.getFieldTitle());
                                    }
                                    break;
                                case ID_CARD_NUMBER:
                                    if (StringUtils.isNotEmpty(value)) {
                                        if (!IdcardUtil.isValidCard(value)) {
                                            oerrors.add("身份证号有误");
                                        }
                                    }
                                    break;
                                case UNIQUE:
                                    String kv = excelField.getFieldValue() + Convert.toStr(value);
                                    if (kvMap.containsKey(kv)) {
                                        FieldRepeatError fieldRepeatError;
                                        if (repeatValueMap.containsKey(kv)) {
                                            fieldRepeatError = repeatValueMap.get(kv);
                                            fieldRepeatError.addLineNumber(startRow + 1);
                                        } else {
                                            fieldRepeatError = new FieldRepeatError(excelField.getFieldTitle(), value, kvMap.get(kv), startRow + 1);
                                        }
                                        repeatValueMap.put(kv, fieldRepeatError);
                                    } else {
                                        kvMap.put(kv, startRow + 1);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }


            }
            startRow++;
            if (errors.size() > 0) {
                String format = String.format("第%s行 %s 不能为空", startRow, StringUtils.join(errors.toArray(), "、"));
                if (oerrors.size() > 0) {
                    format += "，" + StringUtils.join(oerrors.toArray(), "、");
                }
                errorMessages.add(format);
            }

        }
        for (FieldRepeatError value : repeatValueMap.values()) {
            errorMessages.add(value.toString());
        }
        return errorMessages;
    }

    public static ExcelImportResult<Map<String, Object>> validData(List<ExcelField> importFields, int startRow, List<Map<String, Object>> list, List<String> ignoreFields) {
        ExcelImportResult<Map<String, Object>> result = new ExcelImportResult<>(true);
        List<String> errorMessages = validCellValue(importFields, startRow, list, ignoreFields);
        if (errorMessages.size() > 0) {
            result.setResult(false);
            result.addErrorStrings(errorMessages);
        }
        result.setData(list);
        return result;
    }

    private static Object getCellValue(Cell cell) {

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                // POI 5.x: 使用 DataFormatter 替代已移除的 setCellType()
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
            default:
                return null;
        }
    }


    private static Object translateValue(Cell cell, ExcelField excelField) {
        String cellValue = Convert.toStr(getCellValue(cell));
        if (cellValue == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date dateCellValue = cell.getDateCellValue();
            return dateCellValue;
        }

        LinkedHashMap<Object, Object> dataMap = excelField.getDataMap();
        if (dataMap == null || dataMap.size() == 0) {
            return cellValue;
        }
        if (excelField.isDictMultiple()) {
            List<String> cellValues = new ArrayList<>();
            String[] split = cellValue.split(excelField.getDictSeparator());
            LinkedHashMap<Object, Object> mapData = new LinkedHashMap<>(dataMap.size());
            for (Map.Entry<Object, Object> entry : dataMap.entrySet()) {
                mapData.put(entry.getValue(), entry.getKey());
            }
            for (String s : split) {
                Object orDefault = mapData.getOrDefault(s, null);
                if (orDefault != null) {
                    cellValues.add(Convert.toStr(orDefault));
                }
            }
            return StringUtils.join(cellValues, excelField.getDictSeparator());
        } else {
            for (Map.Entry<Object, Object> entry : dataMap.entrySet()) {
                if (cellValue.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return null;
        }

    }


}
