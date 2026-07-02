package com.jeestudio.tools.excel;


import com.jeestudio.tools.base.annotation.Dict;
import com.jeestudio.tools.base.annotation.Excel;
import com.jeestudio.tools.base.enums.AlignEnum;
import com.jeestudio.tools.base.enums.ImportValidEnum;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: Excel字段元数据
 */
public class ExcelField {

    private String fieldValue;
    private String fieldTitle;
    @Deprecated
    private Excel excel;

    AlignEnum alignEnum = AlignEnum.CENTER;

    private boolean skipImportValid;
    private ImportValidEnum[] importValidEnums;

    private LinkedHashMap<Object, Object> dataMap;
    @Deprecated
    private Dict dict;
    /**
     * 字典是否多选
     */
    private boolean dictMultiple;

    /**
     * 字典分隔符 默认,
     */
    private String dictSeparator = ",";
    @Deprecated
    private Field field;

    private boolean date;

    private Class defineClass;//该字段所属的class
    private boolean collection;//是否为下级数据字段
    private boolean onlyForExcelTitle;//是否仅用于excel表头
    private List<ExcelField> children = new ArrayList<>();
    private boolean dictSingleSheet;//字典单独放一个sheet

    public ExcelField() {
    }

    public ExcelField(String fieldValue, String fieldTitle) {
        this.fieldValue = fieldValue;
        this.fieldTitle = fieldTitle;
    }

    public ExcelField(String fieldValue, Excel excel, LinkedHashMap<Object, Object> dataMap, Dict dict, Field field) {
        this.fieldValue = fieldValue;
        this.excel = excel;
        this.dataMap = dataMap;
        this.fieldTitle = excel.title();
        this.dict = dict;
        this.field = field;
    }

    public ExcelField(List<ExcelField> children) {
        this.collection = true;
        this.children = children;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    @Deprecated
    public Excel getExcel() {
        return excel;
    }

    @Deprecated
    public void setExcel(Excel excel) {
        this.excel = excel;
    }

    public AlignEnum getAlignEnum() {
        return alignEnum;
    }

    public void setAlignEnum(AlignEnum alignEnum) {
        this.alignEnum = alignEnum;
    }

    public boolean isSkipImportValid() {
        return skipImportValid;
    }

    public void setSkipImportValid(boolean skipImportValid) {
        this.skipImportValid = skipImportValid;
    }

    public ImportValidEnum[] getImportValidEnums() {
        return importValidEnums;
    }

    public void setImportValidEnums(ImportValidEnum[] importValidEnums) {
        this.importValidEnums = importValidEnums;
    }

    public LinkedHashMap<Object, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(LinkedHashMap<Object, Object> dataMap) {
        this.dataMap = dataMap;
    }

    @Deprecated
    public Field getField() {
        return field;
    }

    @Deprecated
    public void setField(Field field) {
        this.field = field;
    }


    public boolean isDate() {
        return date;
    }

    public void setDate(boolean date) {
        this.date = date;
    }

    public boolean isDictMultiple() {
        return dictMultiple;
    }

    public void setDictMultiple(boolean dictMultiple) {
        this.dictMultiple = dictMultiple;
    }

    public String getDictSeparator() {
        return dictSeparator;
    }

    public void setDictSeparator(String dictSeparator) {
        this.dictSeparator = dictSeparator;
    }

    public Class getDefineClass() {
        return defineClass;
    }

    public void setDefineClass(Class defineClass) {
        this.defineClass = defineClass;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public List<ExcelField> getChildren() {
        return children;
    }

    public void setChildren(List<ExcelField> children) {
        this.children = children;
    }

    public boolean isOnlyForExcelTitle() {
        return onlyForExcelTitle;
    }

    public void setOnlyForExcelTitle(boolean onlyForExcelTitle) {
        this.onlyForExcelTitle = onlyForExcelTitle;
    }

    public boolean isDictSingleSheet() {
        return dictSingleSheet;
    }

    public void setDictSingleSheet(boolean dictSingleSheet) {
        this.dictSingleSheet = dictSingleSheet;
    }
}
