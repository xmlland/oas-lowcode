package com.jeestudio.bpm.modules.oa.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.MenuResult;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 快捷入口
 */
@RestController
@RequestMapping("${adminPath}/oa/shortcut")
@Tag(name = "快捷入口")
@Slf4j
public class OaShortcutController extends BaseController {

    @Autowired
    private ZformService zformService;

    @Autowired
    private GenTableService genTableService;

    /**
     * 查询已定义快捷入口，查不到则按defaultSize构造一个返回
     *
     * @param subSystemCode
     * @param defaultSize
     * @return
     */
    @Operation(summary = "获取我的快捷入口")
    @RequiresPermissions("user")
    @GetMapping("/getMyShortcut")
    public ResultJson getMyShortcut(String subSystemCode, int defaultSize) throws Exception {
        String json = null;
        String shortcutFormNo = "oa_shortcut";
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        String loginName = currentUserName.get();
        queryWrapper.eq("a.login_name", loginName);
        List<LinkedHashMap> list = zformService.findMapList(shortcutFormNo, queryWrapper);
        if (list.size() == 1) {
            json = StringUtil.getString(list.get(0).get("entry_json"));
        } else {
            //构造json，保存后返回
            User user = UserUtil.get(currentUserId.get());
            List<MenuResult> menuList = UserUtil.getMenuListHt(user, subSystemCode);
            //type = 1, isShow = 1
            menuList = menuList.stream().filter(k -> k.getType().equals(Global.YES) && k.getIsShow().equals(Global.YES)).limit(defaultSize).collect(Collectors.toList());
            Gson gson = new Gson();
            json = gson.toJson(menuList);

            JSONObject shortcutMap = new JSONObject();
            shortcutMap.put("formNo", shortcutFormNo);
            shortcutMap.put("login_name", loginName);
            shortcutMap.put("entry_json", json);
            shortcutMap.put("is_sys", Global.NO);
            Zform zform = zformService.getZformFromMap(shortcutMap, loginName);
            zform.setPreId(loginName);
            GenTable shortcutGenTable = genTableService.getGenTableWithDefination(shortcutFormNo);
            zformService.save(zform, shortcutGenTable);
        }
        return ResultJson.success().put("shortcut", json);
    }

    @Operation(summary = "获取全部快捷入口")
    @RequiresPermissions("user")
    @GetMapping("/getAllShortcut")
    public ResultJson getAllShortcut(String subSystemCode) {
        User user = UserUtil.get(currentUserId.get());
        List<MenuResult> menuList = UserUtil.getMenuListHt(user, subSystemCode);
        //type = 1, isShow = 1
        menuList = menuList.stream().filter(k -> k.getType().equals(Global.YES) && k.getIsShow().equals(Global.YES) && StringUtil.isNoneEmpty(k.getPageUrl())).collect(Collectors.toList());
        Gson gson = new Gson();
        String json = gson.toJson(menuList);
        return ResultJson.success().put("shortcut", json);
    }

    @Operation(summary = "保存我的快捷入口")
    @RequiresPermissions("user")
    @PostMapping("/saveMyShortcut")
    public ResultJson saveMyShortcut(@RequestBody JSONObject shortcut) throws Exception {
        String shortcutFormNo = "oa_shortcut";
        String loginName = currentUserName.get();
        JSONObject shortcutMap = new JSONObject();
        shortcutMap.put("formNo", shortcutFormNo);
        shortcutMap.put("login_name", loginName);
        shortcutMap.put("entry_json", shortcut.getString("shortcut"));
        shortcutMap.put("is_sys", Global.NO);
        Zform zform = zformService.getZformFromMap(shortcutMap, loginName);
        zform.setId(loginName);
        GenTable shortcutGenTable = genTableService.getGenTableWithDefination(shortcutFormNo);
        zformService.save(zform, shortcutGenTable);
        return ResultJson.success();
    }
}
