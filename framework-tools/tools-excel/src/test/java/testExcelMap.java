import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.excel.ExcelExportUtil;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.excel.ExcelImportUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 2023年03月10日 15:34:00
 *
 * @author U-002
 */
public class testExcelMap {

    public static void main(String[] args) throws IOException {
        Workbook workbook = test1();
//        Workbook workbook = test2();

        workbook.write(Files.newOutputStream(Paths.get("D:/testImportExcel2.xlsx")));
    }

    public static Workbook test1() {

        List<ExcelField> fields = new ArrayList<>();
        fields.add(new ExcelField("code", "编码"));
        fields.add(new ExcelField("name", "名称"));
        ExcelField excelField = new ExcelField("type", "类型");
        LinkedHashMap<Object, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("1", "类型1");
        dataMap.put("2", "类型2");
        excelField.setDataMap(dataMap);
        fields.add(excelField);
        ExcelField excelField3 = new ExcelField("child1", "子表1");
        List<ExcelField> children = new ArrayList<>();
        children.add(new ExcelField("child_filed1", "子项1"));
        children.add(new ExcelField("child_filed2", "子项2"));
        ExcelField excelField31 = new ExcelField("child1_child1", "子表的子表1");
        List<ExcelField> children2 = new ArrayList<>();
        children2.add(new ExcelField("child1_child1_filed1", "子表的子表子项1"));
        children2.add(new ExcelField("child1_child1_filed2", "子表的子表子项2"));
        excelField31.setCollection(true);
        excelField31.setChildren(children2);
        children.add(excelField31);
        excelField3.setCollection(true);
        excelField3.setChildren(children);
        fields.add(excelField3);

        Workbook workbook = ExcelImportUtil.exportTemplate(fields);
        return workbook;
    }

