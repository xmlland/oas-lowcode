package com.jeestudio.bpm.controller.act;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jeestudio.bpm.common.entity.act.TaskPermission;
import com.jeestudio.bpm.common.entity.act.TaskSetting;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.entity.gen.GenTable;
import com.jeestudio.bpm.common.entity.gen.GenTableColumn;
import com.jeestudio.bpm.common.entity.system.*;
import com.jeestudio.bpm.common.view.system.OfficeView;
import com.jeestudio.bpm.service.act.TaskPermissionService;
import com.jeestudio.bpm.service.act.TaskSettingService;
import com.jeestudio.bpm.service.gen.GenTableService;
import com.jeestudio.bpm.service.system.*;
import com.jeestudio.bpm.utils.OfficeUtil;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.utils.BeanUtil;
import com.jeestudio.bpm.utils.Global;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.bpm.utils.StringUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 工作流节点
 */
@Tag(name = "工作流节点")
@RestController
@RequestMapping("${adminPath}/act/taskSetting")
public class TaskSettingController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    private TaskSettingService taskSettingService;

    @Autowired
    private TaskPermissionService taskPermissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private PostService postService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GenTableService genTableService;

    @Autowired
    private DictDataService dictDataService;

    /**
     * 获取节点配置
     */
    @Operation(summary = "获取节点配置")
    @RequiresPermissions("user")
    @GetMapping("/getTaskSetting")
    public ResultJson getTaskSetting(String procDefKey, String userTaskId, String userTaskName) {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
        TaskSetting taskSettingParam = new TaskSetting();
        taskSettingParam.setUserTaskName(userTaskName);
        taskSettingParam.setProcDefKey(procDefKey);
        taskSettingParam.setUserTaskId(userTaskId);
        TaskSetting taskSetting = taskSettingService.getByProcAndTask(taskSettingParam);
        if (taskSetting != null) {
            taskSettingParam = taskSetting;
        }
        List<Office> officeList = officeService.findList(new Office());
        List<OfficeView> officeViewList = Lists.newArrayList();
        OfficeUtil.OfficeViewCopy(officeList, officeViewList);
        List<Post> postList = postService.findList(new Post());
        List<Level> levelList = levelService.findList(new Level());
        List<Role> roleList = roleService.findList(new Role());
        List<Organization> orgList = organizationService.findList(new Organization());
        resultMap.put("taskSetting", taskSettingParam);
        resultMap.put("officeList", officeViewList);
        resultMap.put("postList", postList);
        resultMap.put("levelList", levelList);
        resultMap.put("roleList", roleList);
        resultMap.put("orgList", orgList);
        return ResultJson.success().put("taskSetting", resultMap);
    }

    /**
     * 保存节点配置
     */
    @Operation(summary = "保存节点配置")
    @RequiresPermissions("user")
    @PostMapping("/saveTaskSetting")
    public ResultJson saveTaskSetting(TaskSetting taskSetting) {
        String str = taskSetting.getTaskPermission().getRuleArgs();
        if (str != null && str != "" && str.charAt(1) == ',') {
            String ruleArgs = taskSetting.getTaskPermission().getRuleArgs().replaceFirst(",", "");
            taskSetting.getTaskPermission().setRuleArgs(ruleArgs);
        }
        if (false == taskSetting.getIsNewRecord()) {
            TaskSetting t = taskSettingService.get(taskSetting.getId());
            BeanUtil.copyBeanNotNull2Bean(taskSetting, t);
            taskSettingService.save(t);
            return ResultJson.success().put("taskSetting", t);
        } else {
            taskSettingService.save(taskSetting);
            return ResultJson.success().setInsertedId(taskSetting.getId()).put("taskSetting", taskSetting);
        }
    }

    /**
     * 根据ID索引获取用户
     */
    @Operation(summary = "根据ID索引获取用户")
    @RequiresPermissions("user")
    @GetMapping("/getUsersByIdIndex")
    public ResultJson getUsersByIdIndex(String objId) {
        List<User> list = Lists.newArrayList();
        if (objId.split(":")[0].equalsIgnoreCase("role")) {
            String condition = " AND b." + objId.split(":")[0] + "_id='" + objId.split(":")[1] + "' ";
            list = userService.findUserForFlow(condition);
        } else if (objId.split(":")[0].equalsIgnoreCase("post")
                || objId.split(":")[0].equalsIgnoreCase("level")
                || objId.split(":")[0].equalsIgnoreCase("office")) {
            String condition = " AND a." + objId.split(":")[0] + "_id='" + objId.split(":")[1] + "' ";
            list = userService.findUserForFlow(condition);
        } else if (objId.split(":")[0].equalsIgnoreCase("org")) {
            list = organizationService.findUserToOrg(objId.split(":")[1]);
        }
        Map<String, User> map = Maps.newHashMap();
        for (User user : list) {
            map.put(user.getId(), user);
        }
        list.clear();
        for (Map.Entry<String, User> entry : map.entrySet()) {
            if (entry.getValue() != null && StringUtil.isNotBlank(entry.getValue().getIsSys()) && Global.YES.equals(entry.getValue().getIsSys())) {
                //过滤系统用户
            } else if (entry.getValue() != null && entry.getValue().isSystem()) {
                //过滤三员管理员
            } else {
                //其他用户
                list.add(entry.getValue());
            }
        }
        return ResultJson.success().put("list", list);
    }

    /**
     * 更新用户任务ID
     */
    @Operation(summary = "更新用户任务ID")
    @RequiresPermissions("user")
    @GetMapping("/updateUserTaskId")
    public ResultJson updateUserTaskId(String oldId, String newId) {
        taskSettingService.updateUserTaskId(oldId, newId);
        return ResultJson.success();
    }

    /**
     * 获取权限列表
     */
    @Operation(summary = "获取权限列表")
    @RequiresPermissions("user")
    @PostMapping("/getPermissionList")
    public ResultJson getPermissionList(@RequestBody TaskPermission taskPermission) {
        Page<TaskPermission> page = taskPermission.getPage();
        LinkedHashMap<String, Object> map = this.permissionData(taskPermission, page.getPageNo(), page.getPageSize());
        return ResultJson.success().setData(map);
    }

    private LinkedHashMap<String, Object> permissionData(TaskPermission taskPermission,
                                                         Integer pageNo,
                                                         Integer pageSize) {
        LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();
        taskPermission.getSqlMap().put("dsf", "AND a.types = '1'");
        String cate = taskPermission.getCategory();
        if ("".equals(cate) || cate == null) {
            taskPermission.setCategory("请选择权限类型");
        }
        if (pageNo != null && pageSize != null) {
            taskPermission.getPageParam().setPageNo(pageNo);
            taskPermission.getPageParam().setPageSize(pageSize);
        }
        Page<TaskPermission> page = taskPermissionService.findPage(
                new Page<TaskPermission>(taskPermission.getPageParam().getPageNo(),
                        taskPermission.getPageParam().getPageSize(),
                        taskPermission.getPageParam().getOrderBy()),
                taskPermission);
        List<TaskPermission> list = page.getList();
        for (int i = 0; i < list.size(); i++) {
            TaskPermission tp = list.get(i);
            String category = dictDataService.getDictLabels(tp.getCategory(), "oa_task_permission_category", "", "");
            tp.setCategoryLabel(category);
        }
        map.put("rows", page.getList());
        map.put("total", page.getCount());
        return map;
    }

    /**
     * 获取表单列表
     */
    @Operation(summary = "获取表单列表")
    @RequiresPermissions("user")
    @PostMapping("/genTableList")
    public ResultJson genTableList() {
        return ResultJson.success().put("genTableList", genTableService.findAll());
    }

    /**
     * 获取表单字段列表
     */
    @Operation(summary = "获取表单字段列表")
    @RequiresPermissions("user")
    @GetMapping("/genTableColumnList")
    public ResultJson genTableColumnList(@RequestParam("name") String name) {
        ResultJson resultJson = ResultJson.success();
        GenTable genTableName = genTableService.getByName(name);
        GenTableColumn column = new GenTableColumn();
        column.setGenTable(genTableName);
        column.setIsForm(Global.YES);
        List<GenTableColumn> list = genTableService.findByColum(column);
        resultJson.put("rows", list);
        resultJson.setRows(list);
        return resultJson;
    }
}
