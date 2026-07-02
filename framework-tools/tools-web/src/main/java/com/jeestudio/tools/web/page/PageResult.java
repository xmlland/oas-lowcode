package com.jeestudio.tools.web.page;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.tools.dict.DictHandler;
import com.jeestudio.tools.dict.util.DictTranslateUtil;
import com.jeestudio.tools.web.ajax.AjaxResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 分页返回结果
 */
public class PageResult<T> extends AjaxResult implements Serializable {

    private static final long serialVersionUID = 2901064538462451006L;
    private int statusCode;
    private List<T> rows;
    private Long total;
    private String title;

    public PageResult() {
        super.setResult(true);
        super.setMsg("获取成功");
        this.title = "消息";
        this.statusCode = 200;
        this.put("statusCode", statusCode);
        this.put("rows", new ArrayList<>());
        this.put("total", 0);
        this.put("title", this.title);
    }

    public PageResult(Class<T> entityClass, JSONObject datagrid) {
        JSONArray rows = datagrid.getJSONArray("rows");
        long total = datagrid.getLongValue("total");
        super.setResult(true);
        this.statusCode = 200;
        super.setMsg("获取成功");
        this.title = "消息";
        this.rows = JSONArray.parseArray(rows.toJSONString(), entityClass);
        this.total = total;
        this.put("statusCode", statusCode);
        this.put("rows", rows);
        this.put("total", total);
        this.put("title", this.title);
    }

    public PageResult(List<T> rows, Long total) {
        super.setResult(true);
        this.statusCode = 200;
        super.setMsg("获取成功");
        this.title = "消息";
        this.rows = rows;
        this.total = total;
        this.put("statusCode", statusCode);
        this.put("rows", rows);
        this.put("total", total);
        this.put("title", this.title);
    }

    public void translateDict(DictHandler dictHandler) {
        JSONArray array = DictTranslateUtil.translateDict(dictHandler, this.rows);
        this.put("rows", array);
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
