package com.jeestudio.tools.security.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 密码常量
 */
public interface PasswordConstant {

    /**
     * 常见弱密码
     */
    List<String> WEAK_PASSWORD_LIST = Arrays.asList("111111", "112233", "123123", "654321", "314159", "101010", "123321",
            "abcdef", "abcabc", "123456", "abc123", "a1b2c3", "aaa111", "ABCDEF", "abcABC",
            "02468", "acegi", "147258", "123!@#", "qwer123", "123qwe",
            "Qwerty", "qweasd", "mnbvcxz", "poiuytrewq", "0p9o8i7u", "asdfghjkl", "!@#$%^&*",
            "root", "password", "pass", "admin", "admin@XXX", "calvin", "bane@7766",
            "talent", "shell", "system", "private", "netscreen", "cisco",
            "administor", "admin_default", "ruijie", "sangfor", "sangfor@2018", "dlanrecover",
            "venus60", "leadsec.waf", "administrator", "h3c", "firewall", "ftppwd", "venus.fw",
            "weboper", "safetybase", "shadow", "test", "p@ssw0rd", "passw0rd", "forbidden", "woaini1314",
            "jintianxingqiwu ", "easypassword", "cmscms", "june1995", "199001", "@163.com", "beijing",
            "!@#$%^&*", "secret", "zhangwei", "sys123", "system123", "knight", "jordan", "hell0w0rd",
            "superman", "success", "loveme", "%null%", "welcome", "buzhidao", "%zhangsan0807%", "zhangsan0807@", "ZS@0807", "Upenv!@#",
            "cnemc","jczz","zlpr","upenv");


    // 键盘字母布局
    String[] KEYBOARD_ROWS = {
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
    };

    String SIMPLE_NUMBER_COMBINATION_EXCEPTION_MESSAGE = "密码中包含简单数字组合";
    String SEQUENTIAL_CHARACTER_COMBINATION_EXCEPTION_MESSAGE = "密码中包含顺序字符组合";

    String ADJACENT_KEY_COMBINATION_EXCEPTION_MESSAGE = "密码中包含键位临近字符组合";

    //密码中包含常见弱密码字符
    String NORMAl_WEAK_PASSWORD_EXCEPTION_MESSAGE = "密码中包含常见弱密码字符";

    String LENGTH_EXCEPTION_MESSAGE = "密码长度不符合要求";
}
