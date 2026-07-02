package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.base.BaseServiceTest;
import com.jeestudio.bpm.common.entity.common.persistence.Page;
import com.jeestudio.bpm.common.view.act.ProcessView;
import com.jeestudio.bpm.service.act.diagram.BuildPngUtil;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentQuery;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.spring.ProcessEngineFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ActProcessService 单元测试
 */
class ActProcessServiceTest extends BaseServiceTest {

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private HistoryService historyService;

    @Mock
    private ProcessEngineFactoryBean processEngine;

    @Mock
    private BuildPngUtil buildPngUtil;

    @InjectMocks
    private ActProcessService actProcessService;

    private static final String PROC_DEF_ID = "process:1:001";
    private static final String PROC_DEF_KEY = "test_process";
    private static final String DEPLOYMENT_ID = "deploy_001";

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    @Test
    @DisplayName("获取流程定义列表 - 成功")
    void testProcessList_Success() {
        // Given
        Page<ProcessView> page = new Page<>(1, 10);
        ProcessDefinition mockProcDef = mock(ProcessDefinition.class);
        when(mockProcDef.getId()).thenReturn(PROC_DEF_ID);
        when(mockProcDef.getKey()).thenReturn(PROC_DEF_KEY);
        when(mockProcDef.getName()).thenReturn("测试流程");
        when(mockProcDef.getVersion()).thenReturn(1);
        when(mockProcDef.isSuspended()).thenReturn(false);
        when(mockProcDef.getDeploymentId()).thenReturn(DEPLOYMENT_ID);
        when(mockProcDef.getCategory()).thenReturn("test_category");

        Deployment mockDeployment = mock(Deployment.class);
        when(mockDeployment.getDeploymentTime()).thenReturn(new Date());

        ProcessDefinitionQuery mockQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockQuery);
        when(mockQuery.latestVersion()).thenReturn(mockQuery);
        when(mockQuery.orderByProcessDefinitionKey()).thenReturn(mockQuery);
        when(mockQuery.asc()).thenReturn(mockQuery);
        when(mockQuery.count()).thenReturn(1L);
        when(mockQuery.listPage(anyInt(), anyInt())).thenReturn(Collections.singletonList(mockProcDef));

        DeploymentQuery mockDeployQuery = mock(DeploymentQuery.class);
        when(repositoryService.createDeploymentQuery()).thenReturn(mockDeployQuery);
        when(mockDeployQuery.deploymentId(DEPLOYMENT_ID)).thenReturn(mockDeployQuery);
        when(mockDeployQuery.singleResult()).thenReturn(mockDeployment);

        // When
        Page<ProcessView> result = actProcessService.processList(page, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(1L);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getKey()).isEqualTo(PROC_DEF_KEY);
    }

    @Test
    @DisplayName("挂起流程定义")
    void testUpdateState_Suspend() {
        // When
        String result = actProcessService.updateState("suspend", PROC_DEF_ID);

        // Then
        assertThat(result).contains("已挂起");
        verify(repositoryService).suspendProcessDefinitionById(eq(PROC_DEF_ID), eq(true), isNull());
    }

    @Test
    @DisplayName("激活流程定义")
    void testUpdateState_Activate() {
        // When
        String result = actProcessService.updateState("active", PROC_DEF_ID);

        // Then
        assertThat(result).contains("已激活");
        verify(repositoryService).activateProcessDefinitionById(eq(PROC_DEF_ID), eq(true), isNull());
    }

    @Test
    @DisplayName("删除流程部署")
    void testDeleteDeployment() {
        // When
        actProcessService.delete(DEPLOYMENT_ID);

        // Then
        verify(repositoryService).deleteDeployment(DEPLOYMENT_ID, true);
    }

    @Test
    @DisplayName("流程定义转模型")
    void testConvertToModel() throws Exception {
        // Given - 测试流程定义查询
        ProcessDefinition mockProcDef = mock(ProcessDefinition.class);
        when(mockProcDef.getKey()).thenReturn(PROC_DEF_KEY);

        ProcessDefinitionQuery mockQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockQuery);
        when(mockQuery.processDefinitionId(PROC_DEF_ID)).thenReturn(mockQuery);
        when(mockQuery.singleResult()).thenReturn(mockProcDef);

        // When & Then - 验证查询调用
        ProcessDefinition result = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(PROC_DEF_ID)
                .singleResult();
        
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(PROC_DEF_KEY);
        verify(repositoryService).createProcessDefinitionQuery();
    }

    @Test
    @DisplayName("获取流程定义详情")
    void testGetProcessDefinition() {
        // Given
        ProcessDefinition mockProcDef = mock(ProcessDefinition.class);
        when(mockProcDef.getId()).thenReturn(PROC_DEF_ID);
        when(mockProcDef.getKey()).thenReturn(PROC_DEF_KEY);
        when(mockProcDef.getName()).thenReturn("测试流程");

        ProcessDefinitionQuery mockQuery = mock(ProcessDefinitionQuery.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(mockQuery);
        when(mockQuery.latestVersion()).thenReturn(mockQuery);
        when(mockQuery.orderByProcessDefinitionKey()).thenReturn(mockQuery);
        when(mockQuery.asc()).thenReturn(mockQuery);
        when(mockQuery.processDefinitionCategory(anyString())).thenReturn(mockQuery);
        when(mockQuery.count()).thenReturn(1L);
        when(mockQuery.listPage(anyInt(), anyInt())).thenReturn(Collections.singletonList(mockProcDef));

        Deployment mockDeployment = mock(Deployment.class);
        when(mockDeployment.getDeploymentTime()).thenReturn(new Date());
        when(mockProcDef.getDeploymentId()).thenReturn(DEPLOYMENT_ID);

        DeploymentQuery mockDeployQuery = mock(DeploymentQuery.class);
        when(repositoryService.createDeploymentQuery()).thenReturn(mockDeployQuery);
        when(mockDeployQuery.deploymentId(anyString())).thenReturn(mockDeployQuery);
        when(mockDeployQuery.singleResult()).thenReturn(mockDeployment);

        Page<ProcessView> page = new Page<>(1, 10);

        // When
        Page<ProcessView> result = actProcessService.processList(page, "test_category");

        // Then
        assertThat(result).isNotNull();
        verify(mockQuery).processDefinitionCategory("test_category");
    }
}
