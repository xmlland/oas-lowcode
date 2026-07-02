package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.system.Dict;
import com.jeestudio.bpm.common.entity.system.DictResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Int;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @Description: 应用代码生成工具
 */
public class GenAppUtil {
    protected static final Logger logger = LoggerFactory.getLogger(GenAppUtil.class);

    public static final int keyWidget        = 100000; //分组
    public static final int keyInput         = 100001; //输入框
    public static final int keyRadio         = 100002;
    public static final int keyCheck         = 100003;
    public static final int keySelect        = 100004;
    public static final int keyCascader      = 100005;
    public static final int keyCalendar      = 100006;
    public static final int keyTime          = 100007;
    public static final int keySwitch        = 100008;
    public static final int keyFileuploadpic = 100009;
    public static final int keyFileupload    = 100010;

    private static HashMap<String, Integer> keyMap = Maps.newHashMap();

    public static String buildFormDefinition(GenTable genTable, List<DictResult> groupDictList) {
        boolean buildAllColumnsForApp = false;
        return buildFormDefinition(genTable, groupDictList, buildAllColumnsForApp);
    }

    public static String buildFormDefinition(GenTable genTable, List<DictResult> groupDictList, boolean buildAllColumnsForApp) {
        //读取分组
        //按分组生成字段配置
        List<Dict> fieldGroupList = findGroupList(genTable, groupDictList);
        JSONObject main = new JSONObject();
        JSONArray widgetList = new JSONArray();
        if (fieldGroupList.size() > 0) {
            //有字段分组，一般基本信息，审批信息，其他信息等
            JSONArray itemNames = new JSONArray();
            JSONArray formField = new JSONArray();
            JSONObject widgetListObject = new JSONObject();
            widgetListObject.put("key", keyWidget);
            widgetListObject.put("type", "collapse");
            widgetListObject.put("category", "container");
            widgetListObject.put("icon", "card");
            JSONArray items = new JSONArray();
            int i = 1;
            for(Dict dictItem : fieldGroupList) {
                JSONObject item = new JSONObject();
                item.put("type", "collapse-item");
                item.put("category", "container");
                item.put("icon", "card");
                item.put("internal", true);

                JSONArray widgetListInItem = new JSONArray();
                genTable.getColumnList().stream().filter(k -> k.getRemarks() != null && k.getRemarks().contains(dictItem.getCode())).forEach(k -> {
                    if (k.getShowType().equals("input")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getInput(keyInput, genTable.getName(), k, 1));
                    } else if (k.getShowType().equals("textarea")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getInput(keyInput, genTable.getName(), k, 2));
                    } else if (k.getShowType().equals("radiobox")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getRadio(keyRadio, genTable.getName(), k));
                    } else if (k.getShowType().equals("checkbox")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getCheck(keyCheck, genTable.getName(), k));
                    } else if (k.getShowType().equals("select") && "yes_no".equals(k.getDictType())) {
                        formField.add(k.getName());
                        widgetListInItem.add(getSwitch(keySwitch, genTable.getName(), k));
                    } else if (k.getShowType().equals("select") && Global.YES.equals(k.getSelectSimple())) {
                        formField.add(k.getName());
                        widgetListInItem.add(getSelect(keySelect, genTable.getName(), k));
                    } else if (k.getShowType().equals("treeselectRedio")
                            || k.getShowType().equals("areaselect")
                            || k.getShowType().equals("officeselectTree")
                            || k.getShowType().equals("treeselectRedio")
                            || k.getShowType().equals("treeselectCheck")
                            || k.getShowType().equals("treeselect")
                            || k.getShowType().equals("parentId")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getCascader(keyCascader, genTable.getName(), k));
                    } else if (k.getShowType().equals("dateselect") && false == k.getDateType().contains("HH")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getCalendar(keyCalendar, genTable.getName(), k));
                    } else if (k.getShowType().equals("dateselect") && k.getDateType().contains("HH")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getTime(keyTime, genTable.getName(), k));
                    } else if (k.getShowType().equals("fileuploadpic")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getFileuploadpic(keyFileuploadpic, genTable.getName(), k));
                    } else if (k.getShowType().equals("fileupload")) {
                        formField.add(k.getName());
                        widgetListInItem.add(getFileupload(keyFileupload, genTable.getName(), k));
                    }
                });
                item.put("widgetList", widgetListInItem);

                JSONObject itemOptions = new JSONObject();
                itemNames.add("collapse-item" + keyWidget * 100 + i * 10);
                itemOptions.put("name","collapse-item" + keyWidget * 100 + i * 10);
                itemOptions.put("label",dictItem.getName());
                itemOptions.put("hidden",false);
                itemOptions.put("labelText","");
                itemOptions.put("rightBtnShow",false);
                itemOptions.put("rightBtnLabel","默认");
                itemOptions.put("rightTextIconShow",false);
                itemOptions.put("rightTextIconName","arrow");
                itemOptions.put("leftIconShow",false);
                itemOptions.put("leftIconName","location");
                itemOptions.put("rightIconShow",false);
                itemOptions.put("rightIconName","arrow");
                itemOptions.put("type","default");
                itemOptions.put("plain",false);
                itemOptions.put("round",true);
                itemOptions.put("hairline",false);
                itemOptions.put("isChildGroup",true);
                itemOptions.put("titleColor","");
                itemOptions.put("titleBgColor","");
                itemOptions.put("titleBgEpColor","");
                itemOptions.put("customClass",new JSONArray());
                itemOptions.put("onCreated","");
                itemOptions.put("onMounted","");
                itemOptions.put("onClick","");
                itemOptions.put("onRightBtnClick","");
                itemOptions.put("onRightIconClick","");
                itemOptions.put("onRightTextIconClick","");
                itemOptions.put("readOnly",true);

                item.put("options", itemOptions);
                item.put("id", "collapse-item-" + keyWidget * 100 + i * 10);
                items.add(item);
                i++;
            }
            widgetListObject.put("items", items);
            String optionsId = "collapse" + keyWidget;
            //options
            JSONObject options = new JSONObject();
            options.put("name",optionsId);
            options.put("itemNames",itemNames);
            options.put("hidden",false);
            options.put("showSingle",false);
            options.put("customClass",new JSONArray());
            widgetListObject.put("options", options);
            //id
            widgetListObject.put("id", optionsId);
            widgetList.add(widgetListObject);

            main.put("widgetList", widgetList);
            JSONObject formConfig = new JSONObject();
            formConfig.put("modelName","formData");
            formConfig.put("refName","vForm");
            formConfig.put("rulesName","rules");
            formConfig.put("labelWidth",87);
            formConfig.put("labelPosition","left");
            formConfig.put("size","");
            formConfig.put("colon",false);
            formConfig.put("labelAlign","left");
            formConfig.put("cssCode","");
            formConfig.put("customClass",new JSONArray());
            formConfig.put("functions","");
            formConfig.put("layoutType","H5");
            formConfig.put("groupLevel",1);
            formConfig.put("isShareTable",false);
            formConfig.put("jsonVersion",3);
            formConfig.put("uploadURL","system/sysFile/batchUploadFiles");
            formConfig.put("delFileURL","system/sysFile/deleteFile?fileId=");
            formConfig.put("previewURL","system/sysFile/fileDownload?fileId=");
            formConfig.put("fileInfoURL","system/sysFile/getFileList?groupId=");
            formConfig.put("uploadTime","chose");
            formConfig.put("uploadModel","ht");
            JSONArray dictMap = new JSONArray();
            JSONObject dictMapObject = new JSONObject();
            dictMapObject.put("name","");
            dictMapObject.put("dicType","");
            dictMap.add(dictMapObject);
            formConfig.put("dictMap",dictMap);
            formConfig.put("onFormCreated","");
            formConfig.put("onFormMounted","");
            formConfig.put("onFormDataChange","");
            main.put("formConfig", formConfig);
            main.put("formField", formField);
        } else {
            //无字段分组
            JSONArray formField = new JSONArray();

            genTable.getColumnList().stream().filter(k -> k.getRemarks().contains("app_") || buildAllColumnsForApp && Global.YES.equals(k.getIsForm())).forEach(k -> {
                if (k.getShowType().equals("input")) {
                    formField.add(k.getName());
                    widgetList.add(getInput(keyInput, genTable.getName(), k, 1));
                } else if (k.getShowType().equals("textarea")) {
                    formField.add(k.getName());
                    widgetList.add(getInput(keyInput, genTable.getName(), k, 2));
                } else if (k.getShowType().equals("radiobox")) {
                    formField.add(k.getName());
                    widgetList.add(getRadio(keyRadio, genTable.getName(), k));
                } else if (k.getShowType().equals("checkbox")) {
                    formField.add(k.getName());
                    widgetList.add(getCheck(keyCheck, genTable.getName(), k));
                } else if (k.getShowType().equals("select") && "yes_no".equals(k.getDictType())) {
                    formField.add(k.getName());
                    widgetList.add(getSwitch(keySwitch, genTable.getName(), k));
                } else if (k.getShowType().equals("select") && Global.YES.equals(k.getSelectSimple())) {
                    formField.add(k.getName());
                    widgetList.add(getSelect(keySelect, genTable.getName(), k));
                } else if (k.getShowType().equals("treeselectRedio")
                        || k.getShowType().equals("areaselect")
                        || k.getShowType().equals("officeselectTree")
                        || k.getShowType().equals("treeselectRedio")
                        || k.getShowType().equals("treeselectCheck")
                        || k.getShowType().equals("treeselect")
                        || k.getShowType().equals("parentId")) {
                    formField.add(k.getName());
                    widgetList.add(getCascader(keyCascader, genTable.getName(), k));
                } else if (k.getShowType().equals("dateselect") && false == k.getDateType().contains("HH")) {
                    formField.add(k.getName());
                    widgetList.add(getCalendar(keyCalendar, genTable.getName(), k));
                } else if (k.getShowType().equals("dateselect") && k.getDateType().contains("HH")) {
                    formField.add(k.getName());
                    widgetList.add(getTime(keyTime, genTable.getName(), k));
                } else if (k.getShowType().equals("fileuploadpic")) {
                    formField.add(k.getName());
                    widgetList.add(getFileuploadpic(keyFileuploadpic, genTable.getName(), k));
                } else if (k.getShowType().equals("fileupload")) {
                    formField.add(k.getName());
                    widgetList.add(getFileupload(keyFileupload, genTable.getName(), k));
                }
            });

            main.put("widgetList", widgetList);
            JSONObject formConfig = new JSONObject();
            formConfig.put("modelName","formData");
            formConfig.put("refName","vForm");
            formConfig.put("rulesName","rules");
            formConfig.put("labelWidth",87);
            formConfig.put("labelPosition","left");
            formConfig.put("size","");
            formConfig.put("colon",false);
            formConfig.put("labelAlign","left");
            formConfig.put("cssCode","");
            formConfig.put("customClass",new JSONArray());
            formConfig.put("functions","");
            formConfig.put("layoutType","H5");
            formConfig.put("groupLevel",1);
            formConfig.put("isShareTable",false);
            formConfig.put("jsonVersion",3);
            formConfig.put("uploadURL","system/sysFile/batchUploadFiles");
            formConfig.put("delFileURL","system/sysFile/deleteFile?fileId=");
            formConfig.put("previewURL","system/sysFile/fileDownload?fileId=");
            formConfig.put("fileInfoURL","system/sysFile/getFileList?groupId=");
            formConfig.put("uploadTime","chose");
            formConfig.put("uploadModel","ht");
            JSONArray dictMap = new JSONArray();
            JSONObject dictMapObject = new JSONObject();
            dictMapObject.put("name","");
            dictMapObject.put("dicType","");
            dictMap.add(dictMapObject);
            formConfig.put("dictMap",dictMap);
            formConfig.put("onFormCreated","");
            formConfig.put("onFormMounted","");
            formConfig.put("onFormDataChange","");
            main.put("formConfig", formConfig);
            main.put("formField", formField);
        }
        String result = main.toJSONString();
        return result;
    }

    private static List<Dict> findGroupList(GenTable genTable, List<DictResult> groupDictList) {
        List<Dict> fieldGroupList = Lists.newArrayList();
        HashSet<String> groupKeys = Sets.newHashSet();
        genTable.getColumnList().stream().filter(k -> k.getRemarks() != null && k.getRemarks().contains("app_")).forEach(k -> {
            String[] groups = k.getRemarks().split(",");
            if (groups.length == 1) {
                groups = k.getRemarks().split("，");
            }
            for(int i = 0; i < groups.length; i++) {
                if (groups[i].contains("app_")) {
                    groupKeys.add(groups[i]);
                }
            }
        });
        for(String item : groupKeys) {
            groupDictList.stream().filter(k -> k.getMember().equals(item)).forEach(k -> {
                Dict dict = new Dict();
                dict.setCode(item);
                dict.setName(k.getMemberName());
                fieldGroupList.add(dict);
            });
        }
        return fieldGroupList;
    }

    //1 输入框
    private static JSONObject getInput(int key, String formNo, GenTableColumn column, int rows) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","input");
        obj.put("icon","text-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("type", "textarea");
        options.put("placeholder", "");
        options.put("labelAlign", null);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : null);
        options.put("size", "default");
        options.put("autosize", true);
        options.put("rows", rows);
        options.put("minValue", StringUtil.isNotEmpty(column.getMinValue()) ? column.getMinValue() : null);
        options.put("maxValue", StringUtil.isNotEmpty(column.getMaxValue()) ? column.getMaxValue() : null);
        options.put("readonly", Global.YES.equals(column.getIsReadonly()) ? true : false);
        options.put("disabled", false);
        options.put("hidden", false);
        options.put("clearable", true);
        options.put("colon", false);
        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("validation", "");
        options.put("validationHint", "");
        //options.put("maxLength", StringUtil.isNotEmpty(column.getMaxLength()) ? column.getMinValue() : null);
        options.put("maxLength", null);
        options.put("showWordLimit", false);
        options.put("prefixIcon", "");
        options.put("suffixIcon", "");
        options.put("appendButton", false);
        options.put("appendButtonLabel", "");
        options.put("appendButtonDisabled", false);
        options.put("buttonIcon", "search");
        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onInput", "");
        options.put("onChange", "");
        options.put("onFocus", "");
        options.put("onBlur", "");
        options.put("onValidate", "");
        options.put("onClear", "");
        options.put("onRightIconClick", "");
        options.put("onLeftIconClick", "");
        options.put("onRightBtnClick", "");

        obj.put("options", options);
        obj.put("id", formNo + "-" + column.getName());
        return obj;
    }

    //2 单选
    private static JSONObject getRadio(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","radio");
        obj.put("icon","radio-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", null);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : null);
        options.put("columnWidth", "200px");
        options.put("iconSize", null);
        options.put("direction", "horizontal");
        options.put("labelPosition", "right");
        options.put("labelDisabled", false);
        options.put("checkColor", "");
        options.put("shape", "round");
        options.put("disabled", false);
        options.put("hidden", false);

        //optionItems
        JSONArray optionItems = new JSONArray();
        JSONObject theOptionItem = new JSONObject();
        theOptionItem.put("label","选项1");
        theOptionItem.put("value","1");
        theOptionItem.put("remark","");
        optionItems.add(theOptionItem);
        options.put("optionItems", optionItems);

        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("validation", "");
        options.put("validationHint", "");
        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onChange", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //3 复选
    private static JSONObject getCheck(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","checkbox");
        obj.put("icon","checkbox-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",new JSONArray());

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", new JSONArray());
        options.put("columnWidth", "200px");
        options.put("iconSize", null);
        options.put("direction", "horizontal");
        options.put("checkColor", "");
        options.put("shape", "square");
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("labelPosition", "right");
        options.put("labelDisabled", false);
        options.put("multipleLimit", 0);
        options.put("disabled", false);
        options.put("hidden", false);

        //optionItems
        JSONArray optionItems = new JSONArray();
        JSONObject theOptionItem = new JSONObject();
        theOptionItem.put("label","选项1");
        theOptionItem.put("value","1");
        theOptionItem.put("remark","");
        optionItems.add(theOptionItem);
        options.put("optionItems", optionItems);

        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("validation", "");
        options.put("validationHint", "");
        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onChange", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //4 下拉
    private static JSONObject getSelect(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","select");
        obj.put("icon","select-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : "");
        options.put("placeholder", "");
        options.put("selectTitle", null);
        options.put("confirmButtonText", "");
        options.put("cancelButtonText", "");
        options.put("toolbarPosition", "top");
        options.put("loading", false);
        options.put("showToolbar", true);
        options.put("allowHtml", true);
        options.put("defaultIndex", 0);
        options.put("itemHeight", 44);
        options.put("visibleItemCount", 6);
        options.put("swipeDuration", 1000);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("hidden", false);

        //optionItems
        JSONArray optionItems = new JSONArray();
        JSONObject theOptionItem = new JSONObject();
        theOptionItem.put("label","选项1");
        theOptionItem.put("value","1");
        theOptionItem.put("remark","");
        theOptionItem.put("children",null);
        optionItems.add(theOptionItem);
        options.put("optionItems", optionItems);

        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("validation", "");
        options.put("validationHint", "");
        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onClick", "");
        options.put("onChange", "");
        options.put("onConfirm", "");
        options.put("onCancel", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //5 级联选择
    private static JSONObject getCascader(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","cascader");
        obj.put("icon","cascader-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : "");
        options.put("placeholder", "");
        options.put("size", "");
        options.put("selectTitle", null);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("hidden", false);
        options.put("readonly", false);
        options.put("multiple", false);
        options.put("isLeaf", true);
        options.put("isObject", true);
        options.put("validation", "");
        options.put("validationHint", "");
        //optionItems
        JSONArray optionItems = new JSONArray();
        JSONObject theOptionItem = new JSONObject();
        theOptionItem.put("label","选项1");
        theOptionItem.put("value","1");
        theOptionItem.put("remark","");
        JSONObject child = new JSONObject();
        child.put("label","子选项1");
        child.put("value","11");
        JSONArray children = new JSONArray();
        children.add(child);
        theOptionItem.put("children", children);
        optionItems.add(theOptionItem);
        options.put("optionItems", optionItems);

        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("requiredHint", "");
        options.put("customRule", "");
        options.put("customRuleHint", "");
        options.put("customClass", "");
        options.put("labelIconClass", null);
        options.put("labelIconPosition", "rear");
        options.put("labelTooltip", null);
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onClick", "");
        options.put("onChange", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //6 日历
    private static JSONObject getCalendar(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","calendar");
        obj.put("icon","calendar");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : null);
        options.put("placeholder", "");
        options.put("calendarType", "single");
        options.put("selectTitle", null);
        options.put("confirmButtonText", "");
        options.put("toolbarPosition", "bottom");
        options.put("itemHeight", 64);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("readonly", Global.YES.equals(column.getIsReadonly()) ? true : false);
        options.put("hidden", false);
        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("validation", "");
        options.put("validationHint", "");

        options.put("calendarBase", "");
        options.put("color", "");
        options.put("poppable", true);
        options.put("minDate", "2020-01-01");
        options.put("maxDate", null);
        options.put("maxRange", "");
        options.put("rangePrompt", "");
        options.put("showMark", true);
        options.put("showTitle", true);
        options.put("showsubTitle", true);
        options.put("showConfirm", true);
        options.put("firstdayOfWeek", 0);
        options.put("height", 500);

        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onClick", "");
        options.put("onChange", "");
        options.put("onConfirm", "");
        options.put("onCancel", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //7 时间
    private static JSONObject getTime(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","time");
        obj.put("icon","time-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : null);
        options.put("placeholder", "");
        options.put("columnWidth", "200px");
        options.put("size", "");
        options.put("autoFullWidth", true);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("readonly", Global.YES.equals(column.getIsReadonly()) ? true : false);
        options.put("disabled", false);
        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("hidden", false);
        options.put("clearable", true);
        options.put("editable", false);
        options.put("validation", "");
        options.put("validationHint", "");

        options.put("dateTimeType", "datetime");
        options.put("toolbarPosition", "top");
        options.put("confirmButtonText", "");
        options.put("cancelButtonText", "");
        options.put("itemHeight", 44);
        options.put("visibleItemCount", 6);
        options.put("swipeDuration", 1000);
        options.put("loading", false);
        options.put("showToolbar", true);

        options.put("customClass", "");
        options.put("labelIconClass", null);
        options.put("labelIconPosition", "rear");
        options.put("labelTooltip", null);
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onClick", "");
        options.put("onChange", "");
        options.put("onConfirm", "");
        options.put("onCancel", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //8 开关
    private static JSONObject getSwitch(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","switch");
        obj.put("icon","switch-field");
        obj.put("formItemFlag",true);
        obj.put("changeVal",null);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelAlign", "");
        options.put("defaultValue", StringUtil.isNotEmpty(column.getDefaultValue()) ? column.getDefaultValue() : null);
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("hidden", false);
        options.put("iconSize", 20);

        options.put("customClass", "");
        options.put("activeText", "");
        options.put("inactiveText", "");
        options.put("activeValue", "");
        options.put("inactiveValue", "");
        options.put("activeColor", null);
        options.put("inactiveColor", null);

        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onChange", "");
        options.put("onClick", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //9 图片 file-upload pic
    private static JSONObject getFileuploadpic(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","file-upload");
        obj.put("icon","file-upload-field");
        obj.put("formItemFlag",true);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("readonly", Global.YES.equals(column.getIsReadonly()) ? true : false);
        options.put("previewSize", 80);
        options.put("previewImage", true);
        options.put("previewFullImage", true);
        options.put("previewOptions", null);
        options.put("deletable", true);
        options.put("showUpload", true);
        options.put("lazyLoad", true);
        options.put("hidden", false);
        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("requiredHint", "");
        options.put("customRule", "");
        options.put("customRuleHint", "");
        options.put("uploadURL", "system/sysFile/batchUploadFiles");
        //+3
        options.put("delFileURL", "system/sysFile/deleteFile?fileId=");
        options.put("previewURL", "system/sysFile/fileDownload?fileId=");
        options.put("fileInfoURL", "system/sysFile/getFileList?groupId=");
        //waterMarkData
        JSONObject waterMarkData = new JSONObject();
        waterMarkData.put("center","");
        waterMarkData.put("rightTop",new JSONArray());
        waterMarkData.put("rightBottom",new JSONArray());
        waterMarkData.put("leftTop",new JSONArray());
        List<Object> list = Lists.newArrayList();
        list.add("nowDateTime()");
        list.add("getLngAndLat()");
        list.add("tableType()");
        list.add("expertName()");
        JSONArray leftBottom = new JSONArray(list);
        waterMarkData.put("leftBottom", leftBottom);
        options.put("waterMarkData", waterMarkData);

        options.put("uploadModel", "ht");
        options.put("uploadTime", "chose");
        options.put("multipleSelect", false);
        options.put("showFileList", true);
        options.put("limit", "100");
        options.put("fileMaxSize", "100");
        options.put("fileTypes", "image/*");
        options.put("fileShowTag", false);
        options.put("fileTag", "");
        options.put("videoMaxTime", 15);

        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onBeforeUpload", "");
        options.put("onUploadSuccess", "");
        options.put("onUploadError", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //10 文件 file-upload
    private static JSONObject getFileupload(int key, String formNo, GenTableColumn column) {
        JSONObject obj = new JSONObject();
        obj.put("key",key);
        obj.put("type","file-upload2");
        obj.put("icon","file-upload-field");
        obj.put("formItemFlag",true);

        JSONObject options = new JSONObject();
        options.put("name", column.getName());
        options.put("label", column.getComments());
        options.put("labelWidth", null);
        options.put("labelHidden", false);
        options.put("disabled", false);
        options.put("readonly", Global.YES.equals(column.getIsReadonly()) ? true : false);
        options.put("previewSize", 80);
        options.put("previewImage", true);
        options.put("previewFullImage", true);
        options.put("previewOptions", null);
        options.put("deletable", true);
        options.put("showUpload", true);
        options.put("lazyLoad", true);
        options.put("hidden", false);
        options.put("required", Global.YES.equals(column.getIsNull()) ? false : true);
        options.put("requiredHint", "");
        options.put("customRule", "");
        options.put("customRuleHint", "");
        options.put("uploadURL", "system/sysFile/batchUploadFiles");
        //+3
        options.put("delFileURL", "system/sysFile/deleteFile?fileId=");
        options.put("previewURL", "system/sysFile/fileDownload?fileId=");
        options.put("fileInfoURL", "system/sysFile/getFileList?groupId=");
        //waterMarkData
        /*JSONObject waterMarkData = new JSONObject();
        waterMarkData.put("center","");
        waterMarkData.put("rightTop",new JSONArray());
        waterMarkData.put("rightBottom",new JSONArray());
        waterMarkData.put("leftTop",new JSONArray());
        List<Object> list = Lists.newArrayList();
        list.add("nowDateTime()");
        list.add("getLngAndLat()");
        list.add("tableType()");
        list.add("expertName()");
        JSONArray leftBottom = new JSONArray(list);
        waterMarkData.put("leftBottom", leftBottom);
        options.put("waterMarkData", waterMarkData);*/

        options.put("uploadModel", "ht");
        options.put("uploadTime", "chose");
        options.put("multipleSelect", false);
        options.put("showFileList", true);
        options.put("limit", "100");
        options.put("fileMaxSize", "100");
        options.put("acceptFiles", ".");

        options.put("customClass", "");
        options.put("onCreated", "");
        options.put("onMounted", "");
        options.put("onBeforeUpload", "");
        options.put("onUploadSuccess", "");
        options.put("onUploadError", "");
        options.put("onValidate", "");

        obj.put("options", options);
        obj.put("id", column.getName());
        return obj;
    }

    //rate slider static-text divider

    public static int getHash(String inputStr, int defaultValue) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(inputStr.getBytes(StandardCharsets.UTF_8));
            BigInteger hashValue = new BigInteger(1, hashBytes);
            BigInteger modValue = hashValue.mod(BigInteger.valueOf(1000000));  // 取模操作，限制在6位以下
            return modValue.intValue();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("获取Hash值失败, inputStr: {}", inputStr, e);
        }
        return defaultValue;
    }
}
