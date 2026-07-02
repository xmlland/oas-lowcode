package com.jeestudio.bpm.common.param;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @Description: 列表选择参数
 */
public class GridselectParam {

    private static final long serialVersionUID = 1L;

    private String tableName;

    /**
     * 目标表名
     */
    private String targetTableName;
    /**
     * 目标字段
     */
    private String targetField;
    /**
     * 目标字段的关联字段,默认值id
     */
    private String targetJoinKey;

    private String searchKey;
    private String searchValue;
    private PageParam pageParam;
    @Deprecated
    private String dsfPlus;

    /**
     * 自定义查询条件
     */
    private List<FilterData> filterList;

    private List<FilterData> targetFilterList;

    private String currentUserName;

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getTargetJoinKey() {
        return targetJoinKey;
    }

    public void setTargetJoinKey(String targetJoinKey) {
        this.targetJoinKey = targetJoinKey;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public PageParam getPageParam() {
        return pageParam;
    }

    public void setPageParam(PageParam pageParam) {
        this.pageParam = pageParam;
    }

    @Deprecated
    public String getDsfPlus() {
        return dsfPlus;
    }
    @Deprecated
    public void setDsfPlus(String dsfPlus) {
        this.dsfPlus = dsfPlus;
    }

    public List<FilterData> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<FilterData> filterList) {
        this.filterList = filterList;
    }

    public List<FilterData> getTargetFilterList() {
        return targetFilterList;
    }

    public void setTargetFilterList(List<FilterData> targetFilterList) {
        this.targetFilterList = targetFilterList;
    }

    public HashMap<String, String> getExtSqlPairMap() {
        if (pageParam == null) {
            return null;
        } else {
            return pageParam.getExtSqlPairMap();
        }
    }

    public void setExtSqlPairMap(HashMap<String, String> extSqlPairMap) {
        if (pageParam == null) {
            pageParam = new PageParam();
        }
        this.pageParam.setExtSqlPairMap(extSqlPairMap);
    }

    public static class FilterData{
        private String key;
        private Object value;

        private Object value2;
        private String type;

        private boolean or;

        private boolean pinyin;
        private List<FilterData> children;

        public FilterData() {
        }

        public FilterData(String key, Object value, Object value2, String type) {
            this.key = key;
            this.value = value;
            this.value2 = value2;
            this.type = type;
        }

        public FilterData(String key, Object value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public FilterData(String key, Object value) {
            this.key = key;
            this.value = value;
            this.type = "eq";
        }

        public FilterData(FilterData [] children) {
            this.children = Arrays.asList(children);
        }

        public FilterData(String key, Object value, String type, boolean or) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.or = or;
        }

        public FilterData(String key, Object value, boolean or) {
            this.key = key;
            this.value = value;
            this.type = "eq";
            this.or = or;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue2() {
            return value2;
        }

        public void setValue2(Object value2) {
            this.value2 = value2;
        }

        public boolean isOr() {
            return or;
        }

        public void setOr(boolean or) {
            this.or = or;
        }

        public boolean isPinyin() {
            return pinyin;
        }

        public void setPinyin(boolean pinyin) {
            this.pinyin = pinyin;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<FilterData> getChildren() {
            return children;
        }

        public void setChildren(List<FilterData> children) {
            this.children = children;
        }

    }
}
