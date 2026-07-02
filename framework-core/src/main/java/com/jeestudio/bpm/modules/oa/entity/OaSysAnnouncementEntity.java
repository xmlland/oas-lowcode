package com.jeestudio.bpm.modules.oa.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 系统公告
 */
@TableName("oa_sys_announcement")
@Data
public class OaSysAnnouncementEntity {
    @TableId
    protected String id;

    /**
    * 逻辑删除标记（0：显示；1：隐藏） delFlag
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /**
    * 备注信息 remarks
    */
    @TableField(value = "remarks")
    private String remarks;

    /**
    * 创建时间 createDate
    */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;

    /**
    * 更新时间 updateDate
    */
    @TableField(value = "update_date", fill = FieldFill.UPDATE)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateDate;

    /**
    * 创建者 createBy.id
    */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
    * 更新者 updateBy.id
    */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /**
    * 归属机构编码 ownerCode
    */
    @TableField(value = "owner_code", fill = FieldFill.INSERT)
    private String ownerCode;

    /**
    * Users name users01Name
    */
    @TableField(value = "users01_name")
    private String users01Name;

    /**
    * 所属机构 office01.id|name
    */
    @TableField(value = "org_id")
    private String orgId;

    /**
    * 发送人 user01.id|name
    */
    @TableField(value = "sender")
    private String sender;

    /**
    * 消息标题 s01
    */
    @TableField(value = "title")
    private String title;

    /**
    * 未读是否自动弹出 s04
    */
    @TableField(value = "automatically_pop")
    private String automaticallyPop;

    /**
    * 发送时间 d11
    */
    @TableField(value = "send_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;

    /**
    * 接收角色 s03
    */
    @TableField(value = "receiving_roles")
    private String receivingRoles;

    /**
    * 消息颜色 s02
    */
    @TableField(value = "message_color")
    private String messageColor;

    /**
    * 菜单链接 s10
    */
    @TableField(value = "menu_href")
    private String menuHref;

    /**
    * 菜单名称 s07
    */
    @TableField(value = "menu_name")
    private String menuName;

    /**
    * 记录ID s08
    */
    @TableField(value = "record_id")
    private String recordId;

    /**
    * 菜单英文名称 s09
    */
    @TableField(value = "menu_name_en")
    private String menuNameEn;

    /**
    * 消息内容 s11
    */
    @TableField(value = "content_")
    private String content;

    /**
    * 接收人员 users01.id|name
    */
    @TableField(value = "receiving_users")
    private String receivingUsers;
}