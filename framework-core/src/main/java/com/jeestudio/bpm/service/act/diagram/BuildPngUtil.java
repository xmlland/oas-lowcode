package com.jeestudio.bpm.service.act.diagram;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Description: 流程图高亮PNG构建工具
 */
@Component
public class BuildPngUtil {

    @Autowired
    HistoryService historyService;

    @Autowired
    RepositoryService repositoryService;

    /**
     * Get highlighted flows for a process instance using BpmnModel (Flowable 6 compatible).
     *
     * @param processDefinitionId the process definition id
     * @param processInstanceId   the process instance id
     * @return list of highlighted flow ids
     */
    public List<String> getHighLightedFlows(String processDefinitionId, String processInstanceId) {

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if (bpmnModel == null || bpmnModel.getProcesses().isEmpty()) {
            return new ArrayList<>();
        }

        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .orderByActivityId().asc().list();

        return getHighLightedFlows(bpmnModel, historicActivityInstances);
    }

    /**
     * Get highlighted flows using an existing BpmnModel and HistoricActivityInstance list.
     *
     * @param bpmnModel                  the BPMN model
     * @param historicActivityInstances   the historic activity instances (ordered by start time asc)
     * @return list of highlighted flow ids
     */
    public List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {

        Set<String> highLightedFlowSet = new LinkedHashSet<>();

        if (bpmnModel == null || bpmnModel.getProcesses().isEmpty()) {
            return new ArrayList<>();
        }

        if (historicActivityInstances == null || historicActivityInstances.isEmpty()) {
            return new ArrayList<>();
        }

        // Collect all historic activity ids into a set
        Set<String> historicActivityIdSet = new LinkedHashSet<>();
        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityIdSet.add(hai.getActivityId());
        }

        Process process = bpmnModel.getProcesses().get(0);

        // For each historic activity, check outgoing flows
        for (String actId : historicActivityIdSet) {
            FlowElement element = process.getFlowElement(actId);
            if (element instanceof FlowNode) {
                FlowNode node = (FlowNode) element;
                for (SequenceFlow seqFlow : node.getOutgoingFlows()) {
                    String targetRef = seqFlow.getTargetRef();
                    if (historicActivityIdSet.contains(targetRef)) {
                        // Direct connection to another historic activity
                        highLightedFlowSet.add(seqFlow.getId());
                    } else {
                        // Check through intermediate nodes (e.g. gateways not recorded in history)
                        highlightThroughIntermediateNodes(seqFlow, targetRef, historicActivityIdSet,
                                process, highLightedFlowSet, new HashSet<>(), 3);
                    }
                }
            }
        }

        return new ArrayList<>(highLightedFlowSet);
    }

    /**
     * Recursively search through intermediate non-task nodes (gateways, events) to find
     * connecting flows to historic activities. Adds all flows along the path.
     */
    private void highlightThroughIntermediateNodes(SequenceFlow incomingFlow, String currentRef,
                                                    Set<String> historicActivityIdSet, Process process,
                                                    Set<String> result, Set<String> visited, int maxDepth) {
        if (maxDepth <= 0 || visited.contains(currentRef)) {
            return;
        }
        visited.add(currentRef);

        FlowElement element = process.getFlowElement(currentRef);
        if (!(element instanceof FlowNode) || element instanceof UserTask) {
            return;
        }

        FlowNode intermediateNode = (FlowNode) element;
        for (SequenceFlow outFlow : intermediateNode.getOutgoingFlows()) {
            if (historicActivityIdSet.contains(outFlow.getTargetRef())) {
                // Found a path: add both the incoming flow and this outgoing flow
                result.add(incomingFlow.getId());
                result.add(outFlow.getId());
            } else {
                // Continue searching deeper
                int beforeSize = result.size();
                highlightThroughIntermediateNodes(outFlow, outFlow.getTargetRef(),
                        historicActivityIdSet, process, result, visited, maxDepth - 1);
                if (result.size() > beforeSize) {
                    // A deeper path was found, also add the incoming flow
                    result.add(incomingFlow.getId());
                }
            }
        }
    }
}
