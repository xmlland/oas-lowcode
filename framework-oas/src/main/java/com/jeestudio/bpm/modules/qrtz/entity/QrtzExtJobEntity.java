package com.jeestudio.bpm.modules.qrtz.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 定时任务
 */
@TableName("qrtz_ext_job")
@Data
public class QrtzExtJobEntity {
    @TableId
    protected String id;

    /**
    * 创建者
    */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
    * DEL FLAG
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

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
    * 任务名称
    */
    @TableField(value = "task_name")
    private String taskName;

    /**
    * cron
    */
    @TableField(value = "cronexpression")
    private String cronExpression;

    /**
    * 任务描述
    */
    @TableField(value = "task_description")
    private String taskDescription;

    /**
    * 执行类
    */
    @TableField(value = "execute_class")
    private String executeClass;

    /**
    * 执行参数
    */
    @TableField(value = "exec_param")
    private String execParam;

    /**
    * 是否启用
    */
    @TableField(value = "job_status")
    private String jobStatus;
}
