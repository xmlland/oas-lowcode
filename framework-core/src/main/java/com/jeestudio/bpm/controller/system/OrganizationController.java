package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.param.AssignParam;
import com.jeestudio.bpm.service.system.OrganizationService;
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
 * @Description: 系统设置组织
 */
@Tag(name = "组织管理")
@RestController
@RequestMapping("${adminPath}/system/org")
public class OrganizationController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    @Autowired
    SecLogService secLogService;

    @Autowired
    OrganizationService organizationService;

    /**
     * 获取组织分配用户
     */
    @Operation(summary = "获取组织分配用户")
    @RequiresPermissions("user")
    @GetMapping("/getAssign")
    public ResultJson getAssign(@RequestParam("id") String id) {
        return ResultJson.success().put("data", organizationService.getAssign(id));
    }

    /**
     * 保存组织分配用户
     */
    @Operation(summary = "保存组织分配用户")
    @RequiresPermissions("user")
    @PostMapping("/saveAssign")
    public ResultJson saveAssign(@RequestBody AssignParam assignParam) {
        try {
            organizationService.saveAssign(assignParam.getId(), assignParam.getIds());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户成功", "分配用户", Global.YES);
            return ResultJson.success("分配用户成功");
        } catch (Exception e) {
            logger.error("Error occurred while trying to save users of organization: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "分配用户失败", "分配用户", Global.NO);
            return ResultJson.failed(e.getMessage());
        }
    }
}
