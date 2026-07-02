package com.jeestudio.tools.web.ajax;

import cn.hutool.core.convert.Convert;

import java.util.HashMap;

/**
 * @Description: 统一响应结果
 */
public class AjaxResult extends HashMap<String, Object> {

    public final static String statusName = "result";
    public final static String messageName = "msg";
    public final static String errCodeName = "errCode";


    public static AjaxResult getInstance() {
        return new AjaxResult();
    }

    public static AjaxResult success() {
        AjaxResult instance = AjaxResult.getInstance();
        instance.put(AjaxResult.statusName, true);
        return instance;
    }

    public static AjaxResult success(Object data) {
        AjaxResult success = success();
        success.put(AjaxResult.statusName, true);
        success.setData(data);
        return success;
    }

    public static AjaxResult error() {
        AjaxResult instance = AjaxResult.getInstance();
        instance.put(AjaxResult.statusName, false);
        return instance;
    }

    public static AjaxResult error(String msg) {
        AjaxResult error = error();
        error.put(AjaxResult.messageName, msg);
        return error;
    }

    public static AjaxResult error(Object errCode, String msg) {

        AjaxResult error = error();
        error.put(AjaxResult.messageName, msg);
        error.put(AjaxResult.errCodeName, errCode);
        return error;
    }

    public AjaxResult setData(String key, Object data) {
        super.put(key, data);
        return this;
    }

    public AjaxResult setData(Object data) {
        this.put("data", data);
        return this;
    }

    public String getMsg() {
        return Convert.toStr(this.get(messageName));
    }

    public boolean getResult() {
        return Convert.toBool(this.get(statusName));
    }


    public void setMsg(String msg) {
        this.setData(messageName, msg);
    }

    public void setResult(boolean result) {
        this.setData(statusName, result);
    }

    public void setErrCode(int errCode) {
        this.setData(errCodeName, errCode);
    }
}
