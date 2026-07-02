package com.jeestudio.bpm.modules.admin.around;

import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

/**
 * @Description: 系统管理扩展服务基类
 */
@Component("adminAroundService")
public class AdminAroundService implements AroundServiceI {
    @Override
    public void beforeGetMap(String id, GenTable genTable, String extFlag, String loginName) {
        System.out.println("adminAroundService-->beforeGetMap");
    }

    @Override
    public LinkedHashMap afterGetMap(LinkedHashMap map, String id, GenTable genTable, String extFlag, String loginName) {
        System.out.println("adminAroundService-->afterGetMap");
        return map;
    }
}