    public static Workbook test2() {
        JSONArray jsonArray = JSON.parseArray("[{\"fieldTitle\":\"2024年03月05日(时间)\",\"fieldValue\":\"time\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(雨花区环保局)\",\"fieldValue\":\"4014301020012\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(高开区环保局)\",\"fieldValue\":\"4014301020004\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(湖南中医药大学)\",\"fieldValue\":\"4014301020005\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(火车新站)\",\"fieldValue\":\"4014301020003\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(马坡岭)\",\"fieldValue\":\"4014301020011\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(伍家岭)\",\"fieldValue\":\"4014301020007\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(天心区环保局)\",\"fieldValue\":\"4014301020008\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(经开区环保局)\",\"fieldValue\":\"4014301020009\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(湖南师范大学)\",\"fieldValue\":\"4014301020006\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(沙坪)\",\"fieldValue\":\"430100011\",\"alignEnum\":\"center\"},{\"fieldTitle\":\"PM₂.₅余量统计(市平均)\",\"fieldValue\":\"city_act\",\"alignEnum\":\"center\"}]");
        List<Map> list = JSON.parseArray("[{\"4014301020012\":\"23\",\"4014301020004\":\"20\",\"4014301020003\":\"14\",\"4014301020006\":\"36\",\"city_act\":\"20\",\"4014301020005\":\"24\",\"430100011\":\"11\",\"4014301020008\":\"17\",\"4014301020007\":\"23\",\"4014301020009\":\"22\",\"time\":\"1\",\"4014301020011\":\"21\"},{\"4014301020012\":\"23\",\"4014301020004\":\"20\",\"4014301020003\":\"19\",\"4014301020006\":\"24\",\"city_act\":\"20\",\"4014301020005\":\"23\",\"430100011\":\"10\",\"4014301020008\":\"20\",\"4014301020007\":\"24\",\"4014301020009\":\"19\",\"time\":\"2\",\"4014301020011\":\"19\"},{\"4014301020012\":\"21\",\"4014301020004\":\"19\",\"4014301020003\":\"17\",\"4014301020006\":\"22\",\"city_act\":\"19\",\"4014301020005\":\"22\",\"430100011\":\"8\",\"4014301020008\":\"20\",\"4014301020007\":\"22\",\"4014301020009\":\"17\",\"time\":\"3\",\"4014301020011\":\"18\"},{\"4014301020012\":\"20\",\"4014301020004\":\"16\",\"4014301020003\":\"9\",\"4014301020006\":\"14\",\"city_act\":\"14\",\"4014301020005\":\"18\",\"430100011\":\"8\",\"4014301020008\":\"16\",\"4014301020007\":\"13\",\"4014301020009\":\"16\",\"time\":\"4\",\"4014301020011\":\"15\"},{\"4014301020012\":\"18\",\"4014301020004\":\"16\",\"4014301020003\":\"12\",\"4014301020006\":\"12\",\"city_act\":\"13\",\"4014301020005\":\"17\",\"430100011\":\"6\",\"4014301020008\":\"12\",\"4014301020007\":\"13\",\"4014301020009\":\"12\",\"time\":\"5\",\"4014301020011\":\"10\"},{\"4014301020012\":\"17\",\"4014301020004\":\"16\",\"4014301020003\":\"14\",\"4014301020006\":\"16\",\"city_act\":\"14\",\"4014301020005\":\"18\",\"430100011\":\"7\",\"4014301020008\":\"13\",\"4014301020007\":\"15\",\"4014301020009\":\"12\",\"time\":\"6\",\"4014301020011\":\"11\"},{\"4014301020012\":\"17\",\"4014301020004\":\"18\",\"4014301020003\":\"17\",\"4014301020006\":\"19\",\"city_act\":\"16\",\"4014301020005\":\"20\",\"430100011\":\"11\",\"4014301020008\":\"16\",\"4014301020007\":\"17\",\"4014301020009\":\"13\",\"time\":\"7\",\"4014301020011\":\"13\"},{\"4014301020012\":\"18\",\"4014301020004\":\"23\",\"4014301020003\":\"17\",\"4014301020006\":\"28\",\"city_act\":\"20\",\"4014301020005\":\"22\",\"430100011\":\"13\",\"4014301020008\":\"22\",\"4014301020007\":\"26\",\"4014301020009\":\"13\",\"time\":\"8\",\"4014301020011\":\"14\"},{\"4014301020012\":\"21\",\"4014301020004\":\"29\",\"4014301020003\":\"29\",\"4014301020006\":\"46\",\"city_act\":\"30\",\"4014301020005\":\"30\",\"430100011\":\"25\",\"4014301020008\":\"32\",\"4014301020007\":\"44\",\"4014301020009\":\"19\",\"time\":\"9\",\"4014301020011\":\"23\"},{\"4014301020012\":\"26\",\"4014301020004\":\"37\",\"4014301020003\":\"46\",\"4014301020006\":\"61\",\"city_act\":\"42\",\"4014301020005\":\"39\",\"430100011\":\"37\",\"4014301020008\":\"45\",\"4014301020007\":\"57\",\"4014301020009\":\"34\",\"time\":\"10\",\"4014301020011\":\"36\"},{\"4014301020012\":\"36\",\"4014301020004\":\"48\",\"4014301020003\":\"68\",\"4014301020006\":\"85\",\"city_act\":\"58\",\"4014301020005\":\"53\",\"430100011\":\"58\",\"4014301020008\":\"60\",\"4014301020007\":\"84\",\"4014301020009\":\"46\",\"time\":\"11\",\"4014301020011\":\"46\"},{\"4014301020012\":\"46\",\"4014301020004\":\"59\",\"4014301020003\":\"78\",\"4014301020006\":\"98\",\"city_act\":\"71\",\"4014301020005\":\"66\",\"430100011\":\"67\",\"4014301020008\":\"74\",\"4014301020007\":\"97\",\"4014301020009\":\"73\",\"time\":\"12\",\"4014301020011\":\"54\"},{\"4014301020012\":\"57\",\"4014301020004\":\"71\",\"4014301020003\":\"90\",\"4014301020006\":\"115\",\"city_act\":\"80\",\"4014301020005\":\"79\",\"430100011\":\"70\",\"4014301020008\":\"83\",\"4014301020007\":\"103\",\"4014301020009\":\"82\",\"time\":\"13\",\"4014301020011\":\"55\"},{\"time\":\"14\"},{\"time\":\"15\"},{\"time\":\"16\"},{\"time\":\"17\"},{\"time\":\"18\"},{\"time\":\"19\"},{\"time\":\"20\"},{\"time\":\"21\"},{\"time\":\"22\"},{\"time\":\"23\"},{\"time\":\"24\"},{\"4014301020012\":\"26\",\"4014301020004\":\"30\",\"4014301020003\":\"33\",\"4014301020006\":\"43\",\"city_act\":\"32\",\"4014301020005\":\"33\",\"4014301020008\":\"33\",\"430100011\":\"25\",\"4014301020007\":\"41\",\"4014301020009\":\"29\",\"time\":\"累计浓度\",\"4014301020011\":\"26\"},{\"4014301020012\":\"509\",\"4014301020004\":\"460\",\"4014301020003\":\"422\",\"4014301020006\":\"289\",\"city_act\":\"435\",\"4014301020005\":\"421\",\"4014301020008\":\"422\",\"430100011\":\"521\",\"4014301020007\":\"314\",\"4014301020009\":\"474\",\"time\":\"保优余量\",\"4014301020011\":\"517\"},{\"4014301020012\":\"46.3\",\"4014301020004\":\"41.8\",\"4014301020003\":\"38.4\",\"4014301020006\":\"26.3\",\"city_act\":\"39.5\",\"4014301020005\":\"38.3\",\"4014301020008\":\"38.4\",\"430100011\":\"47.4\",\"4014301020007\":\"28.5\",\"4014301020009\":\"43.1\",\"time\":\"保优浓度均值\",\"4014301020011\":\"47\"},{\"4014301020012\":\"11\",\"4014301020004\":\"11\",\"4014301020003\":\"11\",\"4014301020006\":\"11\",\"city_act\":\"11\",\"4014301020005\":\"11\",\"4014301020008\":\"11\",\"430100011\":\"11\",\"4014301020007\":\"11\",\"4014301020009\":\"11\",\"time\":\"保优时间\",\"4014301020011\":\"11\"},{\"4014301020012\":\"2\",\"4014301020004\":\"5\",\"4014301020003\":\"6\",\"4014301020006\":\"10\",\"4014301020005\":\"6\",\"4014301020008\":\"6\",\"430100011\":\"1\",\"4014301020007\":\"9\",\"4014301020009\":\"4\",\"time\":\"排名\",\"4014301020011\":\"2\"}]", Map.class);

        List<ExcelField> excelExportFieldList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            ExcelField excelField = new ExcelField();
            excelField.setFieldValue((String) jsonArray.getJSONObject(i).get("fieldValue"));
            excelField.setFieldTitle((String) jsonArray.getJSONObject(i).get("fieldTitle"));
            excelExportFieldList.add(excelField);
        }

