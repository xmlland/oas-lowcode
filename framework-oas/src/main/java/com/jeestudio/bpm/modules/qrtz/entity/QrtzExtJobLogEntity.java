package com.jeestudio.bpm.modules.qrtz.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 定时任务日志
 */
@TableName("qrtz_ext_job_log")
@Data
public class QrtzExtJobLogEntity {
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
    * 任务id
    */
    @TableField(value = "job_id")
    private String jobId;

    /**
    * 执行人
    */
    @TableField(value = "exec_user")
    private String execUser;

    /**
    * 是否成功
    */
    @TableField(value = "job_success")
    private String jobSuccess;

    /**
    * 日志时间
    */
    @TableField(value = "log_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date logTime;

    /**
    * 耗时(ms)
    */
    @TableField(value = "use_times")
    private String useTimes;

    /**
    * 异常信息
    */
    @TableField(value = "exception_message")
    private String exceptionMessage;
}
