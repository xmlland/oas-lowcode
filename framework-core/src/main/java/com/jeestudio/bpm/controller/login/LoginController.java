package com.jeestudio.bpm.controller.login;


import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.IdUtil;
import com.google.gson.Gson;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.security.TranEncryptUtil;
import com.jeestudio.bpm.security.enums.Auth2Enum;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.oauth2.OAuth2Service;
import com.jeestudio.bpm.service.system.*;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.auth.JwtUtil;
import com.jeestudio.tools.security.enums.PasswordCharTypeEnum;
import com.jeestudio.tools.security.exceptions.WeakPasswordException;
import com.jeestudio.tools.security.pojo.AsymmetricKey;
import com.jeestudio.tools.security.utils.PasswordUtil;
import com.jeestudio.tools.security.utils.security.SM2Util;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * @Description: 登录认证
 */
@Tag(name = "登录认证")
@RestController
@RequestMapping("${adminPath}")
public class LoginController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private static final CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private SecLogService secLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuDataService menuDataService;

    @Value(value = "${sec.showValidateCode}")
    private boolean showValidateCode;

    private static final String VALIDATE_CODE = "validateCode";

    @Autowired
    OAuth2Service oAuth2Service;

    @Value(value = "${ldap.switch}")
    private String ldapSwitch;

    @Value(value = "${ldap.url}")
    private String ldapUrl;

    @Value(value = "${ldap.searchBase}")
    private String ldapSearchBase;

    @Value(value = "${ldap.defaultBase}")
    private String ldapDefaultBase;

    @Autowired
    private ZformService zformService;

    @Value("${spring.profiles.active}")
    String active;

    @Autowired
    private SysSettingService sysSettingService;

    @Autowired
    private AuthService authService;

    @Autowired
    ProjectProperties projectProperties;

    /**
     * 使用AES/CBC加密（getSettings等接口仍在使用）
     */
    public static String decrypt(String data) throws Exception {
        if (data == null) return null;
        return Aes.aesDecrypt(data);
    }

    public static String encrypt(String data) throws Exception {
        if (data == null) return null;
        return Aes.aesEncrypt(data);
    }

    /**
     * 获取登录用临时SM2公钥（匿名接口）
     * 每次请求生成唯一密钥对，私钥存Redis 5分钟有效，一次性使用
     */
    @Operation(summary = "获取登录SM2公钥")
    @GetMapping("/getLoginPublicKey")
    public String getLoginPublicKey() {
        ResultJson resultJson = new ResultJson();
        String keyId = IdUtil.fastSimpleUUID();
        AsymmetricKey sm2Key = SM2Util.generateKey();
        cacheUtil.setObjectCacheExpireMinute("LOGIN_SM2:" + keyId, sm2Key.getPrivateKey(), 5L);
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.put("keyId", keyId);
        resultJson.put("publicKey", sm2Key.getPublicKey());
        return JsonConvertUtil.objectToJson(resultJson);
    }

    /**
     * SM2解密登录数据（使用临时密钥对）
     */
    private String decryptLoginData(String data, String loginKeyId) throws Exception {
        if (data == null) return null;
        String privateKey = (String) cacheUtil.getObjectCache("LOGIN_SM2:" + loginKeyId);
        if (StringUtil.isEmpty(privateKey)) {
            throw new Exception("Login key expired or invalid");
        }
        cacheUtil.deleteObjectCache("LOGIN_SM2:" + loginKeyId);
        return SM2Util.decrypt(privateKey, data);
    }

