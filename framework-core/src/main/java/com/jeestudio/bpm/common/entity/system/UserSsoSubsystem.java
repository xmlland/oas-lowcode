package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 系统用户在其他子系统中的映射
 */
public class UserSsoSubsystem extends DataEntity<UserSsoSubsystem> {

    private static final long serialVersionUID = 1L;

    /** 归属机构编码。 */
    private String ownerCode;
    /** 平台用户 ID。 */
    private String userId;
    /** 关联的单点登录子系统。 */
    private SubSystem ssoSubsystem;
    /** 用户在子系统中的登录账号。 */
    private String loginName;
    /** 用户在子系统中的密码或凭据密文。 */
    private String password;
    /** 是否允许登录该子系统。 */
    private String isAllow;

    public UserSsoSubsystem() {
        super();
    }

    public UserSsoSubsystem(String id){
        super(id);
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SubSystem getSsoSubsystem() {
        return ssoSubsystem;
    }

    public void setSsoSubsystem(SubSystem ssoSubsystem) {
        this.ssoSubsystem = ssoSubsystem;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsAllow() {
        return isAllow;
    }

    public void setIsAllow(String isAllow) {
        this.isAllow = isAllow;
    }
}
