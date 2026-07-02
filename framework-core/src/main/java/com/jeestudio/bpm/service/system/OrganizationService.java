package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.common.entity.system.Organization;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.mapper.base.system.OrganizationDao;
import com.jeestudio.bpm.service.common.CrudService;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 组织管理服务
 */
@Service
public class OrganizationService extends CrudService<OrganizationDao, Organization> {

    @Transactional(readOnly = false)
    public List<User> findUserToOrg(String id) {
        return dao.findUserToOrg(id);
    }

    @Transactional(readOnly = false)
    public void insertUserToOrg(Organization organization, User user) {
        List<User> userList = findUserToOrg(organization.getId());
        if (false == userList.contains(user)) dao.insertUserToOrg(organization, user);
    }

    @Transactional(readOnly = false)
    public void deleteUserToOrg(String userId, String orgId) {
        dao.deleteUserToOrg(userId, orgId);
    }

    @Transactional(readOnly = false)
    public int findOrgNumberBy(String org) {
        return dao.findOrgNumberBy(org);
    }

    @Transactional(readOnly = false)
    public List<Organization> findListByUser(Organization organization) {
        return dao.findListByUser(organization);
    }

    public int getCountByOrgAndUser(String orgId, String userId) {
        int count = dao.getCountByOrgAndUser(orgId, userId);
        return count;
    }

    @Transactional(readOnly = true)
    public List<String> getAssign(String id) {
        return dao.getAssign(id);
    }

    @Transactional(readOnly = false)
    public void saveAssign(String id, String ids) {
        dao.deleteAssignById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            dao.saveAssign(id, Arrays.asList(id_s));
        }
    }
}
