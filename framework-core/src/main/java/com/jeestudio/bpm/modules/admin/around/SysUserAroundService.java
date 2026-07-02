package com.jeestudio.bpm.modules.admin.around;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeestudio.bpm.common.around.AroundServiceI;
import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.param.GridselectParam;
import com.jeestudio.bpm.mapper.base.common.ZformDao;
import com.jeestudio.bpm.service.common.ZformService;
import com.jeestudio.bpm.utils.SqlInjectionUtil;
import com.jeestudio.tools.base.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Description: 用户管理扩展服务
 */
@Component("sys_userAroundService")
public class SysUserAroundService implements AroundServiceI {


    private final ZformDao zformDao;
    private final ZformService zformService;

    public SysUserAroundService(@Qualifier("sqlSessionFactoryBase") ZformDao zformDao, @Qualifier("zformService") ZformService zformService) {
        this.zformDao = zformDao;
        this.zformService = zformService;
    }

    @Override
    public void beforeGridselectDataMap(GridselectParam gridselectParam) {
        List<GridselectParam.FilterData> filterList = gridselectParam.getFilterList();
        if (filterList == null || filterList.isEmpty()) {
            return;
        }
        List<GridselectParam.FilterData> filterList2 = new ArrayList<>(filterList.size());
        for (GridselectParam.FilterData filterData : filterList) {
            if ("roleCode".equals(filterData.getKey())) {
                // 使用参数化查询: 通过 queryWrapperConsumer 传递参数，避免 SQL 注入
                String roleEnname = ConvertUtil.getString(filterData.getValue());
                SqlInjectionUtil.filterContent(roleEnname);
                Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> 
                    queryWrapper.apply("a.id in (select user_id from sys_user_role a left join sys_role b on a.role_id = b.id where enname = {0})", roleEnname);
                // 不再使用字符串拼接方式，改用 queryWrapperConsumer 参数化方式
                filterData.setType("apply");
                filterData.setKey("queryWrapperConsumer");
                filterData.setValue(consumer);
                filterList2.add(filterData);
            }else if ("b.parent_ids".equals(filterData.getKey())) {
                // 查询父级机构或者部门为某个值的用户
                GridselectParam.FilterData filterDataOrg = new GridselectParam.FilterData();
                List<GridselectParam.FilterData> list = new ArrayList<>(2);
                list.add(new GridselectParam.FilterData("b.parent_ids", filterData.getValue(), "like"));
                list.add(new GridselectParam.FilterData("b.id", filterData.getValue(), "eq", true));
                filterDataOrg.setChildren(list);
                filterList2.add(filterDataOrg);
            } else {
                filterList2.add(filterData);
            }
        }
        gridselectParam.setFilterList(filterList2);
    }

    @Override
    public void beforeGetZformFromMap(JSONObject zformMap, String loginName, Boolean isChildren) throws Exception {
        if (zformMap.containsKey("role_id")) {
            // 查询某个角色下的用户
            String role_id = zformMap.getString("role_id");
            Consumer<QueryWrapper<Zform>> consumer = queryWrapper -> queryWrapper.apply("a.id in (select user_id from sys_user_role where role_id = {0})", role_id);
            zformMap.put("queryWrapperConsumer", consumer);
            zformMap.remove("role_id");
        }
    }



