package com.jeestudio.bpm.modules.oa.around;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.Role;
import com.jeestudio.bpm.common.entity.system.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: 系统公告扩展服务
 */
@Component("oa_sys_announcementAroundService")
public class SysAnnouncementAroundService  implements AroundServiceI {
	@Override
	public void beforeSetSqlMap(Zform zform, GenTable genTable, User currentUser) {
		QueryWrapper<Zform> wrapper = zform.getQueryWrapper();
		String queryUnread = getHttpServletRequest().getParameter("queryUnread");
		if("queryUnread".equals(queryUnread)){
			List<Role> roleList = currentUser.getRoleList();
			String id = currentUser.getId();

			if (roleList != null && roleList.size() > 0) {
				wrapper.and(w ->
						w.in("receiving_roles", roleList.stream().map(Role::getId).toArray())
								.or()
								.like("receiving_users", id)
				);
			} else {
				wrapper.like("receiving_users", id);
			}
		}
		AroundServiceI.super.beforeSetSqlMap(zform, genTable, currentUser);
	}
}
