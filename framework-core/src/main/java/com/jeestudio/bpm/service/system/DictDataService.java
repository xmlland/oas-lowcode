package com.jeestudio.bpm.service.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.jeestudio.bpm.cache.cacheUtils.CacheUtil;
import com.jeestudio.bpm.common.entity.system.Dict;
import com.jeestudio.bpm.common.entity.system.DictGenView;
import com.jeestudio.bpm.common.entity.system.DictResult;
import com.jeestudio.bpm.mapper.base.system.DictDao;
import com.jeestudio.bpm.service.async.AsyncService;
import com.jeestudio.bpm.utils.DictUtil;
import com.jeestudio.bpm.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 字典数据服务
 */
@Service
public class DictDataService {

    @Autowired
    private DictDao dictDao;

    private static CacheUtil cacheUtil = new CacheUtil();

    @Autowired
    private AsyncService asyncService;

    @Transactional(readOnly = true)
    public List<DictResult> dictTypes(String type) {
        Object dictObject = "";
        List<DictResult> list = new ArrayList<>();
        if ("".equals(type) == false && type != null) {
            String[] types = type.split(",");
            for (int i = 0; i < types.length; i++) {
                dictObject = cacheUtil.getHashCache(Global.DICT_CACHE, "_" + types[i]);
                if (dictObject == null) {
                    list.addAll(dictDao.dictTypes(types[i]));
                    asyncService.asyncSaveHashCache(Global.DICT_CACHE, "_" + types[i], JsonConvertUtil.objectToJson(dictDao.dictTypes(types[i])));
                } else {
                    list.addAll(JsonConvertUtil.gsonBuilder().fromJson(dictObject.toString(), new TypeToken<List<DictResult>>() {
                    }.getType()));
                }
            }
        } else {
            dictObject = cacheUtil.getHashCache(Global.DICT_CACHE, "_types");
            if (dictObject == null) {
                list.addAll(dictDao.dictTypes(type));
                asyncService.asyncSaveHashCache(Global.DICT_CACHE, "_types", JsonConvertUtil.objectToJson(dictDao.dictTypes(type)));
            } else {
                list.addAll(JsonConvertUtil.gsonBuilder().fromJson(dictObject.toString(), new TypeToken<List<DictResult>>() {
                }.getType()));
            }
        }
        return list;
    }

    @Transactional(readOnly = false)
    public void save(Dict dict) {
        if (StringUtil.isBlank(dict.getId())) {
            dict.preInsert();
            dictDao.insert(dict);
        } else {
            dict.preUpdate();
            dictDao.update(dict);
        }
        asyncService.deleteListHashStartWithHashKey(Global.DICT_CACHE, "_");
    }

    public List<DictGenView> getDictGenView(String dictType) {
        return dictDao.getDictGenView(dictType);
    }

    public List<DictGenView> findDictListLike(String key) {
        return dictDao.findDictListLike(key);
    }

    /**
     * Get hash key by type and edit flag
     *
     * @param type
     * @param isEdit
     * @return hash key
     */
    private String getHashKey(String type, Boolean isEdit) {
        if (StringUtil.isEmpty(type)) type = "";
        String hashKey = "_dict_" + type + "_" + String.valueOf(isEdit) + "_";
        return hashKey;
    }

    /**
     * Get dict labels by values and type
     *
     * @param values
     * @param type
     * @param defaultValue
     * @return dict labels
     */
    public String getDictLabels(String values, String type, String defaultValue, String lang) {
        String labels = "";
        if (StringUtil.isNotEmpty(type) && StringUtil.isNotEmpty(values)) {
            String[] vals = values.split(",");
            int valueCount = vals.length;
            List<String> valList = Arrays.asList(vals);
            for (DictResult dict : this.getDictList(type, false)) {
                if (valList.contains(dict.getMember())) {
                    if (Global.LANG_EN.equals(lang)) {
                        labels += "," + dict.getMemberNameEn();
                    } else {
                        labels += "," + dict.getMemberName();
                    }
                    valueCount--;
                    if (valueCount == 0) break;
                }
            }
            if (StringUtil.isNotEmpty(labels)) labels = labels.substring(1);
        }
        return StringUtil.isEmpty(labels) ? defaultValue : labels;
    }

    /**
     * Get dict value(s) by type and labels
     *
     * @param labels
     * @param type         the parentCode of the dict list
     * @param defaultValue
     * @return dict value(s)
     */
    public String getDictValues(String labels, String type, String defaultValue, String lang) {
        String values = "";
        if (StringUtil.isNotBlank(type) && StringUtil.isNotBlank(labels)) {
            String[] labs = labels.split(",");
            int labelCount = labs.length;
            List<String> labList = Arrays.asList(labs);
            for (DictResult dict : this.getDictList(type, false)) {
                String name = dict.getMemberName();
                if (Global.LANG_EN.equals(lang)) name = dict.getMemberNameEn();
                if (labList.contains(name)) {
                    values += "," + dict.getMember();
                    labelCount--;
                    if (labelCount == 0) break;
                }
            }
            if (StringUtil.isNotEmpty(values)) values = values.substring(1);
            return values;
        }
        return defaultValue;
    }

