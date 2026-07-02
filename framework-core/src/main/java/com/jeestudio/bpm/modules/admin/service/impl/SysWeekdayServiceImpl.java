package com.jeestudio.bpm.modules.admin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeestudio.bpm.modules.admin.dao.SysWeekdayMapper;
import com.jeestudio.bpm.modules.admin.entity.SysWeekdayEntity;
import com.jeestudio.bpm.modules.admin.service.SysWeekdayServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.DbTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * @Description: 工作日历服务实现
 */
@Service
@Transactional
public class SysWeekdayServiceImpl extends ServiceImpl<SysWeekdayMapper, SysWeekdayEntity> implements SysWeekdayServiceI {

    private String dbType = DbTypeUtil.getDbType();
    @Autowired
    ZformService zformService;

    @Override
    public String calcDate(String date, int days) {
        Date dateObj = null;
        if ("mysql".equals(dbType) || "kingbase".equals(dbType)) {
            dateObj = baseMapper.calcDateMysql(date, days);
        } else if ("oracle".equals(dbType)) {
            // 已知限制：Oracle 工作日计算需结合目标库版本继续验证
            dateObj = baseMapper.calcDateOracle(date, days);
        } else if ("mssql".equals(dbType)) {
            dateObj = baseMapper.calcDateMssql(date, days);
        } else {
            // 已知限制：暂不支持当前数据库类型的工作日计算
        }
        if (dateObj != null) {
            return DateUtil.formatDate(dateObj);
        } else {
            return null;
        }

    }
}
