package com.jeestudio.bpm.controller.system;

import com.google.common.collect.Lists;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.param.PageParam;
import com.jeestudio.bpm.common.view.common.TreeView;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 市级行政区树表接口
 */
@Tag(name = "区域管理")
@RestController
@RequestMapping("${adminPath}/system/area")
public class AreaController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ZformController.class);

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private ZformService zformService;

    /**
     * 获取市级行政区树
     *
     * @param parentId 行政区父级id;
     */
    @Operation(summary = "获取市级行政区树")
    @RequiresPermissions("user")
    @PostMapping("/cityTreeData")
    public ResultJson treeData(@RequestParam("parentId") String parentId, @RequestParam("formNo") String formNo, @RequestParam("traceFlag") String traceFlag) throws Exception {
        //创建接受表单数据的实体类
        Zform zform = new Zform();
        //创建行政区父级对象并将对象赋给行政区的parent属性
        zform.setParent(new Zform(parentId, formNo));
        //
        zform.setFormNo(formNo);
        GenTable genTable = genTableService.getGenTableWithDefination(zform.getFormNo());
        if (zform.getPageParam() == null) {
            zform.setPageParam(new PageParam());
        }
        Page<Zform> page = new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize(), zform.getPageParam().getOrderBy());
        page.setFromIndex(zform.getPageParam().getFromIndex());
        page = zformService.findPage(page,
                zform,
                "",
                currentUserName.get(),
                genTable,
                traceFlag,
                "");
        String userCount = null;
        List<TreeView> treeViewList = Lists.newArrayList();
        for (Zform theZform : page.getList()) {
            if (StringUtil.isEmpty(theZform.getS49()) || Global.NO.equals(theZform.getS49())) {
                userCount = "";
            } else {
                userCount = "(" + theZform.getS49() + ")";
            }
            String theZformId = theZform.getId();
            //判断行政区id是否以"31","11","12","50"开头，以00结尾,可以得到所有的省和市
            if (theZformId.startsWith("31") || theZformId.startsWith("11") || theZformId.startsWith("12") || theZformId.startsWith("50") || theZformId.endsWith("00") || theZformId.equals("1")) {
                TreeView treeView = new TreeView();
                treeView.setId(theZform.getId());
                treeView.setName(theZform.getS01() + userCount);
                treeView.setName_EN(theZform.getS50() + userCount);
                treeView.setParentId(theZform.getParent() != null ? theZform.getParent().getId() : "0");
                treeView.setSort(theZform.getSort() != null ? theZform.getSort() : 0);
                treeView.setHasChildren(theZform.isHasChildren());
                treeView.setNo(theZform.getS02());
                treeViewList.add(treeView);
            }
        }
        return ResultJson.success().put("data", treeViewList);
    }
}
