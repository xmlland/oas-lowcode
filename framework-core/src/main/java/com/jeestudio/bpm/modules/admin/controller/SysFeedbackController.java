package com.jeestudio.bpm.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.admin.entity.SysFeedbackEntity;
import com.jeestudio.bpm.modules.admin.service.SysFeedbackServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.UserUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 用户反馈
 */
@RestController
@RequestMapping("${adminPath}/admin/sysFeedback")
@Tag(name = "用户反馈")
@Slf4j
public class SysFeedbackController extends BaseController {


    @Autowired
    ZformService zformService;

    @Autowired
    SysFeedbackServiceI sysFeedbackService;


    /**
     * updateRead
     */
    @Operation(summary = "标记反馈已读")
    @RequiresPermissions("user")
    @PostMapping("/updateRead")
    public ResultJson updateRead(String id) {
        SysFeedbackEntity byId = sysFeedbackService.getById(id);
        byId.setUserRead(Global.YES);
        sysFeedbackService.updateById(byId);
        long needRead = this.countNeedRead();
        return success().put("needRead", needRead);
    }

    private long countNeedRead() {
        QueryWrapper<SysFeedbackEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_read", Global.NO);
        queryWrapper.eq("submit_user", UserUtil.getCurrentUserId());
        return sysFeedbackService.count(queryWrapper);
    }

    /**
     * queryNeedRead
     */
    @Operation(summary = "查询未读反馈")
    @RequiresPermissions("user")
    @GetMapping("/queryNeedRead")
    public ResultJson queryNeedRead() {

        long needRead = this.countNeedRead();
        return success().put("needRead", needRead);
    }
}
