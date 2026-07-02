package com.jeestudio.bpm.utils;


import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.common.security.Digests;
import com.jeestudio.bpm.contextHolder.ApplicationContextHolder;
import com.jeestudio.bpm.mapper.base.system.DatapermissionDao;
import com.jeestudio.bpm.mapper.base.system.MenuDao;
import com.jeestudio.bpm.mapper.base.system.RoleDao;
import com.jeestudio.bpm.mapper.base.system.UserDao;
import com.jeestudio.bpm.service.async.AsyncService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 用户上下文工具
 */
@Slf4j
public class UserUtil {
    private static ThreadLocal<String> currentUserName = new ThreadLocal<>();

    protected static ThreadLocal<String> currentToken = new ThreadLocal<>();
    public static final int HASH_INTERATIONS = 1024;
    public static final int SALT_SIZE = 8;

    private static MenuDao menuDao = ApplicationContextHolder.getBean(MenuDao.class);
    private static UserDao userDao = ApplicationContextHolder.getBean(UserDao.class);
    private static RoleDao roleDao = ApplicationContextHolder.getBean(RoleDao.class);
    private static DatapermissionDao datapermissionDao = ApplicationContextHolder.getBean(DatapermissionDao.class);
    //private static CacheUtil cacheUtil = ApplicationContextHolder.getBean(CacheUtil.class);
    private static CacheUtil cacheUtil = new CacheUtil();
    private static AsyncService asyncService = ApplicationContextHolder.getBean(AsyncService.class);

    public static final String LoginSsoHash = "LoginSsoHash";

    public static List<MenuResult> getMenuListDesign(User user) {
        List<MenuResult> list = getMenuList(user,"");
        return list.stream().filter(t -> (t.getPageID().equals("79") || t.getParentIDS().contains(",79,"))).collect(Collectors.toList());
    }

    public static List<MenuResult> getMenuListHt(User user,String subSystemCode) {
        List<MenuResult> list = getMenuList(user,subSystemCode);
        return list.stream().filter(t -> !t.getPageID().equals("79") && !t.getParentIDS().contains(",79,")).collect(Collectors.toList());
    }

    /**
     * Get current user's menu
     */
    public static List<MenuResult> getMenuList(User user,String subSystemCode) {
        if(cacheUtil != null && userDao != null){
            Object menuObject = cacheUtil.getHashCache(Global.MENU_CACHE, subSystemCode+",_hashMenu_," + user.getId());
            List<MenuResult> menuList = new ArrayList<>();
            List<MenuResult> menuListByUseOffice = new ArrayList<>();
            String menuString = "";
            if (menuObject == null){
                Menu m = new Menu();
                Role theRole = new Role();
                theRole.setUser(user);
                List<Role> roleList = roleDao.findList(theRole);
                List<String> roleEnNameList = roleList.stream().map(Role::getEnname).collect(Collectors.toList());
                if (user.isAdmin() || "admin".equals(user.getId()) || roleEnNameList.contains("system")){
                    log.info("getMenuList current user is admin,{},role arr：{}", user.getLoginName(), roleEnNameList);
                    menuList = menuDao.findMenuAllList(m,subSystemCode);
                }else{
                    m.setUserId(user.getId());
                    menuList = menuDao.findMenuByUserId(m,subSystemCode);
                    menuListByUseOffice = menuDao.findMenuByUserOffice(m,subSystemCode);
                    menuList.addAll(menuListByUseOffice);
                }
                // region 使用java去重
                menuList = menuList.stream().distinct().collect(Collectors.toList());
                // endregion
                menuString = JsonConvertUtil.gsonBuilder().toJson(menuList);
                asyncService.asyncSaveHashCache(Global.MENU_CACHE, subSystemCode+",_hashMenu_,"+ user.getId(), menuString);
            }else{
                menuList = JsonConvertUtil.gsonBuilder().fromJson(menuObject.toString(), new TypeToken<List<MenuResult>>() {}.getType());
            }
            return menuList;
        }else{
            return null;
        }
    }

    /**
     * 判断当前用户是否有某个权限
     * @param permission 权限编码
     * @return 是否有权限
     */
    public static boolean hasmenuPermission(String permission){
        User user = getCurrentUser();
        if (user == null){
            return false;
        }
        List<String> menuList = getMenuPermissionList(user);
        if (menuList == null){
            return false;
        }
        return menuList.contains(permission);
    }

    /**
     * Get user by id
     * @param id
     * @return user
     */
    public static User get(String id){
        if(cacheUtil != null && userDao != null){
            Object userObject = cacheUtil.getHashCache(Global.USER_CACHE,"_" + id);
            User user = null;
            if (userObject == null){
                user = userDao.get(id);
                if (user == null){
                    return null;
                }
                user.setRoleList(roleDao.findList(new Role(user)));
                String userString = JsonConvertUtil.objectToJson(user);
                asyncService.asyncSaveHashCache(Global.USER_CACHE,"_" + user.getLoginName(),userString);
                asyncService.asyncSaveHashCache(Global.USER_CACHE,"_" + user.getId(),userString);
            }else{
                user = JsonConvertUtil.gsonBuilder().fromJson(userObject.toString() , User.class);
            }
            return user;
        }else{
            return null;
        }
    }

