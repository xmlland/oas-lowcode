package com.jeestudio.tools.excel;


import java.util.ArrayList;
import java.util.List;

/**
 * @Description: Excel导入结果
 */
public class ExcelImportResult<T> {
    private Boolean result;
    private List<String> errorMessages=new ArrayList<>();
    private List<T> data;
    public ExcelImportResult(boolean result){
        this.setResult(result);
    }
    public void addErrorString(String error){
        this.errorMessages.add(error);
    }
    public void addErrorStrings(List<String> errors){
        this.errorMessages.addAll(errors);
    }


    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
