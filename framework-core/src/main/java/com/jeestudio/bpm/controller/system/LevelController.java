package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.system.Level;
import com.jeestudio.bpm.service.system.LevelService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 系统设置职务级别
 */
@Tag(name = "职务级别")
@RestController
@RequestMapping("${adminPath}/system/level")
public class LevelController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ZformController.class);

    @Autowired
    LevelService levelService;

    /**
     * 获取职务级别列表
     */
    @Operation(summary = "获取职务级别列表")
    @RequiresPermissions("user")
    @PostMapping("/data")
    public ResultJson data(@RequestBody Level level) {
        Page<Level> page = levelService.findPage(new Page<Level>(level.getPageParam().getPageNo(), level.getPageParam().getPageSize(), level.getPageParam().getOrderBy()), level);
        return ResultJson.success().put("data", page);
    }
}