        List<Map<String, Object>> listResult = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listResult.add( list.get(i) );
        }

        // 创建样式生成器函数
        TriFunction<Cell,ExcelField, JSONObject, CellStyle> titleCellStyle = (Cell cell, ExcelField value, JSONObject data) -> {

            System.out.println("设置标题样式字段："+value.getFieldTitle());
            CellStyle style = cell.getCellStyle();

//            if ( "2024年03月05日(时间)".equals( value.getFieldTitle() )){
//                style.setFillForegroundColor( IndexedColors.RED.getIndex());
//                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//            }

            return style;
        };
        TriFunction<Cell,ExcelField, JSONObject, CellStyle> dataCellStyle = (Cell cell, ExcelField value, JSONObject data) -> {

            CellStyle style = cell.getCellStyle();

            StringBuilder text = new StringBuilder("设置数据样式字段："+value.getFieldTitle()+" 字段数据是：");
            switch (cell.getCellType()) {
                case STRING:
                    text.append(cell.getStringCellValue());
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        text.append(cell.getDateCellValue());
                    } else {
                        text.append(cell.getNumericCellValue());
                    }
                    break;
                case BOOLEAN:
                    text.append(cell.getBooleanCellValue());
                    break;
                case FORMULA:
                    text.append(cell.getCellFormula());
                    break;
                default:
            }
            System.out.println(text.toString());

            if ( !"time".equals( value.getFieldValue() ) && !"xh".equals( value.getFieldValue() ) && !"排名".equals( data.getString( "time" ) ) ){
                if ( StringUtils.isNotEmpty( data.getString( value.getFieldValue() ) )){
                    double val = Double.parseDouble( data.getString( value.getFieldValue() ) );
                    if ( val > 35.0  ){
                        style.setFillForegroundColor( IndexedColors.YELLOW.getIndex() );
                    }else{
                        style.setFillForegroundColor( IndexedColors.GREEN.getIndex() );
                    }
                }
            }
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            return style;
        };

        Workbook workbook = ExcelExportUtil.export("Sheet1",
                excelExportFieldList,
                listResult,
                titleCellStyle,
                dataCellStyle);
        return  workbook;
    }
}
