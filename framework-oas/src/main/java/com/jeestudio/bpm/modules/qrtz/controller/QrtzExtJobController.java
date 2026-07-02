package com.jeestudio.bpm.modules.qrtz.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.qrtz.service.QrtzExtJobServiceI;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 定时任务
 */
@RestController
@RequestMapping("${adminPath}/qrtz/qrtzExtJob")
@Tag(name = "定时任务")
@Slf4j
public class QrtzExtJobController extends BaseController {

    @Autowired
    QrtzExtJobServiceI qrtzExtJobService;


    @Operation(summary = "暂停任务")
    @RequiresPermissions("user")
    @PostMapping("/pause")
    public ResultJson pause(@RequestParam String id) {
        qrtzExtJobService.pause(qrtzExtJobService.getById(id));
        return ResultJson.success();
    }

    @Operation(summary = "恢复任务")
    @RequiresPermissions("user")
    @PostMapping("/resume")
    public ResultJson resume(@RequestParam String id) {
        qrtzExtJobService.resume(qrtzExtJobService.getById(id));
        return ResultJson.success();
    }

    @Operation(summary = "执行任务")
    @RequiresPermissions("user")
    @PostMapping("/execute")
    public ResultJson execute(@RequestParam String id) {
        qrtzExtJobService.execute(qrtzExtJobService.getById(id));
        return ResultJson.success();
    }
}
