package com.jeestudio.bpm.service.system;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.Office;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.entity.tagtree.TagTree;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.view.system.UserView;
import com.jeestudio.bpm.config.ProjectProperties;
import com.jeestudio.bpm.mapper.base.system.OfficeDao;
import com.jeestudio.bpm.mapper.base.system.RoleDao;
import com.jeestudio.bpm.mapper.base.system.UserDao;
import com.jeestudio.bpm.modules.admin.dao.SysSubSystemMapper;
import com.jeestudio.bpm.modules.admin.vo.UserExport;
import com.jeestudio.bpm.security.PasswordEncryptHandler;
import com.jeestudio.bpm.security.TranEncryptUtil;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.*;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import com.jeestudio.tools.security.enums.PasswordCharTypeEnum;
import com.jeestudio.tools.security.exceptions.WeakPasswordException;
import com.jeestudio.tools.security.utils.PasswordUtil;
import com.jeestudio.tools.security.utils.security.SM2Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Description: 用户管理服务
 */
@Service
public class UserService {
    @Autowired
    ProjectProperties projectProperties;

    protected static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value(value = "${sec.switch}")
    private boolean secSwitch;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OfficeDao officeDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    SysSubSystemMapper sysSubSystemMapper;

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    @Autowired
    RoleService roleService;

    public User get(String id) {
        return userDao.get(id);
    }

    public List<User> findUserListByOfficeIdList(List<String> officeIdList) {
        return userDao.findUserListByOfficeIdList(officeIdList);
    }

    public List<User> findUserListByLevelIdList(List<String> levelIdList) {
        return userDao.findUserListByLevelIdList(levelIdList);
    }

    public List<User> findUserListByPostIdList(List<String> postIdList) {
        return userDao.findUserListByPostIdList(postIdList);
    }

    public List<User> findUserListByRoleIdList(List<String> roleIdList) {
        return userDao.findUserListByRoleIdList(roleIdList);
    }

    public List<User> findUserListByUserIdList(List<String> userIdList) {
        return userDao.findUserListByUserIdList(userIdList);
    }

    public List<User> findUserListByLoginNameList(List<String> assgineeList) {
        return userDao.findUserListByLoginNameList(assgineeList);
    }

    @Deprecated
    public List<User> findUserListByOrgIdList(List<String> orgIdList) {
        return userDao.findUserListByOrgIdList(orgIdList);
    }

    public List<User> findUserListByParentId(String parentId) {
        return userDao.findUserListByParentId(parentId);
    }

    public List<User> findUserListByParentIdList(List<String> parentIdList) {
        return userDao.findUserListByParentIdList(parentIdList);
    }

