package com.jeestudio.bpm.controller.ai;

import com.jeestudio.bpm.service.ai.TransService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 翻译服务
 */
@Tag(name = "翻译服务")
@RestController
@RequestMapping("${adminPath}/ai/trans")
public class TansController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(TansController.class);

    @Autowired
    private TransService transService;

    @Operation(summary = "获取翻译结果")
    @Parameters({@Parameter(name = "query", description = "query", required = true),
            @Parameter(name = "from", description = "from", required = true),
            @Parameter(name = "to", description = "to", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getTransResult")
    public ResultJson getTransResult(@RequestParam("query") String query, @RequestParam("from") String from, @RequestParam("to") String to) {
        return ResultJson.success().put("data", transService.getTransResult(query, from, to));
    }
}
