package com.jeestudio.bpm.common.entity.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.DataEntity;
import com.jeestudio.bpm.utils.Collections3;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;
import java.util.Date;
import java.util.List;

/**
 * @Description: 系统用户
 */
public class User extends DataEntity<User> {

    private static final long serialVersionUID = 1L;

    /** 归属公司或单位。 */
    private Office company;
    /** 归属部门。 */
    private Office office;
    /** 登录账号。 */
    private String loginName;
    /** 登录密码密文。 */
    private String password;
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
    /** 用户头像地址。 */
    private String photo;
    /** 用户二维码内容或图片地址。 */
    private String qrCode;
    /** 修改账号时保存的原登录账号。 */
    private String oldLoginName;
    /** 修改密码时提交的新密码明文或临时值。 */
    private String newPassword;
    /** 用户签名图片或签章信息。 */
    private String sign;
    /** 上一次登录 IP。 */
    private String oldLoginIp;
    /** 上一次登录时间。 */
    private Date oldLoginDate;
    /** 查询或编辑时使用的单个角色对象。 */
    private Role role;
    /** 用户拥有的角色列表。 */
    private List<Role> roleList = Lists.newArrayList();
    /** 兼容旧加密方式的密码字段。 */
    private String desPassword;
    /** 是否允许单点登录。 */
    private String ssoLoginFlag;
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
    /** 单点登录密码密文。 */
    private String ssoDesPassword;
    /** 修改单点登录密码时提交的新密码临时值。 */
    private String newSsoDesPassword;
    /** 用户可访问的单点登录子系统列表。 */
    private List<UserSsoSubsystem> userSsoSubsystemList = Lists.newArrayList();
    /** 是否系统内置用户。 */
    private String isSys = Global.NO;
    /** 用户密级。 */
    private String secLevel;
    /** 登录异常次数。 */
    private Integer loginExceptionCount;
    /** 密码过期时间。 */
    private Date passwordExpiredDate;
    /** 修改密码时提交的旧密码。 */
    private String oldPassword;
    /** 修改密码时提交的确认新密码。 */
    private String confirmNewPassword;
    /** 图片验证码 Base64 或验证码临时值。 */
    private String imageBaseCode;
    /** 用户状态，按业务字典解释。 */
    private String status;
    /** 用户职务或显示头衔。 */
    private String title;
    /** 用户绑定的数据权限列表。 */
    private List<Datapermission> datapermissionList = Lists.newArrayList();

    /** 外部企业或租户标识。 */
    private String extEntId;

    public User() {
        super();
        this.loginFlag = Global.YES;
    }

    public User(String id){
        super(id);
    }

    public User(String id, String loginName){
        super(id);
        this.loginName = loginName;
    }

    public User(Role role){
        super();
        this.role = role;
    }

    public User(String companyId, String loginName, String name) {
        this.company = new Office(companyId);
        this.loginName = loginName;
        this.name = name;
        this.useable = Global.YES;
        this.loginFlag = Global.YES;
    }

    public User(Office company, String loginName, String name) {
        this.company = company;
        this.loginName = loginName;
        this.name = name;
        this.useable = Global.YES;
        this.loginFlag = Global.YES;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldLoginIp() {
        if (oldLoginIp == null){
            return loginIp;
        }
        return oldLoginIp;
    }

    public void setOldLoginIp(String oldLoginIp) {
        this.oldLoginIp = oldLoginIp;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getOldLoginDate() {
        if (oldLoginDate == null){
            return loginDate;
        }
        return oldLoginDate;
    }

    public void setOldLoginDate(Date oldLoginDate) {
        this.oldLoginDate = oldLoginDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @JsonIgnore
    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public String getRoleNames() {
        return Collections3.extractToString(roleList, "name", ",");
    }

    public String getRoleEnNames() {
        return Collections3.extractToString(roleList, "enname", ",");
    }

    public boolean isAdmin(){
        return isAdmin(this.id);
    }

    public static boolean isAdmin(String id){
        return id != null && "admin".equals(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }

    public String getDesPassword() {
        return desPassword;
    }

    public void setDesPassword(String desPassword) {
        this.desPassword = desPassword;
    }

    public String getSsoLoginFlag() {
        return ssoLoginFlag;
    }

    public void setSsoLoginFlag(String ssoLoginFlag) {
        this.ssoLoginFlag = ssoLoginFlag;
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

    public String getSsoDesPassword() {
        return ssoDesPassword;
    }

    public void setSsoDesPassword(String ssoDesPassword) {
        this.ssoDesPassword = ssoDesPassword;
    }

    public String getNewSsoDesPassword() {
        return newSsoDesPassword;
    }

    public void setNewSsoDesPassword(String newSsoDesPassword) {
        this.newSsoDesPassword = newSsoDesPassword;
    }

    public List<UserSsoSubsystem> getUserSsoSubsystemList() {
        return userSsoSubsystemList;
    }

    public void setUserSsoSubsystemList(List<UserSsoSubsystem> userSsoSubsystemList) {
        this.userSsoSubsystemList = userSsoSubsystemList;
    }

    public boolean isSystem(){
        return isSystem(this.loginName);
    }

    public static boolean isSystem(String loginName){
        return loginName != null && (loginName.startsWith("sysadmin") || loginName.startsWith("secadmin") || loginName.startsWith("auditadmin"));
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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getImageBaseCode() {
        return imageBaseCode;
    }

    public void setImageBaseCode(String imageBaseCode) {
        this.imageBaseCode = imageBaseCode;
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

    public List<Datapermission> getDatapermissionList() {
        return datapermissionList;
    }

    public void setDatapermissionList(List<Datapermission> datapermissionList) {
        this.datapermissionList = datapermissionList;
    }

    public String getExtEntId() {
        return extEntId;
    }

    public void setExtEntId(String extEntId) {
        this.extEntId = extEntId;
    }
}