    /**
     * Get dict list by types
     *
     * @param types
     * @param isEdit 历史字典是否显示 2023-03-08 17:19:04 zry
     * @return dict list
     */
    public List<DictResult> getDictList(String types, Boolean isEdit) {
        List<DictResult> list = Lists.newArrayList();
        if (StringUtil.isEmpty(types)) {
            types = DictUtil.typeOfDataParam;
        }
        String[] typeString = types.split(",");
        for (int i = 0; i < typeString.length; i++) {
            if (StringUtil.isEmpty(typeString[i])) continue;
            String hashKey = this.getHashKey(typeString[i], isEdit);
            Object dictObject = cacheUtil.getHashCache(Global.DICT_CACHE, hashKey);
            if (dictObject == null) {
                List<DictResult> theList = dictDao.findDictTree(typeString[i], isEdit ? Global.YES : "");
                for (DictResult dictResult : theList) {
                    dictResult.setType(typeString[i]);
                }
                List<DictResult> dataParamsList = dictDao.findDictTreeDataParams(isEdit ? Global.YES : "");
                list.addAll(theList);
                List<DictResult> traceList = Lists.newArrayList();
                for (DictResult dict : theList) {
                    this.buildDictTreeDataParams(dataParamsList, traceList, dict);
                }
                for (DictResult dictResult : traceList) {
                    dictResult.setType(typeString[i]);
                }
                list.addAll(traceList);
                List<DictResult> cacheList = Lists.newArrayList();
                cacheList.addAll(theList);
                cacheList.addAll(traceList);
                asyncService.asyncSaveHashCache(Global.DICT_CACHE, hashKey, JsonConvertUtil.objectToJson(cacheList));
            } else {
//                list.addAll(JsonConvertUtil.gsonBuilder().fromJson((String) dictObject, new TypeToken<List<DictResult>>() {
//                }.getType()));
                list.addAll(JSONArray.parseArray((String) dictObject, DictResult.class));
            }
        }
        return list;
    }

    /**
     * Get dict list for App
     */
    public JSONObject getDictListForApp(String types, Boolean isEdit) {
        JSONObject jsonObject = new JSONObject();

        if (StringUtil.isEmpty(types)) types = DictUtil.typeOfDataParam;
        Map<String, String> typeMap = Maps.newHashMap();
        String[] typeString = types.split(",");
        List<String> typeList = Lists.newArrayList();
        for (int i = 0; i < typeString.length; i++) {
            if (StringUtil.isEmpty(typeString[i])) continue;
            if (false == typeList.contains(typeString[i])) {
                typeList.add(typeString[i]);
            }
        }
        for (String theTypeString : typeList) {
            List<DictResult> list = Lists.newArrayList();
            String hashKey = this.getHashKey(theTypeString, isEdit);
            Object dictObject = cacheUtil.getHashCache(Global.DICT_CACHE, hashKey);
            if (dictObject == null) {
                List<DictResult> theList = dictDao.findDictTree(theTypeString, isEdit ? Global.YES : "");
                for (DictResult dictResult : theList) {
                    dictResult.setType(theTypeString);
                }
                List<DictResult> dataParamsList = dictDao.findDictTreeDataParams(isEdit ? Global.YES : "");
                list.addAll(theList);
                List<DictResult> traceList = Lists.newArrayList();
                for (DictResult dict : theList) {
                    this.buildDictTreeDataParams(dataParamsList, traceList, dict);
                }
                for (DictResult dictResult : traceList) {
                    dictResult.setType(theTypeString);
                }
                list.addAll(traceList);
                List<DictResult> cacheList = Lists.newArrayList();
                cacheList.addAll(theList);
                cacheList.addAll(traceList);
                asyncService.asyncSaveHashCache(Global.DICT_CACHE, hashKey, JsonConvertUtil.objectToJson(cacheList));
            } else {
                list.addAll(JsonConvertUtil.gsonBuilder().fromJson((String) dictObject, new TypeToken<List<DictResult>>() {
                }.getType()));
            }
            jsonObject.put(theTypeString, list);
        }
        return jsonObject;
    }

    public void deleteCascade(String code) {
        dictDao.deleteCascade(code);
    }

    private void buildDictTree(List<DictResult> traceList, DictResult dict, String editFlag) {
        List<DictResult> childrenList = Lists.newArrayList();
        if (dict.isHasChildren()) {
            childrenList = dictDao.findDictTree(dict.getMember(), editFlag);
            traceList.addAll(childrenList);
            for (DictResult theDict : childrenList) {
                this.buildDictTree(traceList, theDict, editFlag);
            }
        }
    }

    private void buildDictTreeDataParams(List<DictResult> dataParmasList, List<DictResult> traceList, DictResult dict) {
        List<DictResult> childrenList = Lists.newArrayList();
        //childrenList = dataParmasList.stream().filter(k -> k.getParentType().equals(dict.getMember())).collect(Collectors.toList());
        for(DictResult theDict : dataParmasList) {
            if (theDict.getParentType() != null && theDict.getParentType().equals(dict.getMember())) {
                childrenList.add(theDict);
            }
        }
        traceList.addAll(childrenList);
    }

    @Async
    public void refreshDictCache() {
        cacheUtil.deleteLeftLikeHashCache(Global.DICT_CACHE, "_");
        this.dictTypes(DictUtil.allRootTypes);
    }

    @Async
    public void refreshDictCacheByType(String type) {
        cacheUtil.deleteLeftLikeHashCache(Global.DICT_CACHE, "_" + type);
        this.dictTypes(type);
    }

    /**
     * 根据字典编码，查找数据字典
     */
    public DictResult getDataDictListByType(String type) {
        return dictDao.getDataDictListByType(type);
    }

    /**
     * 根据字典ID，统计其包含的字典项数量
     */
    public Integer getDataDictChildrenCount(String parentId, String code) {
        return dictDao.getDataDictChildrenCount(parentId, code);
    }
}
