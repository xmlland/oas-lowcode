package com.jeestudio.bpm.modules.mini.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.modules.mini.entity.MiniProgramInfoEntity;
import com.jeestudio.bpm.service.system.MenuDataService;
import com.jeestudio.bpm.service.system.RoleService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.security.utils.security.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 微信小程序服务
 */
@Service
@Transactional
@Slf4j
public class WxService {

    @Autowired
    MiniProgramInfoServiceI miniProgramInfoService;

    @Autowired
    ProjectProperties projectProperties;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    private MenuDataService menuDataService;

    private static CacheUtil cacheUtil = new CacheUtil();

    private final static String code2SessionUrl = "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code";

    private final static String getPhoneNumberUrl = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token={}";
    private static final String access_token = "wx_access_token";
    private final static long access_token_expires_in = 110;//微信官方是7200秒 这里存110分钟
    private final static String accessToken_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}";

    public String getAccessToken(String appId, String secret) {

        String token = ConvertUtil.getString(cacheUtil.getObjectCache(access_token + appId));
        if (StrUtil.isEmpty(token)) {
            String httpUrl = StrUtil.format(accessToken_url, appId, secret);
            String body = HttpUtil.createGet(httpUrl).execute().body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            if (!jsonObject.containsKey("access_token")) {
                log.error("获取access_token失败,{}", body);
                throw new BusinessException("获取access_token失败");
            }
            token = jsonObject.getStr("access_token");
            cacheUtil.setObjectCacheExpireHour(access_token, token, access_token_expires_in);
            return token;
        } else {
            return token;
        }
    }

    public Map<String, Object> jsCodeToOpenId(String js_code, String id) throws Exception {
        MiniProgramInfoEntity program = miniProgramInfoService.getById(id);
        String appSecret = SM4Util.decrypt(projectProperties.getSm4Key(), program.getEncryptAppSecret());
        String httpUrl = StrUtil.format(code2SessionUrl, program.getAppId(), appSecret, js_code);
        String result = HttpUtil.get(httpUrl);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        if (jsonObject.containsKey("openid")) {
            Map<String, Object> res = new HashMap<>();
            String openid = jsonObject.getStr("openid");
            if (StrUtil.isBlank(openid)) {
                throw new BusinessException("获取openid失败");
            }
            String encrypt = SM4Util.encrypt(projectProperties.getSm4Key(), openid);
            String no = id + "_" + encrypt;
            User user = userService.getByNo(no);
            if (user == null) {
                //没有用户则创建用户
                user = new User();
                user.setNo(no);
                user.setLoginFlag(Global.YES);
                user.setUseable(Global.YES);
                user.setName(encrypt);
                user.setCompany(new Office("1"));
                user.setLoginName(encrypt);
                userService.saveUser(user, "admin");
            }
            if (!Global.YES.equals(user.getUseable())) {//Account discontinued
                throw new BusinessException("该账号已停用，无法登录");
            }
            if (StrUtil.isNotEmpty(program.getRoleIds())) {
                //绑定角色
                for (String roleId : program.getRoleIds().split(",")) {
                    roleService.saveUserRole(user.getId(), roleId);
                }
            }
            if (StrUtil.isNotEmpty(program.getDataRoleIds())) {
                //绑定数据角色
                for (String roleId : program.getDataRoleIds().split(",")) {
                    roleService.saveUserDataRoleId(user.getId(), roleId);
                }
            }
            // Token
            ThreadLocal<String> token = new ThreadLocal<>();
            token.set(cacheUtil.getTokenSetCache(encrypt, user.getId()));
            cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
            menuDataService.refreshUserMenuCache(user.getId());
            res.put("token", token.get());
            ResultJson currentUserView = userService.getCurrentUserView(user.getId());
            res.put("data", currentUserView.getData());
            return res;
        } else {
            log.error("获取openid失败,{}", jsonObject);
            throw new BusinessException("获取openid失败");
        }
    }

    public String getUserPhone(String code, String id) {
        MiniProgramInfoEntity program = miniProgramInfoService.getById(id);
        String appSecret = SM4Util.decrypt(projectProperties.getSm4Key(), program.getEncryptAppSecret());
        String accessToken = getAccessToken(program.getAppId(), appSecret);
        String httpUrl = StrUtil.format(getPhoneNumberUrl, accessToken);
        JSONObject map = new JSONObject();
        map.set("code", code);
        String result = HttpUtil.createPost(httpUrl).body(map.toString()).execute().body();
        JSONObject resultObject = JSONUtil.parseObj(result);
        if (!resultObject.containsKey("phone_info")) {
            log.error("获取手机号失败,{}", result);
            throw new BusinessException("获取手机号失败");
        }
        JSONObject phone_info = resultObject.getJSONObject("phone_info");
        if (!phone_info.containsKey("phoneNumber")) {
            log.error("获取手机号失败,{}", result);
            throw new BusinessException("获取手机号失败");
        }
        return phone_info.getStr("phoneNumber");
    }

    public String updateUserInfo(String name, String email, String phone, String mobile,String appId) throws Exception {
        return userService.updateUser(UserUtil.getCurrentUser().getId(), name, email, phone, mobile, appId);
    }


}