    /**
     * Get users by login
     * @param loginName
     * @return user
     */
    public static User getByLoginName(String loginName) {
        if (StringUtil.isEmpty(loginName)){
            return null;
        }
        if(cacheUtil != null && userDao != null){
            Object userObject = cacheUtil.getHashCache(Global.USER_CACHE,"_" + loginName);
            User user = null;
            if (userObject == null){
                user = userDao.getByLoginName(new User(null, loginName));
                if (user == null){
                    return null;
                }
                user.setRoleList(roleDao.findList(new Role(user)));
                List<Datapermission> userDatapermissionList = datapermissionDao.findListByUserId(user.getId());
                List<String> roleIds = user.getRoleList().stream()
                        .map(Role::getId)
                        .collect(Collectors.toList());
                List<Datapermission> roleDatapermissionList = datapermissionDao.findListByRoleIds(roleIds);
                // 规则说明：用户数据角色与角色数据角色条件冲突时，以用户数据角色为准
                userDatapermissionList.addAll(roleDatapermissionList);
                user.setDatapermissionList(userDatapermissionList);
                String userString = JsonConvertUtil.gsonBuilder().toJson(user);
                asyncService.asyncSaveHashCache(Global.USER_CACHE,"_" + user.getLoginName(),userString);
                asyncService.asyncSaveHashCache(Global.USER_CACHE,"_" + user.getId(),userString);
            }else{
                user = JsonConvertUtil.gsonBuilder().fromJson(userObject.toString(), User.class);
            }
            return user;
        }else{
            return null;
        }
    }

    /**
     * Get menu list of the user
     * @param user
     * @return menu list
     */
    public static List<String> getMenuPermissionList(User user) {
        if(cacheUtil != null && userDao != null){
            Object menuObject = cacheUtil.getHashCache(Global.MENU_CACHE, "_permission_" + user.getId());
            List<String> menuList = new ArrayList<>();
            List<MenuResult> menuListByUseOffice = new ArrayList<>();
            if (menuObject == null || "".equals(menuObject)){
                Menu m = new Menu();
                Role theRole = new Role();
                theRole.setUser(user);
                List<Role> roleList = roleDao.findList(theRole);
                List<String> roleEnNameList = roleList.stream().map(Role::getEnname).collect(Collectors.toList());
                if (user.isAdmin() || "admin".equals(user.getId()) || roleEnNameList.contains("system")){
                    log.info("getMenuPermissionList current user is admin,{},role arr：{}", user.getLoginName(), roleEnNameList);
                    menuList = menuDao.findAllPermissionList(m);
                }else{
                    m.setUserId(user.getId());
                    menuList = menuDao.findPermissionByUserId(m);
                    menuListByUseOffice = menuDao.findMenuByUserOffice(m,null);
                    if (CollUtil.isNotEmpty(menuListByUseOffice)){
                        List<String> pageIdList = menuListByUseOffice.stream().map(i -> i.getPermission()).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(pageIdList)) {
                            menuList.addAll(pageIdList);
                        }
                    }

                }
                String menuString = JsonConvertUtil.gsonBuilder().toJson(menuList);
                asyncService.asyncSaveHashCache(Global.MENU_CACHE, "_permission_" + user.getId(), menuString);
            }else{
                menuList = JsonConvertUtil.gsonBuilder().fromJson(menuObject.toString(), new TypeToken<List<String>>() {}.getType());
            }
            return menuList;
        }else{
            return null;
        }
    }

    /**
     * Encrypt password
     * @param plainPassword
     * @return password encrypted
     */
    @Deprecated
    public static String encryptPassword(String plainPassword) {
        byte[] salt = Digests.generateSalt(SALT_SIZE);
        byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), salt,
                HASH_INTERATIONS);
        return Encodes.encodeHex(salt) + Encodes.encodeHex(hashPassword);
    }

    /**
     * 根据用户名获取该用户所在部门code
     * @param loginName
     * @return
     */
    public static String getOfficeByLoginName(String loginName){
        return userDao.getOfficeCodeByLoginName(loginName);
    }

    public static void setLoginSso(String key, String value) {
        if(cacheUtil != null) {
            cacheUtil.setHashCache(LoginSsoHash, key, value);
        }
    }

    public static String getLoginSso(String key) {
        String value = null;
        if(cacheUtil != null) {
            Object objectValue = cacheUtil.getHashCache(LoginSsoHash, key);
            if (objectValue != null) {
                value = objectValue.toString();
            }
        }
        return value;
    }
    public static void setCurrentLoginName(String loginName) {
        currentUserName.set(loginName);
    }
    public static void setCurrentToken(String token) {
        currentToken.set(token);
    }

    public static String getCurrentLoginName() {
        return currentUserName.get();
    }

    public static String getCurrentToken() {
        return currentToken.get();
    }

    public static User getCurrentUser() {
        if (currentUserName.get() != null) {
            User user = getByLoginName(currentUserName.get());
            return user;
        }
        return null;
    }

    public static String getCurrentUserId() {
        if (currentUserName.get() != null) {
            User user = getByLoginName(currentUserName.get());
            return user.getId();
        }
        return "unknown";
    }

    public static boolean hasRoleEngName(String... roleEngNameList){
        List<Role> roleList = getCurrentUser().getRoleList();
        Map<String,Role> roleMap  = new HashMap<>();
        roleList.forEach(r-> roleMap.put(r.getEnname(),r));

        for (String name : roleEngNameList) {
            if(roleMap.containsKey(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为管理员
     *
     * @return
     */
    public static boolean isAdmin() {
        return getCurrentUser().isAdmin() || hasRoleEngName("system");
    }

    public static boolean hasRoleEngNameLike(String roleStr){
        List<Role> roleList = getCurrentUser().getRoleList();
        for (Role role : roleList) {
            if(role.getEnname().contains(roleStr)){
                return true;
            }
        }
        return false;
    }


}
