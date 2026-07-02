import cn.hutool.core.io.FileUtil;
import com.jeestudio.tools.excel.ExcelField;
import com.jeestudio.tools.excel.ExcelImportUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 2023年03月10日 16:07:00
 *
 * @author U-002
 */
public class testExcelImportMap {
    public static void main(String[] args) {
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

        List<Map<String, Object>> mapList = ExcelImportUtil.importData("sheet1", fields, FileUtil.getInputStream("D:/testImportExcel2.xlsx"), "testImportExcel2.xlsx", new ArrayList<>());
        for (Map<String, Object> map : mapList) {
            System.out.println(map);
        }

    }
}