/*    public static void main(String[] args) throws Exception {
        String randomStr = "rCervNjBqkjyRfQeeyF7ohlWsjZm47Q7EQd08af9bVwhVD69af3DtrJn3z+8TGPXnAcwdUxx6I2aLmG6mwpMYQRStEXzI7j5Qz6HY+lUs3BHtgxUlckzgs+cNox/8YJm5eRzxvWpei2a5Iv5yzwLr1EdEfqMUa1GcLWL0PyTpnsPYnZjP8MswqWhxygp1jWK";
        String decrypt = decrypt(randomStr);
        byte[] decodedBytes = Base64.getDecoder().decode(decrypt);
        String decodedString = new String(decodedBytes);
        System.out.println(decodedString);
        String encodedString = Base64.getEncoder().encodeToString(decodedString.getBytes());
        System.out.println(encrypt(encodedString));
    }*/

    @Autowired
    ValidatePassword validatePassword;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ResultJson resultJson = new ResultJson();
        // 兼容说明：登录数据仍沿用 DES 解密入口
        String username = "";
        String password = "";
        String randomStr = request.getParameter("randomStr");
        String loginKeyId = request.getParameter("loginKeyId");
        Map<String, String> map = null;
        if (StringUtil.isNotBlank(randomStr) && StringUtil.isNotBlank(loginKeyId)) {
            try {
                String decrypt = decryptLoginData(randomStr, loginKeyId);
                String decodedString = cn.hutool.core.codec.Base64.decodeStr(decrypt);
                map = new Gson().fromJson(decodedString, Map.class);
            } catch (Exception e) {
                logger.error("解密登录数据失败", e);
                resultJson.setCode(-1);
                resultJson.setMsg("用户或密码错误，请重试。");
                resultJson.setMsg_en("The userName or password is wrong, Please try again.");
                return JsonConvertUtil.objectToJson(resultJson);
            }
        } else {
            resultJson.setCode(-1);
            resultJson.setMsg("用户或密码错误，请重试。");
            resultJson.setMsg_en("The userName or password is wrong, Please try again.");
            return JsonConvertUtil.objectToJson(resultJson);
        }
        String ldapusername = map.get("ldapusername");
        String ldappassword = map.get("ldappassword");
        if (StringUtil.isNotEmpty(ldapusername) && StringUtil.isNotEmpty(ldappassword)) {
            String account = this.loginLdap(ldapusername, ldappassword);
            if (StringUtil.isNotEmpty(account)) {
                User user = UserUtil.getByLoginName(account);
                this.auth2Valid(user, map.get("auth2code"));
                // Token
                String tokenValue = cacheUtil.getTokenSetCache(account, user.getId());
                userService.clearLoginExceptionCount(account);
                resultJson.setCode(ResultJson.CODE_SUCCESS);
                resultJson.setMsg("登录成功");
                resultJson.setMsg_en("Login successfully");
                resultJson.setToken(tokenValue);
                resultJson.put("loginName", user.getLoginName());
                resultJson.put("name", user.getName());
                resultJson.put("secLevel", user.getSecLevel());
                resultJson.put("officeCode", user.getOffice().getCode());
                resultJson.put("companyCode", user.getCompany().getCode());
                // header 里面添加 token
                response.setHeader("token", tokenValue);

                secLogService.saveSecLog(account, ip.get(), "域用户登录成功", "登录", Global.YES);
                cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
                menuDataService.refreshUserMenuCache(user.getId());
            } else {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("登录失败");
                resultJson.setMsg_en("LDAP login failed");
                secLogService.saveSecLog(account, ip.get(), "域用户登录失败", "登录", Global.NO);
            }
            return JsonConvertUtil.objectToJson(resultJson);
        } else {
            username = map.get("username");
            if (StringUtil.isBlank(username)) {
                resultJson.setCode(-1);
                resultJson.setMsg("用户或密码错误，请重试。");
                resultJson.setMsg_en("The userName or password is wrong, Please try again.");
                return JsonConvertUtil.objectToJson(resultJson);
            }
            if (showValidateCode) {
                String validateCode = map.get(VALIDATE_CODE);
                String imgUUID = map.get("imgUUID");
                Object codeObject = cacheUtil.getObjectCache(VALIDATE_CODE + imgUUID);
                String code = null;
                if (codeObject != null) {
                    code = (String) codeObject;
                }
                //本地访问不需要验证码
                //String ipAddress = ip.get();
                //改为从配置文件中获取是否为开发环境
                boolean isLocal = !active.contains("prod");
                ;//"127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress);
                if (isLocal || (StringUtil.isNotEmpty(validateCode) && StringUtil.isNotEmpty(code) && (validateCode.equalsIgnoreCase(code)))) {

                } else {
                    resultJson.setCode(ResultJson.CODE_FAILED);
                    resultJson.setMsg("验证码错误，请重试");
                    resultJson.setMsg_en("You have provided a wrong validation code, please try again");
                    secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
                    return JsonConvertUtil.objectToJson(resultJson);
                }
            }
            //校验用户
            password = map.get("password");
            if (StringUtil.isBlank(password)) {
                resultJson.setCode(-1);
                resultJson.setMsg("用户或密码错误，请重试。");
                resultJson.setMsg_en("The userName or password is wrong, Please try again.");
                return JsonConvertUtil.objectToJson(resultJson);
            }
            User user = userService.getByLoginName(username);
            if (user != null && Global.NO.equals(user.getLoginFlag())) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("该帐号已禁止登录");
                resultJson.setMsg_en("This account is no longer allowed to log in");
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
                return JsonConvertUtil.objectToJson(resultJson);
            }
            if (user != null && Global.YES.equals(user.getSsoLoginFlag())) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("该帐号只允许域控登录");
                resultJson.setMsg_en("Only domain login is allowed for this account");
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
                return JsonConvertUtil.objectToJson(resultJson);
            }
            if (active.contains("prod") && projectProperties.isValidWeakPassword()) {
                PasswordCharTypeEnum[] passwordCharTypeEnums = validatePassword.getPasswordCharType().toArray(new PasswordCharTypeEnum[0]);
                try {
                    PasswordUtil.isWeakPassword(password, projectProperties.getMinPasswordLength(), projectProperties.getPasswordCharTypeCount(), passwordCharTypeEnums);
                }catch (WeakPasswordException e){
                    logger.error("密码校验失败,{}", e.getMessage());
                    secLogService.saveSecLog(username, ip.get(), "用户名: " + username + "密码校验失败,"+ e.getMessage(), "登录", Global.NO);
                    return  handlerErrorCount(username);
                }
            }
            if (user == null || false == validatePassword.validateUserPassword(password, user.getPassword(), user.getLoginName(), ip.get())) {
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
                return  handlerErrorCount(username);
            }
            if (false == Global.YES.equals(user.getUseable())) {//账号已停用
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("该账号已停用，无法登录");
                resultJson.setMsg_en("The account has been disabled and cannot log in.");
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
                return JsonConvertUtil.objectToJson(resultJson);
            }
            this.auth2Valid(user, map.get("auth2code"));
            // Token
            String tokenValue = cacheUtil.getTokenSetCache(username, user.getId());
            userService.clearLoginExceptionCount(username);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("登录成功");
            resultJson.setMsg_en("Login Successful");
            resultJson.setToken(tokenValue);
            // 安全约束：token 返回前端前需要加密。
            resultJson.put("tokenEncrypt", true);
            resultJson.setToken(TokenSecurityUtil.encrypt(resultJson.getToken()));
            resultJson.put("loginName", user.getLoginName());
            resultJson.put("name", user.getName());
            resultJson.put("secLevel", user.getSecLevel());
            resultJson.put("officeCode", user.getOffice().getCode());
            resultJson.put("companyCode", user.getCompany().getCode());
            resultJson.put("extEntId", user.getExtEntId());
            cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
            menuDataService.refreshUserMenuCache(user.getId());

            if (secLogService.getSecSwitch() && userService.isPasswordExpired(user.getLoginName()) && user.getPasswordExpiredDate().compareTo(DateUtil.strToDate("1900")) != 0) {
                resultJson.put("passwordExpired", "true");
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("该帐号密码已过期");
                resultJson.setMsg_en("The password has expired");
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
            } else if (secLogService.getSecSwitch() && userService.getLoginExceptionCount(user.getLoginName()) >= 5) {
                userService.lockUser(user.getLoginName());
                resultJson.setCode(ResultJson.CODE_FAILED);
                resultJson.setMsg("该帐号已禁止登录");
                resultJson.setMsg_en("This account is no longer allowed to log in");
                secLogService.saveSecLog(username, ip.get(), "登录失败", "登录", Global.NO);
            } else {
                // header 里面添加 token
                response.setHeader("token", tokenValue);
                secLogService.saveSecLog(username, ip.get(), "登录成功", "登录", Global.YES);
            }
            TranEncryptUtil.setPublicKey(user.getId());
            resultJson.setPublicKey(TranEncryptUtil.getPublicKey());

            String target = map.get("target");
            String responseType = map.get("responseType");
            String clientId = map.get("clientId");
            String redirectURI = map.get("redirectURI");
            String referer = map.get("referer");
            if ("oauth2".equals(target) && StringUtil.isNotEmpty(responseType) && StringUtil.isNotEmpty(clientId) && StringUtil.isNotEmpty(redirectURI)) {
                String url = oAuth2Service.getOauth2ServerAuthorizeUrl(referer) + "?response_type=" + responseType + "&client_id=" + clientId + "&redirect_uri=" + URLEncodeUtil.encode((redirectURI));
                try {
                    url = url + "&oauth2_token=" + tokenValue;
                    resultJson.put("redirect", url);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return JsonConvertUtil.objectToJson(resultJson);
        }
    }

    private String handlerErrorCount(String username) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        userService.addLoginExceptionCount(username);
        Integer loginExceptionCount = userService.getLoginExceptionCount(username);
        if (loginExceptionCount == null) {
            resultJson.setMsg("用户或密码错误，请重试");
            resultJson.setMsg_en("Username and password do not match or you do not have an account yet.");
        } else if (loginExceptionCount >= 10) {
            resultJson.setMsg("已经错了10次，该用户已被锁定，请联系管理员");
            resultJson.setMsg_en("Error 10 times, the user has been locked, please contact the administrator");
            userService.lockUser(username);
        } else if (loginExceptionCount >= 5) {
            resultJson.setMsg("错误了" + loginExceptionCount + "次，还剩" + (10 - loginExceptionCount) + "次用户将被锁定");
            resultJson.setMsg_en("Error " + loginExceptionCount + " time, " + (10 - loginExceptionCount) + " times left, user will be locked");
        } else {
            resultJson.setMsg("用户或密码错误，请重试");
            resultJson.setMsg_en("Username and password do not match or you do not have an account yet.");
        }
        return JsonConvertUtil.objectToJson(resultJson);
    }

    /**
     * 双因素验证，查询系统设置，二次验证
     */
    private void auth2Valid(User user, String auth2code) throws Exception {
        com.alibaba.fastjson.JSONObject jsonObject = sysSettingService.getSettingMap(Auth2Enum.AUTH2_PREFIX.getValue(), currentUserNameGuest);
        boolean auth2IsOpen = Boolean.TRUE.equals(jsonObject.getOrDefault(Auth2Enum.AUTH2_OPEN, false));
        if (auth2IsOpen) {
            String roles = jsonObject.getString(Auth2Enum.AUTH2_ROLES.getValue());
            String users = jsonObject.getString(Auth2Enum.AUTH2_USERS.getValue());
            if(StringUtil.hasCommonElements(user.getRoleEnNames(), roles) || StringUtil.hasCommonElements(user.getLoginName(), users)) {
                if (false == authService.validateCode(auth2code)) {
                    secLogService.saveSecLog(user.getLoginName(), ip.get(), "登录失败，双因素验证失败", "登录", Global.NO);
                    throw new RuntimeException("双因素登录验证失败");
                }
            }
        }
    }

    /**
     * 免密码登录。
     */
    @Operation(summary = "获取令牌")
    @PostMapping("/getToken")
    public ResultJson getToken(@RequestParam("walletAddress") String walletAddress) {
        ResultJson resultJson = new ResultJson();
        //校验用户
        User user = userService.createFreeUser(walletAddress);
        // Token
        String tokenValue = cacheUtil.getTokenSetCache(walletAddress, user.getId());
        userService.clearLoginExceptionCount(walletAddress);
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("登录成功");
        resultJson.setMsg_en("Login Successful");
        resultJson.setToken(tokenValue);
        resultJson.put("loginName", user.getLoginName());
        resultJson.put("name", user.getName());
        resultJson.put("secLevel", user.getSecLevel());
        resultJson.put("officeCode", user.getOffice().getCode());
        resultJson.put("companyCode", user.getCompany().getCode());

        return resultJson;
    }

    /**
     * LDAP登录
     */
    private String loginLdap(String username, String password) {
        String account = username;
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");//"none","simple","strong"
        env.put(Context.SECURITY_PRINCIPAL, ldapDefaultBase + "\\" + username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.PROVIDER_URL, ldapUrl);
        try {
            LdapContext ctx = new InitialLdapContext(env, null);
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchFilter = "(&(objectClass=user)(sAMAccountName=" + this.escapeLdapFilter(account) + "))";
            String[] returnedAts = {"sAMAccountName", "cn"};
            searchCtls.setReturningAttributes(returnedAts);
            NamingEnumeration<javax.naming.directory.SearchResult> answer = ctx.search(ldapSearchBase, searchFilter, searchCtls);
            if (answer.hasMoreElements()) {
                javax.naming.directory.SearchResult sr = answer.next();
                Attributes atts = sr.getAttributes();
                account = (String) atts.get("sAMAccountName").get();
            }
            ctx.close();
        } catch (NamingException e) {
            logger.error("Error occurred while trying to login by LDAP: " + ExceptionUtils.getStackTrace(e));
            account = "";
        }
        return account;
    }

    private String escapeLdapFilter(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\':
                    sb.append("\\5c");
                    break;
                case '*':
                    sb.append("\\2a");
                    break;
                case '(':
                    sb.append("\\28");
                    break;
                case ')':
                    sb.append("\\29");
                    break;
                case '\u0000': // NUL 字符
                    sb.append("\\00");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 登录服务中断
     */
    public String loginOutage(HttpServletRequest request, HttpServletResponse response) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_FAILED);
        resultJson.setMsg("登录异常，请稍后再试");
        resultJson.setMsg_en("Login Error. Please try again later.");
        return JsonConvertUtil.objectToJson(resultJson);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出")
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityUtils.getSubject().logout();
        cacheUtil.deleteTokenCache(token.get(), currentUserName.get(), currentUserId.get());
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("退出成功");
        resultJson.setMsg_en("Logout successful");
        secLogService.saveSecLog(currentUserName.get(), ip.get(), "退出成功", "退出", Global.YES);
        return JsonConvertUtil.objectToJson(resultJson);
    }

    /**
     * 获取验证码开关
     */
    @Operation(summary = "获取验证码开关")
    @GetMapping("/showValidateCode")
    public ResultJson showValidateCode() {
        ResultJson resultJson = new ResultJson();
        resultJson.put("showValidateCode", this.showValidateCode);
        resultJson.setToken(token.get());
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        return resultJson;
    }

    /**
     * 获取LDAP开关
     */
    @Operation(summary = "获取LDAP开关")
    @GetMapping("/getLdapSwitch")
    public String getLdapSwitch() {
        return String.valueOf(this.ldapSwitch);
    }

    /**
     * 获取验证码
     */
    @Operation(summary = "生成验证码")
    @ResponseBody
    @GetMapping("/createCharacter")
    public ResultJson toCreateCode() {
        String randomUUID = IdUtil.randomUUID();
        String imgCode = drawImg(randomUUID);
        ResultJson success = ResultJson.success();
        success.put("imgBase64", imgCode);
        success.put("imgUUID", randomUUID);
        return success;
    }

    @Operation(summary = "获取系统配置")
    @GetMapping("/getSettings")
    public String getSettings(String prefix) throws Exception {
        String settings = sysSettingService.getSettingMap(prefix, currentUserNameGuest).toJSONString();
        return encrypt(Base64.getEncoder().encodeToString(settings.getBytes()));
    }

    /**
     * 获取天地图Key
     * 从 sys_setting 中读取 tdtKey 配置项
     * 如果值以 PATH: 开头，则解析为环境变量
     */
    @Operation(summary = "获取天地图Key")
    @GetMapping("/getTdtKey")
    public ResultJson getTdtKey() {
        try {
            String value = sysSettingService.getSettingValueByKey("tdtKey", currentUserNameGuest);
            if (value != null && value.startsWith("PATH:")) {
                String envVar = value.substring(5).trim();
                String envValue = System.getenv(envVar);
                if (envValue != null && !envValue.isEmpty()) {
                    value = envValue;
                } else {
                    logger.warn("环境变量 {} 未配置", envVar);
                    value = "";
                }
            }
            return ResultJson.success(value != null ? value : "");
        } catch (Exception e) {
            logger.error("获取天地图Key失败", e);
            return ResultJson.success("");
        }
    }

    /**
     * 图片格式
     */
    private static final String IMG_FORMAT = "JPEG";

    /**
     * base64 图片前缀
     */
    private static final String BASE64_PRE = "data:image/jpg;base64,";

    /**
     * 绘画验证码
     */
    private String drawImg(String uuid) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        // 随机产生5个字符
        for (int i = 0; i < 4; i++) {
            code.append(randomChar());
        }
        // 定义图片的宽度和高度
        int width = 4 * 20;
        int height = 30;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图片的上下文
        Graphics gr = image.getGraphics();
        // 设定图片背景颜色
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, width, height);
        // 设定图片边框
        gr.setColor(Color.GRAY);
        gr.drawRect(0, 0, width - 1, height - 1);
        // 画十条干扰线
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            gr.setColor(randomColor());
            gr.drawLine(x1, y1, x2, y2);
        }
        // 设置字体，画验证码
        gr.setColor(randomColor());
        gr.setFont(randomFont());
        gr.drawString(code.toString(), 10, 22);
        // 图像生效
        gr.dispose();

        cacheUtil.setObjectCacheExpireMinute(VALIDATE_CODE + uuid, code.toString().toLowerCase(), 1L);

        java.io.ByteArrayOutputStream byteStream = new java.io.ByteArrayOutputStream();
        //写入流中
        try {
            ImageIO.write(image, IMG_FORMAT, byteStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //转换成字节
        byte[] bytes = byteStream.toByteArray();
        //转换成base64串
        String base64 = Base64.getEncoder().encodeToString(bytes).trim();
        base64 = base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
        return BASE64_PRE + base64;
    }

    /**
     * 随机参数一个字符
     */
    private char randomChar() {
        Random r = new Random();
        String s = "abcdefghjkmnprstuvwxyzABCDEFGHJKMNPRSTUVWXYZ23456789";
        return s.charAt(r.nextInt(s.length()));
    }

    // 生成随机的颜色
    private Color randomColor() {
        int red = r.nextInt(150);
        int green = r.nextInt(150);
        int blue = r.nextInt(150);
        return new Color(red, green, blue);
    }

    private final String[] fontNames = {"宋体", "华文楷体", "黑体", "微软雅黑", "楷体_GB2312"};
    private final Random r = new Random();

    // 生成随机的字体
    private Font randomFont() {
        int index = r.nextInt(fontNames.length);
        String fontName = fontNames[index];// 生成随机的字体名称
        int style = r.nextInt(4);
        int size = r.nextInt(3) + 24; // 生成随机字号, 24 ~ 28
        return new Font(fontName, style, size);
    }
}
