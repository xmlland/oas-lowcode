package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.common.entity.system.Datapermission;
import com.jeestudio.bpm.mapper.base.system.DatapermissionDao;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: 数据权限服务
 */
@Service
public class DatapermissionService {

    @Autowired
    private DatapermissionDao datapermissionDao;

    public List<Datapermission> getDatapermissionTree() {
        Datapermission datapermission = new Datapermission();
        return datapermissionDao.findList(datapermission);
    }

    @Transactional(readOnly = true)
    public List<String> getPermission(String id) {
        return datapermissionDao.getPermission(id);
    }

    public void savePermission(String id, String ids) {
        datapermissionDao.deletePermissionById(id);
        String[] id_s = {};
        if (StringUtil.isNotBlank(ids)) {
            id_s = ids.split(",");
            datapermissionDao.savePermission(id, Arrays.asList(id_s));
        }
    }
}
