package com.jeestudio.bpm.modules.prt.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.modules.prt.entity.PrtCardEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


/**
 * @Description: 打印卡片Mapper
 */
@Mapper
public interface PrtCardMapper extends BaseMapper<PrtCardEntity> {

    /**
     * 根据用户角色查询用户的卡片
     * @param userId userId
     * @return
     */
    List<Map<String,Object>> listCardByUserRole(String userId);

    /**
     * 根据userId查询用户的卡片
     * @param userId userId
     * @return
     */
    List<Map<String,Object>> listCardByUserId(String userId);
}
