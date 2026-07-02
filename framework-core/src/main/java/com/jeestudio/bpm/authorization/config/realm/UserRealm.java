package com.jeestudio.bpm.authorization.config.realm;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.service.system.UserService;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.bpm.utils.*;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: Shiro用户认证域
 */
@Component
public class UserRealm extends AuthorizingRealm {

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private UserService userService;

    public UserRealm() {
    }

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        return authenticationToken instanceof JwtToken;
    }

    /**
     * Get authorization info
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        String username = JwtUtil.getClaim(principalCollection.toString(), Global.USERNAME);
        LinkedHashMap<String, List<String>> resultJson = userService.getUserPermissionByLoginName(username);
        List<String> menuObj = resultJson.get("menu");
        if (menuObj != null) {
            for (String menu : menuObj) {
                simpleAuthorizationInfo.addStringPermission(menu);
            }
        }
        simpleAuthorizationInfo.addStringPermission("user");
        List<String> roleObj = resultJson.get("role");
        if (roleObj != null) {
            for (String role : roleObj) {
                simpleAuthorizationInfo.addRole(role);
            }
        }
        return simpleAuthorizationInfo;
    }

    /**
     * Get authorization info
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getCredentials();
        String username = JwtUtil.getClaim(token, Global.USERNAME);
        String userId = JwtUtil.getClaim(token, Global.USERID);
        User user = UserUtil.getByLoginName(username);
        String clientId = JwtUtil.getClaim(token, "clientId");
        String key = Global.PREFIX_SHIRO_REFRESH_TOKEN + username + "_" + userId + "_token_clientId_" + clientId;
        boolean exist = cacheUtil.existKey(key);
        if (exist) {
            Map<String, Object> verify = JwtUtil.verify(username, userId, token);
            Exception exception = (Exception) verify.get("exception");
            if (user == null) {
                throw new AuthenticationException("This account does not exist.");
            } else if (false == Global.YES.equals(user.getUseable())) {
                throw new AuthenticationException("The account has been disabled and cannot be logged in.");
            } else if (Global.NO.equals(user.getLoginFlag())) {
                throw new AuthenticationException("This account is no longer allowed to log in.");
            } else if (exception != null && exception instanceof SignatureVerificationException) {
                throw new AuthenticationException("Incorrect token.");
            } else if (exception != null && exception instanceof TokenExpiredException) {
                throw new AuthenticationException("Token expired.");
            } else if (false == Boolean.valueOf(cacheUtil.existKey(key))) {
                throw new AuthenticationException("Token expired or incorrect.");
            } else {
                return new SimpleAuthenticationInfo(token, token, username);
            }
        } else {
            throw new AuthenticationException("Token expired or incorrect.");
        }
    }
}
