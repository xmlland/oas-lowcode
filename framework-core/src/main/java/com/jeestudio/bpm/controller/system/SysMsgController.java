package com.jeestudio.bpm.controller.system;

import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.system.SysMsg;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.service.system.SysMsgService;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 系统设置系统消息
 */
@Tag(name = "系统消息")
@RestController
@RequestMapping("${adminPath}/system/sysMsg")
public class SysMsgController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(SysMsgController.class);

    @Autowired
    SysMsgService sysMsgService;

    /**
     * 获取消息列表
     */
    @Operation(summary = "获取消息列表")
    @RequiresPermissions("user")
    @PostMapping("/data")
    public ResultJson data(@RequestBody SysMsg sysMsg) {
        sysMsg.setRecipient(new User(currentUserId.get()));
        Page<SysMsg> page = sysMsgService.findPage(new Page<SysMsg>(sysMsg.getPageParam().getPageNo(), sysMsg.getPageParam().getPageSize()), sysMsg);
        return ResultJson.success().setRows(page.getList()).setTotal(page.getCount());
    }

    /**
     * 标记已读
     */
    @Operation(summary = "标记已读")
    @RequiresPermissions("user")
    @PostMapping("/setRead")
    public ResultJson setRead(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        SysMsg sysMsg = sysMsgService.get(id);
        if (Global.YES.equals(sysMsg.getStatus())) {
            sysMsg.setReadTime(new Date());
            sysMsg.setStatus(Global.NO);
            sysMsgService.save(sysMsg);
        }
        return ResultJson.success("消息已读");
    }

    /**
     * 全部标记已读
     */
    @Operation(summary = "全部标记已读")
    @RequiresPermissions("user")
    @PostMapping("/setReadAll")
    public ResultJson setReadAll() {
        String message = "";
        SysMsg sysMsg = new SysMsg();
        sysMsg.setRecipient(UserUtil.getByLoginName(currentUserName.get()));
        sysMsg.setStatus(Global.YES);
        List<SysMsg> list = sysMsgService.findList(sysMsg);
        if (list == null || list.size() == 0) {
            message = "无未读消息";
        } else {
            for (SysMsg msg : list) {
                msg.setReadTime(new Date());
                msg.setStatus(Global.NO);
                sysMsgService.save(msg);
            }
            message = "标记已读成功";
        }
        return ResultJson.success(message);
    }

    /**
     * 批量标记已读
     */
    @Operation(summary = "批量标记已读")
    @RequiresPermissions("user")
    @PostMapping("/setReadBatch")
    public ResultJson setReadBatch(@RequestBody Map<String, String> body) {
        String ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResultJson.failed("参数 ids 不能为空");
        }
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SysMsg sysMsg = sysMsgService.get(id.trim());
            if (sysMsg != null && Global.YES.equals(sysMsg.getStatus())) {
                sysMsg.setReadTime(new Date());
                sysMsg.setStatus(Global.NO);
                sysMsgService.save(sysMsg);
            }
        }
        return ResultJson.success("批量标记已读成功");
    }

    /**
     * 获取未读消息数
     */
    @Operation(summary = "获取未读消息数")
    @RequiresPermissions("user")
    @GetMapping("/getUnreadCount")
    public ResultJson getUnreadCount() {
        Integer count = sysMsgService.getUnreadCount(currentUserId.get(), Global.YES);
        return ResultJson.success().setTotal(count);
    }
}
