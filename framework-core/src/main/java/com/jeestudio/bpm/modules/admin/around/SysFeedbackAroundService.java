package com.jeestudio.bpm.modules.admin.around;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.modules.admin.service.SysSecIConfigService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * @Description: 用户反馈扩展服务
 */
@Component("sys_feedbackAroundService")
@Slf4j
public class SysFeedbackAroundService implements AroundServiceI {
    @Autowired
    SysSecIConfigService sysSecIConfigService;

    @Autowired
    ZformDao zformDao;

    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        boolean isCommon = false;
        if (zformMap.containsKey("is_common")) {
            String is_common = zformMap.getString("is_common");
            if (Global.YES.equals(is_common)) {
                isCommon = true;
                log.info("sys_feedbackAroundService beforeFindPageMap 根据is_common查询数据，is_common：{}", is_common);
                Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> queryWrapper.eq("a.is_common", Global.YES);
                zformMap.put("queryWrapperConsumer", consumer);
            }
            zformMap.remove("is_common");
        }
        if (zformMap.containsKey("key_word") && StrUtil.isNotEmpty(zformMap.getString("key_word"))) {
            String key_word = zformMap.getString("key_word");
            log.info("sys_feedbackAroundService beforeFindPageMap 根据key_word查询数据，key_word：{}", key_word);
            Consumer<QueryWrapper<Zform>> queryWrapperConsumer = (Consumer<QueryWrapper<Zform>>) zformMap.get("queryWrapperConsumer");
            if (queryWrapperConsumer == null) {
                queryWrapperConsumer = queryWrapper -> {
                };
            }

            if (isCommon) {
                queryWrapperConsumer = queryWrapperConsumer.andThen(wrapper -> {
                    wrapper.and(w -> {
                        w.like("a.title_", key_word);
                        w.or();
                        w.like("a.reply_content", key_word);
                    });
                });
            } else {
                queryWrapperConsumer = queryWrapperConsumer.andThen(wrapper -> {
                    wrapper.and(w -> {
                        w.like("a.title_", key_word);
                        w.or();
                        w.like("a.desc_", key_word);
                        w.or();
                        w.like("a.reply_content", key_word);
                    });
                });
            }
            zformMap.put("queryWrapperConsumer", queryWrapperConsumer);
            zformMap.remove("key_word");
        }
        if (!isCommon) {
            log.info("sys_feedbackAroundService beforeFindPageMap 仅查询当前用户提交的数据，当前用户：{}", loginName);
            Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> {
                queryWrapper.eq("a.submit_user", UserUtil.getCurrentUserId());
            };
            zformMap.put("queryWrapperConsumer", consumer);
        }
    }

}
