package com.jeestudio.bpm.modules.oa.controller;

import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.oa.service.SerialNoService;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 业务编号
 */
@RestController
@RequestMapping("${adminPath}/oa/serialNo")
@Tag(name = "业务编号")
@Slf4j
public class SerialNoController extends BaseController {

    @Autowired
    private SerialNoService serialNoService;

    @Operation(summary = "生成业务编号")
    @GetMapping("/generate")
    public ResultJson generate(
            @Parameter(description = "编号前缀", required = true, example = "BK")
            @RequestParam String prefix) {
        try {
            String serialNo = serialNoService.generateSerialNo(prefix);
            return ResultJson.success().put("serialNo", serialNo);
        } catch (Exception e) {
            log.error("生成编号失败", e);
            return ResultJson.failed("生成编号失败");
        }
    }
}
