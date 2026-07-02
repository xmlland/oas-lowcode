package com.jeestudio.tools.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description: Excel导出工具
 */
public class ExcelExportUtil extends ExcelUtil {

    /**
     * 创建标题行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param titles   标题
     * @param <T>
     */
    public static <T> void createTitleRow(Workbook workBook, Sheet sheet, List<ExcelField> titles) {
        createTitleRow(workBook, sheet, titles, 0);
    }
    /**
     * 创建标题行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param titles   标题
     * @param titleCellStyle 标题单元格样式
     * @param <T>
     */
    public static <T> void createTitleRow(Workbook workBook, Sheet sheet, List<ExcelField> titles, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle) {
        createTitleRow(workBook, sheet, titles, 0,titleCellStyle);
    }

    /**
     * 创建标题行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param titles     标题
     * @param titleIndex 标题行
     * @param <T>
     */
    public static <T> void createTitleRow(Workbook workBook, Sheet sheet, List<ExcelField> titles, int titleIndex) {
        createTitleRow( workBook,sheet,titles,titleIndex,null);
    }

    /**
     * 创建标题行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param titles     标题
     * @param titleIndex 标题行
     * @param titleCellStyle 标题单元格样式
     * @param <T>
     */
    public static <T> void createTitleRow(Workbook workBook, Sheet sheet, List<ExcelField> titles, int titleIndex, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle) {
        // 创建第一行
        Row row = sheet.createRow(titleIndex);
        Cell cell0 = row.createCell(0);
        cell0.setCellValue("序号");
        cell0.setCellStyle(getTitleStyle(workBook));
        if ( titleCellStyle != null ) {
            ExcelField xh = new ExcelField("xh","序号");
            cell0.setCellStyle(titleCellStyle.apply( cell0,xh,null ));
        }
        int i = 1;
        //设置列名
        for (ExcelField excelField : titles) {
            Cell cell = row.createCell(i);
            cell.setCellValue(excelField.getFieldTitle());
            cell.setCellStyle(getTitleStyle(workBook));
            if ( titleCellStyle != null ) {
                cell.setCellStyle(titleCellStyle.apply( cell,excelField,null ));
            }
            i++;
        }
    }

