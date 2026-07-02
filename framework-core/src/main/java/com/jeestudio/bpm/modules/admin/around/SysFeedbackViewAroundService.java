package com.jeestudio.bpm.modules.admin.around;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @Description: 用户反馈查看扩展服务
 */
@Component("sys_feedback@viewAroundService")
@Slf4j
public class SysFeedbackViewAroundService implements AroundServiceI {
    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    ZformDao zformDao;

    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        if (zformMap.containsKey("is_common")) {
            String is_common = zformMap.getString("is_common");

            if (StringUtil.isEmpty(is_common)) {
                is_common = Global.NO;
            }
            log.info("sys_feedback@viewAroundService beforeFindPageMap 根据is_common查询数据，is_common：{}", is_common);

            String finalIs_common = is_common;
            Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> {
                if (Global.YES.equals(finalIs_common)) {
                    queryWrapper.eq("a.is_common", Global.YES);
                } else {
                    queryWrapper.isNotNull("a.submit_user");
                    queryWrapper.ne("a.submit_user", Global.YES);
                }
            };
            zformMap.put("queryWrapperConsumer", consumer);
            zformMap.remove("is_common");
        }
    }
}

