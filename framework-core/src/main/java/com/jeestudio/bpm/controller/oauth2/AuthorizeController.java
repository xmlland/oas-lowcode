package com.jeestudio.bpm.controller.oauth2;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.pojo.OAuth2User;
import com.jeestudio.bpm.common.pojo.OfficeInfo;
import com.jeestudio.bpm.common.pojo.UserInfo;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.oauth2.OAuth2Service;
import com.jeestudio.bpm.service.oauth2.OAuthOfficeService;
import com.jeestudio.bpm.service.oauth2.OAuthUserService;
import com.jeestudio.bpm.service.system.MenuDataService;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @Description: OAuth2单点登录授权
 */
@Tag(name = "OAuth2授权")
@RestController
@Slf4j
@RequestMapping("/${adminPath}/oauth2")
public class AuthorizeController extends BaseController {

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private UserService userService;
    @Autowired
    private MenuDataService menuDataService;

    @Autowired
    private SecLogService secLogService;
    @Autowired
    OAuth2Service oAuth2Service;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getLoginNameByToken(String code) {
        try {
            HttpServletRequest request = ContextHolderUtil.getHttpServletRequest();
            //构建获取access token的URL
            String tokenUrl = oAuth2Service.getOauth2ServerApi() + "/oauth2/accessToken"
                    + "?grant_type=authorization_code"
                    + "&client_id=" + oAuth2Service.getOauth2ClientId()
                    + "&client_secret=" + oAuth2Service.getOauth2ClientSecret()
                    + "&code=" + code
                    + "&redirect_uri=" + oAuth2Service.getOauth2RedirectUri(request);
            //获取access token
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponse = restTemplate.postForObject(tokenUrl, null, Map.class);
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                log.error("获取access_token失败,响应:{}", tokenResponse);
                return null;
            }
            String accessToken = String.valueOf(tokenResponse.get("access_token"));

            //获取user info
            String userInfoUrl = oAuth2Service.getOauth2ServerApi() + "/oauth2/userInfo"
                    + "?clientId=" + oAuth2Service.getOauth2ClientId()
                    + "&access_token=" + accessToken;
            OAuth2User resourceResponse = restTemplate.getForObject(userInfoUrl, OAuth2User.class);
            return resourceResponse != null ? resourceResponse.getLoginName() : null;
        } catch (Exception e) {
            log.error("通过token换取用户失败,{},{}", token, ExceptionUtil.stacktraceToString(e));
            return null;
        }

    }

    @Operation(summary = "第三方登录")
    @RequestMapping("/login")
    public ResultJson login(HttpServletRequest request, HttpServletResponse response, String code) {
        try {
            if (StrUtil.isNotEmpty(code)) {
                String loginName = this.getLoginNameByToken(code);
                User user = userService.getByLoginName(loginName);
                if (user == null) {
                    return ResultJson.failed("用户不存在");
                }
                if (Global.NO.equals(user.getUseable()) || Global.NO.equals(user.getLoginFlag())) {
                    return ResultJson.failed("该账号已停用，无法登录");
                }
                token.set(cacheUtil.getTokenSetCache(loginName, user.getId()));
                cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
                menuDataService.refreshUserMenuCache(user.getId());
                secLogService.saveSecLog(loginName, ip.get(), "oauth2登录成功", "登录", Global.YES);
                log.info("oauth2登录成功,{}", loginName);
                return ResultJson.success();
            }
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("client_id", oAuth2Service.getOauth2ClientId());
            queryParams.put("oauth2Server", oAuth2Service.getOauth2Server(request));
            queryParams.put("response_type", "code");
            queryParams.put("oauth2_token", ConvertUtil.getString(request.getParameter("oauth2_token")));
            queryParams.put("redirect_uri", oAuth2Service.getOauth2RedirectUri(request));
            WebUtils.issueRedirect(request, response, oAuth2Service.getOauth2ServerApi(request) + "/oauth2/authorize", queryParams);
        } catch (Exception e) {
            log.error("oauth2登录失败,{}", ExceptionUtil.stacktraceToString(e));
            return ResultJson.failed("登录失败");
        }
        return ResultJson.success();
    }


    private void checkClientId(String clientId) {
        if (!oAuth2Service.checkClientId(clientId)) {
            log.error("clientId错误,{}", clientId);
            throw new BusinessException(StrUtil.format("clientId错误,{}", clientId));
        }
    }

    private void checkClientIp(String clientId) {
        String ipStr = ip.get();
        if (!oAuth2Service.checkClientIp(clientId, ipStr)) {
            log.error("ClientIp禁止访问,{}", ipStr);
            throw new BusinessException(StrUtil.format("ClientIp禁止访问,{}", ipStr));
        }
    }

    private void checkClientSecret(String clientId, String clientSecret) {
        if (!oAuth2Service.checkClientSecret(clientId, clientSecret)) {
            log.error("secret错误,{},{}", clientId, clientSecret);
            throw new BusinessException("secret错误");
        }
    }

    private String getUserId() {
        Subject subject = SecurityUtils.getSubject();
        String token = (String) subject.getPrincipal();
        String currentUser = JwtUtil.getCurrentUser(token);
        return currentUser.split("_")[1];
    }

    @Operation(summary = "授权")
    @RequestMapping("/authorize")
    public Object authorize(HttpServletRequest request, HttpServletResponse response) {

        try {
            //从请求参数中直接读取OAuth2授权信息
            String clientId = request.getParameter("client_id");
            String responseType = request.getParameter("response_type");
            String redirectURI = request.getParameter("redirect_uri");
            //检查传入的客户端id是否正确
            this.checkClientId(clientId);
            String oauth2_token = request.getParameter("oauth2_token");
            //如果用户没有携带token，跳转到登陆页面
            if (StrUtil.isEmpty(oauth2_token)) {

                //判断url是否有参数 拼接target=oauth2
                StringBuilder url = new StringBuilder(oAuth2Service.getOauth2ServerLoginUrl(request));
                if (redirectURI != null && redirectURI.contains("?")) {
                    url.append("&");
                } else {
                    url.append("?");
                }
                url.append("target=oauth2");
                url.append("&clientId=").append(clientId);
                url.append("&responseType=").append(responseType);
                url.append("&redirectURI=").append(redirectURI);
                WebUtils.issueRedirect(request, response, url.toString());
                return null;
            }
            JwtToken token = new JwtToken(oauth2_token);
            Subject su = SecurityUtils.getSubject();
            su.login(token);

            //responseType目前仅支持CODE，另外还有TOKEN
            if ("code".equals(responseType)) {
                String authorizationCode = UUID.randomUUID().toString();
                oAuth2Service.addAuthCode(authorizationCode, getUserId());
                //得到到客户端重定向地址
                StringBuilder url = new StringBuilder(URLDecoder.decode(redirectURI, CharsetUtil.CHARSET_UTF_8));
                if (redirectURI.contains("?")) {
                    url.append("&");
                } else {
                    url.append("?");
                }
                WebUtils.issueRedirect(request, response, url + "code=" + authorizationCode);
                return null;
            } else {
                throw new RuntimeException("暂不支持responseType:" + responseType);
            }

        } catch (Exception e) {
            log.error("构造oauth2客户端信息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "获取访问令牌")
    @RequestMapping("/accessToken")
    public Object accessToken(HttpServletRequest request) {

        try {
            //从请求参数中直接读取OAuth2令牌请求信息
            String clientId = request.getParameter("client_id");
            String clientSecret = request.getParameter("client_secret");
            String code = request.getParameter("code");
            //检查传入的客户端id是否正确 以及ip是否允许访问
            this.checkClientIp(clientId);
            // 检查客户端Secret是否正确
            this.checkClientSecret(clientId, clientSecret);
            if (!oAuth2Service.checkAuthCode(code)) {
                log.warn("错误的授权码,{}", code);
                throw new RuntimeException("错误的授权码:" + code);
            }
            //生成Access Token
            final String accessToken = UUID.randomUUID().toString();
            oAuth2Service.addAccessToken(accessToken, oAuth2Service.getUserIdByAuthCode(code));
            //生成OAuth响应
            Map<String, Object> tokenResponse = new LinkedHashMap<>();
            tokenResponse.put("access_token", accessToken);
            tokenResponse.put("expires_in", oAuth2Service.getExpireIn());
            return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("构造oauth2客户端信息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "获取用户信息")
    @RequestMapping("/userInfo")
    public Object userInfo(HttpServletRequest request) {
        try {
            String clientId = request.getParameter("clientId");
            //检查传入的客户端id是否正确 以及ip是否允许访问
            this.checkClientIp(clientId);
            //获取Access Token
            String accessToken = request.getParameter("access_token");
            if (!oAuth2Service.checkAccessToken(accessToken)) {
                // 如果不存在/过期了，返回未验证错误，需重新验证
                log.error("accessToken错误,{}", accessToken);
                throw new BusinessException(StrUtil.format("accessToken错误,{}", accessToken));
            }
            //返回用户
            OAuth2User user = oAuth2Service.getUserByAccessToken(accessToken);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("构造oauth2客户端信息失败,{}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException(e);
        }
    }

    @Autowired
    OAuthOfficeService oAuthOfficeService;

    @Autowired
    OAuthUserService oAuthUserService;

    @Operation(summary = "获取机构列表")
    @RequestMapping("/officeList")
    public ResultJson officeList(@RequestBody List<String> orgIdList, String clientId, String clientSecret) {
        //检查传入的客户端id是否正确 以及ip是否允许访问
        this.checkClientIp(clientId);
        // 检查客户端Secret是否正确
        this.checkClientSecret(clientId, clientSecret);
        List<OfficeInfo> list = oAuthOfficeService.list(orgIdList);
        return ResultJson.success().put("list", list);
    }

    @Operation(summary = "获取用户列表")
    @RequestMapping("/userList")
    public ResultJson userList(@RequestBody List<String> orgIdList, String clientId, String clientSecret, boolean syncRole) {
        //检查传入的客户端id是否正确 以及ip是否允许访问
        this.checkClientIp(clientId);
        // 检查客户端Secret是否正确
        this.checkClientSecret(clientId, clientSecret);
        List<UserInfo> list = oAuthUserService.list(orgIdList, syncRole);
        return ResultJson.success().put("list", list);
    }
}
