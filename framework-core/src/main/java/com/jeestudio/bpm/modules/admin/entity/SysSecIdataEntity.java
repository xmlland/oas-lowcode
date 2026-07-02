package com.jeestudio.bpm.modules.admin.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 安全数据
 */
@TableName("sys_sec_idata")
@Data
public class SysSecIdataEntity {
    @TableId
    protected String id;

    /**
    * 创建者 createBy.id
    */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
    * 创建时间 createDate
    */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    /**
    * 更新者 updateBy.id
    */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /**
    * 更新时间 updateDate
    */
    @TableField(value = "update_date", fill = FieldFill.UPDATE)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date updateDate;

    /**
    * 备注信息 remarks
    */
    @TableField(value = "remarks")
    private String remarks;

    /**
    * 归属机构编码 ownerCode
    */
    @TableField(value = "owner_code", fill = FieldFill.INSERT)
    private String ownerCode;

    /**
    * FK parent.id
    */
    @TableField(value = "parent_id")
    private String parentId;

    /**
    * DEL FLAG delFlag
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /**
    * 环境 s01
    */
    @TableField(value = "env")
    private String env;

    /**
    * 记录主键 s02
    */
    @TableField(value = "data_id")
    private String dataId;

    /**
    * 原始值 s03
    */
    @TableField(value = "raw_data")
    private String rawData;

    /**
    * 操作时间 d01
    */
    @TableField(value = "opt_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date optTime;

    /**
    * 完整性值 s04
    */
    @TableField(value = "integrity_value")
    private String integrityValue;

    /**
    * 操作人姓名 s05
    */
    @TableField(value = "opt_name")
    private String optName;


    /**
     * 校验通过 s06
     */
    @TableField(value = "valid_pass")
    private String validPass;

    /**
     * 校验时间 d02
     */
    @TableField(value = "valid_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validTime;
}
