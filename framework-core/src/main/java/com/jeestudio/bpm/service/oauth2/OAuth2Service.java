package com.jeestudio.bpm.service.oauth2;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.pojo.OAuth2User;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: OAuth2单点登录服务
 */
@Service
@Slf4j
public class OAuth2Service {

    private static CacheUtil cacheUtil = new CacheUtil();
    public static final String OAUTH2_SERVER = "oauth2Server";
    private static final String OAUTH2_SERVER_KEY = "oauth2ServerKey";
    private static final String OAUTH2_REDIRECT_REFERER = "oauth2RedirectReferer";

    private static final String CACHE_PREFIX = ":OAuth2:";
    private static final String CACHE_PREFIX_AUTH_CODE = CACHE_PREFIX + "AUTH_CODE:";
    private static final String CACHE_PREFIX_ACCESS_TOKEN = CACHE_PREFIX + "ACCESS_TOKEN:";

    //access_token过期时间
    private static final Long ACCESS_EXPIRE_IN_SECONDS = (long) 60 * 60;
    @Autowired
    ZformService zformService;


    @Autowired
    GenTableService genTableService;

    @Autowired
    UserService userService;

    @Autowired
    ProjectProperties projectProperties;

    public String getOauth2ServerLoginUrl(HttpServletRequest request) {
        String oauth2Server = request.getParameter(OAuth2Service.OAUTH2_SERVER);
        return oauth2Server + projectProperties.getOauth2ServerLoginUrl();
    }

    public String getOauth2ServerAuthorizeUrl(String oauth2ServerReferer) {
        return oauth2ServerReferer + projectProperties.getOauth2ServerAuthorizeUrl();
    }
    public String getOauth2Server(String oauth2ServerKey) {
        Map<String, Object> oauth2ServerMap = projectProperties.getOauth2ServerMap();
        if (oauth2ServerMap.containsKey(oauth2ServerKey)) {
            return oauth2ServerMap.get(oauth2ServerKey).toString();
        }
        if (oauth2ServerMap.size() > 0) {
            return oauth2ServerMap.values().iterator().next().toString();
        }
        throw new RuntimeException("oauth2Server配置不正确");
    }
    public String getOauth2Server(HttpServletRequest request) {
        String oauth2ServerKey = request.getParameter(OAUTH2_SERVER_KEY);
        return getOauth2Server(oauth2ServerKey);
    }

    public String getOauth2ServerApi() {
        return getOauth2Server("") + projectProperties.getOauth2ServerContextPath();
    }

    public String getOauth2ServerApi(HttpServletRequest request) {
        return getOauth2Server(request) + projectProperties.getOauth2ServerContextPath();
    }

    public String getOauth2ClientId() {
        return projectProperties.getOauth2ClientId();
    }

    public String getOauth2ClientSecret() {
        return projectProperties.getOauth2ClientSecret();
    }

    public String getOauth2RedirectUri(HttpServletRequest request) {
        return URLEncodeUtil.encode(ConvertUtil.getString(request.getParameter(OAUTH2_REDIRECT_REFERER)) + projectProperties.getOauth2RedirectUri());
    }

    private List<LinkedHashMap> listByClientId(String clientId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("client_id", clientId);
        return zformService.findMapList("sys_oauth2_client", queryWrapper);
    }

    /**
     * 检查客户端id是否存在
     *
     * @param clientId
     * @return
     */
    public boolean checkClientId(String clientId) {
        List<LinkedHashMap> mapList = this.listByClientId(clientId);
        return mapList.size() > 0;
    }

