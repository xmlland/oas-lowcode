package com.jeestudio.bpm.modules.mini.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
/**
 * @Description: 小程序信息实体
 */

@TableName("mini_program_info")
@Data
public class MiniProgramInfoEntity {
    @TableId
    protected String id;

    /*
    * DEL FLAG
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /*
    * 创建者
    */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /*
    * 创建时间
    */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    /*
    * 更新者
    */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /*
    * 更新时间
    */
    @TableField(value = "update_date", fill = FieldFill.UPDATE)
    @JsonFormat(pattern="yyyy-MM-dd", timezone = "GMT+8")
    private Date updateDate;

    /*
    * 备注信息
    */
    @TableField(value = "remarks")
    private String remarks;

    /*
    * 归属机构编码
    */
    @TableField(value = "owner_code", fill = FieldFill.INSERT)
    private String ownerCode;

    /*
    * app_secret
    */
    @TableField(value = "encrypt_app_secret")
    private String encryptAppSecret;

    /*
    * 名称
    */
    @TableField(value = "app_name")
    private String appName;

    /*
    * AppID
    */
    @TableField(value = "app_id")
    private String appId;

    /*
    * AppSecret
    */
    @TableField(value = "app_secret")
    private String appSecret;

    /*
    * 角色id
    */
    @TableField(value = "role_ids")
    private String roleIds;

    /*
     * 数据角色
     */
    @TableField(value = "data_role_ids")
    private String dataRoleIds;
}
