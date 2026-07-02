package com.jeestudio.bpm.common.entity.common;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.excel.ExcelField;
import lombok.Data;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * @Description: 导出扩展实体
 */
@Data
public class ExportExtraEntity {

    /** 数据单元格样式回调，可根据单元格、字段注解和行数据动态返回样式。 */
    private TriFunction<Cell, ExcelField, JSONObject, CellStyle> dataCellStyle;
}
