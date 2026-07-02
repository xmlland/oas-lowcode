package com.jeestudio.bpm.modules.prt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.modules.prt.entity.PrtCardEntity;

import java.util.List;
import java.util.Map;


/**
 * @Description: 打印卡片服务接口
 */
public interface PrtCardServiceI extends IService<PrtCardEntity> {

    /**
     * 根据userId查询用户的卡片
     * @param userId userId
     * @return
     */
    List<Map<String,Object>> listCardByUserId(String userId);

    /**
     * 保存用户卡片配置
     * @param userId userId
     * @param loginName loginName
     * @param zformList 卡片配置
     */
    void saveUserCard(String userId, String loginName, List<Zform> zformList);
}
