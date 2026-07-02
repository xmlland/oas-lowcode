package com.jeestudio.bpm.modules.mini.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.mini.service.MiniProgramInfoServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.SecLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 小程序信息
 */
@Tag(name = "小程序信息")
@RestController
@RequestMapping("${adminPath}/mini/miniProgramInfo")
@Slf4j
public class MiniProgramInfoController extends BaseController {

    @Autowired
    private SecLogService secLogService;

    @Autowired
    ZformService zformService;

    @Autowired
    MiniProgramInfoServiceI miniProgramInfoService;

}
