package com.jeestudio.bpm.controller.app;

import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.flowable.engine.repository.ProcessDefinition;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description: App通用数据访问接口
 */
@Tag(name = "移动端表单")
@RestController
@RequestMapping("${adminPath}/app/zform")
public class AppZformController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(AppZformController.class);

    @Autowired
    protected ZformService zformService;

    @Autowired
    protected GenTableService genTableService;

    /**
     * 根据ID获取数据
     */
    @Operation(summary = "根据ID获取数据")
    @RequiresPermissions("user")
    @PostMapping("/get")
    public ResultJson get(@RequestParam("id") String id, @RequestParam("formNo") String formNo, @RequestParam("procDefKey") String procDefKey) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        Zform zform = null;
        String loginName = currentUserName.get();
        if (StringUtil.isEmpty(procDefKey)) {
            zform = zformService.get(id, loginName, genTable);
        } else {
            if (StringUtil.isEmpty(id)) {
                zform = new Zform();
                zform.setFormNo(formNo);
                zform.setProcDefKey(procDefKey);
            } else {
                zform = zformService.get(id, loginName, genTable);
            }
            //流程表单
            if (false == StringUtil.isEmpty(genTable.getProcessDefinitionCategory())) {
                if (StringUtil.isBlank(zform.getProcInsId())) {
                    procDefKey = zform.getProcDefKey().replaceAll("'", "");
                    ProcessDefinition processDefinition = zformService.getProcessDefinition(procDefKey);
                    zform.getAct().setProcDefId(processDefinition.getId());
                    zform.getAct().setTaskDefKey(processDefinition.getDescription().split(",")[1]);
                } else {
                    zformService.setAct(zform, loginName);
                }
                zformService.setRuleArgs(zform, loginName);
            }
        }
        return ResultJson.success().put("data", zform);
    }

    /**
     * 获取数据列表
     */
    @Operation(summary = "获取数据列表")
    @RequiresPermissions("user")
    @PostMapping("/data/{formNo}")
    public ResultJson list(@PathVariable("formNo") String formNo, @RequestParam("path") String path, @RequestBody Zform zform, @RequestParam("length") String length) throws Exception {
        zform.setFormNo(formNo);
        int len = StringUtil.isBlank(length) ? 0 : Integer.parseInt(length);

        GenTable genTable = genTableService.getGenTableWithDefination(formNo);
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPage(page,
                zform,
                path,
                currentUserName.get(),
                genTable,
                "",
                "");

        List<Zform> rows = page.getList();
        LinkedHashMap<String, Object> datas = Maps.newLinkedHashMap();
        if ((len + 10) < rows.size()) {
            datas.put("rows", rows.subList(len, len + 10));
            datas.put("nomMore", false);
        } else if ((len + 10) == rows.size()) {
            datas.put("rows", rows.subList(len, len + 10));
            datas.put("nomMore", true);
        } else {
            datas.put("rows", rows.subList(len, rows.size()));
            datas.put("nomMore", true);
        }
        return ResultJson.success().setData(datas).put("formNo", formNo);
    }

    /**
     * 保存数据
     */
    @Operation(summary = "保存数据")
    @RequiresPermissions("user")
    @PostMapping("/save")
    public ResultJson save(@RequestBody Zform zform) throws Exception {
        ResultJson resultJson = zformService.saveZform(zform, currentUserName.get(), "/dynamic/zform");
        resultJson.setToken(token.get());
        return resultJson;
    }

    /**
     * 删除数据
     */
    @Operation(summary = "删除数据")
    @RequiresPermissions("user")
    @PostMapping("/delete")
    public ResultJson delete(@RequestBody Zform zform) throws Exception {
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        zformService.deleteAll(zform.getId(), zform.getFormNo(), genTable, currentUserName.get());
        return ResultJson.success();
    }
}