    /**
     * 创建数据行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param keys     标题key
     * @param list     数据列表
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list) {
        createDataRowMap(workBook, sheet, keys, list,null);
    }
    /**
     * 创建数据行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param keys     标题key
     * @param list     数据列表
     * @param dataCellStyle     数据单元格 样式
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        createDataRowMap(workBook, sheet, keys, list, 1, true,dataCellStyle);
    }
    /**
     * 创建数据行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list, int startIndex) {
        createDataRowMap(workBook, sheet, keys, list, startIndex, true,null);
    }
    /**
     * 创建数据行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     * @param dataCellStyle     数据单元格 样式
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list, int startIndex, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        createDataRowMap(workBook, sheet, keys, list, startIndex, true,dataCellStyle);
    }

    /**
     * 创建数据行
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     * @param showIndex  是否显示序号
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list, int startIndex, boolean showIndex) {
        createDataRowMap(workBook, sheet, keys, list, startIndex, showIndex,null);
    }
    /**
     * 创建数据行
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     * @param showIndex  是否显示序号
     * @param dataCellStyle     数据单元格 样式
     */
    public static void createDataRowMap(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<Map<String, Object>> list, int startIndex, boolean showIndex, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        CellStyle leftStyle = getLeftStyle(workBook);
        CellStyle centerStyle = getCenterStyle(workBook);
        CellStyle rightStyle = getRightStyle(workBook);
        leftStyle.setWrapText(true);
        centerStyle.setWrapText(true);
        rightStyle.setWrapText(true);
        int i = startIndex;

        //记录每一列的最大字符数
        JSONObject cellMaxChar = new JSONObject();

        //初始化cellMaxChar
        for(int keyIndex = 0;keyIndex<keys.size();keyIndex++){
            cellMaxChar.put("cell" + keyIndex,keys.get(keyIndex).getFieldTitle());
        }

        for(Iterator var11 = list.iterator(); var11.hasNext(); ++i) {
            Map<String, Object> map = (Map)var11.next();
            int j = 0;
            Row row1 = sheet.createRow(i);
            if (showIndex) {
                Cell cell0 = row1.createCell(0);
                cell0.setCellValue((double)(i - startIndex + 1));
                if (dataCellStyle != null) {
                    ExcelField xh = new ExcelField("xh", "序号");
                    JSONObject json = new JSONObject();
                    json.put("xh", i - startIndex + 1);
                    cell0.setCellStyle(getCenterStyle(workBook));
                    cell0.setCellStyle((CellStyle)dataCellStyle.apply(cell0, xh, json));
                } else {
                    cell0.setCellStyle(centerStyle);
                }

                j = 1;
            }

            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(map));

            for(Iterator var30 = keys.iterator(); var30.hasNext(); ++j) {
                ExcelField excelField = (ExcelField)var30.next();
                Cell cell = row1.createCell(j);
                Object fieldValue = jsonObject.get(excelField.getFieldValue());
                String align;
                if (excelField.getDataMap() == null) {
                    cell.setCellValue(Convert.toStr(fieldValue));
                } else if (!excelField.isDictMultiple()) {
                    align = Convert.toStr(fieldValue);
                    Object orDefault = excelField.getDataMap().getOrDefault(align, align);
                    if (orDefault != null && StrUtil.isNotEmpty(orDefault.toString())) {
                        cell.setCellValue(Convert.toStr(orDefault));
                    } else {
                        cell.setCellValue(Convert.toStr(align));
                    }
                } else {
                    align = Convert.toStr(fieldValue);
                    if (align == null) {
                        cell.setCellValue(align);
                    } else {
                        List<Object> res = new ArrayList();
                        String[] split = align.split(excelField.getDictSeparator());
                        String[] var23 = split;
                        int var24 = split.length;

                        for(int var25 = 0; var25 < var24; ++var25) {
                            String s = var23[var25];
                            Object orDefault = excelField.getDataMap().getOrDefault(s, (Object)null);
                            if (orDefault != null) {
                                res.add(orDefault);
                            }
                        }

                        cell.setCellValue(StringUtils.join(res, excelField.getDictSeparator()));
                    }
                }

                String value = cell.getStringCellValue();
                if(value != null){
                    String cellMaxCharValue = cellMaxChar.getString("cell" + j);
                    if(cellMaxCharValue != null){
                        if(value.length() > cellMaxCharValue.length()){
                            cellMaxChar.put("cell" + j,value);
                        }
                    }else{
                        cellMaxChar.put("cell" + j,value);
                    }

                }

                if (dataCellStyle != null) {
                    align = excelField.getAlignEnum().getValue();
                    if (LEFT.equals(align)) {
                        cell.setCellStyle(getLeftStyle(workBook));
                    } else if (CENTER.equals(align)) {
                        cell.setCellStyle(getCenterStyle(workBook));
                    } else if (RIGHT.equals(align)) {
                        cell.setCellStyle(getRightStyle(workBook));
                    } else {
                        cell.setCellStyle(getContentStyle(workBook));
                    }

                    cell.setCellStyle((CellStyle)dataCellStyle.apply(cell, excelField, jsonObject));
                } else {
                    align = excelField.getAlignEnum().getValue();
                    if (LEFT.equals(align)) {
                        cell.setCellStyle(leftStyle);
                    } else if (CENTER.equals(align)) {
                        cell.setCellStyle(centerStyle);
                    } else if (RIGHT.equals(align)) {
                        cell.setCellStyle(rightStyle);
                    } else {
                        cell.setCellStyle(getContentStyle(workBook));
                    }
                }
            }
        }

        sheet.createFreezePane(1, startIndex, 1, startIndex);

