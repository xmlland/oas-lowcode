package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.utils.Global;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 菜单视图
 */
@Data
public class MenuResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 父级菜单视图对象。 */
    private MenuResult parent;
    /** 菜单 ID。 */
    private String pageID;
    /** 父级菜单 ID。 */
    private String parentID;
    /** 父级菜单 ID 路径。 */
    private String parentIDS;
    /** 菜单中文名称。 */
    private String pageName;
    /** 菜单英文名称。 */
    private String pageName_EN;
    /** 菜单图标。 */
    private String pageIcon;
    /** 菜单访问地址。 */
    private String pageUrl;
    /** 菜单标记，供特殊业务入口识别。 */
    private String sign;
    /** 菜单动作地址或接口地址。 */
    private String actionUrl;
    /** 菜单类型，区分目录、菜单、按钮等。 */
    private String type;
    /** 排序号。 */
    private Integer orderNo = 30;
    /** 备注。 */
    private String remarks;
    /** 权限标识。 */
    private String permission;
    /** 链接打开目标。 */
    private String target;
    /** 是否显示。 */
    private String isShow = Global.YES;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Date createdOn;
    /** 修改人。 */
    private String modifiedBy;
    /** 修改时间。 */
    private Date modifiedOn;

    /** 前端组件路径。 */
    private String component;
    /** 前端路由路径。 */
    private String url;
    /** 是否移动端菜单。 */
    private String appMenu;

}
