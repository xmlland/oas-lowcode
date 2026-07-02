package com.jeestudio.bpm.common.entity.gen;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 代码生成子表
 */
public class GenTableChildren implements Serializable {

    /** 子表配置 ID。 */
    private String id;
    /** 前端渲染子表时使用的唯一键。 */
    private String key;

    /** 子表排序值。 */
    private String tableSort;
    /** 子表字段视图列表。 */
    private List<GenTableColumnView> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public List<GenTableColumnView> getData() {
        return data;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setData(List<GenTableColumnView> data) {
        this.data = data;
    }

    public String getTableSort() {
        return tableSort;
    }

    public void setTableSort(String tableSort) {
        this.tableSort = tableSort;
    }
}