        for(short index = 0; index < keys.size() + 1; ++index) {
            String maxCellValue = cellMaxChar.getString("cell" + index);
            try {
                if(maxCellValue != null){
                    int utf8ByteLength = maxCellValue.getBytes("GBK").length;
                    int newWidth = utf8ByteLength * 260 + 1000;
                    if(newWidth > 20000){
                        sheet.setColumnWidth(index,20000);
                    }else{
                        int columnWidth = sheet.getColumnWidth(index);
                        if(newWidth > columnWidth){
                            sheet.setColumnWidth(index,newWidth);
                        }
                    }

                }
            } catch (UnsupportedEncodingException e) {
                throw new BusinessException("导出失败");
            }

        }
    }

    /**
     * 创建数据行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param keys     标题key
     * @param list     数据列表
     * @param <T>
     */
    public static <T> void createDataRow(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<T> list) {
        createDataRow(workBook, sheet, keys, list, 1,null);
    }
    /**
     * 创建数据行
     *
     * @param workBook workBook
     * @param sheet    sheet
     * @param keys     标题key
     * @param list     数据列表
     * @param dataCellStyle     数据单元格样式
     * @param <T>
     */
    public static <T> void createDataRow(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<T> list, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        createDataRow(workBook, sheet, keys, list, 1,dataCellStyle);
    }

    /**
     * 创建数据行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     * @param <T>
     */
    public static <T> void createDataRow(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<T> list, int startIndex) {
        createDataRow(workBook, sheet, keys, list, startIndex,null);
    }
    /**
     * 创建数据行
     *
     * @param workBook   workBook
     * @param sheet      sheet
     * @param keys       标题key
     * @param list       数据列表
     * @param startIndex 开始行
     * @param dataCellStyle     数据单元格样式
     * @param <T>
     */
    public static <T> void createDataRow(Workbook workBook, Sheet sheet, List<ExcelField> keys, List<T> list, int startIndex, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (T t : list) {
            mapList.add(BeanUtil.beanToMap(t));
        }
        createDataRowMap(workBook, sheet, keys, mapList, startIndex, true,dataCellStyle);
    }

    /**
     * 导出excel
     *
     * @param tClass 实体
     * @param list   数据列表
     * @param <T>
     */
    public static <T> Workbook export(Class<T> tClass, List<T> list) {
        return export( tClass, list,null,null);
    }

    /**
     * 导出excel
     *
     * @param tClass 实体
     * @param list   数据列表
     * @param titleCellStyle     标题单元格样式
     * @param dataCellStyle     数据单元格样式
     */
    public static <T> Workbook export(Class<T> tClass, List<T> list, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        return export("sheet1", tClass, list,titleCellStyle,dataCellStyle);
    }

    /**
     * 导出excel
     *
     * @param sheetName sheet
     * @param tClass    实体
     * @param list      数据列表
     * @param <T>
     */
    public static <T> Workbook export(String sheetName, Class<T> tClass, List<T> list) {
        return export(sheetName, tClass, list,null,null);
    }

    /**
     * 导出excel
     *
     * @param sheetName sheet
     * @param tClass    实体
     * @param list      数据列表
     * @param titleCellStyle     标题单元格样式
     * @param dataCellStyle     数据单元格样式
     */
    public static <T> Workbook export(String sheetName, Class<T> tClass, List<T> list, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        Workbook workBook = createWorkBook(sheetName);
        Sheet sheet = workBook.getSheet(sheetName);
        List<ExcelField> exportTitleList = getExportTitleList(tClass);
        createTitleRow(workBook, sheet, exportTitleList,titleCellStyle);
        //增加大批量数据分sheet导出 zry 2022-11-18 13:58:12
        int maxSize = 65535 - 1;//表头有一行需要减1
        if (list.size() > maxSize) {
            workBook.removeSheetAt(0);
            PageUtil.setOneAsFirstPageNo();
            int totalPage = PageUtil.totalPage(list.size(), maxSize);
            for (int i = 1; i <= totalPage; i++) {
                int[] trans = PageUtil.transToStartEnd(i, maxSize);
                List<T> subList = list.subList(trans[0], Math.min(trans[1], list.size()));
                Sheet sheetIndex = workBook.createSheet(sheetName + "_" + i);
                createTitleRow(workBook, sheetIndex, exportTitleList,titleCellStyle);
                createDataRow(workBook, sheetIndex, exportTitleList, subList,dataCellStyle);
            }

        } else {
            createDataRow(workBook, sheet, exportTitleList, list,dataCellStyle);
        }

        return workBook;
    }

    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list) {
        return export(sheetName, exportTitleList, list, null,null);
    }
    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        return export(sheetName, exportTitleList, list, 1,titleCellStyle,dataCellStyle);
    }

    /**
     * 导出excel
     *
     * @param sheetName       sheet
     * @param exportTitleList 标题
     * @param list            数据
     * @param startIndex      开始行
     * @return
     */
    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list, int startIndex) {
        return export(sheetName, exportTitleList, list, startIndex, null, null);
    }
    /**
     * 导出excel
     *
     * @param sheetName       sheet
     * @param exportTitleList 标题
     * @param list            数据
     * @param startIndex      开始行
     * @param titleCellStyle     标题单元格样式
     * @param dataCellStyle     数据单元格样式
     * @return
     */
    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list, int startIndex, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        return export(sheetName, exportTitleList, list, startIndex, 65535,titleCellStyle,dataCellStyle);
    }

    /**
     * 导出excel
     *
     * @param sheetName       sheet
     * @param exportTitleList 标题
     * @param list            数据
     * @param startIndex      开始行
     * @param maxRows         每个sheet最大行数 为-1时不分sheet
     * @return
     */
    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list, int startIndex, int maxRows) {
        return export(sheetName, exportTitleList, list, startIndex, maxRows,null,null);
    }
    /**
     * 导出excel
     *
     * @param sheetName       sheet
     * @param exportTitleList 标题
     * @param list            数据
     * @param startIndex      开始行
     * @param maxRows         每个sheet最大行数 为-1时不分sheet
     * @param titleCellStyle     标题单元格样式
     * @param dataCellStyle     数据单元格样式
     * @return
     */
    public static Workbook export(String sheetName, List<ExcelField> exportTitleList, List<Map<String, Object>> list, int startIndex, int maxRows, TriFunction<Cell, ExcelField, JSONObject, CellStyle> titleCellStyle, TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle) {
        Workbook workBook = createWorkBook(sheetName);
        Sheet sheet = workBook.getSheet(sheetName);
        createTitleRow(workBook, sheet, exportTitleList, startIndex - 1,titleCellStyle);
        //增加大批量数据分sheet导出 zry 2022-11-18 13:58:12
        int maxSize = maxRows - startIndex;//表头有一行需要减1
        //如果maxRows为-1，则不分sheet
        if (maxRows == -1) {
            maxSize = list.size();
        }
        if (list.size() > maxSize) {
            workBook.removeSheetAt(0);
            PageUtil.setOneAsFirstPageNo();
            int totalPage = PageUtil.totalPage(list.size(), maxSize);
            for (int i = 1; i <= totalPage; i++) {
                int[] trans = PageUtil.transToStartEnd(i, maxSize);
                List<Map<String, Object>> subList = list.subList(trans[0], Math.min(trans[1], list.size()));
                Sheet sheetIndex = workBook.createSheet(sheetName + "_" + i);
                createTitleRow(workBook, sheetIndex, exportTitleList, startIndex - 1,titleCellStyle);
                createDataRowMap(workBook, sheetIndex, exportTitleList, subList, startIndex, true,dataCellStyle);
            }

        } else {
            createDataRowMap(workBook, sheet, exportTitleList, list, startIndex, true,dataCellStyle);
        }

        return workBook;
    }

    /**
     * 导出excel
     *
     * @param sheet     sheet
     * @param rowIndex  行号 从0开始
     * @param cellIndex 列号 从0开始
     * @param value     值
     */
    public static void setCellValue(Sheet sheet, int rowIndex, int cellIndex, Object value) {
        setCellValue(sheet, rowIndex, cellIndex, value, getLeftStyle(sheet.getWorkbook()));
    }

    /**
     * 设置单元格值
     *
     * @param sheet     sheet
     * @param rowIndex  行号 从0开始
     * @param cellIndex 列号 从0开始
     * @param value     值
     * @param cellStyle 样式
     */
    public static void setCellValue(Sheet sheet, int rowIndex, int cellIndex, Object value, CellStyle cellStyle) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            cell = row.createCell(cellIndex);
        }
        cell.setCellValue(Convert.toStr(value));
        cell.setCellStyle(cellStyle);
    }

    /**
     * 合并单元格
     *
     * @param sheet    sheet
     * @param firstRow 开始行 从0开始
     * @param lastRow  结束行 从0开始
     * @param firstCol 开始列 从0开始
     * @param lastCol  结束列 从0开始
     */
    public static void addMergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress mergedRegion = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(mergedRegion);
    }

    /**
     * 合并单元格
     *
     * @param sheet    sheet
     * @param row      行号 从0开始
     * @param firstCol 开始列 从0开始
     * @param lastCol  结束列 从0开始
     */
    public static void addMergedRegion(Sheet sheet, int row, int firstCol, int lastCol) {
        addMergedRegion(sheet, row, row, firstCol, lastCol);
    }
}