    public void beforeSaveZform(Zform zform, String loginName, String businessKey) throws Exception {

        //如果更新了用户名 同步修改 工作流 相关 表的 用户名
        //act_hi_comment（message_） ； act_hi_identitylink(user_id_) ；act_hi_taskinst(owner_) ；act_ru_identitylink(user_id_)  act_ru_task(owner_)
        if (zform.getId() != null) {
            QueryWrapper<Zform> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("a.id", zform.getId());
            List<LinkedHashMap> sys_userOldList = zformService.findMapList("sys_user", queryWrapper);


            JSONObject sys_userNew = zformService.getMapByZform(zform);

            if (CollUtil.isNotEmpty(sys_userOldList)) {
                LinkedHashMap sys_userOld = sys_userOldList.get(0);

                if (!sys_userNew.getString("login_name").equals(sys_userOld.get("login_name"))) {
                    String login_nameNew = sys_userNew.getString("login_name");
                    String login_nameOld = (String) sys_userOld.get("login_name");

                    updateHi_comment(login_nameOld,login_nameNew);
                    updateActTable(login_nameOld,login_nameNew,"act_hi_identitylink","user_id_");
                    updateActTable(login_nameOld,login_nameNew,"act_hi_taskinst","owner_");
                    updateActTable(login_nameOld,login_nameNew,"act_ru_identitylink","user_id_");
                    updateActTable(login_nameOld,login_nameNew,"act_ru_task","owner_");


                }


            }

        }


    }



    private void updateHi_comment(String login_nameOld, String login_nameNew) {
        // 使用参数化查询，避免 SQL 注入
        String sql = "select a.id_,a.message_ from act_hi_comment a where a.message_ LIKE #{param.loginNamePattern}";
        Map<String, Object> param = new HashMap<>();
        param.put("loginNamePattern", login_nameOld + "_|_owner");
        List<LinkedHashMap> act_hi_commentMapList = zformDao.findMapBySqlParm(sql, param);
        
        // Step 2: 构建需要更新的 message_ 和 id_ 列表
        List<String> ids = new ArrayList<>();
        for (LinkedHashMap record : act_hi_commentMapList) {
            String id = (String) record.get("id_");
            ids.add(id);
        }

        if (!ids.isEmpty()) {
            // 构建新的 message_
            String newMessage = login_nameNew + "_|_owner";

            // 使用参数化更新 SQL
            String updateSql = "update act_hi_comment set message_ = #{param.newMessage} where id_ in (" +
                    ids.stream().map(id -> "#{param.id_" + ids.indexOf(id) + "}").collect(Collectors.joining(",")) + ")";
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("newMessage", newMessage);
            for (int i = 0; i < ids.size(); i++) {
                updateParam.put("id_" + i, ids.get(i));
            }
            
            // 执行参数化更新操作
            zformDao.updateSqlParm(updateSql, updateParam);
        }

    }

    private void updateActTable(String loginNameOld, String loginNameNew, String tableName, String field) {
        // 验证表名和列名是否为合法SQL标识符，防止SQL注入
        SqlInjectionUtil.validateIdentifier(tableName);
        SqlInjectionUtil.validateIdentifier(field);
        
        // 使用参数化查询，避免 SQL 注入
        String sql = "select a.id_," + field + " from " + tableName + " a where a." + field + " = #{param.loginNameOld}";
        Map<String, Object> param = new HashMap<>();
        param.put("loginNameOld", loginNameOld);
        List<LinkedHashMap> act_hi_commentMapList = zformDao.findMapBySqlParm(sql, param);
        
        // Step 2: 构建需要更新的 id_ 列表
        List<String> ids = new ArrayList<>();
        for (LinkedHashMap record : act_hi_commentMapList) {
            String id = (String) record.get("id_");
            ids.add(id);
        }

        if (!ids.isEmpty()) {
            // 使用参数化更新 SQL
            String updateSql = "update " + tableName + " set " + field + " = #{param.loginNameNew} where id_ in (" +
                    ids.stream().map(id -> "#{param.id_" + ids.indexOf(id) + "}").collect(Collectors.joining(",")) + ")";
            Map<String, Object> updateParam = new HashMap<>();
            updateParam.put("loginNameNew", loginNameNew);
            for (int i = 0; i < ids.size(); i++) {
                updateParam.put("id_" + i, ids.get(i));
            }

            // 执行参数化更新操作
            zformDao.updateSqlParm(updateSql, updateParam);
        }


    }

}
