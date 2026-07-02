package com.jeestudio.tools.excel;

import com.jeestudio.tools.base.annotation.Dict;
import com.jeestudio.tools.base.annotation.Excel;
import com.jeestudio.tools.base.enums.AlignEnum;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.dict.DictHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: Excel导入导出工具
 */
public class ExcelUtil {
    public static DictHandler dictHandler = null;

    public static String EXCEL_2003_EXTEND_NAME = "xls";
    public static String EXCEL_2007_EXTEND_NAME = "xlsx";

    public static String LEFT = AlignEnum.LEFT.getValue();
    public static String CENTER = AlignEnum.CENTER.getValue();
    public static String RIGHT = AlignEnum.RIGHT.getValue();

    public static void init(DictHandler dictHandler) {
        ExcelUtil.dictHandler = dictHandler;
    }

    /**
     * 创建excel工作簿 并添加一个sheet
     *
     * @param sheetName sheet
     * @return
     */
    public static Workbook createWorkBook(String sheetName) {
        // 创建excel工作簿
        Workbook wb = new XSSFWorkbook();//zry 修改为XSSFWorkbook 对应 xlsx 2022-11-18 14:15:07
        // 创建第一个sheet（页），并命名
        Sheet sheet = wb.createSheet(sheetName);

        return wb;
    }

    /**
     * 获取标题样式
     *
     * @param workbook
     * @return
     */
    public static CellStyle getTitleStyle(Workbook workbook) {
        CellStyle cs = workbook.createCellStyle();
        Font f = workbook.createFont();
        // 创建第一种字体样式（用于列名）
        f.setFontName("宋体");
        f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        cs.setFont(f);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        return cs;
    }


    /**
     * 获取内容样式
     *
     * @param workbook
     * @return
     */
    public static CellStyle getContentStyle(Workbook workbook) {
        CellStyle cs2 = workbook.createCellStyle();
        Font f = workbook.createFont();
        // 创建字体样式（用于数据）
        f.setFontName("宋体");
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.BLACK.getIndex());
        cs2.setFont(f);
        cs2.setBorderLeft(BorderStyle.THIN);
        cs2.setBorderRight(BorderStyle.THIN);
        cs2.setBorderTop(BorderStyle.THIN);
        cs2.setBorderBottom(BorderStyle.THIN);
        return cs2;
    }

    /**
     * 获取内容居左样式
     *
     * @param workbook
     * @return
     */
    public static CellStyle getLeftStyle(Workbook workbook) {
        CellStyle contentStyle = getContentStyle(workbook);
        contentStyle.setAlignment(HorizontalAlignment.LEFT);
        return contentStyle;
    }

    /**
     * 获取内容居中样式
     *
     * @param workbook
     * @return
     */
    public static CellStyle getCenterStyle(Workbook workbook) {
        CellStyle contentStyle = getContentStyle(workbook);
        contentStyle.setAlignment(HorizontalAlignment.CENTER);
        return contentStyle;
    }

    /**
     * 获取内容居右样式
     *
     * @param workbook
     * @return
     */
    public static CellStyle getRightStyle(Workbook workbook) {
        CellStyle contentStyle = getContentStyle(workbook);
        contentStyle.setAlignment(HorizontalAlignment.RIGHT);
        return contentStyle;
    }

    public static <T> List<ExcelField> getExportTitleList(Class<T> tClass) {
        List<ExcelField> titles = new ArrayList<>();
        Field[] declaredFields = tClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Excel excel = declaredField.getAnnotation(Excel.class);
            if (excel != null && excel.exportField()) {
                LinkedHashMap<Object, Object> fieldDictionary = dictHandler.getDictionary(declaredField);
                titles.add(new ExcelField(declaredField.getName(), excel, fieldDictionary, declaredField.getAnnotation(Dict.class), declaredField));
            }
        }
        return titles;
    }


    public static <T> List<ExcelField> getImportTitleList(Class<T> tClass) {
        List<ExcelField> titles = new ArrayList<>();
        Field[] declaredFields = tClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Excel excel = declaredField.getAnnotation(Excel.class);
            if (excel != null && excel.importField()) {
                if (declaredField.getType().isAssignableFrom(List.class)) {
                    ParameterizedType pt = (ParameterizedType) declaredField.getGenericType();
                    Class<?> clz = (Class) pt.getActualTypeArguments()[0];
                    List<ExcelField> children = getImportTitleList(clz);
                    ExcelField excelField = new ExcelField(children);
                    excelField.setFieldValue(declaredField.getName());
                    excelField.setField(declaredField);
                    excelField.setDefineClass(clz);
                    excelField.setChildren(children);
                    titles.add(excelField);
                    //children 不需要加到总的列表中，因为后面会统一处理 zry 2023-08-29 20:49:35
                    /*for (ExcelField child : children) {
                        ExcelField excelTitleField = new ExcelField();
                        excelTitleField.setFieldValue(child.getFieldValue());
                        excelTitleField.setFieldTitle(child.getFieldTitle());
                        //excelTitleField.setExcel(child.getExcel());
                        excelTitleField.setDataMap(child.getDataMap());
                        //excelTitleField.setDict(child.getDict());
                        excelTitleField.setField(child.getField());
                        excelTitleField.setCollection(child.isCollection());
                        excelTitleField.setDictSingleSheet(child.isDictSingleSheet());
                        excelTitleField.setOnlyForExcelTitle(true);
                        titles.add(excelTitleField);
                    }*/
                } else {
                    LinkedHashMap<Object, Object> fieldDictionary = dictHandler.getDictionary(declaredField);
                    ExcelField excelField = new ExcelField(declaredField.getName(), excel, fieldDictionary, declaredField.getAnnotation(Dict.class), declaredField);
                    excelField.setDefineClass(tClass);
                    excelField.setDictSingleSheet(excel.dictSingleSheet());
                    titles.add(excelField);
                }
            }
        }
        Map<String, List<ExcelField>> map = titles.stream().filter(k -> StringUtils.isNotEmpty(k.getFieldTitle())).collect(Collectors.groupingBy(ExcelField::getFieldTitle));
        for (Map.Entry<String, List<ExcelField>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                throw new BusinessException("EXCEL配置title重复，" + entry.getKey());
            }
        }
        return titles;
    }
}
