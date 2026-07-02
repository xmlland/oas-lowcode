package com.jeestudio.bpm.service.act;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.view.act.ProcessView;
import com.jeestudio.bpm.utils.Global;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.editor.constants.ModelDataJsonConstants;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import com.jeestudio.bpm.service.act.diagram.BuildPngUtil;
import com.jeestudio.bpm.service.act.diagram.MyProcessDiagramGenerator;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @Description: 工作流流程服务
 */
@Service
public class ActProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngineFactoryBean processEngine;

    @Autowired
    private BuildPngUtil buildPngUtil;

    public Page<ProcessView> processList(Page<ProcessView> page, String category) {

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion().orderByProcessDefinitionKey().asc();

        if (StringUtils.isNotEmpty(category)){
            processDefinitionQuery.processDefinitionCategory(category);
        }

        page.setCount(processDefinitionQuery.count());
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(page.getFirstResult(), page.getMaxResults());
        for (ProcessDefinition processDefinition : processDefinitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            ProcessView processView = new ProcessView();
            processView.setId(processDefinition.getId());
            processView.setCategory(processDefinition.getCategory());
            processView.setKey(processDefinition.getKey());
            processView.setName(processDefinition.getName());
            processView.setVersion(processDefinition.getVersion());
            processView.setSuspended(processDefinition.isSuspended());
            processView.setDeploymentId(processDefinition.getDeploymentId());
            processView.setDeploymentTime(deployment.getDeploymentTime());

            page.getList().add(processView);
        }

        return page;
    }

    public String updateState(String state, String procDefId) {
        if (state.equals("active")) {
            repositoryService.activateProcessDefinitionById(procDefId, true, null);
            return "已激活ID为[" + procDefId + "]的流程定义。";
        } else if (state.equals("suspend")) {
            repositoryService.suspendProcessDefinitionById(procDefId, true, null);
            return "已挂起ID为[" + procDefId + "]的流程定义。";
        }
        return "无操作";
    }

    public void delete(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    public void deleteAll(String key) {
        List<Deployment> list = repositoryService.createDeploymentQuery().processDefinitionKey(key).list();
        for (Deployment deployment : list) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }


    public Model toModel(String procDefId) throws UnsupportedEncodingException, XMLStreamException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), processDefinition.getResourceName());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, StandardCharsets.UTF_8);
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getCategory());//.getDeploymentId());
        modelData.setDeploymentId(processDefinition.getDeploymentId());
        modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count()+1)));

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes(StandardCharsets.UTF_8));

        return modelData;
    }

    public String deploy(String category, MultipartFile file) {
        String message = "";
        String fileName = file.getOriginalFilename();

        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment = null;
            String extension = FilenameUtils.getExtension(fileName);
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().addZipInputStream(zip).deploy();
            } else if (extension.equals("png")) {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            } else if (fileName.indexOf("bpmn20.xml") != -1) {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            } else if (extension.equals("bpmn")) {
                String baseName = FilenameUtils.getBaseName(fileName);
                deployment = repositoryService.createDeployment().addInputStream(baseName + ".bpmn20.xml", fileInputStream).deploy();
            } else {
                message = "不支持的文件类型：" + extension;
            }

            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();

            for (ProcessDefinition processDefinition : list) {
                repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
                message += "部署成功，流程ID=" + processDefinition.getId() + "<br/>";
            }

            if (list.size() == 0){
                message = "部署失败，没有流程。";
            }

        } catch (Exception e) {
            throw new FlowableException("部署失败！", e);
        }
        return message;
    }

    public Page<ProcessInstance> runningList(Page<ProcessInstance> page, String procInsId, String procDefKey, String procName) {
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

        if (StringUtils.isNotBlank(procInsId)){
            processInstanceQuery.processInstanceId(procInsId);
        }

        if (StringUtils.isNotBlank(procDefKey)){
            processInstanceQuery.processDefinitionKey(procDefKey);
        }
        if (StringUtils.isNotBlank(procName)){
            processInstanceQuery.processDefinitionName(procName);
        }
        page.setCount(processInstanceQuery.count());
        page.setList(processInstanceQuery.listPage(page.getFirstResult(), page.getMaxResults()));
        return page;
    }

    public Page<HistoricProcessInstance> historyList(Page<HistoricProcessInstance> page, String procInsId, String procDefKey) {

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().finished()
                .orderByProcessInstanceEndTime().desc();

        if (StringUtils.isNotBlank(procInsId)){
            query.processInstanceId(procInsId);
        }

        if (StringUtils.isNotBlank(procDefKey)){
            query.processDefinitionKey(procDefKey);
        }

        page.setCount(query.count());
        page.setList(query.listPage(page.getFirstResult(), page.getMaxResults()));
        return page;
    }

    public InputStream tracePhoto(String processDefinitionId, String executionId) {
        // Resolve processDefinitionId if not provided
        if (StringUtils.isBlank(processDefinitionId) || Global.NO.equals(processDefinitionId)) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(executionId).singleResult();
            if (processInstance == null) {
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(executionId).singleResult();
                processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            } else {
                processDefinitionId = processInstance.getProcessDefinitionId();
            }
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        new BpmnAutoLayout(bpmnModel).execute();

        String processInstanceId = executionId;

        // Get currently active activity ids
        List<String> activeActivityIds = new ArrayList<>();
        if (runtimeService.createExecutionQuery().executionId(executionId).count() > 0){
            activeActivityIds = runtimeService.getActiveActivityIds(executionId);
        }

        // Query all historic activity instances
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .orderByActivityId().asc().list();

        // Collect all visited activity ids (deduplicated, preserving order)
        LinkedHashSet<String> allVisitedSet = new LinkedHashSet<>();
        if (historicActivityInstances != null) {
            for (HistoricActivityInstance hai : historicActivityInstances) {
                allVisitedSet.add(hai.getActivityId());
            }
        }
        List<String> highLightedActivities = new ArrayList<>(allVisitedSet);

        // Get highlighted flows using historic activity instances
        List<String> highLightedFlows = buildPngUtil.getHighLightedFlows(bpmnModel, historicActivityInstances);

        // Filter out sequence flows with missing source or target elements
        for (FlowNode flowNode : bpmnModel.getProcesses().get(0).findFlowElementsOfType(FlowNode.class)) {
            List<SequenceFlow> normalSequence = new ArrayList<>();
            for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                String sourceRef = sequenceFlow.getSourceRef();
                String targetRef = sequenceFlow.getTargetRef();
                FlowElement sourceElement = bpmnModel.getFlowElement(sourceRef);
                FlowElement targetElement = bpmnModel.getFlowElement(targetRef);
                if (sourceElement != null && targetElement != null) {
                    normalSequence.add(sequenceFlow);
                }
            }
            flowNode.getOutgoingFlows().clear();
            for (SequenceFlow sequenceFlow : normalSequence) {
                flowNode.getOutgoingFlows().add(sequenceFlow);
            }
        }

        return new MyProcessDiagramGenerator().generateDiagram(bpmnModel, "png",
                highLightedActivities, activeActivityIds, highLightedFlows,
                processEngine.getProcessEngineConfiguration().getActivityFontName(),
                processEngine.getProcessEngineConfiguration().getLabelFontName(),
                processEngine.getProcessEngineConfiguration().getAnnotationFontName(),
                processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0);
    }

    public void deleteProcIns(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }
}
