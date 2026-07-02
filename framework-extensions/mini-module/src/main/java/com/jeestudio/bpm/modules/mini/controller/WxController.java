package com.jeestudio.bpm.modules.mini.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.modules.mini.service.WxService;
import com.jeestudio.bpm.service.system.MenuDataService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 微信小程序接口
 */
@RestController
@RequestMapping("${adminPath}/wx/")
@Tag(name = "微信小程序")
@Slf4j
public class WxController extends BaseController {

    @Autowired
    WxService wxService;

    @Autowired
    UserService userService;

    /** 限流记录：IP -> 最后请求时间戳 */
    private static final ConcurrentHashMap<String, Long> RATE_LIMIT_MAP = new ConcurrentHashMap<>();
    /** 限流窗口：60秒 */
    private static final long RATE_LIMIT_WINDOW_MS = TimeUnit.SECONDS.toMillis(60);
    /** 每窗口最大请求数 */
    private static final int RATE_LIMIT_MAX_REQUESTS = 10;

    /**
     * jsCodeToOpenId
     */
    @Operation(summary = "获取微信OpenId")
    @GetMapping("/jsCodeToOpenId")
    public ResultJson jsCodeToOpenId(String js_code, String id, jakarta.servlet.http.HttpServletRequest request) throws Exception {
        // 安全加固：校验 js_code 参数格式（微信js_code通常为32位字符）
        if (js_code == null || !js_code.matches("^[a-zA-Z0-9_\\-]{20,128}$")) {
            log.warn("jsCodeToOpenId收到非法js_code参数: {}", js_code);
            return ResultJson.failed("参数格式错误");
        }
        // 安全加固：基于IP的简单限流，防止暴力调用微信API
        String clientIp = getClientIp(request);
        if (!checkRateLimit(clientIp)) {
            log.warn("jsCodeToOpenId限流触发, IP: {}", clientIp);
            return ResultJson.failed("请求过于频繁，请稍后再试");
        }
        Map<String, Object> result = wxService.jsCodeToOpenId(js_code, id);
        ResultJson resultJson = ResultJson.success();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            resultJson.put(entry.getKey(), entry.getValue());
        }
        return resultJson;
    }

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    MenuDataService menuDataService;

    /**
     * updateUserPhone
     */
    @Operation(summary = "更新用户手机号")
    @RequiresPermissions("user")
    @PostMapping("/updateUserPhone")
    public ResultJson updateUserPhone(@RequestParam String code, @RequestParam String id) throws Exception {
        ResultJson resultJson = ResultJson.success();
        String userPhone = wxService.getUserPhone(code, id);
        String userId = wxService.updateUserInfo(null, null, null, userPhone, id);
        // TokenX
        ThreadLocal<String> token = new ThreadLocal<>();
        User user = userService.get(userId);
        token.set(cacheUtil.getTokenSetCache(user.getLoginName(), userId));
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
        menuDataService.refreshUserMenuCache(user.getId());
        ResultJson currentUserView = userService.getCurrentUserView(userId);
        resultJson.put("data", currentUserView.getData()).put("userPhone", userPhone);
        resultJson.setToken(token.get());
        return resultJson;
    }


    /**
     * updateUserInfo
     */
    @Operation(summary = "更新用户信息")
    @RequiresPermissions("user")
    @PostMapping("/updateUserInfo")
    public ResultJson updateUserInfo(@RequestBody JSONObject jsonObject) throws Exception {
        ResultJson resultJson = ResultJson.success();
        wxService.updateUserInfo(jsonObject.getString("name"), jsonObject.getString("email"),
                jsonObject.getString("phone"), jsonObject.getString("mobile"), null);
        return resultJson;
    }

    /**
     * 获取客户端真实IP（考虑代理场景）
     */
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    /**
     * 简单IP限流检查（固定窗口），针对 jsCodeToOpenId 接口
     */
    private boolean checkRateLimit(String ip) {
        long now = System.currentTimeMillis();
        Long lastTime = RATE_LIMIT_MAP.get(ip);
        if (lastTime == null || (now - lastTime) > RATE_LIMIT_WINDOW_MS) {
            RATE_LIMIT_MAP.put(ip, now);
            return true;
        }
        return false;
    }
}
