package com.jeestudio.tools.excel;


import com.jeestudio.tools.base.exceptions.BusinessException;

/**
 * @Description: Excel导入异常
 */
public class ExcelImportException extends BusinessException {
    public ExcelImportException(String message) {
        super(message);
    }
}
