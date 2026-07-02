package com.jeestudio.bpm.common.view.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 菜单权限表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysPermission implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 菜单或权限 ID。 */
	private String id;

	/** 上级菜单 ID。 */
	private String parentId;

	/** 菜单中文名称。 */
	private String name;

	/** 菜单英文名称。 */
	private String name_EN;

	/** 菜单权限编码，多个权限以逗号分隔，例如 sys:schedule:list,sys:schedule:info。 */
	private String perms;
	/** 权限策略：1 显示，2 禁用。 */
	private String permsType;

	/** 菜单图标。 */
	private String icon;

	/** 前端组件路径。 */
	private String component;

	/** 前端组件名称。 */
	private String componentName;

	/** 前端访问路径。 */
	private String url;
	/** 一级菜单跳转地址。 */
	private String redirect;

	/** 菜单排序号。 */
	private Double sortNo;

	/** 菜单类型：0 一级菜单，1 子菜单，2 按钮权限。 */
	//@Dict(dicCode = "menu_type")
	private Integer menuType;

	/** 是否叶子节点。 */
	//@TableField(value="is_leaf")
	private boolean leaf;

	/** 是否路由菜单。 */
	//@TableField(value="is_route")
	private boolean route;


	/** 是否缓存页面。 */
	//@TableField(value="keep_alive")
	private boolean keepAlive;

	/** 菜单描述。 */
	private String description;

	/** 创建人 ID。 */
	private String createBy;

	/** 删除状态：0 正常，1 已删除。 */
	private Integer delFlag;

	/** 是否配置菜单数据权限。 */
	private Integer ruleFlag;

	/** 是否隐藏路由菜单。 */
	private boolean hidden;

	/** 是否隐藏页签。 */
	private boolean hideTab;

	/** 创建时间。 */
	private Date createTime;

	/** 更新人 ID。 */
	private String updateBy;

	/** 更新时间。 */
	private Date updateTime;

	/** 按钮权限状态：0 无效，1 有效。 */
	private String status;

	/** 是否总是显示根菜单，当前保留用于兼容前端路由结构。 */
	private boolean alwaysShow;

	/** 外链菜单打开方式：false 内部打开，true 外部打开。 */
	private boolean internalOrExternal;

	public SysPermission() {

	}

	public SysPermission(boolean index) {
		if (index) {
			this.id = "9502685863ab87f0ad1134142788a385";
			this.name = "首页";
			this.component = "dashboard/Analysis";
			this.componentName = "dashboard-analysis";
			this.url = "/dashboard/analysis";
			this.icon = "home";
			this.menuType = 0;
			this.sortNo = 0.0;
			this.ruleFlag = 0;
			this.delFlag = 0;
			this.alwaysShow = false;
			this.route = true;
			this.keepAlive = true;
			this.leaf = true;
			this.hidden = false;
		}
	}
}
