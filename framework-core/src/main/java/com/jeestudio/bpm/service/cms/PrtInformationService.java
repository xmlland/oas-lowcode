package com.jeestudio.bpm.service.cms;

import com.jeestudio.bpm.common.entity.cms.PrtChannel;
import com.jeestudio.bpm.common.entity.cms.PrtInformation;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.mapper.base.cms.PrtInformationDao;
import com.jeestudio.bpm.utils.Encodes;
import com.jeestudio.bpm.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 内容信息服务
 */
@Service
public class PrtInformationService {

    @Autowired
    PrtInformationDao prtInformationDao;

    public PrtInformation get(String id) {
        return prtInformationDao.get(id);
    }

    public List<PrtInformation> findList(PrtInformation prtInformation) {
        return prtInformationDao.findList(prtInformation);
    }

    public Page<PrtInformation> findPage(Page<PrtInformation> page, PrtInformation prtInformation, String loginName) {
        prtInformation.setPage(page);
        page.setList(this.findList(prtInformation));
        Page<PrtInformation> findPage = page;
        List<PrtInformation> list = findPage.getList();
        for (PrtInformation pi : list) {
            pi.setTypesimg(Encodes.unescapeHtml(pi.getTypesimg()));
            if (StringUtil.isNotBlank(pi.getCurrentAssignee())) {
                String[] assigneeArray = pi.getCurrentAssignee().split(",");
                for (String assignee : assigneeArray) {
                    if (loginName.equals(assignee)) {
                        pi.setCustomSort("1");
                        continue;
                    }
                }
            }
        }
        return page;
    }

    public List<PrtChannel> findChannelList(PrtChannel prtChannel) {
        if (StringUtil.isNotBlank(prtChannel.getParentIds())) {
            prtChannel.setParentIds("," + prtChannel.getParentIds() + ",");
        }
        return prtInformationDao.findChannelList(prtChannel);
    }
}
