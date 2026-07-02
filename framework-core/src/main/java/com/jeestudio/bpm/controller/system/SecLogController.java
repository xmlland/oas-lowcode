package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description: 系统设置安全日志
 */
@Tag(name = "安全日志")
@RestController
@RequestMapping("${adminPath}/system/secLog")
public class SecLogController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(SecLogController.class);

    @Autowired
    private SecLogService secLogService;

    /**
     * 获取安全日志列表
     */
    @Operation(summary = "获取安全日志列表")
    @RequiresPermissions("user")
    @PostMapping("/data")
    public ResultJson data(@RequestBody Zform zform, @RequestParam("path") String path, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag) throws Exception {
        Page<Zform> page = secLogService.data(zform, path, formNo, traceFlag, currentUserName.get(), ip.get());
        return ResultJson.success().setRows(page.getList()).setTotal(page.getCount());
    }

    /**
     * 获取安全开关
     */
    @Operation(summary = "获取安全开关")
    @RequiresPermissions("user")
    @GetMapping("/secSwitch")
    public ResultJson secSwitch() {
        return ResultJson.success().put("secSwitch", secLogService.getSecSwitch());
    }

    /**
     * 获取密码过期配置
     */
    @Operation(summary = "获取密码过期配置")
    @RequiresPermissions("user")
    @GetMapping("/passwordExpired")
    public ResultJson passwordExpired() {
        return ResultJson.success().put("passwordExpired", secLogService.passwordExpired(currentUserName.get()));
    }

    /**
     * 获取日志空间
     */
    @Operation(summary = "获取日志空间")
    @RequiresPermissions("user")
    @GetMapping("/getSecLogSpace")
    public ResultJson getSecLogSpace() {
        ResultJson resultJson = ResultJson.success();
        Map<String, String> map = secLogService.getSecLogSpace();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            resultJson.put(entry.getKey(), entry.getValue());
        }
        return resultJson;
    }

    /**
     * 检查完整性保护
     */
    @Operation(summary = "检查完整性保护")
    @RequiresPermissions("user")
    @PostMapping("/checkIntegrityProtection")
    public ResultJson checkIntegrityProtection() {
        new Thread(() -> {
            try {
                secLogService.checkIntegrityProtection();
            } catch (Exception e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }).start();
        return success();
    }
}
