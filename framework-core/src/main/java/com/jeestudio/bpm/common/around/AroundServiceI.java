package com.jeestudio.bpm.common.around;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeestudio.bpm.common.entity.common.ExportExtraEntity;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.system.User;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.ImportParam;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.excel.ExcelField;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 动态表单Around钩子服务接口
 */
public interface AroundServiceI {

    /** 获取当前HTTP请求 */
    default jakarta.servlet.http.HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /** 获取单条数据前钩子 */
    default void beforeGetMap(String id, GenTable genTable, String extFlag, String loginName) {

    }

    /** 获取单条数据后钩子（可改写返回map） */
    default LinkedHashMap afterGetMap(LinkedHashMap map, String id, GenTable genTable, String extFlag, String loginName) {
        return map;
    }

    /** 设置查询SQL前钩子 */
    default void beforeSetSqlMap(Zform zform, GenTable genTable, User currentUser) {

    }

    /** 分页查询前钩子 */
    default void beforeFindPageMap(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {

    }

    /** 分页查询后钩子（可改写返回页） */
    default Page<Zform> afterFindPageMap(Page<Zform> pageMap, Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag) {
        return pageMap;
    }


    /** 保存前钩子 */
    default void beforeSaveZform(Zform zform, String loginName, String businessKey) throws Exception {

    }

    /** 批量保存选择前钩子 */
    default void beforeBatchSaveSelect(List<JSONObject> data, String loginName, String businessKey) {

    }

    /** 保存后钩子（可改写返回结果） */
    default ResultJson afterSaveZform(ResultJson resultJson, Zform zform, String loginName, String businessKey) throws Exception {
        return resultJson;
    }


    /** 批量保存选择后钩子 */
    default void afterBatchSaveSelect(List<Zform> zformList, String loginName, String businessKey) throws Exception {

    }

    /** 批量删除前钩子 */
    default void beforeDeleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {

    }

    /** 批量删除后钩子 */
    default void afterDeleteAll(String ids, String formNo, GenTable genTable, String loginName) throws Exception {

    }


    /** 导出数据前钩子 */
    default void beforeExportDataMap(Page<Zform> data, Zform zform, GenTable genTable, String loginName) {

    }

    /** 导出数据后钩子（可改写返回行） */
    default List<Map<String, Object>> afterExportDataMap(List<Map<String, Object>> rowArr, Page<Zform> data, Zform zform, GenTable genTable, String loginName) {
        return rowArr;
    }

    /** 导入数据前钩子（旧重载，已废弃，改用 ImportParam 版本） */
    @Deprecated
    default void beforeImportData(String ownerCode, List<Map<String, Object>> mapList, String formNo, String parentFormNo, GenTable genTable, String parentId, String uniqueId, User currentUser) {

    }
    /** 导入数据前钩子 */
    default void beforeImportData(ImportParam importParam) {

    }

    /** 保存导入数据前钩子（旧重载1，已废弃，改用 ImportParam 版本） */
    @Deprecated
    default void beforeSaveImportData(List<Zform> importData, String ownerCode, List<Map<String, Object>> mapList, String formNo, String parentFormNo, GenTable genTable, String parentId, String uniqueId, User currentUser) {

    }

    /** 保存导入数据前钩子（旧重载2，已废弃，改用 ImportParam 版本） */
    @Deprecated
    default void beforeSaveImportData(List<Zform> importData, String ownerCode, List<ExcelField> importFieldList, List<Map<String, Object>> mapList, String formNo, String parentFormNo, GenTable genTable, String parentId, String uniqueId, User currentUser) {

    }
    /** 保存导入数据前钩子 */
    default void beforeSaveImportData(List<Zform> importData, ImportParam importParam) {

    }

    /** 导入数据后钩子（旧重载，已废弃，改用 ImportParam 版本） */
    @Deprecated
    default void afterImportData(List<Zform> importData, String ownerCode, List<Map<String, Object>> mapList, String formNo, String parentFormNo, GenTable genTable, String parentId, String uniqueId, User currentUser) {

    }
    /** 导入数据后钩子 */
    default void afterImportData(List<Zform> importData, ImportParam importParam) {

    }

    /** 获取Excel导入字段列表后钩子（可改写返回） */
    default List<ExcelField> afterGetExcelImportFieldList(List<ExcelField> excelFieldList, GenTable genTable, String areaId) {
        return excelFieldList;
    }

    /** 列表选择数据前钩子 */
    default void beforeGridselectDataMap(GridselectParam gridselectParam) {

    }

    /** 从Map构建Zform前钩子 */
    default void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {

    }

    /** 从Map构建Zform查询前钩子 */
    default void beforeGetZformFromMapSearch(JSONObject zformMap, String loginName, Boolean isSearch) throws Exception {

    }

    /** 批量操作钩子（可改写返回msg） */
    default String onBatchOperation(Zform zform, String ids, String formNo, GenTable genTable, String data, String extData, String loginName, String msg) throws Exception {
        return msg;
    }

    /** 获取含流程信息的Zform后钩子（可改写返回） */
    default LinkedHashMap afterGetZformWithActMap(LinkedHashMap zformMap, String formNo, String id, String procDefKey, String procTaskId) {
        return zformMap;
    }

    /** 自定义返回页（返回null表示使用默认页） */
    default Page<Zform> customReturnPage(Page<Zform> page, Zform zform, String path, String loginName, GenTable genTable, String traceFlag, String parentId, String extFlag){
        return null;
    }

    /** URL导出前钩子 */
    default void beforeExpdataByUrl(String fileName, List<ExcelField> excelFieldList, JSONObject zformMap,String path){
    }

    /** URL导出后钩子 */
    default void afterExpdataByUrl(String fileName, List<ExcelField> excelFieldList, JSONArray jsonRows, boolean b, JSONArray mergeColumnConfig, ExportExtraEntity extraData){
    }
    /** 列表选择设置SQL前钩子 */
    default void beforeGridselectSetSqlMap( GenTable genTable) {

    }
}
