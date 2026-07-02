package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.entity.system.Auth;
import com.jeestudio.bpm.common.view.system.OfficeView;
import com.jeestudio.bpm.service.system.OfficeService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.service.system.SecLogService;
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
 * @Description: 系统设置机构
 */
@Tag(name = "机构管理")
@RestController
@RequestMapping("${adminPath}/system/office")
public class OfficeController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ZformController.class);

    @Autowired
    OfficeService officeService;

    @Autowired
    private SecLogService secLogService;

    /**
     * 获取机构列表
     */
    @Operation(summary = "获取机构列表")
    @RequiresPermissions("user")
    @GetMapping("/viewData")
    public ResultJson viewData() {
        return ResultJson.success().put("data", officeService.findOfficeViewData(new OfficeView()));
    }


    /**
     * 保存机构权限
     */
    @Operation(summary = "保存机构权限")
    @RequiresPermissions("user")
    @PostMapping("/saveAuthOffice")
    public ResultJson saveAuthOffice(@RequestBody Auth auth) {
        try {
            ResultJson resultJson = officeService.saveAuth(auth.getId(), auth.getIds());
            resultJson.setToken(token.get());
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "权限设置成功", "权限设置", Global.YES);
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to save authorization: " + ExceptionUtils.getStackTrace(e));
            secLogService.saveSecLog(currentUserName.get(), ip.get(), "权限设置失败", "权限设置", Global.NO);
            return failed();
        }
    }

    /**
     * 获取机构权限
     */
    @Operation(summary = "获取机构权限")
    @RequiresPermissions("user")
    @GetMapping("/getAuth")
    public ResultJson getAuth(@RequestParam("id") String id) {
        try {
            ResultJson resultJson = officeService.getAuth(id);
            resultJson.setToken(token.get());
            return resultJson;
        } catch (Exception e) {
            logger.error("Error occurred while trying to achieve authorization: " + ExceptionUtils.getStackTrace(e));
            return failed();
        }
    }


}
