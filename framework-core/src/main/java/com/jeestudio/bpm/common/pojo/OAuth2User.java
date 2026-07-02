package com.jeestudio.bpm.common.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: OAuth2用户
 */
@Data
public class OAuth2User implements Serializable {
    private static final long serialVersionUID = 6545304628134350338L;

    /** 用户 ID。 */
    private String id;
    /** 登录账号。 */
    private String loginName;
    /** 工号或用户编号。 */
    private String no;
    /** 用户姓名。 */
    private String name;
    /** 邮箱地址。 */
    private String email;
    /** 办公电话。 */
    private String phone;
    /** 手机号码。 */
    private String mobile;
}
