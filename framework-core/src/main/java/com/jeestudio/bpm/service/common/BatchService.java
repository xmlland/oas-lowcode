package com.jeestudio.bpm.service.common;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.mapper.base.common.BatchDao;
import com.jeestudio.bpm.service.gen.GenTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 批量操作服务
 */
@Service
public class BatchService {

	@Autowired
	BatchDao batchDao;

	@Autowired
	GenTableService genTableService;

	/*
	 * 字段从GenTable中取, 自定义列表名从自定义Map中取, 使用相同的忽视字段, 忽视joinSql
	 * */
	public void batchInsert(String sourceTable, String targetTable,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String extSql){
		batchInsert(sourceTable,targetTable,customSourceFieldMap,ignoreFieldList,wrapper,"",extSql);
	}

	/*
	 * 字段从GenTable中取, 自定义列表名从自定义Map中取, 使用相同的忽视字段
	 * */
	public void batchInsert(String sourceTable, String targetTable,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String joinSql,String extSql){
		List<String> customFieldList = new LinkedList<>();
		customSourceFieldMap.forEach((key,value)-> customFieldList.add(key));

		batchInsert(sourceTable, targetTable,customFieldList,customSourceFieldMap, ignoreFieldList,wrapper,joinSql,extSql);
	}


	/*
	 * 字段从GenTable中取, 使用相同的忽视字段, 且忽视join sql
	 * */
	public void batchInsert(String sourceTable, String targetTable,List<String> customFieldList,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String extSql){
		batchInsert(sourceTable, targetTable,customFieldList,customSourceFieldMap, ignoreFieldList,wrapper,"",extSql);
	}

	/*
	* 字段从GenTable中取, 且使用相同的忽视字段
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> customFieldList,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String joinSql,String extSql){
		batchInsert(sourceTable,targetTable,customFieldList,ignoreFieldList,ignoreFieldList,customSourceFieldMap,wrapper,joinSql,extSql);
	}


	/*
	* 字段从GenTable中取
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> customFieldList,List<String> ignoreTargetFieldList,List<String> ignoreSourceFieldList,LinkedHashMap<String,String> customSourceFieldMap,Wrapper wrapper,String joinSql,String extSql){
		GenTable targetGenTable = genTableService.getGenTableWithDefination(targetTable);
		GenTable sourceGenTable = genTableService.getGenTableWithDefination(sourceTable);

		List<GenTableColumn> targetColumnList = targetGenTable.getColumnList();
		List<String> targetFieldList = new LinkedList<>();
		targetColumnList.forEach(targetColumn-> targetFieldList.add(targetColumn.getName()));

		List<GenTableColumn> sourceColumnList = sourceGenTable.getColumnList();
		LinkedHashMap<String,String> sourceFieldMap = new LinkedHashMap<>();
		sourceColumnList.forEach(targetColumn-> sourceFieldMap.put(targetColumn.getName(),"a."+targetColumn.getName()));

		batchInsert(sourceTable,targetTable,targetFieldList,ignoreTargetFieldList,customFieldList,sourceFieldMap,ignoreSourceFieldList,customSourceFieldMap,wrapper,joinSql,extSql);
	}


	/*
	 * 使用相同的忽视字段， 且忽略joinSql
	 * */
	public void batchInsert(String sourceTable, String targetTable,List<String> targetFieldList,List<String> customFieldList,LinkedHashMap<String,String> sourceFieldMap,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String extSql){
		batchInsert(sourceTable,targetTable,targetFieldList,ignoreFieldList,customFieldList,sourceFieldMap,ignoreFieldList,customSourceFieldMap,wrapper,"",extSql);
	}

	/*
	* 使用相同的忽视字段
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> targetFieldList,List<String> customFieldList,LinkedHashMap<String,String> sourceFieldMap,LinkedHashMap<String,String> customSourceFieldMap,List<String> ignoreFieldList,Wrapper wrapper,String joinSql,String extSql){
		batchInsert(sourceTable,targetTable,targetFieldList,ignoreFieldList,customFieldList,sourceFieldMap,ignoreFieldList,customSourceFieldMap,wrapper,joinSql,extSql);
	}

	/*
	* 允许忽视字段, 并增加自定义字段
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> targetFieldList,List<String> ignoreTargetFieldList,List<String> customFieldList,LinkedHashMap<String,String> sourceFieldMap,List<String> ignoreSourceFieldList,LinkedHashMap<String,String> customSourceFieldMap,Wrapper wrapper,String joinSql,String extSql){
		List<String> actualTargetFieldList = new LinkedList<>(targetFieldList);	// 目标字段列表
		actualTargetFieldList.removeAll(ignoreTargetFieldList);									// 忽视字段
		actualTargetFieldList.addAll(customFieldList);													// 增加自定义字段

		LinkedHashMap<String,String> actualSourceFieldMap = new LinkedHashMap<>(sourceFieldMap);	// 源字段列表
		ignoreSourceFieldList.forEach(actualSourceFieldMap::remove);															// 忽视字段
		actualSourceFieldMap.putAll(customSourceFieldMap);																				// 增加自定义字段

		batchInsert(sourceTable,targetTable,actualTargetFieldList,actualSourceFieldMap,wrapper,joinSql,extSql);
	}

	/*
	* 原始Sql 但是忽视joinSql
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> targetFieldList,LinkedHashMap<String,String> sourceFieldMap,Wrapper wrapper,String extSql){
		batchInsert(sourceTable,targetTable,targetFieldList,sourceFieldMap,wrapper,"",extSql);
	}

	/*
	*	原始Sql
	*
	* 字段Map: {
	* 	fieldName : sourceField
	* }
	* 	->
	* sourceField AS fieldName
	*
	* */
	public void batchInsert(String sourceTable, String targetTable,List<String> targetFieldList,LinkedHashMap<String,String> sourceFieldMap,Wrapper wrapper,String joinSql,String extSql){
		batchDao.batchInsert(sourceTable,targetTable,targetFieldList,sourceFieldMap,wrapper,joinSql,extSql);
	}
}
