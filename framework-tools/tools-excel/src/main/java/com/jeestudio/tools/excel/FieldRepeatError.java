package com.jeestudio.tools.excel;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 字段重复错误信息
 */
public class FieldRepeatError {
    public String fieldTitle;
    public String repeatValue;
    public List<Integer> lineNumbers=new ArrayList<>();
    public FieldRepeatError(String fieldTitle,String repeatValue,int firstLine,int nowLine){
        setFieldTitle(fieldTitle);
        setRepeatValue(repeatValue);
        addLineNumber(firstLine);
        addLineNumber(nowLine);
    }
    public void addLineNumber(int lineNumber){
        this.lineNumbers.add(lineNumber);
    }
    @Override
    public String toString(){
        return String.format("第%s行，%s出现重复值 %s ", StringUtils.join(lineNumbers.toArray(),"、"),fieldTitle,repeatValue);
    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    public String getRepeatValue() {
        return repeatValue;
    }

    public void setRepeatValue(String repeatValue) {
        this.repeatValue = repeatValue;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public void setLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers = lineNumbers;
    }
}
