package com.jeestudio.bpm.common.entity.system;

import com.jeestudio.bpm.common.entity.common.DataEntity;

/**
 * @Description: 子系统
 */
public class SubSystem extends DataEntity<SubSystem> {

    private static final long serialVersionUID = 1L;

    /** 子系统编码。 */
    private String code;
    /** 子系统名称。 */
    private String name;
    /** 子系统基础访问地址。 */
    private String baseurl;
    /** 排序号。 */
    private int sort;
    /** 是否随平台启动。 */
    private String startup;
    /** 是否启用缓存。 */
    private String cache;
    /** 统一认证地址。 */
    private String caurl;
    /** 是否启用。 */
    private String enable;
    /** 是否允许同步用户或组织信息。 */
    private String syncable;
    /** 是否内部系统。 */
    private String internal;
    /** 获取访问令牌的服务地址或服务名。 */
    private String getTokenService;
    /** 修改用户信息的服务地址或服务名。 */
    private String modifyUserService;
    /** 子系统服务说明。 */
    private String serviceDesc;
    /** 获取令牌时使用的用户名密码配置。 */
    private String tokenUserNamePassword;

    public SubSystem() {
        super();
    }

    public SubSystem(String id){
        super(id);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getStartup() {
        return startup;
    }

    public void setStartup(String startup) {
        this.startup = startup;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getCaurl() {
        return caurl;
    }

    public void setCaurl(String caurl) {
        this.caurl = caurl;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getSyncable() {
        return syncable;
    }

    public void setSyncable(String syncable) {
        this.syncable = syncable;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public String getGetTokenService() {
        return getTokenService;
    }

    public void setGetTokenService(String getTokenService) {
        this.getTokenService = getTokenService;
    }

    public String getModifyUserService() {
        return modifyUserService;
    }

    public void setModifyUserService(String modifyUserService) {
        this.modifyUserService = modifyUserService;
    }

    public String getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    public String getTokenUserNamePassword() {
        return tokenUserNamePassword;
    }

    public void setTokenUserNamePassword(String tokenUserNamePassword) {
        this.tokenUserNamePassword = tokenUserNamePassword;
    }
}
