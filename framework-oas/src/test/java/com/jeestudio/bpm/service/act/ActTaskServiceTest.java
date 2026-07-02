package com.jeestudio.bpm.service.act;

import com.jeestudio.bpm.base.BaseServiceTest;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ActTaskService 单元测试
 */
class ActTaskServiceTest extends BaseServiceTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ActTaskService actTaskService;

    private static final String PROC_DEF_KEY = "test_process";
    private static final String BUSINESS_TABLE = "test_table";
    private static final String BUSINESS_ID = "business_001";
    private static final String PROC_INS_ID = "proc_ins_001";
    private static final String TASK_ID = "task_001";

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    @Test
    @DisplayName("启动流程 - 成功")
    void testStartProcess_Success() {
        // Given
        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn(PROC_INS_ID);
        when(runtimeService.startProcessInstanceByKey(
                eq(PROC_DEF_KEY),
                eq(BUSINESS_TABLE + ":" + BUSINESS_ID),
                anyMap()))
                .thenReturn(mockInstance);

        // When
        String procInsId = actTaskService.startProcess(PROC_DEF_KEY, BUSINESS_TABLE, BUSINESS_ID, TEST_LOGIN_NAME);

        // Then
        assertThat(procInsId).isEqualTo(PROC_INS_ID);
        verify(identityService).setAuthenticatedUserId(TEST_LOGIN_NAME);
        verify(runtimeService).startProcessInstanceByKey(
                eq(PROC_DEF_KEY),
                eq(BUSINESS_TABLE + ":" + BUSINESS_ID),
                anyMap());
    }

    @Test
    @DisplayName("启动流程 - 带流程变量")
    void testStartProcess_WithVariables() {
        // Given
        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn(PROC_INS_ID);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("applyUserId", "custom_user");
        vars.put("customVar", "customValue");
        
        when(runtimeService.startProcessInstanceByKey(
                eq(PROC_DEF_KEY),
                eq(BUSINESS_TABLE + ":" + BUSINESS_ID),
                anyMap()))
                .thenReturn(mockInstance);

        // When
        String procInsId = actTaskService.startProcess(PROC_DEF_KEY, BUSINESS_TABLE, BUSINESS_ID, "测试流程", vars, TEST_LOGIN_NAME);

        // Then
        assertThat(procInsId).isEqualTo(PROC_INS_ID);
        verify(identityService).setAuthenticatedUserId("custom_user");
        verify(runtimeService).startProcessInstanceByKey(
                eq(PROC_DEF_KEY),
                eq(BUSINESS_TABLE + ":" + BUSINESS_ID),
                argThat(map -> map.containsKey("title") && "测试流程".equals(map.get("title"))));
    }

    @Test
    @DisplayName("完成任务 - 成功")
    void testCompleteTask_Success() {
        // Given
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", true);

        // When
        actTaskService.complete(TASK_ID, PROC_INS_ID, null, vars);

        // Then
        verify(taskService).complete(eq(TASK_ID), anyMap());
        verify(taskService, never()).addComment(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("完成任务 - 带评论")
    void testCompleteTask_WithComment() {
        // Given
        String comment = "审批通过";
        Map<String, Object> vars = new HashMap<>();
        vars.put("approved", true);

        // When
        actTaskService.complete(TASK_ID, PROC_INS_ID, comment, vars);

        // Then
        verify(taskService).addComment(TASK_ID, PROC_INS_ID, comment);
        verify(taskService).complete(eq(TASK_ID), anyMap());
    }

    @Test
    @DisplayName("领取任务 - 成功")
    void testClaimTask_Success() {
        // When
        actTaskService.claim(TASK_ID, TEST_LOGIN_NAME);

        // Then
        verify(taskService).claim(TASK_ID, TEST_LOGIN_NAME);
    }

    @Test
    @DisplayName("获取流程实例")
    void testGetProcessInstance() {
        // Given
        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn(PROC_INS_ID);

        ProcessInstanceQuery mockQuery = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(mockQuery);
        when(mockQuery.processInstanceId(PROC_INS_ID)).thenReturn(mockQuery);
        when(mockQuery.singleResult()).thenReturn(mockInstance);

        // When
        ProcessInstance result = actTaskService.getProcIns(PROC_INS_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(PROC_INS_ID);
        verify(runtimeService).createProcessInstanceQuery();
    }
}
