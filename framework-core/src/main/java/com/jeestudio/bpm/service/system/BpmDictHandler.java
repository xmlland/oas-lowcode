package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.DictResult;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.tools.dict.DictHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: 字典处理器
 */
@Component
public class BpmDictHandler extends DictHandler {

	@Autowired
	DictDataService dictService;

	@Autowired
	ZformService zformService;

	@Override
	public LinkedHashMap<Object, Object> getDictionaryByDictCode(String s) {
		List<DictResult> dictList = dictService.getDictList(s, false);
		LinkedHashMap<Object,Object> dictMap = new LinkedHashMap<>();
		dictList.forEach(dict -> dictMap.put(dict.getMember(),dict.getMemberName()));
		return dictMap;
	}

	@Override
	public LinkedHashMap<Object, Object> getDictionaryByDictTable(String dictTable, String dictValue, String dictText, JSONArray orderCondition) {
		// 后续优化：根据 orderCondition 支持动态排序
		List<LinkedHashMap> mapList = zformService.findMapList(dictTable, new QueryWrapper<Zform>());
		LinkedHashMap<Object,Object> dictMap = new LinkedHashMap<>();
		mapList.forEach(map-> dictMap.put(map.get(dictValue),map.get(dictText)));
		return dictMap;
	}
}
