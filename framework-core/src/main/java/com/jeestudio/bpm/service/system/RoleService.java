package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.mapper.base.system.RoleDao;
import com.jeestudio.bpm.service.common.CrudService;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.bpm.utils.UserUtil;
import com.jeestudio.tools.base.exceptions.BusinessException;
import com.jeestudio.tools.base.utils.ConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 角色管理服务
 */
@Service
@Slf4j
public class RoleService extends CrudService<RoleDao, Role> {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    ZformService zformService;

    @Transactional(readOnly = true)
    public List<String> getAuth(String id) {
        return roleDao.getAuth(id);
    }

    @Transactional(readOnly = false)
    public void saveAuth(String id, String ids) {
        roleDao.deleteAuthById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            roleDao.saveAuth(id, Arrays.asList(id_s));
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAssign(String id) {
        return roleDao.getAssign(id);
    }

    @Transactional(readOnly = true)
    public List<String> getDataAssign(String id) {
        return roleDao.getDataAssign(id);
    }

    @Transactional(readOnly = false)
    public void saveAssign(String id, String ids) {
        roleDao.deleteAssignById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            roleDao.saveAssign(id, Arrays.asList(id_s));
        }
    }

    @Transactional(readOnly = false)
    public void saveDataAssign(String id, String ids) {
        roleDao.deleteDataAssignById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            roleDao.saveDataAssign(id, Arrays.asList(id_s));
        }
    }

    public Role getRoleByCode(String code) {
        return roleDao.findUniqueByProperty("enname", code);
    }


    /**
     * 保存用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public void saveUserRole(String userId, String... roleId) throws Exception {
        String loginName = UserUtil.getCurrentLoginName();
        for (String role : roleId) {
            if (StringUtil.isNotEmpty(role)) {
                QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userId);
                queryWrapper.eq("role_id", role);
                List<LinkedHashMap> sys_user_role = zformService.findMapList("sys_user_role", queryWrapper,false);
                if (sys_user_role.size()>0){
                    log.warn("用户角色已存在，不再重复添加");
                    continue;
                }
                JSONObject userRole = new JSONObject();
                userRole.put("user_id", userId);
                userRole.put("role_id", role);
                userRole.put("formNo", "sys_user_role");
                Zform zformRole = zformService.getZformFromMap(userRole, loginName);
                try {
                    zformService.saveZform(zformRole, loginName, null);
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        }
    }

    public void saveUserDataRole(String userId, String... roleCode) throws Exception {
        String loginName = UserUtil.getCurrentLoginName();
        for (String code : roleCode) {
            List<LinkedHashMap> mapList = zformService.findMapList("sys_datarole", new QueryWrapper<Zform>().eq("enname", code));
            if (mapList.size()>0) {
                String id = ConvertUtil.getString(mapList.get(0).get("id"));
                QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", userId);
                queryWrapper.eq("datarole_id", id);
                List<LinkedHashMap> sys_user_role = zformService.findMapList("sys_user_datarole", queryWrapper,false);
                if (sys_user_role.size()>0){
                    log.warn("用户数据角色已存在，不再重复添加");
                    continue;
                }
                JSONObject userRole = new JSONObject();
                userRole.put("user_id", userId);
                userRole.put("datarole_id", id);
                userRole.put("formNo", "sys_user_datarole");
                Zform zformRole = zformService.getZformFromMap(userRole, loginName);
                try {
                    zformService.saveZform(zformRole, loginName, null);
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }
            }
        }
    }

    public void saveUserDataRoleId(String userId, String... roleId) throws Exception {
        String loginName = UserUtil.getCurrentLoginName();
        for (String id : roleId) {
            QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("datarole_id", id);
            List<LinkedHashMap> sys_user_role = zformService.findMapList("sys_user_datarole", queryWrapper, false);
            if (sys_user_role.size() > 0) {
                log.warn("用户数据角色已存在，不再重复添加");
                continue;
            }
            JSONObject userRole = new JSONObject();
            userRole.put("user_id", userId);
            userRole.put("datarole_id", id);
            userRole.put("formNo", "sys_user_datarole");
            Zform zformRole = zformService.getZformFromMap(userRole, loginName);
            try {
                zformService.saveZform(zformRole, loginName, null);
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
    }
}
