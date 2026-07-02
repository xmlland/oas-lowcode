package com.jeestudio.bpm.modules.gen.around;

import cn.hutool.core.util.StrUtil;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 表单配置扩展服务
 */
@Component("gen_tableAroundService")
public class GenTableAroundService implements AroundServiceI {
    @Autowired
    GenTableService genTableService;

    @Override
    public void beforeSaveZform(Zform zform, String loginName, String businessKey) {
        GenTable genTable = genTableService.get(zform.getId());
        String exportRuleName = ConvertUtil.getString(genTable.getExportRuleName());
        String[] split = exportRuleName.split(",");
        List<String> strings = new ArrayList<>();
        for (String s : split) {
            if (StrUtil.isNotEmpty(s)){
                strings.add(s);
            }
        }

        String l06 = ConvertUtil.getString(zform.getL06()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");
        String sqlColumnsFriendly = ConvertUtil.getString(genTable.getSqlColumnsFriendly()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");
        if (!l06.equals(sqlColumnsFriendly)){
            if (!strings.contains("lockfsql")) {
                strings.add("lockfsql");
            }
        }
        String l01 = ConvertUtil.getString(zform.getL01()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");
        String sqlColumns = ConvertUtil.getString(genTable.getSqlColumns()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");

        if (!l01.equals(sqlColumns)){
            if (!strings.contains("locksql")) {
                strings.add("locksql");
            }
        }
        String l02 = ConvertUtil.getString(zform.getL02()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");
        String sqlJoins = ConvertUtil.getString(genTable.getSqlJoins()).toLowerCase().trim().replaceAll(System.lineSeparator(),"").replaceAll("\r\n","").replaceAll("\r","").replaceAll("\n","");

        if (!l02.equals(sqlJoins)){
            if (!strings.contains("lockjoins")) {
                strings.add("lockjoins");
            }
        }
        if (strings.size()>0){
            genTable.setExportRuleName(String.join(",",strings));
            genTableService.save(genTable);
        }
    }
}
