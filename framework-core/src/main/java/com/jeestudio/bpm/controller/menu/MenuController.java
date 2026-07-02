package com.jeestudio.bpm.controller.menu;

import com.jeestudio.bpm.common.entity.system.MenuResult;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.service.system.MenuDataService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.bpm.controller.base.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 系统管理菜单
 */
@Slf4j
@Tag(name = "菜单管理")
@RestController
@RequestMapping("${adminPath}/sys/menu")
public class MenuController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    MenuDataService menuDataService;

    @Value(value = "${sec.switch}")
    private boolean secSwitch;

    /**
     * 获取菜单列表
     */
    @Operation(summary = "获取菜单列表")
    @RequiresPermissions("user")
    @GetMapping("/getMenuList")
    public ResultJson getMenuList() {
        User user = UserUtil.get(currentUserId.get());
        List<MenuResult> menuList = UserUtil.getMenuList(user, "");
        return ResultJson.success().put("menu", menuList);
    }

    /**
     * 获取后台菜单列表
     */
    @Operation(summary = "获取后台菜单列表")
    @RequiresPermissions("user")
    @GetMapping("/getMenuListHt")
    public ResultJson getMenuListHt(String subSystemCode) {
        User user = UserUtil.get(currentUserId.get());
        List<MenuResult> menuList = UserUtil.getMenuListHt(user, subSystemCode);
        return ResultJson.success().put("menu", menuList);
    }

    /**
     * 获取设计器菜单列表
     */
    @Operation(summary = "获取设计器菜单列表")
    @RequiresPermissions("user")
    @GetMapping("/getMenuListDesign")
    public ResultJson getMenuListDesign() {
        User user = UserUtil.get(currentUserId.get());
        List<MenuResult> menuList = UserUtil.getMenuListDesign(user);
        return ResultJson.success().put("menu", menuList);
    }

    /**
     * 检查用户权限
     */
    @Operation(summary = "检查用户权限")
    @RequiresPermissions("user")
    @PostMapping("/hasPermission")
    public ResultJson hasPermission(String permission) {
        Boolean hasPermission = false;
        String userId = currentUserId.get();
        if (Global.YES.equals(userId)) {
            hasPermission = true;
        } else if (StringUtil.isEmpty(permission)) {

        } else {
            hasPermission = menuDataService.hasPermission(userId, permission);
        }
        return ResultJson.success().put("hasPermission", hasPermission);
    }

    /**
     * 刷新菜单缓存
     */
    @Operation(summary = "刷新菜单缓存")
    @RequiresPermissions("user")
    @GetMapping("/refreshMenuCache")
    public ResultJson refreshMenuCache() {
        menuDataService.refreshMenuCache();
        return ResultJson.success();
    }

    /**
     * 创建菜单分组
     */
    @Operation(summary = "创建菜单分组")
    @RequiresPermissions("user")
    @PostMapping("/createMenuGroup")
    public ResultJson createMenuGroup(String formNo, String parentId, String icon) {
        menuDataService.createMenuGroup(formNo, parentId, icon);
        return ResultJson.success();
    }

    /**
     * 获取流程菜单列表
     */
    @Operation(summary = "获取流程菜单列表")
    @RequiresPermissions("user")
    @GetMapping("/getWorkflowMenuList")
    public ResultJson getWorkflowMenuList(String processDefinitionCategory) {
        return ResultJson.success().put("workflow_menu", menuDataService.findWrokflowNodeList(processDefinitionCategory));
    }
}
