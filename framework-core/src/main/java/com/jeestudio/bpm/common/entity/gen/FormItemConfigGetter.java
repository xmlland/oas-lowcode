package com.jeestudio.bpm.common.entity.gen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.JsonFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description: 表单项配置获取器
 */
public interface FormItemConfigGetter {

    Logger logger = LoggerFactory.getLogger(FormItemConfigGetter.class);

    GenTableColumnFormItemConfig getGenTableColumnFormItemConfig();

    default GenTableColumnFormItemConfig formatGenTableColumnFormItemConfig(String formItemConfig) {
        try {
            String nullValue = "null";
            GenTableColumnFormItemConfig config = new GenTableColumnFormItemConfig();
            if (formItemConfig != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(formItemConfig);

                JsonNode theNode = rootNode.path("colProps").path("span");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSpan(theNode.asText());
                }
                theNode = rootNode.path("formItemProps").path("extra");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setExtra(theNode.asText());
                }

                theNode = rootNode.path("formControlProps").path("placeholder");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setPlaceholder(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("min");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMin(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("max");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMax(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("tableFilterData");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTableFilterData(JsonFormatUtil.arrayToStr(theNode.toString()) );
                }
                theNode = rootNode.path("formControlProps").path("filterData");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFilterData(JsonFormatUtil.arrayToStr(theNode.toString()) );
                }
                theNode = rootNode.path("formControlProps").path("formatFuncStr");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFormatFuncStr(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("minValue");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMinValue(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("maxValue");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMaxValue(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("dataScope");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setDataScope(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetOrgId");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText()) ){
                    theNode = theNode.path("id");
                    if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText()) ){
                        config.setTargetOrgId(theNode.asText());
                    }
                }

                //是否多选
                theNode = rootNode.path("formControlProps").path("multiple");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMultiple(theNode.asText());
                }

                //用户选择 start

                theNode = rootNode.path("formControlProps").path("createSysUser");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setCreateSysUser(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("loginNameField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setLoginNameField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("userNameField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setUserNameField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("parentOrgField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setParentOrgField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("userRoles");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setUserRoles(theNode.asText());
                }
                //用户选择 end

                //机构选择 start
                theNode = rootNode.path("formControlProps").path("createSysOffice");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setCreateSysOffice(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("parentOrgId");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText()) ){
                    theNode = theNode.path("id");
                    if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText()) ){
                        config.setParentOrgId(theNode.asText());
                    }
                }
                theNode = rootNode.path("formControlProps").path("orgNameField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setOrgNameField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("areaIdField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setAreaIdField(theNode.asText());
                }
                //机构选择 end


                theNode = rootNode.path("formControlProps").path("modalWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModalWidth(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("hideLoginName");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setHideLoginName(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("freeChoice");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFreeChoice(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("showRank");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setShowRank(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("rootAreaId");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setRootAreaId(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("accepts");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setAccepts(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("fileCount");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFileCount(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("maxSize");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setMaxSize(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetFormNo");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTargetFormNo(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("targetField");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setTargetField(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("columns");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    JSONArray jsonArray = JSONArray.parseArray(theNode.toString());
                    List<String> columnsJSONList = Lists.newArrayList();
                    JSONArray searchKey = new JSONArray();
                    JSONArray searchLabel = new JSONArray();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (Global.YES.equals(jsonObject.getString("isShow"))) {
                            columnsJSONList.add(JsonFormatUtil.objectToStr(jsonObject.toString(), new String[]{"title", "dataIndex", "minWidth", "align"}, new String[]{"id", "__temp_id__", "queryDataIndex", "isCode", "isName", "isShow", "isQuery"}));
                        }
                        if (Global.YES.equals(jsonObject.getString("isQuery"))) {
                            if (jsonObject.containsKey("queryDataIndex")){
                                searchKey.add(jsonObject.getString("queryDataIndex"));
                            }else{
                                searchKey.add(jsonObject.getString("dataIndex"));
                            }
                            searchLabel.add(jsonObject.getString("title"));
                        }
                    }
                    config.setColumns(theNode.toString());
                    config.setColumnsList(columnsJSONList);
                    config.setSearchKey(JsonFormatUtil.toJSONStr(searchKey));
                    config.setSearchLabel(JsonFormatUtil.toJSONStr(searchLabel));
                }
                theNode = rootNode.path("formControlProps").path("searchLabelWidth");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSearchLabelWidth(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("searchConfig");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setSearchConfig(JsonFormatUtil.objectToStr(theNode.toString()) );
                }
                theNode = rootNode.path("formControlProps").path("modalTitle");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setModalTitle(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("formUpdateMap");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setFormUpdateMap(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("nameDataIndex");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setNameDataIndex(theNode.asText());
                }
                theNode = rootNode.path("formControlProps").path("prefix");
                if (false == theNode.isMissingNode() && false == nullValue.equals(theNode.asText())) {
                    config.setPrefix(theNode.asText());
                }
            }
            return config;
        } catch (Exception e) {
            logger.warn("解析表单列配置失败, formItemConfig: {}", formItemConfig, e);
            return new GenTableColumnFormItemConfig();
        }
    }
}
