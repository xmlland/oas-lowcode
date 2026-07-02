package com.jeestudio.bpm.modules.prt.entity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Description: 打印卡片
 */
@TableName("prt_card")
@Data
public class PrtCardEntity {
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
    * DEL FLAG delFlag
    */
    @TableField(value = "del_flag")
    private String delFlag = "0";

    /**
    * 标题 s01
    */
    @TableField(value = "card_title")
    private String cardTitle;

    /**
    * 类型 s02
    */
    @TableField(value = "card_type")
    private String cardType;

    /**
    * 链接 s03
    */
    @TableField(value = "card_link")
    private String cardLink;

    /**
    * 排序 sort
    */
    @TableField(value = "sort")
    private Integer sort;

    /**
    * 是否启用 s08
    */
    @TableField(value = "is_enable")
    private String isEnable;

    /**
    * 详情类型 s05
    */
    @TableField(value = "detail_type")
    private String detailType;

    /**
    * 详情链接 s04
    */
    @TableField(value = "detail_link")
    private String detailLink;

    /**
    * col配置 s06
    */
    @TableField(value = "col_props")
    private String colProps;

    /**
    * 备注 s07
    */
    @TableField(value = "remark_info")
    private String remarkInfo;
}