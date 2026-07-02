package com.jeestudio.bpm.modules.prt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.modules.prt.dao.PrtCardMapper;
import com.jeestudio.bpm.modules.prt.entity.PrtCardEntity;
import com.jeestudio.bpm.modules.prt.service.PrtCardServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 打印卡片服务实现
 */
@Service
@Transactional
public class PrtCardServiceImpl extends ServiceImpl<PrtCardMapper, PrtCardEntity> implements PrtCardServiceI {


    @Autowired
    ZformService zformService;

    @Autowired
    GenTableService genTableService;

    @Autowired
    ZformDao zformDao;

    @Override
    public List<Map<String,Object>> listCardByUserId(String userId) {

        //该用户配置的卡片
        List<Map<String, Object>> userCardList = baseMapper.listCardByUserId(userId);
        //该用户角色可以查看的卡片
        List<Map<String, Object>> userRoleCardList = baseMapper.listCardByUserRole(userId);
        //该用户可以查看的卡片idList
        List<String> userRoleCardIdList = userRoleCardList.stream().map(k -> ConvertUtil.getString(k.get("id"))).collect(Collectors.toList());

        List<Map<String, Object>> allCardList = new ArrayList<>();
        List<String> userCardIdList = new ArrayList<>();
        for (Map<String, Object> userCard : userCardList) {
            String id = ConvertUtil.getString(userCard.get("id"));
            if (userRoleCardIdList.contains(id)){
                //如果用户配置的卡片在角色卡片中存在，则添加
                allCardList.add(userCard);
                userCardIdList.add(id);
            }
        }
        for (Map<String, Object> userRoleCard : userRoleCardList) {
            if (!userCardIdList.contains(ConvertUtil.getString(userRoleCard.get("id")))){
                //如果用户对应的角色卡片中在用户配置忠不存在，则添加
                allCardList.add(userRoleCard);
            }
        }
        return allCardList;
    }

    @Override
    public void saveUserCard(String userId, String loginName, List<Zform> zformList) {
        if (StringUtil.isNotEmpty(userId)){
            // 使用参数化查询，避免 SQL 注入
            String deleteSql = "delete from prt_card_user where user_id = #{param.userId}";
            Map<String, Object> param = new HashMap<>();
            param.put("userId", userId);
            zformDao.deleteSqlParm(deleteSql, param);
        }
        zformService.superBatchSave(zformList,genTableService.getGenTableWithDefination("prt_card_user"),loginName);
    }
}
