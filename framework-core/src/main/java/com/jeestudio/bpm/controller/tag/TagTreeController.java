package com.jeestudio.bpm.controller.tag;

import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.service.system.*;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.jetbrains.annotations.Nullable;

/**
 * @Description: 机构、用户和行政区等树
 */
@Tag(name = "树选择")
@RestController
@RequestMapping("${adminPath}/sys/tagTree")
public class TagTreeController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(TagTreeController.class);

    @Autowired
    UserService userService;

    @Autowired
    OfficeService officeService;

    @Autowired
    AreaService areaService;

    @Autowired
    MenuDataService menuDataService;

    @Autowired
    DatapermissionService datapermissionService;

    /**
     * 异步获取用户树
     */
    @Operation(summary = "异步获取用户树")
    @RequiresPermissions("user")
    @PostMapping("/userTreeAsync")
    public ResultJson userTreeAsync(@RequestParam("id") String id) {
        return ResultJson.success().put("data", userService.getUserTagTreeAsync(id));
    }

    /**
     * 获取用户树
     */
    @Operation(summary = "获取用户树")
    @RequiresPermissions("user")
    @PostMapping("/userTree")
    public ResultJson userTree(@RequestBody @Nullable GridselectParam gridselectParam) {
        return ResultJson.success().put("data", userService.getUserTagTree(gridselectParam, currentUserName.get()));
    }

    /**
     * 获取机构树
     */
    @Operation(summary = "获取机构树")
    @RequiresPermissions("user")
    @PostMapping("/officeTree")
    public ResultJson officeTree() {
        return ResultJson.success().put("data", officeService.getOfficeTagTree());
    }

    /**
     * 异步获取机构树
     */
    @Operation(summary = "异步获取机构树")
    @RequiresPermissions("user")
    @PostMapping("/officeTreeAsync")
    public ResultJson officeTreeAsync(@RequestParam("id") String id) {
        return ResultJson.success().put("data", officeService.getOfficeTagTreeAsync(id));
    }

    /**
     * 异步获取区域树
     */
    @Operation(summary = "异步获取区域树")
    @RequiresPermissions("user")
    @PostMapping("/areaTreeAsync")
    public ResultJson areaTreeAsync(@RequestParam("id") String id) {
        return ResultJson.success().put("data", areaService.getAreaTagTreeAsync(id));
    }

    /**
     * 获取区域树
     */
    @Operation(summary = "获取区域树")
    @RequiresPermissions("user")
    @PostMapping("/areaTree")
    public ResultJson areaTree() {
        return ResultJson.success().put("data", areaService.getAreaTagTree());
    }

    /**
     * 获取区域子树
     */
    @Operation(summary = "获取区域子树")
    //@RequiresPermissions("user")
    @PostMapping("/areaSubTree")
    public ResultJson areaSubTree(@RequestParam("id") String id) {
        return ResultJson.success().put("data", areaService.getAreaTagTree(id));
    }

    /**
     * 获取菜单树
     */
    @Operation(summary = "获取菜单树")
    @RequiresPermissions("user")
    @GetMapping("/menuTree")
    public ResultJson menuTree() {
        return ResultJson.success().put("data", menuDataService.getMenuTagTree());
    }

    /**
     * 获取数据权限树
     */
    @Operation(summary = "获取数据权限树")
    @RequiresPermissions("user")
    @GetMapping("/dataPermissionTree")
    public ResultJson dataPermissionTree() {
        return ResultJson.success().put("data", datapermissionService.getDatapermissionTree());
    }
}
