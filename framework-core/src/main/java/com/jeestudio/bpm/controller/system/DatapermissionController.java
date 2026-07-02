package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.param.AssignParam;
import com.jeestudio.bpm.service.system.DatapermissionService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 系统设置数据权限
 */
@Tag(name = "数据权限")
@RestController
@RequestMapping("${adminPath}/system/datapermission")
public class DatapermissionController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(DatapermissionController.class);

    @Autowired
    DatapermissionService datapermissionService;

    /**
     * 获取数据权限
     */
    @Operation(summary = "获取数据权限")
    @RequiresPermissions("user")
    @GetMapping("/getPermission")
    public ResultJson getPermission(@RequestParam("id") String id) {
        return ResultJson.success().put("data", datapermissionService.getPermission(id));
    }

    /**
     * 保存数据权限
     */
    @Operation(summary = "保存数据权限")
    @RequiresPermissions("user")
    @PostMapping("/savePermission")
    public ResultJson savePermission(@RequestBody AssignParam assignParam) {
        if (StringUtil.isEmpty(assignParam.getIds())) {
            return ResultJson.failed("请选择数据权限");
        } else {
            datapermissionService.savePermission(assignParam.getId(), assignParam.getIds());
            return ResultJson.success();
        }
    }
}
