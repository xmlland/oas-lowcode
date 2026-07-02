package com.jeestudio.bpm.modules.oa.controller;

import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.feign.RequestVo;
import com.jeestudio.bpm.modules.oa.service.OaWordTemplateServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: Word模板
 */
@RestController
@RequestMapping("${adminPath}/oa/oaWordTemplate")
@Tag(name = "Word模板")
@Slf4j
public class OaWordTemplateController extends BaseController {

    @Autowired
    private SecLogService secLogService;

    @Autowired
    ZformService zformService;

    @Autowired
    SysFileService sysFileService;

    @Autowired
    OaWordTemplateServiceI wordTemplateService;

    /**
     * exportWord
     */
    @Operation(summary = "导出Word文档")
    @RequiresPermissions("user")
    @GetMapping("/exportWord")
    public ResultJson exportWord(String fileGroupId) {
        List<SysFile> sysFiles = sysFileService.getFileListByGroupId(fileGroupId);
        return ResultJson.success().put("data", zformService.exportWord(sysFiles.get(0).getId(), new RequestVo(), "导出测试"));
    }


    /**
     * check template
     */
    @Operation(summary = "检查模板")
    @RequiresPermissions("user")
    @RequestMapping("/checkTemplate")
    public ResultJson checkTemplate() {
        return ResultJson.success().setData(wordTemplateService.checkTemplate());
    }
}
