package com.jeestudio.bpm.utils;

/**
 * @Description: 全局常量
 */
public class Global {

    private static Global global = new Global();

    public static final String YES = "1";
    public static final String NO = "0";

    /**
     * English language flag
     */
    public static final String LANG_EN = "EN";

    /**
     * JWT-account:
     */
    public final static String USERNAME = "username";
    public final static String USERID = "userId";

    /**
     * Redis expiration date minute
     */
    public final static Long EXRP_MINUTE = 300L;

    public final static String PREFIX_SHIRO_CACHE = "shiro:cache:";
    public final static String PREFIX_SHIRO_REFRESH_TOKEN = "shiro:refresh:";
    public final static String USER_CACHE = "user:cache:";
    public final static String USER_TEMP_PASSWORD = "user:tempPassword:";
    public final static String USER_AUTH_KEY = "user:auth:";

    public final static String SM2KEY_CACHE = "sm2key:cache:";
    public final static String MENU_CACHE = "menu:cache:";
    public final static String DICT_CACHE = "dcit:cache:";

    /**
     * Default root code of a tree
     */
    public static final String DEFAULT_ROOT_CODE = "0";

    public final static String UNKNOWN = "unknown";
    public final static String STRING_NULL = "null";
}
