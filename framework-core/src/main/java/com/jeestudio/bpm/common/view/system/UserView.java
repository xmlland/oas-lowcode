package com.jeestudio.bpm.common.view.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 用户视图
 */
public class UserView extends DataEntity<UserView> {

    private static final long serialVersionUID = 1L;

    /** 归属公司或单位。 */
    private Office company;
    /** 归属部门。 */
    private Office office;
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
    /** 用户类型，按业务字典解释。 */
    private String userType;
    /** 最近一次登录 IP。 */
    private String loginIp;
    /** 最近一次登录时间。 */
    private Date loginDate;
    /** 是否允许登录。 */
    private String loginFlag;
    /** 修改账号时保存的原登录账号。 */
    private String oldLoginName;
    /** 上一次登录 IP。 */
    private String oldLoginIp;
    /** 上一次登录时间。 */
    private Date oldLoginDate;
    /** 主岗位。 */
    private Post post;
    /** 兼职岗位。 */
    private Post partPost;
    /** 职务级别。 */
    private Level level;
    /** 是否启用。 */
    private String useable;
    /** 用户排序号。 */
    private Integer sortIndex;
    /** 是否系统内置用户。 */
    private String isSys = Global.NO;
    /** 用户密级。 */
    private String secLevel;
    /** 登录异常次数。 */
    private Integer loginExceptionCount;
    /** 密码过期时间。 */
    private Date passwordExpiredDate;
    /** 用户状态，按业务字典解释。 */
    private String status;
    /** 用户签名图片或签章信息。 */
    private String sign;

    /** 用户职务或显示头衔。 */
    private String title;
    /** 角色编码列表。 */
    private List<String> roles;
    /** 有权限访问的子系统编码列表。 */
    private List<String> subSystemCodes;

    /** 密码是否已过期。 */
    private boolean passwordExpired;

    /** 公司所属行政区信息。 */
    private Map<String, String> companyAreaInfo;

    /** 部门所属行政区信息。 */
    private Map<String, String> officeAreaInfo;

    public UserView() {
        super();
        this.loginFlag = Global.YES;
    }

    public UserView(String id) {
        super(id);
    }

    public UserView(String id, String loginName) {
        super(id);
        this.loginName = loginName;
    }

    public UserView(Role role) {
        super();
    }

    public String getLoginFlag() {
        return loginFlag;
    }

    public void setLoginFlag(String loginFlag) {
        this.loginFlag = loginFlag;
    }

    public String getId() {
        return id;
    }

    public Office getCompany() {
        return company;
    }

    public void setCompany(Office company) {
        this.company = company;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return name;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public String getOldLoginName() {
        return oldLoginName;
    }

    public void setOldLoginName(String oldLoginName) {
        this.oldLoginName = oldLoginName;
    }

    public String getOldLoginIp() {
        if (oldLoginIp == null) {
            return loginIp;
        }
        return oldLoginIp;
    }

    public void setOldLoginIp(String oldLoginIp) {
        this.oldLoginIp = oldLoginIp;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getOldLoginDate() {
        if (oldLoginDate == null) {
            return loginDate;
        }
        return oldLoginDate;
    }

    public void setOldLoginDate(Date oldLoginDate) {
        this.oldLoginDate = oldLoginDate;
    }

    public boolean isAdmin() {
        return isAdmin(this.id);
    }

    public static boolean isAdmin(String id) {
        return id != null && "1".equals(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Post getPartPost() {
        return partPost;
    }

    public void setPartPost(Post partPost) {
        this.partPost = partPost;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getUseable() {
        return useable;
    }

    public void setUseable(String useable) {
        this.useable = useable;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public boolean isSystem() {
        return isSystem(this.loginName);
    }

    public static boolean isSystem(String loginName) {
        return loginName != null && (loginName.startsWith("sysadmin") || loginName.startsWith("secadmin") || loginName.startsWith("auditadmin"));
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getIsSys() {
        return isSys;
    }

    public void setIsSys(String isSys) {
        if (StringUtil.isEmpty(isSys)) isSys = "0";
        this.isSys = isSys;
    }

    public String getSecLevel() {
        return secLevel;
    }

    public void setSecLevel(String secLevel) {
        this.secLevel = secLevel;
    }

    public Integer getLoginExceptionCount() {
        return loginExceptionCount;
    }

    public void setLoginExceptionCount(Integer loginExceptionCount) {
        this.loginExceptionCount = loginExceptionCount;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getPasswordExpiredDate() {
        return passwordExpiredDate;
    }

    public void setPasswordExpiredDate(Date passwordExpiredDate) {
        this.passwordExpiredDate = passwordExpiredDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getSubSystemCodes() {
        return subSystemCodes;
    }

    public void setSubSystemCodes(List<String> subSystemCodes) {
        this.subSystemCodes = subSystemCodes;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public Map<String, String> getCompanyAreaInfo() {
        return companyAreaInfo;
    }

    public void setCompanyAreaInfo(Map<String, String> companyAreaInfo) {
        this.companyAreaInfo = companyAreaInfo;
    }

    public Map<String, String> getOfficeAreaInfo() {
        return officeAreaInfo;
    }

    public void setOfficeAreaInfo(Map<String, String> officeAreaInfo) {
        this.officeAreaInfo = officeAreaInfo;
    }
}
