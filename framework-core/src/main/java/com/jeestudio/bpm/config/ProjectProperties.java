package com.jeestudio.bpm.config;

import com.jeestudio.bpm.cache.cacheUtils.RedisUtil;
import com.jeestudio.bpm.security.DefaultPasswordEncryptHandler;
import com.jeestudio.bpm.security.pwd.DefaultValidateUserPasswordHandler;
import com.jeestudio.bpm.security.storage.Sm3IntegrityHandler;
import com.jeestudio.bpm.security.storage.Sm4SecretHandler;
import com.jeestudio.bpm.service.oauth2.UserRoleSyncHandler;
import com.jeestudio.bpm.service.secLog.SecLogAppender;
import com.jeestudio.bpm.service.secLog.StdOutSecLogAppender;
import com.jeestudio.bpm.utils.Aes;
import com.jeestudio.bpm.utils.DataToExcel;
import com.jeestudio.bpm.utils.JwtUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 项目配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "project")
@Data
public class ProjectProperties {

    private static final Logger logger = LoggerFactory.getLogger(ProjectProperties.class);

    /**
     * 初始化安全组件的密钥，并校验关键配置是否已设置
     */
    @PostConstruct
    public void initSecurityKeys() {
        // JwtUtil.init 内部已做严格校验，密钥为空会抛出 IllegalArgumentException
        JwtUtil.init(this.jwtSecretKey);

        if (this.loginEncryptKey == null || this.loginEncryptKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "登录加密密钥未配置！请在环境变量 LOGIN_ENCRYPT_KEY 或配置 project.login-encrypt-key 中设置至少32字符的强随机密钥");
        }
        Aes.init(this.loginEncryptKey);
        DataToExcel.init(this.excelProtectPassword);
        if (this.sm4Key == null || this.sm4Key.isEmpty()) {
            logger.info("project.sm4Key is not configured. SM4 encryption features will not work.");
        }
    }

    /**
     * 密码过期天数 默认一百年
     */
    private int passwordExpiredDay = 36500;

    /**
     * 密码最小长度
     */
    private int minPasswordLength = 10;

    /**
     * 密码字符类型数量
     */
    private int passwordCharTypeCount = 3;

    /**
     * 密码字符类型 默认大写字母 大小写字母 数字 特殊字符
     */
    private String passwordCharType="upperCase,lowerCase,number,character";
    /**
     * 是否校验弱密码 设置为true时  会在登录及修改密码时进行校验
     */
    private boolean validWeakPassword = false;

    /**
     * 默认密码是否强制修改
     */
    private boolean defaultPasswordForceModify  = true;

    /**
     * 自定义用户密码校验类
     */
    private Class<DefaultValidateUserPasswordHandler> customValidateUserPasswordHandler = DefaultValidateUserPasswordHandler.class;

    /**
     * sm4加密key 用于数据存储加密  使用SM4Util.generateKey()可以生成
     */
    private String sm4Key = "";

    /**
     * JWT签名密钥，必须为强随机字符串（至少32字符），从环境变量 JWT_SECRET_KEY 读取
     */
    private String jwtSecretKey = "";

    /**
     * 登录参数加密密钥（AES-256），从环境变量 LOGIN_ENCRYPT_KEY 读取
     */
    private String loginEncryptKey = "";

    /**
     * Excel表单保护密码，从环境变量 EXCEL_PROTECT_PASSWORD 读取
     */
    private String excelProtectPassword = "";

    /**
     * 文件服务内部通信Token，从环境变量 FILE_SERVICE_TOKEN 读取
     */
    private String fileServiceToken = "";

    /**
     * CORS允许的Origin列表，多个用逗号分隔，默认为空表示不允许跨域
     */
    private String corsAllowedOrigins = "";

    /**
     * swagger是否启用
     */
    private boolean swaggerEnable = false;

    /**
     * 是否启用查询权限校验
     */
    private boolean checkSelectPermission = false;

    /**
     * 查询权限白名单
     */
    private List<String> selectPermissionWhiteList = Arrays.asList(
            "oa_sys_announcement_read",
            "oa_sys_msg",
            "sys_subsystem",
            "prt_channel",
            "prt_information",
            "prt_model",
            "prt_site",
            "prt_card"
    );


    /**
     * 密码加密处理类
     */
    private Class<DefaultPasswordEncryptHandler> passwordEncryptHandler = DefaultPasswordEncryptHandler.class;


    /**
     * 是否启用传输加密
     */
    private boolean transmittalEncryption = false;


    /**
     * 传输加密白名单
     */
    private List<String> encryptWhiteList = new ArrayList<>();

    /**
     * 机密性保护是否开启
     */
    private boolean secretProtection = true;

    /**
     * 机密性保护处理类
     */
    private Class<Sm4SecretHandler> secretHandler = Sm4SecretHandler.class;

    /**
     * 完整性保护是否开启
     */
    private boolean integrityProtection = true;

    /**
     * 完整性保护处理类
     */
    private Class<Sm3IntegrityHandler> integrityHandler = Sm3IntegrityHandler.class;

    /**
     * 日志是否异步保存
     */
    private boolean secLogAsyncSave = false;

    /**
     * 缓存实现类 redisCacheUtil/timedCacheUtil
     */
    private String cacheImpl = "redisCacheUtil";


    //以下为oauth2服务端配置

    /**
     * oauth2服务端登录地址
     */
    private String oauth2ServerLoginUrl = "#/user/login";
    /**
     * oauth2服务端授权地址
     */
    private String oauth2ServerAuthorizeUrl = "jeeStudio/gtoa/a/oauth2/authorize";


    //以下为oauth2客户端配置
    /**
     * oauth2服务端地址
     */
    private Map<String, Object> oauth2ServerMap = new LinkedHashMap<String, Object>() {
        private static final long serialVersionUID = 2925001113598349224L;

        {
            put("server", "http://127.0.0.1:8080/");
        }
    };
    /**
     * oauth2服务端上下文路径
     */
    private String oauth2ServerContextPath = "jeeStudio/gtoa/a";
    /**
     * oauth2客户端id
     */
    private String oauth2ClientId = "";
    /**
     * oauth2客户端密钥
     */
    private String oauth2ClientSecret = "";

    /**
     * oauth2客户端重定向地址
     */
    private String oauth2RedirectUri = "#/user/oauth2";

    /**
     * 是否开启同步机构的定时任务
     */
    private boolean syncOfficeJob = false;

    /**
     * 需要同步的机构列表
     */
    private List<String> syncOfficeList = new ArrayList<>();

    /**
     * 同步用户的角色
     */
    private Map<String, String> syncUserRoleMap = new HashMap<>();

    /**
     * 某个用户角色是否同步
     */
    private Class<UserRoleSyncHandler> userRoleSyncHandler = UserRoleSyncHandler.class;

    /**
     * 服务访问路径
     */
    private String serverPath = "";


    /**
     * 日志输出器
     */
    private List<Class<? extends SecLogAppender>> logAppender = Collections.singletonList(StdOutSecLogAppender.class);


    private uploadConfig uploadConfig = new uploadConfig();

    @Value("${allowedExtensions}")
    private String allowedExtensions;

    public  class uploadConfig {
        private List<String> contentTypes = Arrays.asList(
                "application/msword", //doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", //docx
                "application/vnd.ms-excel",//xls
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",//xlsx
                "application/x-rar-compressed",//rar
                "application/vnd.rar", // rar (标准 MIME 类型)
                "application/x-zip-compressed",//zip
                "application/zip",//Mac-OS zip (标准 MIME 类型)
                "application/pdf",//pdf
                "image/png",//png
                "image/jpeg",//jpg jpeg
                "text/plain",//txt
                "application/pdf",//pdf
                "video/mp4",//mp4
                "audio/mpeg",//mp3
                "video/quicktime",//mov
                "video/x-msvideo",//avi
                "audio/ogg"//ogg
        );
        private List<String> acceptFiles;

        public List<String> getContentTypes() {
            return contentTypes;
        }

        public void setContentTypes(List<String> contentTypes) {
            this.contentTypes = contentTypes;
        }

        public List<String> getAcceptFiles() {
            String[] split = allowedExtensions.split("\\.");
            acceptFiles = Arrays.asList(split);
            return acceptFiles.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        }

        public void setAcceptFiles(List<String> acceptFiles) {
            this.acceptFiles = acceptFiles;
        }
    }
}