    @Transactional(readOnly = false)
    public void deleteUser(User user) {
        /*userDao.delete(user);*/
        Zform zform = new Zform(user.getId());
        zform.setFormNo("sys_user");
        zformService.delete(zform);
        userDao.deleteUserRole(user);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + user.getLoginName());
    }

    public List<TagTree> getUserTagTreeAsync(String id) {
        Office office = new Office();
        User user = new User();
        user.setUseable(Global.YES);
        List<TagTree> userList = Lists.newArrayList();
        if (StringUtil.isNotEmpty(id)) {
            office.setId(id);
            user.setOffice(office);
            office.setParent(new Office(id));
            userList = userDao.findUserTagTree(user);
            for (TagTree tagTree : userList) {
                tagTree.setHasChildren(false);
            }
        } else {
            office.setParent(new Office(Global.DEFAULT_ROOT_CODE));
        }
        List<TagTree> officeList = officeDao.findOfficeTagTree(office);
        for (TagTree tagTree : officeList) {
            tagTree.setHasChildren(true);
            tagTree.setDisabled(true);
        }
        userList.addAll(officeList);
        return userList;
    }

    public List<User> findUserForFlow(String condition) {
        List<User> list = userDao.findUserForFlow(condition);
        return list;
    }

    public List<TagTree> getUserTagTree(GridselectParam gridselectParam, String loginName) {
        User user = new User();
        user.setUseable(Global.YES);
        user.setLoginName(loginName);
        List<TagTree> userList = new ArrayList<TagTree>();

        TagTree theTagTree = new TagTree();
        QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
        if (gridselectParam != null) {
            User currentUser = UserUtil.getByLoginName(loginName);
            List<GridselectParam.FilterData> filterList = gridselectParam.getFilterList();
            if (filterList != null && filterList.size() > 0) {
                for (GridselectParam.FilterData filterData : filterList) {
                    zformService.replaceFilterDataValue(currentUser, filterData);
                    zformService.filterDataChild(queryWrapper, filterData);
                }
            }
        }

        userList = userDao.findUserTagTreeAll(user);
        //List<TagTree> officeList = officeDao.findOfficeTagTreeAll();
        List<TagTree> officeList = officeDao.findOfficeTagTreeFilter(queryWrapper);
        for (TagTree office : officeList) {
            office.setDisabled(true);
        }
        userList.addAll(officeList);
        Map<String, TagTree> map = Maps.newHashMap();
        for (TagTree tagTree : userList) {
            map.put(tagTree.getId(), tagTree);
        }
        for (TagTree tagTree : userList) {
            if (StringUtil.isNotBlank(tagTree.getParentId())) {
                TagTree tt = map.get(tagTree.getParentId());
                if (tt != null) {
                    tt.setHasChildren(true);
                }
            }
        }
        return userList;
    }



    public UserView getUserView(String userId) {
        UserView userView = userDao.getUserView(userId);
        Role theRole = new Role();
        theRole.setUser(new User(userId));
        List<Role> roleList = roleDao.findList(theRole);
        List<String> subSystemCodes = sysSubSystemMapper.listSubSystemCodesByUserId(userId);
        userView.setSubSystemCodes(subSystemCodes);
        List<String> roles = Lists.newArrayList();
        for (Role role : roleList) {
            roles.add(role.getEnname());
        }
        userView.setRoles(roles);
        // 获取用户所属单位和部门的区域信息。
        if (userView.getCompany() != null && userView.getCompany().getArea() != null) {
            userView.setCompanyAreaInfo(zformService.parseAreaInfo(userView.getCompany().getArea().getId()));
        }
        if (userView.getOffice() != null && userView.getOffice().getArea() != null) {
            userView.setOfficeAreaInfo(zformService.parseAreaInfo(userView.getOffice().getArea().getId()));
        }
        return userView;
    }

    public User getByLoginName(String loginName) {
        User user = new User();
        user.setLoginName(loginName);
        return userDao.getByLoginName(user);
    }

    public User getByNo(String no) {
        return userDao.getByNo(no);
    }

    public List<UserView> findUserViewByName(String name) {
        return userDao.findUserViewByName(name);
    }

    public void clearLoginExceptionCount(String loginName) {
        userDao.clearLoginExceptionCount(loginName);
    }

    public Boolean isPasswordExpired(String loginName) {
        Date expiredDate = userDao.isPasswordExpired(loginName);
        Date nowDate = new Date();
        Boolean expired = false;
        if (expiredDate == null) {
            expired = false;
        } else {

            int compareTo = expiredDate.compareTo(nowDate);
            if (compareTo > 0) {
                expired = false;
            } else {
                expired = true;
            }
        }
        return expired;
    }

    public void unlockUser(String loginName) {
        userDao.unlockUser(loginName);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + loginName);
    }

    public void lockUser(String loginName) {
        userDao.lockUser(loginName);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + loginName);
    }

    public void enableUser(String loginName) {
        userDao.enableUser(loginName);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + loginName);
    }

    public void disableUser(String loginName) {
        userDao.disableUser(loginName);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + loginName);
    }

    public void addLoginExceptionCount(String loginName) {
        userDao.addLoginExceptionCount(loginName);
    }

    @Autowired
    PasswordEncryptHandler passwordEncryptHandler;

    public void changePasswordOk(String password, String loginName,int passwordExpiredDay) {
        if (projectProperties.isValidWeakPassword()) {
            PasswordCharTypeEnum[] passwordCharTypeEnums = validatePassword.getPasswordCharType().toArray(new PasswordCharTypeEnum[0]);
            try {
                PasswordUtil.isWeakPassword(password, projectProperties.getMinPasswordLength(), projectProperties.getPasswordCharTypeCount(), passwordCharTypeEnums);
            } catch (WeakPasswordException e) {
                logger.error("密码校验失败,{}", e.getMessage());
                throw new BusinessException(e.getMessage());
            }
        }
        password =  passwordEncryptHandler.encrypt(password,null,loginName);
        Date passwordExpiredDate = null;
        if (passwordExpiredDay == -1) {
            passwordExpiredDate = DateUtil.strToDate("1900");//Global.PASSWORD_EXPIRED_DAY;
        }else {
            passwordExpiredDate = DateUtil.addDays(new Date(), passwordExpiredDay);
        }

        userDao.changePassword(password, passwordExpiredDate, loginName);
        cacheUtil.deleteHashCache(Global.USER_CACHE, "_" + loginName);
    }

    @Transactional(readOnly = true)
    public void updateByMasterData(User user) {
        userDao.updateByMasterData(user);
    }

    public LinkedHashMap<String, List<String>> getUserPermissionByLoginName(String loginName) {
        User user = UserUtil.getByLoginName(loginName);
        LinkedHashMap<String, List<String>> permissionMap = new LinkedHashMap<String, List<String>>();
        if (user != null) {
            List<String> roleList = new ArrayList<>();
            for (Role role : user.getRoleList()) {
                roleList.add(role.getEnname());
            }
            permissionMap.put("role", roleList);
            List<String> list = UserUtil.getMenuPermissionList(user);
            List<String> menuList = new ArrayList<>();
            for (String menuPermission : list) {
                if (StringUtil.isNotBlank(menuPermission)) {
                    for (String permission : StringUtil.split(menuPermission, ",")) {
                        menuList.add(permission);
                    }
                }
            }
            permissionMap.put("menu", menuList);
        }
        return permissionMap;
    }

    public ResultJson getCurrentUserView(String userId) {
        ResultJson resultJson = new ResultJson();
        try {
            UserView userView = this.getUserView(userId);

            userView.setPasswordExpired(isPasswordExpired(userView.getLoginName()));
            resultJson.put("userView", userView);
            // 密码强度相关
            resultJson.put("minPasswordLength", projectProperties.getMinPasswordLength());
            TranEncryptUtil.setPublicKey(userId);
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("获取用户成功");
            resultJson.setMsg_en("Get current user view success");
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("获取用户失败");
            resultJson.setMsg_en("Get current user view fail");
        }
        return resultJson;
    }

    public ResultJson save(Zform zform, String loginName) throws Exception {
        boolean isValid = true;
        String password = zform.getS03();
        if (this.secSwitch && StringUtil.isNotEmpty(password)) {
            isValid = this.validPassword(password);
            if (isValid) {
                Date theDate = DateUtil.addDays(new Date(), 7);
                zform.setD01(theDate);
                zform.setS11("0");
            }
        }
        if (false == isValid) {
            ResultJson resultJson = new ResultJson();
            resultJson.setCode(ResultJson.CODE_FAILED);
            int minPasswordLength = getMinPasswordLength();
            if (password.length() < minPasswordLength) {
                resultJson.setMsg("密码至少" + minPasswordLength + "位字符");
                resultJson.setMsg_en("At least " + minPasswordLength + " characters are required.");
            } else {
                resultJson.setMsg("密码格式要求：大写字母，小写字母，数字和特殊符号，至少" + projectProperties.getPasswordCharTypeCount() + "种");
                resultJson.setMsg_en("Passwords must use at least " + projectProperties.getPasswordCharTypeCount() + " of the four available character types: uppercase letters, lowercase letters, numbers, and symbols.");
            }
            return resultJson;
        } else {
            return this.saveUser(zform, loginName);
        }
    }

    public ResultJson saveUser(User user, String loginName) {
        try {
            JSONObject userJson = new JSONObject();
            if (!user.getIsNewRecord()) {
                userJson.put("id", user.getId());
            }
            userJson.put("login_name", user.getLoginName());
            userJson.put("name", user.getName());
            userJson.put("login_flag", user.getLoginFlag());
            userJson.put("useable", user.getUseable());
            userJson.put("no", user.getNo());
            userJson.put("mobile", user.getMobile());
            userJson.put("phone", user.getPhone());
            userJson.put("email", user.getEmail());
            userJson.put("formNo", "sys_user");
            userJson.put("sort", user.getSortIndex());
            userJson.put("password", user.getPassword());
            Date passwordExpiredDate = user.getPasswordExpiredDate();
            if(passwordExpiredDate != null){
                String passwordExpiredDateStr = cn.hutool.core.date.DateUtil.formatDateTime(passwordExpiredDate);
                userJson.put("password_expired_date",passwordExpiredDateStr);
            }
            Zform zformUser = zformService.getZformFromMap(userJson, loginName);
            String parentId = user.getCompany().getId();
            if (user.getOffice() != null && StringUtil.isNotEmpty(user.getOffice().getId())) {
                parentId = user.getOffice().getId();
            }
            zformUser.setParent(new Zform(parentId));
            zformUser.setPreId(user.getPreId());
            ResultJson save = this.save(zformUser, loginName);
            user.setId(zformUser.getId());
            return save;
        } catch (Exception e) {
            logger.error(ExceptionUtil.stacktraceToString(e));
            throw new BusinessException(e.getMessage());
        }
    }

    private ResultJson saveUser(Zform zform, String loginName) {
        ResultJson resultJson = new ResultJson();
        try {
            GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
            User currentUser = UserUtil.getByLoginName(loginName);
            zform.setTempRuleArgsClass(TaskPermission.class.getSimpleName());
            if (false == zform.getIsNewRecord()) {
                //Update
                Zform t = zformService.get(zform.getId(), genTable);
                BeanUtil.copyBeanNotNull2Bean(zform, t);
                //s03 is password
                if (StringUtil.isNotEmpty(zform.getS03())) {
                    t.setS03(UserUtil.encryptPassword(zform.getS03()));
                }
                t.setUpdateBy(currentUser);
                zformService.saveAct(this.getClass().getName(), t, loginName, genTable);
            } else {
                //Insert
                zform.setCreateBy(currentUser);
                zform.setUpdateBy(currentUser);
                zform.setOwnerCode(currentUser.getCompany().getCode());
                //s03 is password
                if (StringUtil.isNotEmpty(zform.getS03())) {
                    zform.setS03(UserUtil.encryptPassword(zform.getS03()));
                }
                zformService.saveAct(this.getClass().getName(), zform, loginName, genTable);
            }
            resultJson.setCode(ResultJson.CODE_SUCCESS);
            resultJson.setMsg("保存用户成功");
            resultJson.setMsg_en("Save user success");
        } catch (Exception e) {
            logger.error("Save user error:" + ExceptionUtils.getStackTrace(e));
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("保存用户失败");
            resultJson.setMsg_en("Save user failed");
            throw new BusinessException(e.getMessage());
        }
        return resultJson;
    }
    @Autowired
    ValidatePassword validatePassword;
    public Integer getLoginExceptionCount(String loginName) {
        return userDao.getLoginExceptionCount(loginName);
    }

    public ResultJson changePassword(String oldPassword, String newPassword, String loginName) throws Exception {
        ResultJson resultJson = new ResultJson();
        User user = UserUtil.getByLoginName(loginName);
        if (false == validatePassword.validateUserPassword(oldPassword, user.getPassword(),loginName,null)) {
            resultJson.setCode(ResultJson.CODE_FAILED);
            resultJson.setMsg("原密码错误");
            resultJson.setMsg_en("The old password you have provided is wrong");
        } else {
            boolean isValid = this.validPassword(newPassword);
            if (false == isValid) {
                resultJson.setCode(ResultJson.CODE_FAILED);
                int minPasswordLength = this.getMinPasswordLength();
                if (newPassword.length() < minPasswordLength) {
                    resultJson.setMsg("密码至少" + minPasswordLength + "位字符");
                    resultJson.setMsg_en("At least " + minPasswordLength + " characters are required.");
                } else {
                    resultJson.setMsg("密码格式要求：大写字母，小写字母，数字和特殊符号，至少" + projectProperties.getPasswordCharTypeCount() + "种");
                    resultJson.setMsg_en("Passwords must use at least " + projectProperties.getPasswordCharTypeCount() + " of the four available character types: uppercase letters, lowercase letters, numbers, and symbols.");
                }
                return resultJson;
            } else {
                this.changePasswordOk(newPassword, loginName,projectProperties.getPasswordExpiredDay());
                resultJson.setCode(ResultJson.CODE_SUCCESS);
                resultJson.setMsg("修改密码成功");
                resultJson.setMsg_en("Password was updated successfully");
            }
        }
        return resultJson;
    }

    public ResultJson changePassword(String loginName) {
        ResultJson resultJson = new ResultJson();

        String newPassword = PasswordUtil.generate(getMinPasswordLength());
        this.changePasswordOk(newPassword, loginName, projectProperties.isDefaultPasswordForceModify() ? -1 : projectProperties.getPasswordExpiredDay());
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("重置密码成功，新密码为：" + newPassword);
        resultJson.setMsg_en("Password was reset successfully");

        return resultJson;
    }

    public ResultJson changeTempPassword(String loginName,String newPassword,String publicKey) {
        ResultJson resultJson = new ResultJson();
        String password =  passwordEncryptHandler.encrypt(newPassword,null,loginName);
        cacheUtil.setHashCacheExpireMinute(Global.USER_TEMP_PASSWORD,loginName,password,60L);
        resultJson.put("msg",SM2Util.encrypt(publicKey,"重置用户的临时密码成功，临时密码为：" + newPassword));
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("重置用户临时密码成功");
        resultJson.setMsg_en("Password was reset successfully");
        return resultJson;
    }

    @Transactional
    public List<UserExport> batchResetPassword(String newPassword, List<LinkedHashMap> userList) {
        if (StringUtil.isNotEmpty(newPassword)) {
            boolean isValid = this.validPassword(newPassword);
            if (false == isValid) {
                int minPasswordLength = this.getMinPasswordLength();
                if (newPassword.length() < minPasswordLength) {
                    throw new BusinessException("密码至少" + minPasswordLength + "位字符");
                }
                throw new BusinessException("密码格式要求：大写字母，小写字母，数字和特殊符号，至少" + projectProperties.getPasswordCharTypeCount() + "种");
            }
        }
        List<UserExport> userExportList = new ArrayList<>();
        for (LinkedHashMap map : userList) {
            String loginName =   ConvertUtil.getString(map.get("login_name"));
            String name =   ConvertUtil.getString(map.get("name"));
            Object parent = map.get("parent");
            String orgName = "";
            if (parent != null) {
                orgName = ConvertUtil.getString(((Map) parent).get("name"));
            }
            String _newPassword = newPassword;
            if (StringUtil.isEmpty(_newPassword)) {
                _newPassword = PasswordUtil.generate();
            }
            this.changePasswordOk(_newPassword, loginName, projectProperties.isDefaultPasswordForceModify() ? -1 : projectProperties.getPasswordExpiredDay());
            UserExport userExport = new UserExport();
            userExport.setLoginName(loginName);
            userExport.setName(name);
            userExport.setOrgName(orgName);
            userExport.setPassword(_newPassword);
            userExportList.add(userExport);
        }
        return userExportList;
    }

    /**
     * 获取密码最小长度 8位或者配置文件中的最小密码长度
     * @return
     */
    private int getMinPasswordLength() {
        return Math.max(8, projectProperties.getMinPasswordLength());
    }

    private boolean validPassword(String password) {
        boolean isValid = true;
        if (this.secSwitch && StringUtil.isNotEmpty(password)) {
            if (password.length() < getMinPasswordLength()) {
                isValid = false;
            } else {
                List<PasswordCharTypeEnum> passwordCharTypeList = validatePassword.getPasswordCharType();
                int total = 0;
                for (PasswordCharTypeEnum passwordCharTypeEnum : passwordCharTypeList) {
                    if (PasswordUtil.validPasswordChar(passwordCharTypeEnum, password)) {
                        total++;
                    }
                }
                // 密码字符类型要求
                if (total < projectProperties.getPasswordCharTypeCount()) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }


    /**
     * 查询免密登录用户，若不存则创建免密登录用户
     */
    public User createFreeUser(String loginName) {
        User user = UserUtil.getByLoginName(loginName);
        if (user == null) {
            userDao.insertFreeUser(loginName, new Date());
        }
        return UserUtil.getByLoginName(loginName);
    }

    public void updateUserRealName(String loginName, String realName) {
        userDao.updateUserRealName(loginName, realName);
    }

    /**
     * 更新用户信息
     *
     * @param id     用户id
     * @param name   姓名
     * @param email  邮箱
     * @param phone  固定电话
     * @param mobile 手机
     */
    public String updateUser(String id, String name, String email, String phone, String mobile,String appId) throws Exception {
        JSONObject userJson = new JSONObject();
        List<String> roleIdList = new ArrayList<>();
        List<String> dataRoleIdList = new ArrayList<>();


        if (StringUtil.isNotEmpty(appId)&&StringUtil.isNotEmpty(mobile)){
            QueryWrapper<Zform> zformQueryWrapper = new QueryWrapper<>();
            zformQueryWrapper.like("a.no", appId);
            zformQueryWrapper.eq("a.mobile", mobile);
            List<LinkedHashMap> mapList = zformService.findMapList("sys_user", zformQueryWrapper);
            if (mapList.size()>0){
                //如果更新的手机号已经存在，删除当前用户,并将id设置为已存在的用户id
                LinkedHashMap linkedHashMap = mapList.get(0);
                String dbId = (String) linkedHashMap.get("id");
                if (StringUtil.isNotEmpty(dbId)&&!dbId.equals(id)){

                    User user = get(id);
                    userJson.put("no", user.getNo());
                    userJson.put("login_name", user.getLoginName());
                    QueryWrapper<Zform> userQueryWrapper = new QueryWrapper<>();
                    userQueryWrapper.eq("a.user_id", id);
                    List<LinkedHashMap> roleList = zformService.findMapList("sys_user_role", userQueryWrapper,false);
                    roleList.forEach(role->{
                        roleIdList.add(ConvertUtil.getString(role.get("role_id")));
                    });
                    List<LinkedHashMap> dataRoleList = zformService.findMapList("sys_user_datarole", userQueryWrapper,false);
                    dataRoleList.forEach(role->{
                        dataRoleIdList.add(ConvertUtil.getString(role.get("datarole_id")));
                    });


                    Zform zform = new Zform(id);
                    zform.setFormNo("sys_user");
                    zformService.delete(zform);
                    id = dbId;

                    QueryWrapper<Zform> userQueryWrapper2 = new QueryWrapper<>();
                    userQueryWrapper2.eq("a.user_id", dbId);
                    roleList = zformService.findMapList("sys_user_role", userQueryWrapper2,false);
                    roleList.forEach(role->{
                        roleIdList.add(ConvertUtil.getString(role.get("role_id")));
                    });
                    dataRoleList = zformService.findMapList("sys_user_datarole", userQueryWrapper2,false);
                    dataRoleList.forEach(role->{
                        dataRoleIdList.add(ConvertUtil.getString(role.get("datarole_id")));
                    });
                }
            }else{
                QueryWrapper<Zform> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.eq("a.user_id", id);
                List<LinkedHashMap> roleList = zformService.findMapList("sys_user_role", userQueryWrapper,false);
                roleList.forEach(role->{
                    roleIdList.add(ConvertUtil.getString(role.get("role_id")));
                });
                List<LinkedHashMap> dataRoleList = zformService.findMapList("sys_user_datarole", userQueryWrapper,false);
                dataRoleList.forEach(role->{
                    dataRoleIdList.add(ConvertUtil.getString(role.get("datarole_id")));
                });
            }

        }

        userJson.put("id", id);
        if (StringUtil.isNotEmpty(name)) {
            userJson.put("name", name);
        }
        if (StringUtil.isNotEmpty(email)) {
            userJson.put("email", email);
        }
        if (StringUtil.isNotEmpty(phone)) {
            userJson.put("phone", phone);
        }
        if (StringUtil.isNotEmpty(mobile)) {
            userJson.put("mobile", mobile);
        }
        userJson.put("formNo", "sys_user");
        Zform zform = zformService.getZformFromMap(userJson, UserUtil.getCurrentLoginName());
        try {
            zformService.saveZform(zform, UserUtil.getCurrentLoginName(), null);

            for (String s : roleIdList) {
                roleService.saveUserRole(id,s);
            }
            for (String s : dataRoleIdList) {
                roleService.saveUserDataRoleId(id,s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    /**
     * 更新签名
     */
    public void updateSign(String signGroupId)  {
        userDao.updateSign(UserUtil.getCurrentLoginName(),signGroupId);
    }

    /**
     * 查询用户主管的部门下的用户
     */
    public List<UserView> findSubordinateListByPrimaryPerson(String primaryPersonId) {
        return userDao.findSubordinateListByPrimaryPerson(primaryPersonId);
    }
}
