package com.jeestudio.bpm.controller.act;

import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.view.act.ProcessView;
import com.jeestudio.bpm.service.act.ActProcessService;
import com.jeestudio.bpm.controller.base.BaseController;
import com.jeestudio.bpm.service.system.SysFileService;
import com.jeestudio.bpm.utils.ResultJson;
import com.jeestudio.tools.base.utils.ConvertUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 工作流流程
 */
@Tag(name = "工作流流程")
@RestController
@RequestMapping("${adminPath}/act/process")
public class ProcessController extends BaseController {

    protected static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ActProcessService actProcessService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SysFileService sysFileService;

    @Value("${fileRoot}")
    private String fileRoot;

    /**
     * 获取流程列表
     */
    @Operation(summary = "获取流程列表")
    @RequiresPermissions("user")
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = "application/json")
    public ResultJson getProcessList(@RequestBody Zform zform) {
        ResultJson resultJson = ResultJson.success();
        String category = zform.getS01();
        Page<ProcessView> page = actProcessService.processList(new Page<>(zform.getPageParam().getPageNo(), zform.getPageParam().getPageSize()), category);
        resultJson.setRows(page.getList());
        resultJson.setTotal(page.getCount());
        return resultJson;
    }

    /**
     * 更新流程状态
     */
    @Operation(summary = "更新流程状态")
    @RequiresPermissions("user")
    @RequestMapping(value = "/update/{state}")
    public ResultJson updateState(@PathVariable("state") String state, @RequestParam("procDefId") String procDefId) {
        return ResultJson.success(actProcessService.updateState(state, procDefId));
    }

    /**
     * 删除流程
     */
    @Operation(summary = "删除流程")
    @RequiresPermissions("user")
    @RequestMapping(value = "/delete")
    public ResultJson delete(@RequestParam("deploymentId") String deploymentId) {
        actProcessService.delete(deploymentId);
        return ResultJson.success();
    }

    @Operation(summary = "删除全部流程")
    @RequiresPermissions("user")
    @RequestMapping(value = "/deleteAll")
    public ResultJson deleteAll(@RequestParam("key") String key) {
        actProcessService.deleteAll(key);
        return ResultJson.success();
    }

    /**
     * 转换为模型
     */
    @Operation(summary = "转换为模型")
    @RequiresPermissions("user")
    @RequestMapping(value = "/toModel")
    public ResultJson toModel(@RequestParam("procDefId") String procDefId) throws XMLStreamException, UnsupportedEncodingException {
        return ResultJson.success("转换模型成功，模型ID=" + actProcessService.toModel(procDefId).getId());
    }

    /**
     * 部署流程
     */
    @Operation(summary = "部署流程")
    @RequiresPermissions("user")
    @RequestMapping(value = "/deploy")
    public ResultJson deploy(@RequestBody Zform zform) throws IOException {
        String groupId = zform.getS02();
        String category = zform.getS01();
        File file = sysFileService.getFirstByGroupId(groupId, fileRoot);
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));
        String fileName = multipartFile.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            return ResultJson.failed("请选择要部署的流程文件");
        } else {
            return ResultJson.success(actProcessService.deploy(category, multipartFile));
        }
    }

    /**
     * 获取运行中流程列表
     */
    @Operation(summary = "获取运行中流程列表")
    @RequiresPermissions("user")
    @RequestMapping(value = "/runningList", method = RequestMethod.POST, produces = "application/json")
    public ResultJson runningList(@RequestBody ProcessView processView) {
        ResultJson resultJson = ResultJson.success();
        Integer pageNo = processView.getPageParam().getPageNo();
        Integer pageSize = processView.getPageParam().getPageSize();
        String procInsId = processView.getProcInsId();
        String procDefKey = processView.getProcDefKey();
        String processDefinitionName = processView.getProcessDefinitionName();
        Page<ProcessInstance> page = actProcessService.runningList(new Page<ProcessInstance>(pageNo, pageSize), procInsId, procDefKey, processDefinitionName);
        List<String> processDefinitionKeyList = page.getList().stream().map(ProcessInstance::getProcessDefinitionKey).collect(Collectors.toList());
        List<Task> taskList = taskService.createTaskQuery().processDefinitionKeyIn(processDefinitionKeyList).list();
        Map<String, Task> taskMap = ConvertUtil.listToMap(taskList, Task::getTaskDefinitionKey);

        List<ProcessView> list = new ArrayList<>();
        for (int i = 0; i < page.getList().size(); i++) {
            ProcessInstance instance = page.getList().get(i);
            ProcessView view = new ProcessView();
            view.setProcessDefinitionName(instance.getProcessDefinitionName());
            view.setId(instance.getId());
            view.setProcessInstanceId(instance.getProcessInstanceId());
            view.setProcessDefinitionId(instance.getProcessDefinitionId());
            view.setActivityId(instance.getActivityId());
            if (taskMap.containsKey(instance.getActivityId())) {
                view.setActivityName(taskMap.get(instance.getActivityId()).getName());
            }
            view.setSuspended(instance.isSuspended());
            list.add(view);
        }
        resultJson.setRows(list);
        resultJson.setTotal(page.getCount());
        return resultJson;
    }

    /**
     * 获取历史流程列表
     */
    @Operation(summary = "获取历史流程列表")
    @RequiresPermissions("user")
    @RequestMapping(value = "/historyList", method = RequestMethod.POST, produces = "application/json")
    public ResultJson historyList(@RequestBody ProcessView processView) {
        ResultJson resultJson = ResultJson.success();
        Integer pageNo = processView.getPageParam().getPageNo();
        Integer pageSize = processView.getPageParam().getPageSize();
        String procInsId = processView.getProcInsId();
        String procDefKey = processView.getProcDefKey();
        Page<HistoricProcessInstance> page = actProcessService.historyList(new Page<HistoricProcessInstance>(pageNo, pageSize), procInsId, procDefKey);
        resultJson.setRows(page.getList());
        resultJson.setTotal(page.getCount());
        return resultJson;
    }

    /**
     * 获取历史流程图
     */
    @Operation(summary = "获取历史流程图")
    @RequiresPermissions("user")
    @RequestMapping(value = "/trace/photo")
    public ResultJson historyPhoto(@RequestParam("processDefinitionId") String processDefinitionId,
                                   @RequestParam("executionId") String executionId,
                                   HttpServletResponse response) throws IOException {
        InputStream imageStream = actProcessService.tracePhoto(processDefinitionId, executionId);

        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
        return ResultJson.success();
    }

    /**
     * 删除流程实例
     */
    @Operation(summary = "删除流程实例")
    @RequiresPermissions("user")
    @RequestMapping(value = "/deleteProcIns")
    public ResultJson deleteProcIns(@RequestParam("processInstanceId") String processInstanceId, @RequestParam("reason") String reason) {
        if (StringUtils.isBlank(reason)) {
            return ResultJson.failed("请填写作废原因");
        } else {
            actProcessService.deleteProcIns(processInstanceId, reason);
            return ResultJson.success("作废成功，流程实例ID=" + processInstanceId);
        }
    }

    /**
     * 工作流待办
     */
    @Operation(summary = "获取待办Map数据")
    @RequiresPermissions("user")
    @RequestMapping(value = "/todoMap")
    public ResultJson todoMap() {
        TaskQuery taskQuery = taskService.createTaskQuery().taskOwner(currentUserName.get());

        List<Task> list = taskQuery.list();
        Map<String, Long> map = list.stream().collect(Collectors.groupingBy(Task::getTaskDefinitionKey, Collectors.counting()));
        Set<String> definitionIdList = list.stream().map(Task::getProcessDefinitionId).collect(Collectors.toSet());
        List<ProcessDefinition> definitionList = new ArrayList<>();
        if (definitionIdList.size() > 0) {
            definitionList = repositoryService.createProcessDefinitionQuery().processDefinitionIds(definitionIdList).list();
        }
        Map<String, ProcessDefinition> definitionMap = ConvertUtil.listToMap(definitionList, ProcessDefinition::getId);
        Map<String, String> mapDefine = new HashMap<>();
        for (Task task : list) {
            mapDefine.put(task.getTaskDefinitionKey(), definitionMap.get(task.getProcessDefinitionId()).getDeploymentId());
        }
        return ResultJson.success().put("data", map).put("dataDefine", mapDefine);
    }
}
