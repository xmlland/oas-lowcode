package com.jeestudio.bpm.common.entity.common;

import java.math.BigDecimal;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.system.Area;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.view.common.DictView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Transient;

/**
 * @Description: 通用的实体类，通过表单设计将字段映射到预设的属性上，实现一个实体类对应多个表
 */
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
@Getter
@Setter
public class Zform extends ActEntity<Zform> {

    private static final long serialVersionUID = 1L;

    @Transient
    /** MyBatis Plus 查询包装器，动态表单高级查询时使用，不参与序列化。 */
    private transient QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
    @Transient
    /** 查询参数类型映射，用于运行时判断条件值的解析方式。 */
    private transient Map<String,String> queryParamType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：创建开始时间。 */
    private Date beginCreateDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：创建结束时间。 */
    private Date endCreateDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：更新开始时间。 */
    private Date beginUpdateDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 查询条件：更新结束时间。 */
    private Date endUpdateDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 归属机构编码。 */
    private String ownerCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 密级或保密标记。 */
    private String dense;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 排序号。 */
    private Integer sort;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 业务状态。 */
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 树表或主子结构中的父级对象。 */
    private Zform parent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 父级名称冗余字段。 */
    private String parentName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 父级 ID 路径。 */
    protected String parentIds;
    /** 通用名称字段。 */
    protected String name = "";
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 通用英文名称字段。 */
    protected String name_EN;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 完整路径名称。 */
    protected String fullPathName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 英文完整路径名称。 */
    protected String fullPathName_EN;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 是否存在子节点。 */
    private boolean hasChildren;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    /** 子节点列表。 */
    private List<Zform> children;

