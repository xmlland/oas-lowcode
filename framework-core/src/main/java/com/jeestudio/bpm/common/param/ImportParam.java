package com.jeestudio.bpm.common.param;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.tools.excel.ExcelField;
import lombok.Data;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Description: 导入参数
 */
@Data
public class ImportParam {
    private String formNo;
    private String parentFormNo;
    private String uniqueId;
    private String toCompany;
    private String fileId;
    private String parentId;
    private String areaId;
    private User currentUser;
    private JSONObject otherParam;

    private String ownerCode;
    private GenTable genTable;
    private List<ExcelField> importFieldList;
    private List<Map<String, Object>> mapList;

    public ImportParam() {
    }
    public ImportParam(String formNo, String parentFormNo, String uniqueId, String toCompany, String fileId, String parentId, String areaId, User currentUser) {
        this.formNo = formNo;
        this.parentFormNo = parentFormNo;
        this.uniqueId = uniqueId;
        this.toCompany = toCompany;
        this.fileId = fileId;
        this.parentId = parentId;
        this.areaId = areaId;
        this.currentUser = currentUser;
    }
    public ImportParam(String formNo, String parentFormNo, String uniqueId, String toCompany, String fileId, String parentId, String areaId, User currentUser, JSONObject otherParam) {
        this.formNo = formNo;
        this.parentFormNo = parentFormNo;
        this.uniqueId = uniqueId;
        this.toCompany = toCompany;
        this.fileId = fileId;
        this.parentId = parentId;
        this.areaId = areaId;
        this.currentUser = currentUser;
        this.otherParam = otherParam;
    }

    public static ImportParam  getImportParam(HttpServletRequest request) {
        JSONObject importParams = JSONObject.parseObject(JSONObject.toJSONString(request.getParameterMap()));
        return getImportParam(importParams);
    }
    public static ImportParam  getImportParam(Map<String, String[]> parameterMap) {
        JSONObject importParams = JSONObject.parseObject(JSONObject.toJSONString(parameterMap));
        return getImportParam(importParams);
    }
    public static ImportParam getImportParam(JSONObject parms) {
        ImportParam importParam = new ImportParam();
        importParam.otherParam = new JSONObject();
        for (String key : parms.keySet()) {
            JSONArray jsonArray = parms.getJSONArray(key);
            Object value;
            if (jsonArray.size() > 1) {
                value = jsonArray;
            }else{
                value = jsonArray.get(0);
            }
            switch (key){
                case "formNo":
                    importParam.setFormNo(value.toString());
                    break;
                case "parentFormNo":
                    importParam.setParentFormNo(value.toString());
                    break;
                case "uniqueId":
                    importParam.setUniqueId(value.toString());
                    break;
                case "toCompany":
                    importParam.setToCompany(value.toString());
                    break;
                case "fileId":
                    importParam.setFileId(value.toString());
                    break;
                case "parentId":
                    importParam.setParentId(value.toString());
                    break;
                case "areaId":
                    importParam.setAreaId(value.toString());
                    break;
                default:
                    importParam.otherParam.put(key,value);
                    break;
            }
        }
        return importParam;
    }


}
