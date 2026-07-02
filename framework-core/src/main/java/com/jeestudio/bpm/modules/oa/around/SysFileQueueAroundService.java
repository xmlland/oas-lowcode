package com.jeestudio.bpm.modules.oa.around;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.utils.UserUtil;
import org.springframework.stereotype.Component;

/**
 * @Description: 文件队列扩展服务
 */
@Component("sys_file_queueAroundService")
public class SysFileQueueAroundService implements AroundServiceI {
	@Override
	public void beforeSetSqlMap(Zform zform, GenTable genTable, User currentUser) {
		QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
		queryWrapper.eq("a.create_by", UserUtil.getCurrentUser().getId());
	}
}
