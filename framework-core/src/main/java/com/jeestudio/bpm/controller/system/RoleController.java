package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.entity.system.Auth;
import com.jeestudio.bpm.common.param.AssignParam;
import com.jeestudio.bpm.service.system.RoleService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 系统设置角色
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("${adminPath}/system/role")
public class RoleController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    SecLogService secLogService;

    @Autowired
    RoleService roleService;

    /**
     * 获取角色权限
     */
    @Operation(summary = "获取角色权限")
    @RequiresPermissions("user")
    @GetMapping("/getAuth")
    public ResultJson getAuth(@RequestParam("id") String id) {
        return ResultJson.success().put("data", roleService.getAuth(id));
    }

    /**
     * 保存角色权限
     */
    @Operation(summary = "保存角色权限")
    @RequiresPermissions("user")
    @PostMapping("/saveAuth")
    public ResultJson saveAuth(@RequestBody Auth auth) {
        try {
            roleService.saveAuth(auth.getId(), auth.getIds());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "权限设置成功", "权限设置", Global.YES);
            return ResultJson.success("权限设置成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to save authorization: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "权限设置失败", "权限设置", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 获取角色分配
     */
    @Operation(summary = "获取角色分配")
    @RequiresPermissions("user")
    @GetMapping("/getAssign")
    public ResultJson getAssign(@RequestParam("id") String id) {
        return ResultJson.success().put("data", roleService.getAssign(id));
    }

    /**
     * 获取数据角色分配
     */
    @Operation(summary = "获取数据角色分配")
    @RequiresPermissions("user")
    @GetMapping("/getDataAssign")
    public ResultJson getDataAssign(@RequestParam("id") String id) {
        return ResultJson.success().put("data", roleService.getDataAssign(id));
    }

    /**
     * 保存角色分配
     */
    @Operation(summary = "保存角色分配")
    @RequiresPermissions("user")
    @PostMapping("/saveAssign")
    public ResultJson saveAssign(@RequestBody AssignParam assignParam) {
        try {
            roleService.saveAssign(assignParam.getId(), assignParam.getIds());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户成功", "分配用户", Global.YES);
            return ResultJson.success("分配用户成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to save roles assignment: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户失败", "分配用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }

    /**
     * 保存数据角色分配
     */
    @Operation(summary = "保存数据角色分配")
    @RequiresPermissions("user")
    @PostMapping("/saveDataAssign")
    public ResultJson saveDataAssign(@RequestBody AssignParam assignParam) {
        try {
            roleService.saveDataAssign(assignParam.getId(), assignParam.getIds());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户成功", "分配用户", Global.YES);
            return ResultJson.success("分配用户成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to save data roles assignment: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户失败", "分配用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }
}
