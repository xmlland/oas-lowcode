package com.jeestudio.bpm.modules.qrtz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobEntity;
import org.apache.ibatis.annotations.Mapper;


/**
 * @Description: 定时任务Mapper
 */
@Mapper
public interface QrtzExtJobMapper extends BaseMapper<QrtzExtJobEntity> {

}
