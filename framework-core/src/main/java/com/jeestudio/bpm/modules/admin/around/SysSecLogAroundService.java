package com.jeestudio.bpm.modules.admin.around;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.utils.Global;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @Description: 安全日志扩展服务
 */
@Component("sys_sec_logAroundService")
public class SysSecLogAroundService implements AroundServiceI {



    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        if (zformMap.containsKey("check_flag")) {
            // 查询完整性娇艳异常的数据
            String check_flag = zformMap.getString("check_flag");
            if (Global.YES.equals(check_flag)){
                Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> queryWrapper.apply("a.id in (select data_id from sys_sec_idata WHERE parent_id = 'sys_sec_log' )");
                zformMap.put("queryWrapperConsumer", consumer);
                zformMap.remove("check_flag");
            }

        }
    }



}
