package com.jeestudio.bpm.common.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.utils.IdGen;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 数据实体基类
 */
public abstract class DataEntity<T> extends BaseEntity<T> {

    private static final long serialVersionUID = 1L;

    /** 备注信息。 */
    protected String remarks = "";
    /** 创建人。 */
    protected User createBy;
    /** 创建时间。 */
    protected Date createDate;
    /** 更新人。 */
    protected User updateBy;
    /** 更新时间。 */
    protected Date updateDate;
    /** 删除标记。 */
    protected String delFlag;

    /** 数据权限或查询规则参数。 */
    protected Map<String,String> rules = null;

    /** 视图标记，用于区分普通查询和特殊视图查询。 */
    protected String viewFlag;

    /** 前端分页和扩展查询参数。 */
    protected PageParam pageParam;

    public DataEntity() {
        super();
        this.delFlag = DEL_FLAG_NORMAL;
    }

    public DataEntity(String id) {
        super(id);
    }

    /** 插入前初始化主键、创建人和创建时间，需要业务保存入口显式调用。 */
    @Override
    public void preInsert(){
        // 未指定主键时使用 UUID；指定 preId 时使用外部主键。
        if (this.isNewRecord || this.id == null){
            if(StringUtil.isEmpty(getPreId())) {
                setId(IdGen.uuid());
            } else {
                setId(getPreId());
            }
        }
        if (this.preCreateBy != null) {
            this.createBy = this.preCreateBy;
        }
        if (this.preCreateDate != null) {
            this.createDate = this.preCreateDate;
        }
        // 插入时不写入 updateBy/updateDate，避免新增数据误显示为已更新。
        /*if (this.preUpdateBy != null) {
            this.updateBy = this.preUpdateBy;
        }
        if (this.preUpdateDate != null) {
            this.updateDate = this.preUpdateDate;
        }*/

        if (this.createDate == null) {
            // 插入时只维护 createDate。
            this.createDate =  new Date();
        } else {
            // 保留外部传入的 createDate。
        }
        if (this.createBy == null) {
            this.createBy = UserUtil.getCurrentUser();
        }
    }

    /** 更新前维护更新人和更新时间，需要业务保存入口显式调用。 */
    @Override
    public void preUpdate(){
        // 每次更新数据都刷新 updateDate 和 updateBy。
        //if (this.updateDate == null) {
            this.updateDate = new Date();
        //}
        //if (this.updateBy == null) {
            this.updateBy = UserUtil.getCurrentUser();
        //}
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public User getCreateBy() {
        return createBy;
    }

    public void setCreateBy(User createBy) {
        this.createBy = createBy;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @JsonIgnore
    public User getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(User updateBy) {
        this.updateBy = updateBy;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @JsonIgnore
    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Map<String,String> getRules() {
        if (rules == null) {
            rules = new HashMap<String,String>();
            if (this.getCreateBy() != null) rules.put("createBy", this.getCreateBy().getId());
        }
        return rules;
    }

    public void setRules(Map rules) {
        this.rules = rules;
    }

    public String getViewFlag() {
        return viewFlag;
    }

    public void setViewFlag(String viewFlag) {
        this.viewFlag = viewFlag;
    }

    public PageParam getPageParam() {
        return pageParam;
    }

    public void setPageParam(PageParam pageParam) {
        this.pageParam = pageParam;
    }
}
