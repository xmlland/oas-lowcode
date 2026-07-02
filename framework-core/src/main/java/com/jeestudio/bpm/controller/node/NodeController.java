package com.jeestudio.bpm.controller.node;

import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.controller.dynamic.ZformController;
import com.jeestudio.bpm.utils.ResultJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Description: 综合办公消息节点
 */
@Tag(name = "综合办公")
@RestController
@RequestMapping("${adminPath}/node/manage")
public class NodeController extends ZformController {

    protected static final Logger logger = LoggerFactory.getLogger(com.jeestudio.bpm.controller.node.NodeController.class);

    @Autowired
    ZformService zformService;

    @Operation(summary = "保存节点")
    @RequiresPermissions("user")
    @PostMapping("/saveNode")
    public ResultJson saveNode(@RequestBody Zform zfrom) throws Exception {
        ResultJson resultJson;
        try {
            //如果点击的是立即发送
            if ("send_now".equals(zfrom.getS03())) {
                zfrom.setD01(new Date());
                zfrom.setS01("success");
                //不是则看日期
            } else if ("timing_transmission".equals(zfrom.getS03())) {
                zfrom.setS01("to_be_sent");
            } else {
                zfrom.setS01("fail");
            }
            super.save(zfrom);
        } catch (Exception e) {
            super.save(zfrom);
        } finally {
            zfrom.setFormNo("oas_node_log");
            if (!zfrom.getIsNewRecord()) {
                Zform updateZfrom = new Zform();
                updateZfrom.setFormNo("oas_node_log");
                updateZfrom.getSqlMap().put("dsf", " AND a.sender = '" + this.currentUserName.get() + "' AND a.send_date = '" + zfrom.getD01() + "' ");
                Page<Zform> zformData = zformService.zformData(updateZfrom, "", this.currentUserName.get(), "");
                if (zformData.getList().size() == 1) {
                    zfrom.setId(zformData.getList().get(0).getId());
                }
            }
            resultJson = super.save(zfrom);
        }
        return resultJson;
    }

    @Operation(summary = "保存节点设置")
    @RequiresPermissions("user")
    @PostMapping("/saveNodeSetting")
    public ResultJson saveNodeSetting(@RequestBody Zform zform) throws Exception {
        zform.setS10("action=" + zform.getS03()
                + "&userid=" + zform.getS04()
                + "&account=" + zform.getS05()
                + "&password=" + zform.getS06());
        if ("Enable".equals(zform.getS08())) {
            ResultJson enable = this.getNodeByState(zform.getFormNo(), "Enable");
            //先查出来数据库中启用的数据，如果有，判断
            if (enable.getRows() != null && enable.getRows() != "") {
                List list = (ArrayList) enable.getRows();
                Zform newZform = (Zform) list.get(0);
                if (!newZform.getId().equals(zform.getId())) {
                    ResultJson resultJson = new ResultJson();
                    resultJson.setCode(ResultJson.CODE_FAILED);
                    resultJson.setMsg("抱歉，已经有启用的短信服务商！");
                    resultJson.setMsg_en("Operation has failed");
                    resultJson.setToken(token.get());
                    return resultJson;
                }
            }
        }
        return super.save(zform);
    }

    @Operation(summary = "按状态查询节点")
    @RequiresPermissions("user")
    @PostMapping("/getNodeByState")
    public ResultJson getNodeByState(@RequestParam("formNo") String formNo, @RequestParam("state") String state) throws Exception {
        Zform zform = new Zform();
        zform.setFormNo(formNo);
        zform.getSqlMap().put("dsf", " AND a.state='" + state + "' ");
        Page<Zform> zformData = zformService.zformData(zform, "", currentUserName.get(), "");
        if (zformData.getList().size() == 1) {
            zform = zformData.getList().get(0);
        }
        List<Zform> list = new ArrayList<>();
        list.add(zform);
        return ResultJson.success().setRows(list);
    }
}
