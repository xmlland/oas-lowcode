package com.jeestudio.bpm.common.param;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 分页参数
 */
public class PageParam {

    private static final long serialVersionUID = 1L;

    /** 当前页码，从 1 开始。 */
    private int pageNo = 1;
    /** 每页条数。 */
    private int pageSize = 10;
    /** 排序表达式。 */
    private String orderBy = "";
    /** 查询参数 Map。 */
    private Map<String, String> paramMap;
    /** 起始偏移量，部分接口直接使用 offset 分页时设置。 */
    private int fromIndex = -1;
    /** 扩展 SQL 片段键值对，动态表单特殊查询时使用。 */
    private HashMap<String, String> extSqlPairMap;

    public PageParam() {
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public HashMap<String, String> getExtSqlPairMap() {
        return extSqlPairMap;
    }

    public void setExtSqlPairMap(HashMap<String, String> extSqlPairMap) {
        this.extSqlPairMap = extSqlPairMap;
    }
}
