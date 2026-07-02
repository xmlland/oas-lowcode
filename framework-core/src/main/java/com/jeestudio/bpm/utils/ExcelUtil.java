package com.jeestudio.bpm.utils;

import java.io.*;
import java.util.*;

import com.jeestudio.tools.base.exceptions.BusinessException;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: Excel工具
 */
public class ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    public static byte[] createExcelNoMerge(String[] title,String[] titleCH,  Map<String, List<Map<String, Object>>> maps){

        HSSFWorkbook wb = new HSSFWorkbook();

        HSSFFont fontData = wb.createFont();
        fontData.setFontName("宋体");
        fontData.setFontHeightInPoints((short) 11);
        CellStyle style = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        style.setFont(font);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        if (title.length==0){
            return null;
        }
        Sheet sheet = null;
        int n = 0;
        for(Map.Entry<String, List<Map<String, Object>>> entry : maps.entrySet()){
            try {
                sheet = wb.createSheet();
                wb.setSheetName(n, entry.getKey());
                wb.setSelectedTab(0);
            }catch (Exception e){
                logger.warn("创建Excel工作表失败, sheetName: {}", entry.getKey(), e);
            }

            Row row0 = sheet.createRow(0);
            for(int i = 0; i<titleCH.length; i++){
                Cell cell_1 = row0.createCell(i);
                cell_1.setCellStyle(style);
                cell_1.setCellValue(titleCH[i]);
            }

            List<Map<String, Object>> list = entry.getValue();
            if(null!=wb){
                Iterator iterator = list.iterator();
                int index = 1;
                while (iterator.hasNext()){
                    Row row = sheet.createRow(index);
                    Map<String, String> map = (Map<String, String>)iterator.next();
                    for(int i = 0; i<title.length; i++){
                        Cell cell = row.createCell(i);
                        cell.setCellValue(String.valueOf(map.get(title[i])));
                        cell.setCellStyle(style);
                    }
                    index++;
                }
            }
            n++;
        }
        sheet.setVerticallyCenter(true);
        for ( int i = 0; i < title.length; i++) {
            sheet.setColumnWidth(i, 4000);
        }
        sheet.setVerticallyCenter(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
        } catch (IOException e) {
            logger.error("Excel写入输出流失败", e);
        }
        return os.toByteArray();
    }


    /**
     * Write data list to excel
     */
    public static ByteArrayInputStream LinkedHashListToExcel(List<List<LinkedHashMap<String, String>>> dataResult,
                                                             int dataRowNumber,
                                                             String templateFilePath) throws IOException {
        //Insert/update existing file
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            FileInputStream file = new FileInputStream(new File(templateFilePath));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            //start inserting data from second row
            int rowIdx = dataRowNumber;
            for (List<LinkedHashMap<String, String>> rowResult : dataResult) {
                Row row = sheet.createRow(rowIdx++);
                int cellIndex = 0;
                for(LinkedHashMap<String, String> linkedHM : rowResult)
                {
                    for (Map.Entry<String, String> entry : linkedHM.entrySet()) {
                        XSSFRichTextString hssfRichTextString= dealSubSup(workbook, entry.getValue());
                        row.createCell(cellIndex).setCellValue(hssfRichTextString);
                        cellIndex++;
                    }
                }

            }
            file.close();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (FileNotFoundException e) {
            throw new BusinessException("导出Excel文件失败");
        }
    }

    public static final String SUB_START="<sub>";
    public static final String SUB_END="</sub>";
    public static final String SUP_START="<sup>";
    public static final String SUP_END="</sup>";
    /**
     * 判断是不是包含sub，sup标签
     * @param s
     * @return
     */
    public static boolean containSubSup(String s) {
        return (s.contains(SUB_START) && s.contains(SUB_END)) || (s.contains(SUP_START) && s.contains(SUP_END));
    }

    public static String getSubSupIndexs(String text,List<List<int[]>>tagIndexList){
        List<int[]> subs=new ArrayList<>();
        List<int[]> sups=new ArrayList<>();
        while (true){
            int[] sub_pair = getNextSubsTagsIndex(text, SUB_START);
            int[] sup_pair = getNextSubsTagsIndex(text, SUP_START);
            boolean subFirst = true;
            boolean supFirst = true;
            if(sub_pair != null && sup_pair != null) {
                //两种标签都存在的时候要考虑到谁在前，在前的标签优先处理
                //因为如果在后的标签处理完，index就定下来，再处理在前的，后面的index就会产生偏移量。从前开始处理不会存在这个问题
                if(sub_pair[0] < sup_pair[0]) {
                    supFirst = false;
                } else {
                    subFirst = false;
                }
            }
            if (sub_pair != null && subFirst) {
                text = removeNextSubTags(text, SUB_START);
                //<sub>标签被去掉之后，结束标签需要相应往前移动
                sub_pair[1] = sub_pair[1] - SUB_START.length();
                subs.add(sub_pair);
                continue;
            }
            if (sup_pair != null && supFirst) {
                text = removeNextSubTags(text, SUP_START);
                //<sup>标签被去掉之后，结束标签需要相应往前移动
                sup_pair[1] = sup_pair[1] - SUP_START.length();
                sups.add(sup_pair);
                continue;
            }
            if (sub_pair == null && sup_pair == null) {
                break;
            }
        }

        tagIndexList.add(subs);
        tagIndexList.add(sups);
        return text;
    }
    /**
     * 获取下一对标签的index，不存在这些标签就返回null
     * @param s
     * @param tag SUB_START 或者SUP_START
     * @return int[]中有两个元素，第一个是开始标签的index，第二个元素是结束标签的index
     */
    public static int[] getNextSubsTagsIndex(String s, String tag) {

        int firstSubStart = s.indexOf(tag);
        if (firstSubStart > -1) {
            int firstSubEnd = s.indexOf(tag.equals(SUB_START) ? SUB_END : SUP_END);
            if (firstSubEnd > firstSubStart) {
                return new int[] { firstSubStart, firstSubEnd };
            }
        }
        return null;
    }

    /**移除下一对sub或者sup标签，返回移除后的字符串
     * @param s
     * @param tag SUB_START 或者SUP_START
     * @return
     */
    public static String removeNextSubTags(String s, String tag) {
        s = s.replaceFirst(tag, "");
        s = s.replaceFirst(tag.equals(SUB_START) ? SUB_END : SUP_END, "");
        return s;
    }
    public static XSSFRichTextString dealSubSup(XSSFWorkbook workbook, String text){
        List<List<int[]>> tagIndexArr = null;
        if (containSubSup(text)) {
            tagIndexArr = new ArrayList<List<int[]>>();
            text = getSubSupIndexs(text, tagIndexArr);
        }
        if (tagIndexArr != null) {
            XSSFRichTextString title = new XSSFRichTextString(text);
            List<int[]> subs = tagIndexArr.get(0);
            List<int[]> sups = tagIndexArr.get(1);
            if (subs.size() > 0) {
                XSSFFont ft = workbook.createFont();
                ft.setTypeOffset(HSSFFont.SS_SUB);
                for (int[] pair : subs) {
                    title.applyFont(pair[0], pair[1], ft);
                }
            }
            if (sups.size() > 0) {
                XSSFFont ft = workbook.createFont();
                ft.setTypeOffset(HSSFFont.SS_SUPER);
                for (int[] pair : sups) {
                    title.applyFont(pair[0], pair[1], ft);
                }
            }
            return  title;
        } else {
            return  new XSSFRichTextString(text);
        }
    }

    /**
     * Read data list from excel
     */
    public static LinkedHashMap<String, List<LinkedHashMap<String, String>>> excelToLinkedHashList(InputStream is,
                                                                                                   int passRowNumber,
                                                                                                   String tableName,
                                                                                                   String parentTableName,
                                                                                                   String primaryKey,
                                                                                                   String columns)
    {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            if (parentTableName.equals("empty")) parentTableName = "";
            if (primaryKey.equals("empty")) primaryKey = "";
            String listKey = tableName+"," + columns + "," + parentTableName + "," + primaryKey;
            String[] columnArray = columns.split(",");
            int columnNumber = columnArray.length;
            ArrayList<LinkedHashMap<String, String>> dataResult = new ArrayList<LinkedHashMap<String, String>>();
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip rows
                if (rowNumber <= (passRowNumber - 1)) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> cellsInRow = currentRow.iterator();
                LinkedHashMap<String, String> rowResult = new LinkedHashMap<String, String>();
                int cellIndex=0;
                PrintStream out = new PrintStream(System.out, true, "UTF-8");
                while (cellsInRow.hasNext() && cellIndex < columnNumber) {
                    Cell currentCell = cellsInRow.next();
                    String cellStrValue = "";
                    switch(currentCell.getCellType())
                    {
                        case STRING:
                            cellStrValue = currentCell.getStringCellValue();
                            break;
                        case NUMERIC:
                            cellStrValue = String.valueOf(currentCell.getNumericCellValue());
                            if (cellStrValue != null && cellStrValue.endsWith(".0")){
                                cellStrValue = cellStrValue.substring(0, cellStrValue.length() - 2);
                            }
                            break;
                        case BOOLEAN:
                            cellStrValue = String.valueOf(currentCell.getBooleanCellValue());
                            break;
                        default:
                            cellStrValue = String.valueOf(currentRow.getCell(cellIndex));
                    }
                    rowResult.put(columnArray[cellIndex], cellStrValue);
                    cellIndex++;
                }
                dataResult.add(rowResult);
                rowNumber++;
            }
            workbook.close();
            LinkedHashMap<String, List<LinkedHashMap<String, String>>> finalReturnResult =
                    new LinkedHashMap<>();
            finalReturnResult.put(listKey, dataResult);
            return finalReturnResult;
        } catch (IOException e) {
            throw new RuntimeException("Fail to parse Excel file:" + e.getMessage());
        }
    }

    //解析Excel日期格式
    public static Date DoubleToDate(Double dVal) {
        Date tDate = new Date();
        long localOffset = tDate.getTimezoneOffset() * 60000; //系统时区偏移 1900/1/1 到 1970/1/1 的 25569 天
        tDate.setTime((long) ((dVal - 25569) * 24 * 3600 * 1000 + localOffset));
        return tDate;
    }
}