    /**
     * 检查客户端id是否存在
     *
     * @param ip
     * @return
     */
    public boolean checkClientIp(String clientId, String ip) {
        List<LinkedHashMap> mapList = this.listByClientId(clientId);
        if (mapList.size() > 0) {
            LinkedHashMap map = mapList.get(0);
            try {
                LinkedHashMap hashMap = zformService.getMap(ConvertUtil.getString(map.get("id")), genTableService.getGenTableWithDefination("sys_oauth2_client"), StrUtil.EMPTY, StrUtil.EMPTY);
                if (hashMap != null) {
                    String allow_ip = ConvertUtil.getString(hashMap.get("allow_ip"));
                    if (StrUtil.isBlankIfStr(allow_ip)) {
                        return true;
                    }
                    String[] ips = allow_ip.split(",");
                    for (String ipConfig : ips) {
                        if (ip.contains(ipConfig)) {
                            return true;
                        }
                    }
                    log.error("ip不在允许范围内,ip:{},allow_ip:{}", ip, allow_ip);
                }
            } catch (Exception e) {
                log.error("获取oauth2客户端信息失败,{}", ExceptionUtil.stacktraceToString(e));
                return false;
            }
        }
        return false;
    }

    /**
     * 检查客户端Secret是否正确
     *
     * @param clientSecret
     * @return
     */
    public boolean checkClientSecret(String clientId, String clientSecret) {
        List<LinkedHashMap> mapList = this.listByClientId(clientId);
        if (mapList.size() > 0) {
            LinkedHashMap map = mapList.get(0);
            try {
                LinkedHashMap hashMap = zformService.getMap(ConvertUtil.getString(map.get("id")), genTableService.getGenTableWithDefination("sys_oauth2_client"), StrUtil.EMPTY, StrUtil.EMPTY);
                if (hashMap != null) {
                    String secret = ConvertUtil.getString(hashMap.get("client_secret"));
                    return secret.equals(clientSecret);
                }
            } catch (Exception e) {
                log.error("获取oauth2客户端信息失败,{}", ExceptionUtil.stacktraceToString(e));
                return false;
            }
        }
        return false;
    }


    /**
     * 添加auth code
     *
     * @param code   auth code
     * @param userId 用户id
     */
    public void addAuthCode(String code, String userId) {
        //10分钟过期
        cacheUtil.setObjectCacheExpireMinute(CACHE_PREFIX_AUTH_CODE + code, userId, 10L);
    }

    /**
     * 检查auth code是否存在
     *
     * @param code auth code
     * @return
     */
    public boolean checkAuthCode(String code) {
        Object objectCache = cacheUtil.getObjectCache(CACHE_PREFIX_AUTH_CODE + code);
        return objectCache != null;
    }

    /**
     * 根据auth code获取用户id
     *
     * @param code auth code
     * @return
     */
    public String getUserIdByAuthCode(String code) {
        Object objectCache = cacheUtil.getObjectCache(CACHE_PREFIX_AUTH_CODE + code);
        return ConvertUtil.getString(objectCache);
    }

    /**
     * 添加accessToken
     *
     * @param accessToken accessToken
     * @param userId      用户id
     */
    public void addAccessToken(String accessToken, String userId) {
        cacheUtil.setObjectCacheExpireMinute(CACHE_PREFIX_ACCESS_TOKEN + accessToken, userId, ACCESS_EXPIRE_IN_SECONDS / 60);
    }

    /**
     * 检查accessToken是否存在
     *
     * @param accessToken accessToken
     * @return
     */
    public boolean checkAccessToken(String accessToken) {
        Object objectCache = cacheUtil.getObjectCache(CACHE_PREFIX_ACCESS_TOKEN + accessToken);
        return objectCache != null;
    }

    /**
     * 获取accessToken过期时间
     *
     * @return 过期时间，单位秒
     */
    public long getExpireIn() {
        return ACCESS_EXPIRE_IN_SECONDS;
    }


    /**
     * 根据accessToken获取用户信息
     *
     * @param accessToken accessToken
     * @return 用户信息
     */
    public OAuth2User getUserByAccessToken(String accessToken) {
        Object objectCache = cacheUtil.getObjectCache(CACHE_PREFIX_ACCESS_TOKEN + accessToken);
        String userId = ConvertUtil.getString(objectCache);
        User user = userService.get(userId);
        if (user != null) {
            OAuth2User oAuth2User = new OAuth2User();
            BeanUtil.copyProperties(user, oAuth2User);
            return oAuth2User;
        }
        return null;
    }
}