    /** 数值字段池：由表单设计器将 numeric/decimal 类字段映射到 b01-b20。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal b20;

    /** 整数字段池：由表单设计器将 integer 类字段映射到 i01-i20。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer i20;

    /** 布尔字段池：由表单设计器将开关、是否类字段映射到 f01-f20。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean f20;

    /** 多选字段池：用于存储复选框等多值控件。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String c10;

    /** 日期时间字段池：由表单设计器将日期、月份、时间类字段映射到 dxx 字段。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d21;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d22;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d23;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d24;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d25;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d26;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d27;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d28;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d29;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD21;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD21;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD22;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD22;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD23;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD23;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD24;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD24;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD25;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD25;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD26;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD26;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD27;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD27;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD28;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD28;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD29;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD29;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d31;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d32;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d33;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d34;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d35;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d36;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d37;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d38;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d39;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD31;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD31;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD32;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD32;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD33;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD33;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD34;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD34;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD35;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD35;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD36;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD36;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD37;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD37;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD38;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD38;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD39;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD39;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d41;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d42;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d43;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d44;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d45;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d46;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d47;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d48;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d49;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD41;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD41;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD42;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD42;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD43;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD43;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD44;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD44;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD45;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD45;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD46;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD46;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD47;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD47;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD48;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD48;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD49;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD49;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d51;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d52;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d53;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d54;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d55;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d56;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d57;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d58;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date d59;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD51;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD51;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD52;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD52;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD53;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD53;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD54;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD54;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD55;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD55;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD56;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD56;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD57;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD57;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD58;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD58;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date beginD59;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endD59;

    /** 短文本字段池：由表单设计器将短文本、编码、名称类字段映射到 sxx 字段。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s20;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s21;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s22;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s23;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s24;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s25;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s26;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s27;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s28;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s29;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s30;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s31;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s32;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s33;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s34;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s35;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s36;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s37;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s38;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s39;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s40;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s41;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s42;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s43;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s44;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s45;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s46;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s47;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s48;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s49;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s50;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s51;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s52;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s53;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s54;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s55;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s56;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s57;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s58;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s59;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s60;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s61;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s62;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s63;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s64;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s65;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s66;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s67;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s68;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s69;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s70;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s71;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s72;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s73;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s74;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s75;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s76;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s77;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s78;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s79;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s80;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s81;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s82;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s83;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s84;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s85;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s86;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s87;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s88;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s89;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s90;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s91;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s92;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s93;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s94;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s95;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s96;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s97;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s98;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s99;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s100;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s101;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s102;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s103;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s104;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s105;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s106;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s107;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s108;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s109;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s110;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s111;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s112;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s113;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s114;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s115;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s116;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s117;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s118;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s119;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s120;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s121;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s122;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s123;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s124;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s125;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s126;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s127;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s128;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s129;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s130;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s131;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s132;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s133;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s134;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s135;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s136;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s137;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s138;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s139;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s140;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s141;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s142;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s143;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s144;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s145;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s146;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s147;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s148;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s149;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s150;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s151;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s152;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s153;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s154;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s155;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s156;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s157;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s158;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s159;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s160;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s161;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s162;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s163;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s164;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s165;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s166;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s167;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s168;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s169;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s170;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s171;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s172;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s173;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s174;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s175;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s176;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s177;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s178;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s179;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s180;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s181;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s182;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s183;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s184;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s185;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s186;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s187;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s188;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s189;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s190;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s191;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s192;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s193;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s194;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s195;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s196;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s197;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s198;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s199;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s200;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s201;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s202;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s203;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s204;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s205;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s206;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s207;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s208;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s209;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s210;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s211;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s212;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s213;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s214;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s215;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s216;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s217;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s218;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s219;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s220;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s221;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s222;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s223;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s224;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s225;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s226;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s227;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s228;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s229;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s230;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s231;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s232;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s233;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s234;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s235;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s236;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s237;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s238;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s239;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s240;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s241;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s242;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s243;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s244;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s245;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s246;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s247;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s248;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s249;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s250;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s251;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s252;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s253;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s254;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s255;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s256;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s257;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s258;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s259;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s260;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s261;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s262;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s263;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s264;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s265;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s266;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s267;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s268;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s269;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s270;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s271;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s272;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s273;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s274;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s275;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s276;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s277;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s278;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s279;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s280;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s281;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s282;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s283;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s284;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s285;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s286;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s287;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s288;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s289;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s290;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s291;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s292;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s293;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s294;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s295;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s296;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s297;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s298;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s299;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s300;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s301;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s302;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s303;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s304;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s305;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s306;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s307;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s308;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s309;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s310;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s311;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s312;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s313;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s314;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s315;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s316;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s317;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s318;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s319;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s320;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s321;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s322;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s323;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s324;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s325;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s326;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s327;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s328;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s329;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s330;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s331;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s332;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s333;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s334;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s335;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s336;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s337;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s338;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s339;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s340;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s341;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s342;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s343;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s344;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s345;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s346;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s347;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s348;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s349;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s350;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s351;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s352;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s353;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s354;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s355;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s356;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s357;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s358;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s359;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s360;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s361;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s362;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s363;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s364;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s365;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s366;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s367;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s368;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s369;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s370;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s371;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s372;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s373;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s374;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s375;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s376;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s377;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s378;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s379;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s380;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s381;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s382;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s383;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s384;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s385;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s386;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s387;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s388;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s389;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s390;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s391;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s392;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s393;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s394;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s395;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s396;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s397;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s398;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s399;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s400;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s401;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s402;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s403;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s404;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s405;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s406;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s407;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s408;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s409;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s410;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s411;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s412;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s413;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s414;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s415;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s416;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s417;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s418;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s419;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s420;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s421;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s422;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s423;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s424;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s425;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s426;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s427;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s428;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s429;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s430;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s431;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s432;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s433;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s434;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s435;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s436;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s437;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s438;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s439;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s440;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s441;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s442;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s443;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s444;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s445;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s446;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s447;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s448;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s449;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s450;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s451;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s452;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s453;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s454;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s455;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s456;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s457;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s458;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s459;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s460;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s461;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s462;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s463;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s464;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s465;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s466;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s467;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s468;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s469;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s470;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s471;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s472;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s473;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s474;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s475;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s476;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s477;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s478;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s479;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s480;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s481;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s482;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s483;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s484;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s485;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s486;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s487;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s488;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s489;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s490;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s491;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s492;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s493;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s494;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s495;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s496;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s497;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s498;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s499;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s500;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s501;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s502;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s503;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s504;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s505;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s506;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s507;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s508;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s509;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s510;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s511;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s512;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s513;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s514;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s515;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s516;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s517;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s518;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s519;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s520;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s521;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s522;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s523;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s524;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s525;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s526;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s527;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s528;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s529;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s530;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s531;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s532;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s533;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s534;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s535;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s536;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s537;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s538;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s539;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s540;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s541;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s542;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s543;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s544;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s545;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s546;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s547;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s548;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s549;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s550;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s551;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s552;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s553;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s554;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s555;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s556;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s557;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s558;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s559;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s560;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s561;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s562;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s563;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s564;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s565;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s566;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s567;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s568;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s569;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s570;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s571;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s572;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s573;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s574;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s575;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s576;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s577;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s578;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s579;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s580;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s581;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s582;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s583;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s584;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s585;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s586;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s587;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s588;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s589;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s590;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s591;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s592;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s593;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s594;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s595;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s596;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s597;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s598;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s599;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String s600;

    //Middle string
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String m20;

    //Long string
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String l10;

    //User
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user15;

    //Office
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Office office01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Office office02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Office office03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Office office04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Office office05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User users01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String users01Name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User users02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String users02Name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User users03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String users03Name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User users04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String users04Name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User users05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String users05Name;

    //Area
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Area area01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Area area02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Area area03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Area area04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Area area05;

    //Grid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform g09;

    //Children list
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Zform> childrenList19;

    private boolean skipDictViewList01;
    private boolean skipDictViewList02;
    private boolean skipDictViewList03;
    private boolean skipDictViewList04;
    private boolean skipDictViewList05;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DictView> dictViewList01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DictView> dictViewList02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DictView> dictViewList03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DictView> dictViewList04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DictView> dictViewList05;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t06;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t07;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t08;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t09;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t10;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t11;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t12;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t13;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t14;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t15;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t16;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t17;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t18;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t19;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Zform t20;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countersign01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempcountersign01;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countersign02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempcountersign02;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countersign03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempcountersign03;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countersign04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempcountersign04;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String countersign05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String tempcountersign05;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String contentGroupId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String newContentGroupId;

    //Param map
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> paramMap;

    //Table name
    private String formNo;

    //Table name ext
    private String formNoExt;

    //Table or view name
    private String tableOrViewName;

    //留痕库或Schema名称
    @Transient
    private String versionSchema;

    //Foreign key
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fk;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String secLevel;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hashKey;

    private JSONObject zformMap;

    public Zform() {
        super();
    }

    public Zform(String id){
        this();
        this.id = id;
    }

    public Zform(String id, String formNo){
        this();
        this.id = id;
        this.formNo = formNo;
    }

    public Zform(Zform zform) {
        this.parent = zform;
    }

    @Transient
    public QueryWrapper<Zform> getQueryWrapper() {
        return queryWrapper;
    }

    public void setQueryWrapper(QueryWrapper<Zform> queryWrapper) {
        this.queryWrapper = queryWrapper;
    }


    public Map<String, String> getQueryParamType() {
        return queryParamType;
    }

    public void setQueryParamType(Map<String, String> queryParamType) {
        this.queryParamType = queryParamType;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginCreateDate() {
        return beginCreateDate;
    }

    public void setBeginCreateDate(Date beginCreateDate) {
        this.beginCreateDate = beginCreateDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndCreateDate() {
        return endCreateDate;
    }

    public void setEndCreateDate(Date endCreateDate) {
        this.endCreateDate = endCreateDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginUpdateDate() {
        return beginUpdateDate;
    }

    public void setBeginUpdateDate(Date beginUpdateDate) {
        this.beginUpdateDate = beginUpdateDate;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndUpdateDate() {
        return endUpdateDate;
    }

    public void setEndUpdateDate(Date endUpdateDate) {
        this.endUpdateDate = endUpdateDate;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getDense() {
        return dense;
    }

    public void setDense(String dense) {
        this.dense = dense;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //@JsonBackReference
    public Zform getParent() {
        return parent;
    }

    public void setParent(Zform parent) {
        this.parent = parent;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }


    public String getC01() {
        return c01;
    }

    public void setC01(String c01) {
        this.c01 = c01;
    }

    public String getC02() {
        return c02;
    }

    public void setC02(String c02) {
        this.c02 = c02;
    }

    public String getC03() {
        return c03;
    }

    public void setC03(String c03) {
        this.c03 = c03;
    }

    public String getC04() {
        return c04;
    }

    public void setC04(String c04) {
        this.c04 = c04;
    }

    public String getC05() {
        return c05;
    }

    public void setC05(String c05) {
        this.c05 = c05;
    }

    public String getC06() {
        return c06;
    }

    public void setC06(String c06) {
        this.c06 = c06;
    }

    public String getC07() {
        return c07;
    }

    public void setC07(String c07) {
        this.c07 = c07;
    }

    public String getC08() {
        return c08;
    }

    public void setC08(String c08) {
        this.c08 = c08;
    }

    public String getC09() {
        return c09;
    }

    public void setC09(String c09) {
        this.c09 = c09;
    }

    public String getC10() {
        return c10;
    }

    public void setC10(String c10) {
        this.c10 = c10;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC01List() {
        List<String> cList = Lists.newArrayList();
        if (this.c01 == null) {
            return null;
        } else {
            String[] cArray = this.c01.split(",");
            for(int i = 0; i < cArray.length; i ++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC01List(List<String> c01List) {
        String cString = "";
        if (c01List != null) {
            for (String c : c01List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c01 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC02List() {
        List<String> cList = Lists.newArrayList();
        if (this.c02 == null) {
            return null;
        } else {
            String[] cArray = this.c02.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC02List(List<String> c02List) {
        String cString = "";
        if (c02List != null) {
            for (String c : c02List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c02 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC03List() {
        List<String> cList = Lists.newArrayList();
        if (this.c03 == null) {
            return null;
        } else {
            String[] cArray = this.c03.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC03List(List<String> c03List) {
        String cString = "";
        if (c03List != null) {
            for (String c : c03List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c03 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC04List() {
        List<String> cList = Lists.newArrayList();
        if (this.c04 == null) {
            return null;
        } else {
            String[] cArray = this.c04.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC04List(List<String> c04List) {
        String cString = "";
        if (c04List != null) {
            for (String c : c04List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c04 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC05List() {
        List<String> cList = Lists.newArrayList();
        if (this.c05 == null) {
            return null;
        } else {
            String[] cArray = this.c05.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC05List(List<String> c05List) {
        String cString = "";
        if (c05List != null) {
            for (String c : c05List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c05 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC06List() {
        List<String> cList = Lists.newArrayList();
        if (this.c06 == null) {
            return null;
        } else {
            String[] cArray = this.c06.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC06List(List<String> c06List) {
        String cString = "";
        if (c06List != null) {
            for (String c : c06List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c06 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC07List() {
        List<String> cList = Lists.newArrayList();
        if (this.c07 == null) {
            return null;
        } else {
            String[] cArray = this.c07.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC07List(List<String> c07List) {
        String cString = "";
        if (c07List != null) {
            for (String c : c07List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c07 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC08List() {
        List<String> cList = Lists.newArrayList();
        if (this.c08 == null) {
            return null;
        } else {
            String[] cArray = this.c08.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC08List(List<String> c08List) {
        String cString = "";
        if (c08List != null) {
            for (String c : c08List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c08 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC09List() {
        List<String> cList = Lists.newArrayList();
        if (this.c09 == null) {
            return null;
        } else {
            String[] cArray = this.c09.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC09List(List<String> c09List) {
        String cString = "";
        if (c09List != null) {
            for (String c : c09List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c09 = cString;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<String> getC10List() {
        List<String> cList = Lists.newArrayList();
        if (this.c10 == null) {
            return null;
        } else {
            String[] cArray = this.c10.split(",");
            for (int i = 0; i < cArray.length; i++) {
                cList.add(cArray[i]);
            }
            return cList;
        }
    }

    public void setC10List(List<String> c10List) {
        String cString = "";
        if (c10List != null) {
            for (String c : c10List) {
                cString += "," + c;
            }
        }
        if (cString.startsWith(",")) {
            cString = cString.substring(1);
        }
        this.c10 = cString;
    }



    public String getS01() {
        return s01;
    }

    public void setS01(String s01) {
        this.s01 = s01;
    }

    public String getS02() {
        return s02;
    }

    public void setS02(String s02) {
        this.s02 = s02;
    }

    public String getS03() {
        return s03;
    }

    public void setS03(String s03) {
        this.s03 = s03;
    }

    public String getS04() {
        return s04;
    }

    public void setS04(String s04) {
        this.s04 = s04;
    }

    public String getS05() {
        return s05;
    }

    public void setS05(String s05) {
        this.s05 = s05;
    }

    public String getS06() {
        return s06;
    }

    public void setS06(String s06) {
        this.s06 = s06;
    }

    public String getS07() {
        return s07;
    }

    public void setS07(String s07) {
        this.s07 = s07;
    }

    public String getS08() {
        return s08;
    }

    public void setS08(String s08) {
        this.s08 = s08;
    }

    public String getS09() {
        return s09;
    }

    public void setS09(String s09) {
        this.s09 = s09;
    }

    public String getS10() {
        return s10;
    }

    public void setS10(String s10) {
        this.s10 = s10;
    }

    public String getS11() {
        return s11;
    }

    public void setS11(String s11) {
        this.s11 = s11;
    }

    public String getS12() {
        return s12;
    }

    public void setS12(String s12) {
        this.s12 = s12;
    }

    public String getS13() {
        return s13;
    }

    public void setS13(String s13) {
        this.s13 = s13;
    }

    public String getS14() {
        return s14;
    }

    public void setS14(String s14) {
        this.s14 = s14;
    }

    public String getS15() {
        return s15;
    }

    public void setS15(String s15) {
        this.s15 = s15;
    }

    public String getS16() {
        return s16;
    }

    public void setS16(String s16) {
        this.s16 = s16;
    }

    public String getS17() {
        return s17;
    }

    public void setS17(String s17) {
        this.s17 = s17;
    }

    public String getS18() {
        return s18;
    }

    public void setS18(String s18) {
        this.s18 = s18;
    }

    public String getS19() {
        return s19;
    }

    public void setS19(String s19) {
        this.s19 = s19;
    }

    public String getS20() {
        return s20;
    }

    public void setS20(String s20) {
        this.s20 = s20;
    }

    public String getS21() {
        return s21;
    }

    public void setS21(String s21) {
        this.s21 = s21;
    }

    public String getS22() {
        return s22;
    }

    public void setS22(String s22) {
        this.s22 = s22;
    }

    public String getS23() {
        return s23;
    }

    public void setS23(String s23) {
        this.s23 = s23;
    }

    public String getS24() {
        return s24;
    }

    public void setS24(String s24) {
        this.s24 = s24;
    }

    public String getS25() {
        return s25;
    }

    public void setS25(String s25) {
        this.s25 = s25;
    }

    public String getS26() {
        return s26;
    }

    public void setS26(String s26) {
        this.s26 = s26;
    }

    public String getS27() {
        return s27;
    }

    public void setS27(String s27) {
        this.s27 = s27;
    }

    public String getS28() {
        return s28;
    }

    public void setS28(String s28) {
        this.s28 = s28;
    }

    public String getS29() {
        return s29;
    }

    public void setS29(String s29) {
        this.s29 = s29;
    }

    public String getS30() {
        return s30;
    }

    public void setS30(String s30) {
        this.s30 = s30;
    }

    public String getM01() {
        return m01;
    }

    public void setM01(String m01) {
        this.m01 = m01;
    }

    public String getM02() {
        return m02;
    }

    public void setM02(String m02) {
        this.m02 = m02;
    }

    public String getM03() {
        return m03;
    }

    public void setM03(String m03) {
        this.m03 = m03;
    }

    public String getM04() {
        return m04;
    }

    public void setM04(String m04) {
        this.m04 = m04;
    }

    public String getM05() {
        return m05;
    }

    public void setM05(String m05) {
        this.m05 = m05;
    }

    public String getM06() {
        return m06;
    }

    public void setM06(String m06) {
        this.m06 = m06;
    }

    public String getM07() {
        return m07;
    }

    public void setM07(String m07) {
        this.m07 = m07;
    }

    public String getM08() {
        return m08;
    }

    public void setM08(String m08) {
        this.m08 = m08;
    }

    public String getM09() {
        return m09;
    }

    public void setM09(String m09) {
        this.m09 = m09;
    }

    public String getM10() {
        return m10;
    }

    public void setM10(String m10) {
        this.m10 = m10;
    }

    public String getM11() {
        return m11;
    }

    public void setM11(String m11) {
        this.m11 = m11;
    }

    public String getM12() {
        return m12;
    }

    public void setM12(String m12) {
        this.m12 = m12;
    }

    public String getM13() {
        return m13;
    }

    public void setM13(String m13) {
        this.m13 = m13;
    }

    public String getM14() {
        return m14;
    }

    public void setM14(String m14) {
        this.m14 = m14;
    }

    public String getM15() {
        return m15;
    }

    public void setM15(String m15) {
        this.m15 = m15;
    }

    public String getM16() {
        return m16;
    }

    public void setM16(String m16) {
        this.m16 = m16;
    }

    public String getM17() {
        return m17;
    }

    public void setM17(String m17) {
        this.m17 = m17;
    }

    public String getM18() {
        return m18;
    }

    public void setM18(String m18) {
        this.m18 = m18;
    }

    public String getM19() {
        return m19;
    }

    public void setM19(String m19) {
        this.m19 = m19;
    }

    public String getM20() {
        return m20;
    }

    public void setM20(String m20) {
        this.m20 = m20;
    }

    public String getL01() {
        return l01;
    }

    public void setL01(String l01) {
        this.l01 = l01;
    }

    public String getL02() {
        return l02;
    }

    public void setL02(String l02) {
        this.l02 = l02;
    }

    public String getL03() {
        return l03;
    }

    public void setL03(String l03) {
        this.l03 = l03;
    }

    public String getL04() {
        return l04;
    }

    public void setL04(String l04) {
        this.l04 = l04;
    }

    public String getL05() {
        return l05;
    }

    public void setL05(String l05) {
        this.l05 = l05;
    }

    public String getL06() {
        return l06;
    }

    public void setL06(String l06) {
        this.l06 = l06;
    }

    public String getL07() {
        return l07;
    }

    public void setL07(String l07) {
        this.l07 = l07;
    }

    public String getL08() {
        return l08;
    }

    public void setL08(String l08) {
        this.l08 = l08;
    }

    public String getL09() {
        return l09;
    }

    public void setL09(String l09) {
        this.l09 = l09;
    }

    public String getL10() {
        return l10;
    }

    public void setL10(String l10) {
        this.l10 = l10;
    }

    public User getUser01() {
        return user01;
    }

    public void setUser01(User user01) {
        this.user01 = user01;
    }

    public User getUser02() {
        return user02;
    }

    public void setUser02(User user02) {
        this.user02 = user02;
    }

    public User getUser03() {
        return user03;
    }

    public void setUser03(User user03) {
        this.user03 = user03;
    }

    public User getUser04() {
        return user04;
    }

    public void setUser04(User user04) {
        this.user04 = user04;
    }

    public User getUser05() {
        return user05;
    }

    public void setUser05(User user05) {
        this.user05 = user05;
    }

    public User getUser06() {
        return user06;
    }

    public void setUser06(User user06) {
        this.user06 = user06;
    }

    public User getUser07() {
        return user07;
    }

    public void setUser07(User user07) {
        this.user07 = user07;
    }

    public User getUser08() {
        return user08;
    }

    public void setUser08(User user08) {
        this.user08 = user08;
    }

    public User getUser09() {
        return user09;
    }

    public void setUser09(User user09) {
        this.user09 = user09;
    }

    public User getUser10() {
        return user10;
    }

    public void setUser10(User user10) {
        this.user10 = user10;
    }

    public User getUser11() {
        return user11;
    }

    public void setUser11(User user11) {
        this.user11 = user11;
    }

    public User getUser12() {
        return user12;
    }

    public void setUser12(User user12) {
        this.user12 = user12;
    }

    public User getUser13() {
        return user13;
    }

    public void setUser13(User user13) {
        this.user13 = user13;
    }

    public User getUser14() {
        return user14;
    }

    public void setUser14(User user14) {
        this.user14 = user14;
    }

    public User getUser15() {
        return user15;
    }

    public void setUser15(User user15) {
        this.user15 = user15;
    }

    public Office getOffice01() {
        return office01;
    }

    public void setOffice01(Office office01) {
        this.office01 = office01;
    }

    public Office getOffice02() {
        return office02;
    }

    public void setOffice02(Office office02) {
        this.office02 = office02;
    }

    public Office getOffice03() {
        return office03;
    }

    public void setOffice03(Office office03) {
        this.office03 = office03;
    }

    public Office getOffice04() {
        return office04;
    }

    public void setOffice04(Office office04) {
        this.office04 = office04;
    }

    public Office getOffice05() {
        return office05;
    }

    public void setOffice05(Office office05) {
        this.office05 = office05;
    }

    public User getUsers01() {
        if (users01 != null && StringUtils.isEmpty(users01.getName())) {
            users01.setName(users01Name);
        }
        return users01;
    }

    public void setUsers01(User users01) {
        this.users01 = users01;
    }

    public String getUsers01Name() {
        if (users01 != null && false == StringUtils.isEmpty(users01.getName())) {
            users01Name = users01.getName();
        }
        return users01Name;
    }

    public void setUsers01Name(String users01Name) {
        this.users01Name = users01Name;
    }

    public User getUsers02() {
        if (users02 != null && StringUtils.isEmpty(users02.getName())) {
            users02.setName(users02Name);
        }
        return users02;
    }

    public void setUsers02(User users02) {
        this.users02 = users02;
    }

    public String getUsers02Name() {
        if (users02 != null && false == StringUtils.isEmpty(users02.getName())) {
            users02Name = users02.getName();
        }
        return users02Name;
    }

    public void setUsers02Name(String users02Name) {
        this.users02Name = users02Name;
    }

    public User getUsers03() {
        if (users03 != null && StringUtils.isEmpty(users03.getName())) {
            users03.setName(users03Name);
        }
        return users03;
    }

    public void setUsers03(User users03) {
        this.users03 = users03;
    }

    public String getUsers03Name() {
        if (users03 != null && false == StringUtils.isEmpty(users03.getName())) {
            users03Name = users03.getName();
        }
        return users03Name;
    }

    public void setUsers03Name(String users03Name) {
        this.users03Name = users03Name;
    }

    public User getUsers04() {
        if (users04 != null && StringUtils.isEmpty(users04.getName())) {
            users04.setName(users04Name);
        }
        return users04;
    }

    public void setUsers04(User users04) {
        this.users04 = users04;
    }

    public String getUsers04Name() {
        if (users04 != null && false == StringUtils.isEmpty(users04.getName())) {
            users04Name = users04.getName();
        }
        return users04Name;
    }

    public void setUsers04Name(String users04Name) {
        this.users04Name = users04Name;
    }

    public User getUsers05() {
        if (users05 != null && StringUtils.isEmpty(users05.getName())) {
            users05.setName(users05Name);
        }
        return users05;
    }

    public void setUsers05(User users05) {
        this.users05 = users05;
    }

    public String getUsers05Name() {
        if (users05 != null && false == StringUtils.isEmpty(users05.getName())) {
            users05Name = users05.getName();
        }
        return users05Name;
    }

    public void setUsers05Name(String users05Name) {
        this.users05Name = users05Name;
    }

    public List<Zform> getChildrenList01() {
        return childrenList01;
    }

    public void setChildrenList01(List<Zform> childrenList01) {
        this.childrenList01 = childrenList01;
    }

    public List<Zform> getChildrenList02() {
        return childrenList02;
    }

    public void setChildrenList02(List<Zform> childrenList02) {
        this.childrenList02 = childrenList02;
    }

    public List<Zform> getChildrenList03() {
        return childrenList03;
    }

    public void setChildrenList03(List<Zform> childrenList03) {
        this.childrenList03 = childrenList03;
    }

    public List<Zform> getChildrenList04() {
        return childrenList04;
    }

    public void setChildrenList04(List<Zform> childrenList04) {
        this.childrenList04 = childrenList04;
    }

    public List<Zform> getChildrenList05() {
        return childrenList05;
    }

    public void setChildrenList05(List<Zform> childrenList05) {
        this.childrenList05 = childrenList05;
    }

    public List<Zform> getChildrenList06() {
        return childrenList06;
    }

    public void setChildrenList06(List<Zform> childrenList06) {
        this.childrenList06 = childrenList06;
    }

    public List<Zform> getChildrenList07() {
        return childrenList07;
    }

    public void setChildrenList07(List<Zform> childrenList07) {
        this.childrenList07 = childrenList07;
    }

    public List<Zform> getChildrenList08() {
        return childrenList08;
    }

    public void setChildrenList08(List<Zform> childrenList08) {
        this.childrenList08 = childrenList08;
    }

    public List<Zform> getChildrenList09() {
        return childrenList09;
    }

    public void setChildrenList09(List<Zform> childrenList09) {
        this.childrenList09 = childrenList09;
    }

    public List<Zform> getChildrenList10() {
        return childrenList10;
    }

    public void setChildrenList10(List<Zform> childrenList10) {
        this.childrenList10 = childrenList10;
    }

    public List<Zform> getChildrenList11() {
        return childrenList11;
    }

    public void setChildrenList11(List<Zform> childrenList11) {
        this.childrenList11 = childrenList11;
    }

    public List<Zform> getChildrenList12() {
        return childrenList12;
    }

    public void setChildrenList12(List<Zform> childrenList12) {
        this.childrenList12 = childrenList12;
    }

    public List<Zform> getChildrenList13() {
        return childrenList13;
    }

    public void setChildrenList13(List<Zform> childrenList13) {
        this.childrenList13 = childrenList13;
    }

    public List<Zform> getChildrenList14() {
        return childrenList14;
    }

    public void setChildrenList14(List<Zform> childrenList14) {
        this.childrenList14 = childrenList14;
    }

    public List<Zform> getChildrenList15() {
        return childrenList15;
    }

    public void setChildrenList15(List<Zform> childrenList15) {
        this.childrenList15 = childrenList15;
    }

    public List<Zform> getChildrenList16() {
        return childrenList16;
    }

    public void setChildrenList16(List<Zform> childrenList16) {
        this.childrenList16 = childrenList16;
    }

    public List<Zform> getChildrenList17() {
        return childrenList17;
    }

    public void setChildrenList17(List<Zform> childrenList17) {
        this.childrenList17 = childrenList17;
    }

    public List<Zform> getChildrenList18() {
        return childrenList18;
    }

    public void setChildrenList18(List<Zform> childrenList18) {
        this.childrenList18 = childrenList18;
    }

    public List<Zform> getChildrenList19() {
        return childrenList19;
    }

    public void setChildrenList19(List<Zform> childrenList19) {
        this.childrenList19 = childrenList19;
    }

    public Area getArea01() {
        return area01;
    }

    public void setArea01(Area area01) {
        this.area01 = area01;
    }

    public Area getArea02() {
        return area02;
    }

    public void setArea02(Area area02) {
        this.area02 = area02;
    }

    public Area getArea03() {
        return area03;
    }

    public void setArea03(Area area03) {
        this.area03 = area03;
    }

    public Area getArea04() {
        return area04;
    }

    public void setArea04(Area area04) {
        this.area04 = area04;
    }

    public Area getArea05() {
        return area05;
    }

    public void setArea05(Area area05) {
        this.area05 = area05;
    }



    public Zform getG01() {
        return g01;
    }

    public void setG01(Zform g01) {
        this.g01 = g01;
    }

    public Zform getG02() {
        return g02;
    }

    public void setG02(Zform g02) {
        this.g02 = g02;
    }

    public Zform getG03() {
        return g03;
    }

    public void setG03(Zform g03) {
        this.g03 = g03;
    }

    public Zform getG04() {
        return g04;
    }

    public void setG04(Zform g04) {
        this.g04 = g04;
    }

    public Zform getG05() {
        return g05;
    }

    public void setG05(Zform g05) {
        this.g05 = g05;
    }

    public Zform getG06() {
        return g06;
    }

    public void setG06(Zform g06) {
        this.g06 = g06;
    }

    public Zform getG07() {
        return g07;
    }

    public void setG07(Zform g07) {
        this.g07 = g07;
    }

    public Zform getG08() {
        return g08;
    }

    public void setG08(Zform g08) {
        this.g08 = g08;
    }

    public Zform getG09() {
        return g09;
    }

    public void setG09(Zform g09) {
        this.g09 = g09;
    }

    public Zform getT01() {
        return t01;
    }

    public void setT01(Zform t01) {
        this.t01 = t01;
    }

    public Zform getT02() {
        return t02;
    }

    public void setT02(Zform t02) {
        this.t02 = t02;
    }

    public Zform getT03() {
        return t03;
    }

    public void setT03(Zform t03) {
        this.t03 = t03;
    }

    public Zform getT04() {
        return t04;
    }

    public void setT04(Zform t04) {
        this.t04 = t04;
    }

    public Zform getT05() {
        return t05;
    }

    public void setT05(Zform t05) {
        this.t05 = t05;
    }

    public Zform getT06() {
        return t06;
    }

    public void setT06(Zform t06) {
        this.t06 = t06;
    }

    public Zform getT07() {
        return t07;
    }

    public void setT07(Zform t07) {
        this.t07 = t07;
    }

    public Zform getT08() {
        return t08;
    }

    public void setT08(Zform t08) {
        this.t08 = t08;
    }

    public Zform getT09() {
        return t09;
    }

    public void setT09(Zform t09) {
        this.t09 = t09;
    }

    public Zform getT10() {
        return t10;
    }

    public void setT10(Zform t10) {
        this.t10 = t10;
    }

    public Zform getT11() {
        return t11;
    }

    public void setT11(Zform t11) {
        this.t11 = t11;
    }

    public Zform getT12() {
        return t12;
    }

    public void setT12(Zform t12) {
        this.t12 = t12;
    }

    public Zform getT13() {
        return t13;
    }

    public void setT13(Zform t13) {
        this.t13 = t13;
    }

    public Zform getT14() {
        return t14;
    }

    public void setT14(Zform t14) {
        this.t14 = t14;
    }

    public Zform getT15() {
        return t15;
    }

    public void setT15(Zform t15) {
        this.t15 = t15;
    }

    public Zform getT16() {
        return t16;
    }

    public void setT16(Zform t16) {
        this.t16 = t16;
    }

    public Zform getT17() {
        return t17;
    }

    public void setT17(Zform t17) {
        this.t17 = t17;
    }

    public Zform getT18() {
        return t18;
    }

    public void setT18(Zform t18) {
        this.t18 = t18;
    }

    public Zform getT19() {
        return t19;
    }

    public void setT19(Zform t19) {
        this.t19 = t19;
    }

    public Zform getT20() {
        return t20;
    }

    public void setT20(Zform t20) {
        this.t20 = t20;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_EN() {
        return name_EN;
    }

    public void setName_EN(String name_EN) {
        this.name_EN = name_EN;
    }

    public String getFullPathName_EN() {
        return fullPathName_EN;
    }

    public void setFullPathName_EN(String fullPathName_EN) {
        this.fullPathName_EN = fullPathName_EN;
    }

    public String getFullPathName() {
        return fullPathName;
    }

    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getFormNo() {
        return formNo;
    }

    public String getTableOrViewName() {
        if (StringUtils.isNotBlank(tableOrViewName)) {
            return tableOrViewName;
        }
        if (StringUtils.isNotBlank(versionSchema)) {
            String tableName = formNo;
            if (tableName.indexOf(".") != -1) {
                tableName = tableName.substring(tableName.indexOf(".") + 1);
            }
            tableName = versionSchema + "." + tableName.substring(tableName.indexOf(".") + 1);
            return tableName;
        } else {
            if (formNo != null && false == "".equals(formNo) && formNo.indexOf("@") != -1) {
                if (StringUtils.isNotEmpty(formNoExt)) {
                    return " " + formNoExt + " ";
                } else {
                    return formNo.substring(0, formNo.indexOf("@"));
                }
            } else {
                return formNo;
            }
        }
    }

    public void setFormNo(String formNo) {
        this.formNo = formNo;
    }

    public String getFormNoExt() {
        return formNoExt;
    }

    public void setFormNoExt(String formNoExt) {
        this.formNoExt = formNoExt;
    }

    public String getVersionSchema() {
        return versionSchema;
    }

    public void setVersionSchema(String versionSchema) {
        this.versionSchema = versionSchema;
    }

    public List<Zform> getChildren() {
        return children;
    }

    public void setChildren(List<Zform> children) {
        this.children = children;
    }

    public String getFk() {
        return fk;
    }

    public void setFk(String fk) {
        this.fk = fk;
    }

    public String getS31() {
        return s31;
    }

    public void setS31(String s31) {
        this.s31 = s31;
    }

    public String getS32() {
        return s32;
    }

    public void setS32(String s32) {
        this.s32 = s32;
    }

    public String getS33() {
        return s33;
    }

    public void setS33(String s33) {
        this.s33 = s33;
    }

    public String getS34() {
        return s34;
    }

    public void setS34(String s34) {
        this.s34 = s34;
    }

    public String getS35() {
        return s35;
    }

    public void setS35(String s35) {
        this.s35 = s35;
    }

    public String getS36() {
        return s36;
    }

    public void setS36(String s36) {
        this.s36 = s36;
    }

    public String getS37() {
        return s37;
    }

    public void setS37(String s37) {
        this.s37 = s37;
    }

    public String getS38() {
        return s38;
    }

    public void setS38(String s38) {
        this.s38 = s38;
    }

    public String getS39() {
        return s39;
    }

    public void setS39(String s39) {
        this.s39 = s39;
    }

    public String getS40() {
        return s40;
    }

    public void setS40(String s40) {
        this.s40 = s40;
    }

    public String getS41() {
        return s41;
    }

    public void setS41(String s41) {
        this.s41 = s41;
    }

    public String getS42() {
        return s42;
    }

    public void setS42(String s42) {
        this.s42 = s42;
    }

    public String getS43() {
        return s43;
    }

    public void setS43(String s43) {
        this.s43 = s43;
    }

    public String getS44() {
        return s44;
    }

    public void setS44(String s44) {
        this.s44 = s44;
    }

    public String getS45() {
        return s45;
    }

    public void setS45(String s45) {
        this.s45 = s45;
    }

    public String getS46() {
        return s46;
    }

    public void setS46(String s46) {
        this.s46 = s46;
    }

    public String getS47() {
        return s47;
    }

    public void setS47(String s47) {
        this.s47 = s47;
    }

    public String getS48() {
        return s48;
    }

    public void setS48(String s48) {
        this.s48 = s48;
    }

    public String getS49() {
        return s49;
    }

    public void setS49(String s49) {
        this.s49 = s49;
    }

    public String getS50() {
        return s50;
    }

    public void setS50(String s50) {
        this.s50 = s50;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD01() {
        return d01;
    }

    public void setD01(Date d01) {
        this.d01 = d01;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD02() {
        return d02;
    }

    public void setD02(Date d02) {
        this.d02 = d02;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD03() {
        return d03;
    }

    public void setD03(Date d03) {
        this.d03 = d03;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD04() {
        return d04;
    }

    public void setD04(Date d04) {
        this.d04 = d04;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD05() {
        return d05;
    }

    public void setD05(Date d05) {
        this.d05 = d05;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD06() {
        return d06;
    }

    public void setD06(Date d06) {
        this.d06 = d06;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD07() {
        return d07;
    }

    public void setD07(Date d07) {
        this.d07 = d07;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD08() {
        return d08;
    }

    public void setD08(Date d08) {
        this.d08 = d08;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getD09() {
        return d09;
    }

    public void setD09(Date d09) {
        this.d09 = d09;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD01() {
        return beginD01;
    }

    public void setBeginD01(Date beginD01) {
        this.d01 = beginD01;
        this.beginD01 = beginD01;
    }
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD01() {
        return endD01;
    }

    public void setEndD01(Date endD01) {
        this.endD01 = endD01;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD02() {
        return beginD02;
    }

    public void setBeginD02(Date beginD02) {
        this.d02 = beginD02;
        this.beginD02 = beginD02;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD02() {
        return endD02;
    }

    public void setEndD02(Date endD02) {
        this.endD02 = endD02;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD03() {
        return beginD03;
    }

    public void setBeginD03(Date beginD03) {
        this.d03 = beginD03;
        this.beginD03 = beginD03;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD03() {
        return endD03;
    }

    public void setEndD03(Date endD03) {
        this.endD03 = endD03;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD04() {
        return beginD04;
    }

    public void setBeginD04(Date beginD04) {
        this.d04 = beginD04;
        this.beginD04 = beginD04;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD04() {
        return endD04;
    }

    public void setEndD04(Date endD04) {
        this.endD04 = endD04;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD05() {
        return beginD05;
    }

    public void setBeginD05(Date beginD05) {
        this.d05 = beginD05;
        this.beginD05 = beginD05;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD05() {
        return endD05;
    }

    public void setEndD05(Date endD05) {
        this.endD05 = endD05;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD06() {
        return beginD06;
    }

    public void setBeginD06(Date beginD06) {
        this.d06 = beginD06;
        this.beginD06 = beginD06;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD06() {
        return endD06;
    }

    public void setEndD06(Date endD06) {
        this.endD06 = endD06;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD07() {
        return beginD07;
    }

    public void setBeginD07(Date beginD07) {
        this.d07 = beginD07;
        this.beginD07 = beginD07;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD07() {
        return endD07;
    }

    public void setEndD07(Date endD07) {
        this.endD07 = endD07;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD08() {
        return beginD08;
    }

    public void setBeginD08(Date beginD08) {
        this.d08 = beginD08;
        this.beginD08 = beginD08;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD08() {
        return endD08;
    }

    public void setEndD08(Date endD08) {
        this.endD08 = endD08;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getBeginD09() {
        return beginD09;
    }

    public void setBeginD09(Date beginD09) {
        this.d09 = beginD09;
        this.beginD09 = beginD09;
    }

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public Date getEndD09() {
        return endD09;
    }

    public void setEndD09(Date endD09) {
        this.endD09 = endD09;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD11() {
        return d11;
    }

    public void setD11(Date d11) {
        this.d11 = d11;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD12() {
        return d12;
    }

    public void setD12(Date d12) {
        this.d12 = d12;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD13() {
        return d13;
    }

    public void setD13(Date d13) {
        this.d13 = d13;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD14() {
        return d14;
    }

    public void setD14(Date d14) {
        this.d14 = d14;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD15() {
        return d15;
    }

    public void setD15(Date d15) {
        this.d15 = d15;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD16() {
        return d16;
    }

    public void setD16(Date d16) {
        this.d16 = d16;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD17() {
        return d17;
    }

    public void setD17(Date d17) {
        this.d17 = d17;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD18() {
        return d18;
    }

    public void setD18(Date d18) {
        this.d18 = d18;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getD19() {
        return d19;
    }

    public void setD19(Date d19) {
        this.d19 = d19;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD11() {
        return beginD11;
    }

    public void setBeginD11(Date beginD11) {
        this.d11 = beginD11;
        this.beginD11 = beginD11;
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD11() {
        return endD11;
    }

    public void setEndD11(Date endD11) {
        this.endD11 = endD11;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD12() {
        return beginD12;
    }

    public void setBeginD12(Date beginD12) {
        this.d12 = beginD12;
        this.beginD12 = beginD12;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD12() {
        return endD12;
    }

    public void setEndD12(Date endD12) {
        this.endD12 = endD12;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD13() {
        return beginD13;
    }

    public void setBeginD13(Date beginD13) {
        this.d13 = beginD13;
        this.beginD13 = beginD13;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD13() {
        return endD13;
    }

    public void setEndD13(Date endD13) {
        this.endD13 = endD13;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD14() {
        return beginD14;
    }

    public void setBeginD14(Date beginD14) {
        this.d14 = beginD14;
        this.beginD14 = beginD14;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD14() {
        return endD14;
    }

    public void setEndD14(Date endD14) {
        this.endD14 = endD14;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD15() {
        return beginD15;
    }

    public void setBeginD15(Date beginD15) {
        this.d15 = beginD15;
        this.beginD15 = beginD15;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD15() {
        return endD15;
    }

    public void setEndD15(Date endD15) {
        this.endD15 = endD15;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD16() {
        return beginD16;
    }

    public void setBeginD16(Date beginD16) {
        this.d16 = beginD16;
        this.beginD16 = beginD16;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD16() {
        return endD16;
    }

    public void setEndD16(Date endD16) {
        this.endD16 = endD16;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD17() {
        return beginD17;
    }

    public void setBeginD17(Date beginD17) {
        this.d17 = beginD17;
        this.beginD17 = beginD17;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD17() {
        return endD17;
    }

    public void setEndD17(Date endD17) {
        this.endD17 = endD17;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD18() {
        return beginD18;
    }

    public void setBeginD18(Date beginD18) {
        this.d18 = beginD18;
        this.beginD18 = beginD18;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD18() {
        return endD18;
    }

    public void setEndD18(Date endD18) {
        this.endD18 = endD18;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginD19() {
        return beginD19;
    }

    public void setBeginD19(Date beginD19) {
        this.d19 = beginD19;
        this.beginD19 = beginD19;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndD19() {
        return endD19;
    }

    public void setEndD19(Date endD19) {
        this.endD19 = endD19;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD21() {
        return d21;
    }

    public void setD21(Date d21) {
        this.d21 = d21;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD22() {
        return d22;
    }

    public void setD22(Date d22) {
        this.d22 = d22;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD23() {
        return d23;
    }

    public void setD23(Date d23) {
        this.d23 = d23;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD24() {
        return d24;
    }

    public void setD24(Date d24) {
        this.d24 = d24;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD25() {
        return d25;
    }

    public void setD25(Date d25) {
        this.d25 = d25;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD26() {
        return d26;
    }

    public void setD26(Date d26) {
        this.d26 = d26;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD27() {
        return d27;
    }

    public void setD27(Date d27) {
        this.d27 = d27;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD28() {
        return d28;
    }

    public void setD28(Date d28) {
        this.d28 = d28;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getD29() {
        return d29;
    }

    public void setD29(Date d29) {
        this.d29 = d29;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD21() {
        return beginD21;
    }

    public void setBeginD21(Date beginD21) {
        this.d21 = beginD21;
        this.beginD21 = beginD21;
    }
    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD21() {
        return endD21;
    }

    public void setEndD21(Date endD21) {
        this.endD21 = endD21;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD22() {
        return beginD22;
    }

    public void setBeginD22(Date beginD22) {
        this.d22 = beginD22;
        this.beginD22 = beginD22;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD22() {
        return endD22;
    }

    public void setEndD22(Date endD22) {
        this.endD22 = endD22;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD23() {
        return beginD23;
    }

    public void setBeginD23(Date beginD23) {
        this.d23 = beginD23;
        this.beginD23 = beginD23;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD23() {
        return endD23;
    }

    public void setEndD23(Date endD23) {
        this.endD23 = endD23;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD24() {
        return beginD24;
    }

    public void setBeginD24(Date beginD24) {
        this.d24 = beginD24;
        this.beginD24 = beginD24;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD24() {
        return endD24;
    }

    public void setEndD24(Date endD24) {
        this.endD24 = endD24;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD25() {
        return beginD25;
    }

    public void setBeginD25(Date beginD25) {
        this.d25 = beginD25;
        this.beginD25 = beginD25;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD25() {
        return endD25;
    }

    public void setEndD25(Date endD25) {
        this.endD25 = endD25;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD26() {
        return beginD26;
    }

    public void setBeginD26(Date beginD26) {
        this.d26 = beginD26;
        this.beginD26 = beginD26;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD26() {
        return endD26;
    }

    public void setEndD26(Date endD26) {
        this.endD26 = endD26;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD27() {
        return beginD27;
    }

    public void setBeginD27(Date beginD27) {
        this.d27 = beginD27;
        this.beginD27 = beginD27;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD27() {
        return endD27;
    }

    public void setEndD27(Date endD27) {
        this.endD27 = endD27;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD28() {
        return beginD28;
    }

    public void setBeginD28(Date beginD28) {
        this.d28 = beginD28;
        this.beginD28 = beginD28;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD28() {
        return endD28;
    }

    public void setEndD28(Date endD28) {
        this.endD28 = endD28;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getBeginD29() {
        return beginD29;
    }

    public void setBeginD29(Date beginD29) {
        this.d29 = beginD29;
        this.beginD29 = beginD29;
    }

    @JsonFormat(pattern = "yyyy", timezone = "GMT+8")
    public Date getEndD29() {
        return endD29;
    }

    public void setEndD29(Date endD29) {
        this.endD29 = endD29;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD31() {
        return d31;
    }

    public void setD31(Date d31) {
        this.d31 = d31;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD32() {
        return d32;
    }

    public void setD32(Date d32) {
        this.d32 = d32;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD33() {
        return d33;
    }

    public void setD33(Date d33) {
        this.d33 = d33;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD34() {
        return d34;
    }

    public void setD34(Date d34) {
        this.d34 = d34;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD35() {
        return d35;
    }

    public void setD35(Date d35) {
        this.d35 = d35;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD36() {
        return d36;
    }

    public void setD36(Date d36) {
        this.d36 = d36;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD37() {
        return d37;
    }

    public void setD37(Date d37) {
        this.d37 = d37;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD38() {
        return d38;
    }

    public void setD38(Date d38) {
        this.d38 = d38;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getD39() {
        return d39;
    }

    public void setD39(Date d39) {
        this.d39 = d39;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD31() {
        return beginD31;
    }

    public void setBeginD31(Date beginD31) {
        this.d31 = beginD31;
        this.beginD31 = beginD31;
    }
    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD31() {
        return endD31;
    }

    public void setEndD31(Date endD31) {
        this.endD31 = endD31;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD32() {
        return beginD32;
    }

    public void setBeginD32(Date beginD32) {
        this.d32 = beginD32;
        this.beginD32 = beginD32;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD32() {
        return endD32;
    }

    public void setEndD32(Date endD32) {
        this.endD32 = endD32;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD33() {
        return beginD33;
    }

    public void setBeginD33(Date beginD33) {
        this.d33 = beginD33;
        this.beginD33 = beginD33;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD33() {
        return endD33;
    }

    public void setEndD33(Date endD33) {
        this.endD33 = endD33;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD34() {
        return beginD34;
    }

    public void setBeginD34(Date beginD34) {
        this.d34 = beginD34;
        this.beginD34 = beginD34;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD34() {
        return endD34;
    }

    public void setEndD34(Date endD34) {
        this.endD34 = endD34;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD35() {
        return beginD35;
    }

    public void setBeginD35(Date beginD35) {
        this.d35 = beginD35;
        this.beginD35 = beginD35;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD35() {
        return endD35;
    }

    public void setEndD35(Date endD35) {
        this.endD35 = endD35;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD36() {
        return beginD36;
    }

    public void setBeginD36(Date beginD36) {
        this.d36 = beginD36;
        this.beginD36 = beginD36;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD36() {
        return endD36;
    }

    public void setEndD36(Date endD36) {
        this.endD36 = endD36;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD37() {
        return beginD37;
    }

    public void setBeginD37(Date beginD37) {
        this.d37 = beginD37;
        this.beginD37 = beginD37;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD37() {
        return endD37;
    }

    public void setEndD37(Date endD37) {
        this.endD37 = endD37;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD38() {
        return beginD38;
    }

    public void setBeginD38(Date beginD38) {
        this.d38 = beginD38;
        this.beginD38 = beginD38;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD38() {
        return endD38;
    }

    public void setEndD38(Date endD38) {
        this.endD38 = endD38;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getBeginD39() {
        return beginD39;
    }

    public void setBeginD39(Date beginD39) {
        this.d39 = beginD39;
        this.beginD39 = beginD39;
    }

    @JsonFormat(pattern = "yyyy-MM", timezone = "GMT+8")
    public Date getEndD39() {
        return endD39;
    }

    public void setEndD39(Date endD39) {
        this.endD39 = endD39;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD41() {
        return d41;
    }

    public void setD41(Date d41) {
        this.d41 = d41;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD42() {
        return d42;
    }

    public void setD42(Date d42) {
        this.d42 = d42;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD43() {
        return d43;
    }

    public void setD43(Date d43) {
        this.d43 = d43;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD44() {
        return d44;
    }

    public void setD44(Date d44) {
        this.d44 = d44;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD45() {
        return d45;
    }

    public void setD45(Date d45) {
        this.d45 = d45;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD46() {
        return d46;
    }

    public void setD46(Date d46) {
        this.d46 = d46;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD47() {
        return d47;
    }

    public void setD47(Date d47) {
        this.d47 = d47;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD48() {
        return d48;
    }

    public void setD48(Date d48) {
        this.d48 = d48;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getD49() {
        return d49;
    }

    public void setD49(Date d49) {
        this.d49 = d49;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD41() {
        return beginD41;
    }

    public void setBeginD41(Date beginD41) {
        this.d41 = beginD41;
        this.beginD41 = beginD41;
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD41() {
        return endD41;
    }

    public void setEndD41(Date endD41) {
        this.endD41 = endD41;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD42() {
        return beginD42;
    }

    public void setBeginD42(Date beginD42) {
        this.d42 = beginD42;
        this.beginD42 = beginD42;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD42() {
        return endD42;
    }

    public void setEndD42(Date endD42) {
        this.endD42 = endD42;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD43() {
        return beginD43;
    }

    public void setBeginD43(Date beginD43) {
        this.d43 = beginD43;
        this.beginD43 = beginD43;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD43() {
        return endD43;
    }

    public void setEndD43(Date endD43) {
        this.endD43 = endD43;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD44() {
        return beginD44;
    }

    public void setBeginD44(Date beginD44) {
        this.d44 = beginD44;
        this.beginD44 = beginD44;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD44() {
        return endD44;
    }

    public void setEndD44(Date endD44) {
        this.endD44 = endD44;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD45() {
        return beginD45;
    }

    public void setBeginD45(Date beginD45) {
        this.d45 = beginD45;
        this.beginD45 = beginD45;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD45() {
        return endD45;
    }

    public void setEndD45(Date endD45) {
        this.endD45 = endD45;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD46() {
        return beginD46;
    }

    public void setBeginD46(Date beginD46) {
        this.d46 = beginD46;
        this.beginD46 = beginD46;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD46() {
        return endD46;
    }

    public void setEndD46(Date endD46) {
        this.endD46 = endD46;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD47() {
        return beginD47;
    }

    public void setBeginD47(Date beginD47) {
        this.d47 = beginD47;
        this.beginD47 = beginD47;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD47() {
        return endD47;
    }

    public void setEndD47(Date endD47) {
        this.endD47 = endD47;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD48() {
        return beginD48;
    }

    public void setBeginD48(Date beginD48) {
        this.d48 = beginD48;
        this.beginD48 = beginD48;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD48() {
        return endD48;
    }

    public void setEndD48(Date endD48) {
        this.endD48 = endD48;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getBeginD49() {
        return beginD49;
    }

    public void setBeginD49(Date beginD49) {
        this.d49 = beginD49;
        this.beginD49 = beginD49;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "GMT+8")
    public Date getEndD49() {
        return endD49;
    }

    public void setEndD49(Date endD49) {
        this.endD49 = endD49;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD51() {
        return d51;
    }

    public void setD51(Date d51) {
        this.d51 = d51;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD52() {
        return d52;
    }

    public void setD52(Date d52) {
        this.d52 = d52;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD53() {
        return d53;
    }

    public void setD53(Date d53) {
        this.d53 = d53;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD54() {
        return d54;
    }

    public void setD54(Date d54) {
        this.d54 = d54;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD55() {
        return d55;
    }

    public void setD55(Date d55) {
        this.d55 = d55;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD56() {
        return d56;
    }

    public void setD56(Date d56) {
        this.d56 = d56;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD57() {
        return d57;
    }

    public void setD57(Date d57) {
        this.d57 = d57;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD58() {
        return d58;
    }

    public void setD58(Date d58) {
        this.d58 = d58;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getD59() {
        return d59;
    }

    public void setD59(Date d59) {
        this.d59 = d59;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD51() {
        return beginD51;
    }

    public void setBeginD51(Date beginD51) {
        this.d51 = beginD51;
        this.beginD51 = beginD51;
    }
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD51() {
        return endD51;
    }

    public void setEndD51(Date endD51) {
        this.endD51 = endD51;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD52() {
        return beginD52;
    }

    public void setBeginD52(Date beginD52) {
        this.d52 = beginD52;
        this.beginD52 = beginD52;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD52() {
        return endD52;
    }

    public void setEndD52(Date endD52) {
        this.endD52 = endD52;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD53() {
        return beginD53;
    }

    public void setBeginD53(Date beginD53) {
        this.d53 = beginD53;
        this.beginD53 = beginD53;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD53() {
        return endD53;
    }

    public void setEndD53(Date endD53) {
        this.endD53 = endD53;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD54() {
        return beginD54;
    }

    public void setBeginD54(Date beginD54) {
        this.d54 = beginD54;
        this.beginD54 = beginD54;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD54() {
        return endD54;
    }

    public void setEndD54(Date endD54) {
        this.endD54 = endD54;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD55() {
        return beginD55;
    }

    public void setBeginD55(Date beginD55) {
        this.d55 = beginD55;
        this.beginD55 = beginD55;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD55() {
        return endD55;
    }

    public void setEndD55(Date endD55) {
        this.endD55 = endD55;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD56() {
        return beginD56;
    }

    public void setBeginD56(Date beginD56) {
        this.d56 = beginD56;
        this.beginD56 = beginD56;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD56() {
        return endD56;
    }

    public void setEndD56(Date endD56) {
        this.endD56 = endD56;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD57() {
        return beginD57;
    }

    public void setBeginD57(Date beginD57) {
        this.d57 = beginD57;
        this.beginD57 = beginD57;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD57() {
        return endD57;
    }

    public void setEndD57(Date endD57) {
        this.endD57 = endD57;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD58() {
        return beginD58;
    }

    public void setBeginD58(Date beginD58) {
        this.d58 = beginD58;
        this.beginD58 = beginD58;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD58() {
        return endD58;
    }

    public void setEndD58(Date endD58) {
        this.endD58 = endD58;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getBeginD59() {
        return beginD59;
    }

    public void setBeginD59(Date beginD59) {
        this.d59 = beginD59;
        this.beginD59 = beginD59;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    public Date getEndD59() {
        return endD59;
    }

    public void setEndD59(Date endD59) {
        this.endD59 = endD59;
    }

    public String getSecLevel() {
        return secLevel;
    }

    public void setSecLevel(String secLevel) {
        this.secLevel = secLevel;
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getCountersign01() {
        return countersign01;
    }

    public void setCountersign01(String countersign01) {
        this.countersign01 = countersign01;
    }

    public String getCountersign02() {
        return countersign02;
    }

    public void setCountersign02(String countersign02) {
        this.countersign02 = countersign02;
    }

    public String getCountersign03() {
        return countersign03;
    }

    public void setCountersign03(String countersign03) {
        this.countersign03 = countersign03;
    }

    public String getCountersign04() {
        return countersign04;
    }

    public void setCountersign04(String countersign04) {
        this.countersign04 = countersign04;
    }

    public String getCountersign05() {
        return countersign05;
    }

    public void setCountersign05(String countersign05) {
        this.countersign05 = countersign05;
    }

    public String getTempcountersign01() {
        return tempcountersign01;
    }

    public void setTempcountersign01(String tempcountersign01) {
        this.tempcountersign01 = tempcountersign01;
    }

    public String getTempcountersign02() {
        return tempcountersign02;
    }

    public void setTempcountersign02(String tempcountersign02) {
        this.tempcountersign02 = tempcountersign02;
    }

    public String getTempcountersign03() {
        return tempcountersign03;
    }

    public void setTempcountersign03(String tempcountersign03) {
        this.tempcountersign03 = tempcountersign03;
    }

    public String getTempcountersign04() {
        return tempcountersign04;
    }

    public void setTempcountersign04(String tempcountersign04) {
        this.tempcountersign04 = tempcountersign04;
    }

    public String getTempcountersign05() {
        return tempcountersign05;
    }

    public void setTempcountersign05(String tempcountersign05) {
        this.tempcountersign05 = tempcountersign05;
    }

    public String getContentGroupId() {
        return contentGroupId;
    }

    public void setContentGroupId(String contentGroupId) {
        this.contentGroupId = contentGroupId;
    }

    public String getNewContentGroupId() {
        newContentGroupId = UUID.randomUUID().toString().replaceAll("-", "");
        return newContentGroupId;
    }

    public boolean isSkipDictViewList01(){
        return skipDictViewList01;
    }

    public void setSkipDictViewList01(boolean skipDictViewList01){
        this.skipDictViewList01 = skipDictViewList01;
    }

    public boolean isSkipDictViewList02(){
        return skipDictViewList02;
    }

    public void setSkipDictViewList02(boolean skipDictViewList02){
        this.skipDictViewList02 = skipDictViewList02;
    }

    public boolean isSkipDictViewList03(){
        return skipDictViewList03;
    }

    public void setSkipDictViewList03(boolean skipDictViewList03){
        this.skipDictViewList03 = skipDictViewList03;
    }

    public boolean isSkipDictViewList04(){
        return skipDictViewList04;
    }

    public void setSkipDictViewList04(boolean skipDictViewList04){
        this.skipDictViewList04 = skipDictViewList04;
    }

    public boolean isSkipDictViewList05(){
        return skipDictViewList05;
    }

    public void setSkipDictViewList05(boolean skipDictViewList05){
        this.skipDictViewList05 = skipDictViewList05;
    }
    public List<DictView> getDictViewList01() {
        return dictViewList01;
    }

    public void setDictViewList01(List<DictView> dictViewList01) {
        this.dictViewList01 = dictViewList01;
    }

    public List<DictView> getDictViewList02() {
        return dictViewList02;
    }

    public void setDictViewList02(List<DictView> dictViewList02) {
        this.dictViewList02 = dictViewList02;
    }

    public List<DictView> getDictViewList03() {
        return dictViewList03;
    }

    public void setDictViewList03(List<DictView> dictViewList03) {
        this.dictViewList03 = dictViewList03;
    }

    public List<DictView> getDictViewList04() {
        return dictViewList04;
    }

    public void setDictViewList04(List<DictView> dictViewList04) {
        this.dictViewList04 = dictViewList04;
    }

    public List<DictView> getDictViewList05() {
        return dictViewList05;
    }

    public void setDictViewList05(List<DictView> dictViewList05) {
        this.dictViewList05 = dictViewList05;
    }

    public List<List<Zform>> getAllChildrenLists() {
        return Arrays.asList(
                childrenList01, childrenList02, childrenList03, childrenList04, childrenList05,
                childrenList06, childrenList07, childrenList08, childrenList09, childrenList10,
                childrenList11, childrenList12, childrenList13, childrenList14, childrenList15,
                childrenList16, childrenList17, childrenList18, childrenList19
        );
    }
}
