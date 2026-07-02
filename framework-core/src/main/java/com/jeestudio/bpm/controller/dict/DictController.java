package com.jeestudio.bpm.controller.dict;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.system.Dict;
import com.jeestudio.bpm.common.entity.system.DictResult;
import com.jeestudio.bpm.common.view.common.TreeView;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.system.DictDataService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.JsonConvertUtil;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: 系统字典管理
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("${adminPath}/sys/dict")
public class DictController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(DictController.class);

    @Autowired
    private ZformService zformService;

    @Autowired
    private DictDataService dictDataService;

    /**
     * 获取字典列表
     */
    @Operation(summary = "获取字典列表")
    @Parameters({@Parameter(name = "type", description = "type", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/dictList")
    public String dictList(@RequestParam("type") String type) {
        return JsonConvertUtil.objectToJson(ResultJson.success().put("dict", dictDataService.dictTypes(type)));
    }

    /**
     * 保存字典
     */
    @Operation(summary = "保存字典")
    @Parameters({@Parameter(name = "dict", description = "dict", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/saveDict")
    public ResultJson saveDict(Dict dict) {
        dictDataService.save(dict);
        return ResultJson.success();
    }

    /**
     * 根据值获取字典标签
     */
    @Operation(summary = "根据值获取字典标签")
    @Parameters({@Parameter(name = "values", description = "values", required = true),
            @Parameter(name = "type", description = "type", required = true),
            @Parameter(name = "defaultValue", description = "defaultValue", required = true),
            @Parameter(name = "lang", description = "lang", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getDictLabels")
    public ResultJson getDictLabels(@RequestParam("values") String values,
                                    @RequestParam("type") String type,
                                    @RequestParam("defaultValue") String defaultValue,
                                    @RequestParam("lang") String lang) {
        return ResultJson.success().put("data", dictDataService.getDictLabels(values, type, defaultValue, lang));
    }

    /**
     * 根据标签获取字典值
     */
    @Operation(summary = "根据标签获取字典值")
    @Parameters({@Parameter(name = "values", description = "values", required = true),
            @Parameter(name = "type", description = "type", required = true),
            @Parameter(name = "defaultValue", description = "defaultValue", required = true),
            @Parameter(name = "lang", description = "lang", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getDictValues")
    public ResultJson getDictValues(@RequestParam("labels") String labels,
                                    @RequestParam("type") String type,
                                    @RequestParam("defaultValue") String defaultValue,
                                    @RequestParam("lang") String lang) {
        return ResultJson.success().put("data", dictDataService.getDictValues(labels, type, defaultValue, lang));
    }

    /**
     * 获取字典查看列表
     */
    @Operation(summary = "获取字典查看列表")
    @Parameters({@Parameter(name = "types", description = "types", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getDictListView")
    public String getDictListView(@RequestParam("types") String types) {
        ResultJson resultJson = new ResultJson();
        resultJson.setCode(ResultJson.CODE_SUCCESS);
        resultJson.setMsg("获取字典成功");
        resultJson.setMsg_en("Get dict success");
        resultJson.setToken(token.get());
        resultJson.put("data", dictDataService.getDictList(types, false));
        return JsonConvertUtil.objectToJson(resultJson).replace("memberNameEn", "memberName_EN");
    }

    /**
     * 获取字典查看列表(扩展)
     */
    @Operation(summary = "获取字典查看列表(扩展)")
    @Parameters({@Parameter(name = "types", description = "types", required = true)})
    @GetMapping("/getDictListView2")
    public String getDictListView2(@RequestParam("types") String types, @RequestParam("isEdit") boolean isEdit) {
        ResultJson resultJson = ResultJson.success().put("data", dictDataService.getDictList(types, isEdit));
        return JsonConvertUtil.objectToJson(resultJson).replace("memberNameEn", "memberName_EN");
    }

    /**
     * 获取字典编辑列表
     */
    @Operation(summary = "获取字典编辑列表")
    @Parameters({@Parameter(name = "types", description = "types", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getDictListEdit")
    public String getDictListEdit(@RequestParam("types") String types) {
        return JsonConvertUtil.objectToJson(ResultJson.success().put("data", dictDataService.getDictList(types, true)));
    }

    /**
     * 获取字典树查看
     */
    @Operation(summary = "获取字典树查看")
    @Parameters({@Parameter(name = "type", description = "type", required = true)})
    @RequiresPermissions("user")
    @GetMapping("/getDictTreeView")
    public String getDictTreeView(@RequestParam("type") String type) {
        List<DictResult> list = dictDataService.getDictList(type, false);
        List<TreeView> treeList = Lists.newArrayList();
        for (DictResult dictResult : list) {
            TreeView treeView = new TreeView();
            treeView.setId(dictResult.getMember());
            treeView.setName(dictResult.getMemberName());
            treeView.setSort(dictResult.getSort());
            treeView.setHasChildren(dictResult.isHasChildren());
            treeView.setParentId(StringUtil.isEmpty(dictResult.getParentType()) ? "0" : dictResult.getParentType());
            treeList.add(treeView);
        }
        return JsonConvertUtil.objectToJson(ResultJson.success().put("data", treeList));
    }

    /**
     * 刷新字典缓存
     */
    @Operation(summary = "刷新字典缓存")
    @RequiresPermissions("user")
    @GetMapping("/refreshDictCache")
    public ResultJson refreshDictCache() {
        dictDataService.refreshDictCache();
        return ResultJson.success();
    }

    @Deprecated
    @RequiresPermissions("user")
    @GetMapping("/saveProcessCategory")
    public ResultJson saveProcessCategory(@RequestParam("name") String name) throws Exception {
        //保存字典
        Zform parent = new Zform();
        parent.setId("261f40db1acd423f89e3f86a736ef3fa");
        parent.setName("流程分类");
        Zform dict = new Zform();
        dict.setFormNo("sys_dictionary");
        dict.setId("");
        dict.setParent(parent);
        dict.setRemarks("");
        dict.setS01(name);//字典名称
        dict.setS02(name);//字典编码
        dict.setS03(name);//字典英文
        dict.setS04("1"); //排序
        dict.setS05("act_category");//上级编码
        dict.setS06("1"); //查看可见
        dict.setS07("1"); //编辑可见
        zformService.saveZform(dict, currentUserName.get(), "/dynamic/zform");
        return ResultJson.success();
    }
}
