package com.jeestudio.bpm.common.around;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.ThreadLocalUtil;
import com.jeestudio.tools.excel.ExcelExportUtil;
import com.jeestudio.tools.excel.ExcelField;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * @Description: 动态表单Around控制器接口
 */
public interface AroundControllerI {

    default jakarta.servlet.http.HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    default Map<String,Object> getRequestParams(){
        return ThreadLocalUtil.getRequestParams();
    }

    default boolean selectDataListByAround(){
        return false;
    }
    default List<Map<String, Object>> selectDataList(){
        return new LinkedList<>();
    }

    default void beforeSetSubTableSql(SubTableExportConfig config,GenTable genTable,GenTable child,String tableName,String tableAs){ }


    default boolean selectDataListBySupplierByAround() {
        return false;
    }
    default List<Supplier<List<Map<String, Object>>>> selectDataListBySupplier(GenTable genTable, Zform zform,String parentId, List<ExcelField> excelExportFieldList) {
        return new ArrayList<>();
    }

    default ResultJson customExpData(List<ExcelField> excelExportFieldList, List<Map<String, Object>> dataList, String filename){
        Workbook workbook = ExcelExportUtil.export("sheet1", excelExportFieldList, dataList, 1,-1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SysFileService sysFileService = SpringUtil.getBean(SysFileService.class);
        SysFile sysFile = sysFileService.saveExportSysFile(filename+".xlsx", out.toByteArray());
        return ResultJson.success().put("fileId", sysFile.getId()).put("file",sysFile);
    }


    default ResultJson customExpDataBySupplier(List<ExcelField> excelExportFieldList, List<Supplier<List<Map<String, Object>>>> supplierList) {
        return null;
    }

    default void beforeReturnDataMap(ResultJson resultJson, Page<Zform> page, GenTable genTable, Zform zform, JSONObject zformMap, String parentId){
    }

    /**
     *
     * @param loginName
     * @param genTable
     * @param zform
     * @param zformMapCopy  前端传递的最原始请求体
     * @param parentId
     * @return
     */
    default ResultJson customReturnResult(String loginName, GenTable genTable, Zform zform, JSONObject zformMapCopy, String parentId) {
        return null;
    }

    /**
     *
     * @param resultJson
     * @param page
     * @param genTable
     * @param zform
     * @param zformMapCopy 前端传递的最原始请求体
     * @param parentId
     */
    default void beforeReturnDataMapWithCopyZformMap(ResultJson resultJson, Page<Zform> page, GenTable genTable, Zform zform, JSONObject zformMapCopy, String parentId, String  loginName){
    }
}
