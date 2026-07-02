package com.jeestudio.bpm.modules.admin.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 用户反馈
 */
@TableName("sys_feedback")
@Data
public class SysFeedbackEntity {
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
    * 是否常见问题 s08
    */
    @TableField(value = "is_common")
    private String isCommon;

    /**
    * 用户是否查看 s09
    */
    @TableField(value = "user_read")
    private String userRead;

    /**
    * DEL FLAG delFlag
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /**
    * 模块 s04
    */
    @TableField(value = "module_")
    private String module;

    /**
    * 标题 s01
    */
    @TableField(value = "title_")
    private String title;

    /**
    * 描述 s02
    */
    @TableField(value = "desc_")
    private String desc;

    /**
    * 图片 s03
    */
    @TableField(value = "file_pic")
    private String filePic;

    /**
    * 是否提交 s07
    */
    @TableField(value = "is_submit")
    private String isSubmit;

    /**
    * 提交时间 d02
    */
    @TableField(value = "submit_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    /**
    * 提交人 user02.id|name
    */
    @TableField(value = "submit_user")
    private String submitUser;

    /**
    * 是否回复 s05
    */
    @TableField(value = "is_reply")
    private String isReply;

    /**
    * 回复人 user01.id|name
    */
    @TableField(value = "reply_user")
    private String replyUser;

    /**
    * 回复时间 d01
    */
    @TableField(value = "reply_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date replyTime;

    /**
    * 回复内容 s06
    */
    @TableField(value = "reply_content")
    private String replyContent;
}