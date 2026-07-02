package com.jeestudio.bpm.modules.admin.around;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
/**
 * @Description: 菜单管理扩展服务
 */

@Component("sys_menuAroundService")
public class SysMenuAroundService implements AroundServiceI {

    //判断是否为创建工作流节点系列菜单，根据map中是否包含workflow_menu判断，值是json string格式
    //1、从workflow_menu json string中拿出菜单项：name ,sort
    //2、创建系列菜单，如果href是/ht/dynamic开头的，则系列菜单项修改为 /模块/表单名称/list
    //3、将当前菜单改为只包含按钮和操作链接的隐藏菜单
    @Autowired
    ZformService zformService;

    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        String isWorkflowMenu = zformMap.getString("is_workflow_menu");
        String menuType = zformMap.getString("type_");
        if (Global.YES.equals(menuType) && Global.YES.equals(isWorkflowMenu)) {
            String workflowMenuKey = "workflow_menu";
            String href = zformMap.getString("href");
            if (zformMap.containsKey(workflowMenuKey)) {
                JSONArray workflowMenuArray = zformMap.getJSONArray(workflowMenuKey);
                for (int i = 0; i < workflowMenuArray.size(); i++) {
                    //1、从workflow_menu json string中拿出菜单项：name ,sort
                    //2、创建系列菜单，如果href是/ht/dynamic开头的，则系列菜单项修改为 /模块/表单名称/list
                    JSONObject theObj = workflowMenuArray.getJSONObject(i);
                    //复制当前菜单数据，然后修改
                    JSONObject workflowZformMap = JSON.parseObject(JSON.toJSONString(zformMap));
                    workflowZformMap.remove("is_workflow_menu");
                    workflowZformMap.remove("sys_menu");
                    workflowZformMap.remove("permissionArr");
                    workflowZformMap.remove(workflowMenuKey);

                    workflowZformMap.put("href", href);
                    workflowZformMap.put("target", "");
                    workflowZformMap.put("name", theObj.getString("user_task_name"));
                    workflowZformMap.put("name_en", theObj.getString("user_task_name"));
                    workflowZformMap.put("sort", theObj.getInteger("sort"));
                    workflowZformMap.put("permission", theObj.getString("user_task_id"));

                    Zform zform = zformService.getZformFromMap(workflowZformMap, loginName);
                    HashMap<String, Object> updateNullParamMap = workflowZformMap.getObject("updateNullParamMap", new TypeReference<HashMap<String, Object>>() {
                    });
                    zformService.saveZform(zform, loginName, "/dynamic/zform", updateNullParamMap);
                }
                //将当前菜单改为只包含按钮和操作链接的隐藏菜单，清理工作流菜单项
                zformMap.put("is_show", Global.NO);
                zformMap.remove(workflowMenuKey);
                zformMap.remove("is_workflow_menu");
            }
        }
    }
}
