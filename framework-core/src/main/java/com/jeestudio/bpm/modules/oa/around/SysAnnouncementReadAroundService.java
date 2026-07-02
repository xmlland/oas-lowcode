package com.jeestudio.bpm.modules.oa.around;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.oa.entity.OaSysAnnouncementReadEntity;
import com.jeestudio.bpm.modules.oa.service.OaSysAnnouncementReadServiceI;
import com.jeestudio.tools.base.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 公告阅读记录扩展服务
 */
@Component("oa_sys_announcement_readAroundService")
public class SysAnnouncementReadAroundService implements AroundServiceI {

	@Autowired
	OaSysAnnouncementReadServiceI oaSysAnnouncementReadService;

	@Override
	public void beforeSaveZform(Zform zform, String loginName, String businessKey) throws Exception {
		String userId = zform.getS02();
		String announcementId = zform.getS01();

		QueryWrapper<OaSysAnnouncementReadEntity> wrapper = new QueryWrapper<>();
		wrapper.eq("user_id",userId);
		wrapper.eq("announcement_id",announcementId);
		OaSysAnnouncementReadEntity read = oaSysAnnouncementReadService.getOne(wrapper);

		if(read!=null){
			throw new BusinessException("一个公告只能确认一次");
		}

		AroundServiceI.super.beforeSaveZform(zform, loginName, businessKey);
	}
}
