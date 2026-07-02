package com.jeestudio.tools.web.page;

import java.io.Serializable;

/**
 * @Description: 分页请求
 */
public class PageRequest implements Serializable{

    private static final long serialVersionUID = -9100048808668856577L;
    private Integer page;
    private Integer rows;

    private String sortByField;
    private String sortByOrder;

    public PageRequest() {
    }

    public PageRequest(Integer page, Integer rows) {
        this.page = page;
        this.rows = rows;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public String getSortByField() {
        return sortByField;
    }

    public void setSortByField(String sortByField) {
        this.sortByField = sortByField;
    }

    public String getSortByOrder() {
        return sortByOrder;
    }

    public void setSortByOrder(String sortByOrder) {
        this.sortByOrder = sortByOrder;
    }
}
