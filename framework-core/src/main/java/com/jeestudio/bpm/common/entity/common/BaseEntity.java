package com.jeestudio.bpm.common.entity.common;

import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.utils.StringUtil;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import jakarta.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Description: 基础实体类
 */
public abstract class BaseEntity<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 删除标记：正常。 */
    public static final String DEL_FLAG_NORMAL = "0";
    /** 删除标记：已删除。 */
    public static final String DEL_FLAG_DELETE = "1";
    /** 删除标记：审核状态，保留用于兼容旧业务。 */
    public static final String DEL_FLAG_AUDIT = "2";

    /** 预设主键，插入前可由调用方指定。 */
    protected String preId;
    /** 预设创建人，插入前可由调用方指定。 */
    protected User preCreateBy;
    /** 预设创建时间，插入前可由调用方指定。 */
    protected Date preCreateDate;
    /** 预设更新人，更新前可由调用方指定。 */
    protected User preUpdateBy;
    /** 预设更新时间，更新前可由调用方指定。 */
    protected Date preUpdateDate;
    /** 实体主键。 */
    protected String id;
    /** 当前操作用户。 */
    protected User currentUser;
    /** 分页对象，列表查询时由调用方设置。 */
    protected Page<T> page;

    /** 自定义 SQL 片段集合，供 Mapper 动态 SQL 使用。 */
    protected Map<String, String> sqlMap;

    /** 是否新记录；为 true 时根据主键判断新增或更新。 */
    protected boolean isNewRecord = true;

    public BaseEntity() {

    }

    public BaseEntity(String id) {
        this();
        this.id = id;
    }

    public String getPreId() {
        return preId;
    }

    public void setPreId(String preId) {
        this.preId = preId;
    }

    public String getId() {
        return id;
    }

    public User getPreCreateBy() {
        return preCreateBy;
    }

    public void setPreCreateBy(User preCreateBy) {
        this.preCreateBy = preCreateBy;
    }

    public Date getPreCreateDate() {
        return preCreateDate;
    }

    public void setPreCreateDate(Date preCreateDate) {
        this.preCreateDate = preCreateDate;
    }

    public User getPreUpdateBy() {
        return preUpdateBy;
    }

    public void setPreUpdateBy(User preUpdateBy) {
        this.preUpdateBy = preUpdateBy;
    }

    public Date getPreUpdateDate() {
        return preUpdateDate;
    }

    public void setPreUpdateDate(Date preUpdateDate) {
        this.preUpdateDate = preUpdateDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCurrentUser() {
        if(currentUser == null){
        }
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    @Transient
    public Page<T> getPage() {
        if (page == null){
            page = new Page<T>();
        }
        return page;
    }

    public Page<T> setPage(Page<T> page) {
        this.page = page;
        return page;
    }

    public Map<String, String> getSqlMap() {
        if (sqlMap == null){
            sqlMap = Maps.newHashMap();
        }
        return sqlMap;
    }

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }

    /** 插入前回调，由子类实现审计字段、主键等初始化逻辑。 */
    public abstract void preInsert();

    /** 更新前回调，由子类实现更新时间、更新人等维护逻辑。 */
    public abstract void preUpdate();

    /** 判断当前对象是否新记录。 */
    public boolean getIsNewRecord() {
        return this.isNewRecord ? StringUtil.isBlank(getId()) : false;
    }

    /** 设置是否强制按新记录处理。 */
    public void setIsNewRecord(boolean isNewRecord) {
        this.isNewRecord = isNewRecord;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        BaseEntity<?> that = (BaseEntity<?>) obj;
        return null == this.getId() ? false : this.getId().equals(that.getId());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
