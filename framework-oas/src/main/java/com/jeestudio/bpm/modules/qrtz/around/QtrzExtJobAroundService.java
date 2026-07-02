package com.jeestudio.bpm.modules.qrtz.around;

import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.modules.qrtz.entity.QrtzExtJobEntity;
import com.jeestudio.bpm.modules.qrtz.service.QrtzExtJobServiceI;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.DateUtil;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @Description: 定时任务扩展服务，维护任务调度状态和时间展示
 */
@Component("qrtz_ext_jobAroundService")
public class QtrzExtJobAroundService implements AroundServiceI {

    @Autowired
    ZformService zformService;

    @Autowired
    QrtzExtJobServiceI qrtzExtJobService;

    @Override
    public Page<Zform> afterFindPageMap(Page<Zform> pageMap, Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        for (LinkedHashMap map : pageMap.getMap()) {
            String prevFireTime = ConvertUtil.getString(map.get("prev_fire_time"));
            String nextFireTime = ConvertUtil.getString(map.get("next_fire_time"));
            if (StringUtil.isEmpty(nextFireTime)) {
                continue;
            }
            Float prev_fire_time = Float.parseFloat(prevFireTime);
            Float next_fire_time = Float.parseFloat(nextFireTime);
            if (prev_fire_time != null && prev_fire_time > 0) {
                map.put("prev_fire_time", DateUtil.dateToString(new Date(prev_fire_time.longValue() * 1000), "yyyy-MM-dd HH:mm:ss"));
            } else {
                map.put("prev_fire_time", "");
            }
            if (next_fire_time != null && next_fire_time > 0) {
                map.put("next_fire_time", DateUtil.dateToString(new Date(next_fire_time.longValue() * 1000), "yyyy-MM-dd HH:mm:ss"));
            } else {
                map.put("next_fire_time", "");
            }
        }
        return pageMap;
    }

    @Override
    public ResultJson afterSaveZform(ResultJson resultJson, Zform zform, String loginName, String businessKey) {
        QrtzExtJobEntity entity = zformService.getEntityByZform(zform, QrtzExtJobEntity.class);
        qrtzExtJobService.deleteScheduler(entity);
        if (Global.YES.equals(entity.getJobStatus())) {
            qrtzExtJobService.addScheduler(entity);
        }
        return resultJson;
    }

    @Override
    public void beforeDeleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {
        String[] split = ids.split(",");
        for (String s : split) {
            QrtzExtJobEntity entity = qrtzExtJobService.getById(s);
            qrtzExtJobService.deleteScheduler(entity);
        }
    }


}
