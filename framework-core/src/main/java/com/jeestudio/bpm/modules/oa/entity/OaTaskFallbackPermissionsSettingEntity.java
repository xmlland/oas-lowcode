package com.jeestudio.bpm.modules.oa.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 任务回退权限配置
 */
@TableName("oa_task_fallback_permissions_setting")
@Data
public class OaTaskFallbackPermissionsSettingEntity {
    @TableId
    protected String id;

    /**
    * DEL FLAG
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /**
    * 创建者
    */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
    * 创建时间
    */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    /**
    * 更新者
    */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /**
    * 更新时间
    */
    @TableField(value = "update_date", fill = FieldFill.UPDATE)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date updateDate;

    /**
    * 备注信息
    */
    @TableField(value = "remarks")
    private String remarks;

    /**
    * 归属机构编码
    */
    @TableField(value = "owner_code", fill = FieldFill.INSERT)
    private String ownerCode;

    /**
    * procDefKey
    */
    @TableField(value = "proc_def_key")
    private String procDefKey;

    /**
    * 任务节点
    */
    @TableField(value = "task_def_key")
    private String taskDefKey;

    /**
    * 可退回至
    */
    @TableField(value = "returnable_to")
    private String returnableTo;
}
