package com.jeestudio.bpm.controller.app;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.system.DictResult;
import com.jeestudio.bpm.common.entity.system.SysFile;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @Description: App需要的接口
 */
@Tag(name = "移动端接口")
@RestController
@RequestMapping("${adminPath}/app")
public class AppController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @Value("${fileRoot}")
    private String fileRoot;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private ZformService zformService;

    /**
     * 下载缩略图
     */
    @Operation(summary = "下载缩略图")
    @GetMapping("/fileDownloadThumbNew")
    public String fileDownloadThumbNew(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam(value = "fileId", required = false) String fileId,
                                       @RequestParam(value = "groupId", required = false) String groupId) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String url = this.getThumbPath(fileId, groupId);
            if (StringUtil.isNotEmpty(url)) {
                File file = new File(fileRoot + Encodes.unescapeHtml(url));
                response.setHeader("Content-Disposition", "attachment;fileName="
                        + FileUtil.getFileName(request.getHeader("User-Agent"), file.getName()));
                inputStream = new FileInputStream(file);
                outputStream = response.getOutputStream();
                int n = 0;
                while ((n = inputStream.read()) != -1) {
                    outputStream.write(n);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Error occurred while trying to download thumb: " + ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            logger.error("Error occurred while trying to download thumb: " + ExceptionUtils.getStackTrace(e));
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("Error occurred while trying to download thumb: " + ExceptionUtils.getStackTrace(e));
            }
        }
        return null;
    }

    private String getThumbPath(String fileId, String groupId) {
        String thumbPath = "";
        if (StringUtil.isEmpty(fileId)) {
            LinkedHashMap<String, Object> map = sysFileService.getFileList(groupId);
            if (map.size() > 0) {
                List<SysFile> sysFileList = (List<SysFile>) map.get("files");
                if (sysFileList.size() > 0) {
                    thumbPath = sysFileList.get(0).getThumbPath();
                }
            }
        } else {
            SysFile sysFile = sysFileService.get(fileId);
            if (sysFile != null) {
                thumbPath = sysFile.getThumbPath();
            }
        }
        return thumbPath;
    }

    /**
     * 获取字典数据
     */
    @Operation(summary = "获取字典数据")
    @RequiresPermissions("user")
    @PostMapping("/getDict")
    public ResultJson getDict(@RequestParam("dictParams") String dictParams) {
        JSONObject dictObject = null;
        if (StringUtil.isEmpty(dictParams)) {
            List<DictResult> dictList = dictDataService.getDictList("data-params", false);
            for (DictResult dict : dictList) {
                dictParams += "," + dict.getMember();
            }
            if (dictParams.length() > 0) dictParams = dictParams.substring(1);
            dictObject = dictDataService.getDictListForApp(dictParams, false);
        } else {
            dictObject = dictDataService.getDictListForApp(dictParams, false);
        }
        return ResultJson.success().put("data", dictObject);
    }

    /**
     * 根据表名称，查询移动端表单定义Json
     *
     * @param formNo
     * @return
     */
    @Operation(summary = "获取表单定义")
    @RequiresPermissions("user")
    @GetMapping("/getFormDefinition/{formNo}")
    public ResultJson getFormDefinition(@PathVariable(name = "formNo") String formNo) {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        HashMap dictMap = new HashMap();
        List<LinkedHashMap> areaTree = null;
        List<LinkedHashMap> officeTree = null;
        List<LinkedHashMap> userTree = null;
        for (GenTableColumn column : genTable.getColumnList()) {
            if (StringUtil.isNotEmpty(column.getDictType())) {
                List<DictResult> dictCacheList = dictDataService.getDictList(column.getDictType(), false);
                List<HashMap> dicList = Lists.newArrayList();
                for (DictResult dictCache : dictCacheList) {
                    HashMap dict = new HashMap();
                    dict.put("label", dictCache.getMemberName());
                    dict.put("value", dictCache.getMember());
                    dict.put("children", null);
                    dicList.add(dict);
                }
                dictMap.put(column.getName(), dicList);
            } else if (StringUtil.isNotEmpty(column.getTableName()) &&
                    (Global.NO.equals(column.getSelectSimple()) || column.getShowType().contains("gridselect"))) {
                //下拉表 或 自定义查询
                GridselectParam gridselectParam = new GridselectParam();
                gridselectParam.setPageParam(new PageParam());
                gridselectParam.getPageParam().setPageSize(300);
                gridselectParam.setTableName(column.getTableName());
                Page<Zform> result = zformService.gridselectDataMapList(gridselectParam);
                List<HashMap> dicList = Lists.newArrayList();
                for (LinkedHashMap map : result.getMap()) {
                    HashMap dict = new HashMap();
                    dict.put("label", StringUtil.getString(map.get(column.getSearchKey())));
                    dict.put("value", StringUtil.getString(map.get(column.getSelectValuefield())));
                    dict.put("children", null);
                    dicList.add(dict);
                }
                dictMap.put(column.getName(), dicList);
            } else if (column.getShowType().equals("areaselect")) {
                //行政区
                if (areaTree == null) {
                    areaTree = zformService.treeDataMobile(ZformService.TREE_TYPE_AREA, null);
                }
                dictMap.put(column.getName(), areaTree);
            } else if (column.getShowType().equals("officeselectTree")) {
                //部门选择
                if (officeTree == null) {
                    officeTree = zformService.treeDataMobile(ZformService.TREE_TYPE_OFFICE, null);
                }
                dictMap.put(column.getName(), officeTree);
            } else if (column.getShowType().equals("treeselectRedio") || column.getShowType().equals("treeselectCheck")) {
                //人员选择
                if (userTree == null) {
                    userTree = zformService.treeDataMobile(ZformService.TREE_TYPE_USER, null);
                }
                dictMap.put(column.getName(), userTree);
            }
        }
        return ResultJson.success().put("table", genTable.getExtJs()).put("dictMap", dictMap);
    }

    /**
     * 查询树
     *
     * @param formNo   sys_area, sys_office, sys_user 或其他自定义树表
     * @param parentId 空-查询全部数据，0查询根节点，其他值-向下查询一级
     * @param opt      控制参数，备用
     * @return
     */
    @Operation(summary = "获取移动端树数据")
    @RequiresPermissions("user")
    @PostMapping("/treeDataMobile")
    public List<LinkedHashMap> treeDataMobile(@RequestParam("formNo") String formNo,
                                              @RequestParam(value = "parentId", required = false, defaultValue = "") String parentId,
                                              @RequestParam(value = "opt", required = false) String opt) {
        return zformService.treeDataMobile(formNo, parentId);
    }
}
