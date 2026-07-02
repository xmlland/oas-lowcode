package com.jeestudio.bpm.modules.oa.around;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import org.springframework.stereotype.Component;
/**
 * @Description: 系统消息扩展服务
 */

@Component("oa_sys_msgAroundService")
public class SysMsgAroundService implements AroundServiceI {

    @Override
    public void beforeSetSqlMap(Zform zform, GenTable genTable, User currentUser) {
        //接收人是当前用户
        zform.setUser02(currentUser);
        QueryWrapper<Zform> queryWrapper = zform.getQueryWrapper();
        queryWrapper.eq("recipient",currentUser.getId());
        queryWrapper.and(e->{
           e.ne("need_hide","1").or().isNull("need_hide");
        });
    }
}
