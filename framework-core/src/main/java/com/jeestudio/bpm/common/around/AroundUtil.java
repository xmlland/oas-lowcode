package com.jeestudio.bpm.common.around;

import cn.hutool.extra.spring.SpringUtil;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.service.gen.GenTableService;

import java.util.Arrays;

/**
 * @Description: 业务扩展服务工具
 */
public class AroundUtil {
    public static AroundServiceI getAroundServiceI(String formNo) {
        GenTableService genTableService = SpringUtil.getBean(GenTableService.class);
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        return getAroundServiceI(genTable);
    }

    public static AroundDaoI getAroundDaoI(String formNo) {
        GenTableService genTableService = SpringUtil.getBean(GenTableService.class);
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        return getAroundDaoI(genTable);
    }

    public static AroundDaoI getAroundDaoI(GenTable genTable) {
        if (genTable == null) {
            return null;
        }
        String[] namesForType = SpringUtil.getBeanNamesForType(AroundDaoI.class);
        if (namesForType.length == 0) {
            return null;
        }
        String name = genTable.getName();//formNo
        if (Arrays.asList(namesForType).contains(name + "AroundDao")) {
            return SpringUtil.getBean(name + "AroundDao", AroundDaoI.class);
        }
        String module = genTable.getModule();//module
        if (Arrays.asList(namesForType).contains(module + "AroundDao")) {
            return SpringUtil.getBean(module + "AroundDao", AroundDaoI.class);
        }
        return null;
    }

    public static AroundServiceI getAroundServiceI(GenTable genTable) {
        if (genTable == null) {
            return null;
        }
        String[] namesForType = SpringUtil.getBeanNamesForType(AroundServiceI.class);
        if (namesForType.length == 0) {
            return null;
        }
        String name = genTable.getName();//formNo
        if (Arrays.asList(namesForType).contains(name + "AroundService")) {
            return SpringUtil.getBean(name + "AroundService", AroundServiceI.class);
        }
        String module = genTable.getModule();//module
        if (Arrays.asList(namesForType).contains(module + "AroundService")) {
            return SpringUtil.getBean(module + "AroundService", AroundServiceI.class);
        }
        return null;
    }


    public static AroundControllerI getAroundControllerI(GenTable genTable) {
        if (genTable == null) {
            return null;
        }
        String[] namesForType = SpringUtil.getBeanNamesForType(AroundControllerI.class);
        if (namesForType.length == 0) {
            return null;
        }
        String name = genTable.getName();//formNo
        if (Arrays.asList(namesForType).contains(name + "AroundController")) {
            return SpringUtil.getBean(name + "AroundController", AroundControllerI.class);
        }
        String module = genTable.getModule();//module
        if (Arrays.asList(namesForType).contains(module + "AroundController")) {
            return SpringUtil.getBean(module + "AroundController", AroundControllerI.class);
        }
        return null;
    }
}
