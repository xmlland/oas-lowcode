package com.jeestudio.bpm.utils;

import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.security.PasswordEncryptHandler;
import com.jeestudio.bpm.security.pwd.ValidateUserPasswordHandler;
import com.jeestudio.bpm.service.system.SecLogService;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.security.enums.PasswordCharTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.jetbrains.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Description: 密码校验组件
 */
@Component
public class ValidatePassword {

    private static final Logger logger = LoggerFactory.getLogger(ValidatePassword.class);

    @Autowired
    PasswordEncryptHandler passwordEncryptHandler;

    @Autowired
    ValidateUserPasswordHandler validateUserPasswordHandler;

    @Autowired
    private SecLogService secLogService;

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    ProjectProperties projectProperties;

    @Value("${spring.profiles.active}")
    String active;

    /**
     * #21修复：升级为SHA-256，增加迭代次数和盐值长度
     * 注意：修改这些参数后，旧密码需要用户重新设置才能与新算法匹配
     * 建议部署时设置过渡期，同时支持新旧两种验证方式
     */
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int HASH_INTERATIONS = 10000;
    public static final int SALT_SIZE = 16;

    public boolean validateUserPassword(String plainPassword, String password, String loginName,@Nullable String ip) {

        ValidateUserPasswordHandler.ValidMode validMode = validateUserPasswordHandler.validMode();
        if (validMode == ValidateUserPasswordHandler.ValidMode.DEFAULT) {
            return this.defaultValid(plainPassword, password, loginName, ip);
        } else if (validMode == ValidateUserPasswordHandler.ValidMode.CUSTOM) {
            return this.customValid(plainPassword, password, loginName, ip);
        } else if (validMode == ValidateUserPasswordHandler.ValidMode.MIX_DEFAULT_CUSTOM) {
            return this.defaultValid(plainPassword, password, loginName, ip) || this.customValid(plainPassword, password, loginName, ip);
        } else if (validMode == ValidateUserPasswordHandler.ValidMode.MIX_CUSTOM_DEFAULT) {
            return this.customValid(plainPassword, password, loginName, ip) || this.defaultValid(plainPassword, password, loginName, ip);
        }
        return false;
    }

    /**
     * 默认校验方式
     *
     * @param plainPassword 明文密码
     * @param password 密文密码
     * @param loginName 登录名
     * @param ip 登录IP
     * @return
     */
    public boolean defaultValid(String plainPassword, String password, String loginName, String ip) {
        HttpServletRequest request = ContextHolderUtil.getHttpServletRequest();
        String remoteHost = ConvertUtil.getString(request.getRemoteHost());
        if (active.contains("debug") && ("127.0.0.1".equals(remoteHost) || remoteHost.contains("192.168."))) {
            // 仅本机回环地址调试时跳过密码校验
            return true;
        }

        // 临时密码， 使用完即删除
        /*Object tempPassword = cacheUtil.getHashCache(Global.USER_TEMP_PASSWORD , loginName);
        if(ip != null && tempPassword != null && tempPassword.equals(passwordEncryptHandler.encrypt(plainPassword, tempPassword.toString(), loginName))){
            secLogService.saveSecLog(loginName, ip, "临时密码验证成功", "临时密码验证", Global.YES);
            cacheUtil.deleteHashCache(Global.USER_TEMP_PASSWORD, loginName);
            return true;
        }*/

        if (StringUtils.isBlank(password)) {
            return false;
        }
        return password.equals(passwordEncryptHandler.encrypt(plainPassword, password, loginName));
    }

    /**
     * 自定义校验方式
     *
     * @param plainPassword 明文密码
     * @param password 密文密码
     * @param loginName 登录名
     * @param ip 登录IP
     * @return
     */
    public boolean customValid(String plainPassword, String password, String loginName, String ip) {
        return validateUserPasswordHandler.validate(plainPassword, password, loginName, ip);
    }

    public static String shaEncode(String inStr) {
        try {
            MessageDigest sha = null;
            sha = MessageDigest.getInstance("SHA");
            byte[] byteArray = inStr.getBytes("UTF-8");
            byte[] md5Bytes = sha.digest(byteArray);
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            logger.error("SHA编码失败", e);
            return UUID.randomUUID().toString();
        }
    }


    /**
     * 获取密码字符类型
     * @return 密码字符类型
     */
    public List<PasswordCharTypeEnum> getPasswordCharType() {
        String passwordCharType = projectProperties.getPasswordCharType();
        //根据, 分割字符串
        String[] split = passwordCharType.split(",");
        List<PasswordCharTypeEnum> passwordCharTypeEnums = new ArrayList<>();
        for (String s : split) {
            if (StringUtils.isNotBlank(s)) {
                passwordCharTypeEnums.add(PasswordCharTypeEnum.valueOf(s));
            }
        }
        return passwordCharTypeEnums;
    }

}
