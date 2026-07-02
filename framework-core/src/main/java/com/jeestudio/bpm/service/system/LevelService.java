package com.jeestudio.bpm.service.system;

import com.jeestudio.bpm.common.entity.system.Level;
import com.jeestudio.bpm.mapper.base.system.LevelDao;
import com.jeestudio.bpm.service.common.CrudService;
import org.springframework.stereotype.Service;

/**
 * @Description: 职务级别服务
 */
@Service
public class LevelService extends CrudService<LevelDao, Level> {
}
