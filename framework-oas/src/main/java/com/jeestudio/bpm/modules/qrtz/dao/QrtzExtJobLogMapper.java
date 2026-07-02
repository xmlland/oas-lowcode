package com.jeestudio.bpm.modules.qrtz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobLogEntity;
import org.apache.ibatis.annotations.Mapper;


/**
 * @Description: 定时任务日志Mapper
 */
@Mapper
public interface QrtzExtJobLogMapper extends BaseMapper<QrtzExtJobLogEntity> {

}
